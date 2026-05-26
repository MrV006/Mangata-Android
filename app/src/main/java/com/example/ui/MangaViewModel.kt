package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class MangaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MangaDatabase.getDatabase(application)
    private val repository = MangaRepository(db)

    // UI States
    private val _currentUser = MutableStateFlow<CachedUserEntity?>(null)
    val currentUser: StateFlow<CachedUserEntity?> = _currentUser.asStateFlow()

    private val _mangas = MutableStateFlow<List<MangaItem>>(emptyList())
    val mangas: StateFlow<List<MangaItem>> = _mangas.asStateFlow()

    private val _chapters = MutableStateFlow<Map<Int, List<ChapterItem>>>(emptyMap())
    val chapters: StateFlow<Map<Int, List<ChapterItem>>> = _chapters.asStateFlow()

    private val _exams = MutableStateFlow<List<ExamItem>>(emptyList())
    val exams: StateFlow<List<ExamItem>> = _exams.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        // Load persist user session first
        viewModelScope.launch {
            _currentUser.value = repository.getCurrentUser()
            checkActiveSession()
            fetchManhwas()
        }
    }

    fun checkActiveSession() {
        val user = _currentUser.value ?: return
        val token = user.token ?: return
        viewModelScope.launch {
            val result = repository.checkSession(user.id, token)
            if (result.isFailure) {
                repository.logout()
                _currentUser.value = null
                _exams.value = emptyList()
                _errorMessage.value = "نشست فعال شما منقضی یا از دیوایس دیگری وارد شده‌اید."
            } else {
                val dbRole = result.getOrNull()?.role
                if (dbRole != null && dbRole != user.role) {
                    _currentUser.value = user.copy(role = dbRole)
                }
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    // Auth actions
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.login(username, password)
            _isLoading.value = false
            if (result.isSuccess) {
                val data = result.getOrNull()
                data?.let {
                    _currentUser.value = CachedUserEntity(
                        id = it.userId,
                        username = it.username,
                        email = it.email,
                        role = it.role,
                        displayName = it.displayName,
                        token = it.token
                    )
                    _successMessage.value = "خوش آمدید، ورود موفقیت‌آمیز بود."
                    fetchManhwas() // Refresh materials
                }
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "خطای ناشناخته در ورود."
            }
        }
    }

    fun register(username: String, email: String, password: String, role: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.register(username, email, password, role)
            _isLoading.value = false
            if (result.isSuccess) {
                val data = result.getOrNull()
                data?.let {
                    _currentUser.value = CachedUserEntity(
                        id = it.userId,
                        username = it.username,
                        email = it.email,
                        role = it.role,
                        displayName = it.displayName,
                        token = it.token
                    )
                    _successMessage.value = "ثبت حساب کاربری با موفقیت در سایت انجام شد."
                    fetchManhwas()
                }
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "خطا در ثبت نام."
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _currentUser.value = null
            _exams.value = emptyList()
            _successMessage.value = "خروج با موفقیت انجام شد."
        }
    }

    // Manga & Chapter Actions
    fun fetchManhwas() {
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                user.token?.let { token ->
                    val checkResult = repository.checkSession(user.id, token)
                    if (checkResult.isFailure) {
                        repository.logout()
                        _currentUser.value = null
                        _exams.value = emptyList()
                        _errorMessage.value = "نشست فعال شما منقضی یا از دیوایس دیگری وارد شده‌اید."
                        return@launch
                    }
                }
            }
            _isLoading.value = true
            val result = repository.getManhwas()
            _isLoading.value = false
            if (result.isSuccess) {
                _mangas.value = result.getOrNull() ?: emptyList()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun createManhwa(title: String, desc: String, coverUrl: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.createManhwa(title, desc, coverUrl)
            _isLoading.value = false
            if (result.isSuccess) {
                _successMessage.value = result.getOrNull()
                fetchManhwas() // refresh List
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun fetchChapters(mangaId: Int) {
        viewModelScope.launch {
            val result = repository.getChapters(mangaId)
            if (result.isSuccess) {
                val list = result.getOrNull() ?: emptyList()
                val currentMap = _chapters.value.toMutableMap()
                currentMap[mangaId] = list
                _chapters.value = currentMap
            }
        }
    }

    // Upload ZIP Manhwa Chapter (Admins / Staff Crew of that manhwa)
    fun uploadChapterZip(file: File, mangaId: Int, chapterNumber: Double, title: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.uploadChapterZip(file, mangaId, chapterNumber, title, user.id)
            _isLoading.value = false
            if (result.isSuccess) {
                _successMessage.value = result.getOrNull()
                fetchChapters(mangaId)
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    // Recruitment Exam Upload for current user
    fun uploadExamFile(file: File) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.uploadExamFile(file, user.id)
            _isLoading.value = false
            if (result.isSuccess) {
                _successMessage.value = result.getOrNull()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    // Fetch Exams list (Admin only)
    fun fetchExams() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getExams(user.id)
            _isLoading.value = false
            if (result.isSuccess) {
                _exams.value = result.getOrNull() ?: emptyList()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    // Core Grading method for Super Admin (real exam scoring)
    fun gradeExam(examId: Int, status: String, score: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.gradeExam(user.id, examId, status, score)
            _isLoading.value = false
            if (result.isSuccess) {
                _successMessage.value = result.getOrNull()
                fetchExams() // Refresh admin exams list
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    // Assign freelancers to manhwa
    fun assignStaff(staffId: Int, mangaId: Int, role: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.assignStaff(user.id, staffId, mangaId, role)
            _isLoading.value = false
            if (result.isSuccess) {
                _successMessage.value = result.getOrNull()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }
}
