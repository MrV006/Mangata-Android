package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.*
import com.example.myket.MyketBillingHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MovieViewModel(application: Application) : AndroidViewModel(application) {

    private val LOG_TAG = "MangaViewModel"
    private val database = MangaDatabase.getDatabase(application)
    private val repository = MangaRepository(database.mangaDao())
    val myketHelper = MyketBillingHelper(application)

    // Base context/Prefs for domain setting
    private val sharedPrefs = application.getSharedPreferences("mangata_prefs", android.content.Context.MODE_PRIVATE)

    // Domain setting state
    private val _siteDomain = MutableStateFlow(sharedPrefs.getString("domain_url", "https://mangata.site") ?: "https://mangata.site")
    val siteDomain: StateFlow<String> = _siteDomain.asStateFlow()

    private val _isDomainConnected = MutableStateFlow(true)
    val isDomainConnected: StateFlow<Boolean> = _isDomainConnected.asStateFlow()

    // UI state streams
    val mangas: StateFlow<List<MangaEntity>> = repository.allMangas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<Bookmark>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val readHistory: StateFlow<List<ReadHistory>> = repository.readHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPurchases: StateFlow<List<UserPurchase>> = repository.userPurchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teamMembers: StateFlow<List<TeamMember>> = repository.allTeamMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active VIP status based on active purchases in the database (to unlock advanced early chapters)
    val isVipActive: StateFlow<Boolean> = repository.userPurchases
        .map { list -> list.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Currently Selected Tab (0 = Home, 1 = Bookmarks, 2 = Team/Collaborators, 3 = Store, 4 = Settings/Domain Connection)
    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    // Currently Selected Manga for Detailed View Panel
    private val _selectedManga = MutableStateFlow<MangaEntity?>(null)
    val selectedManga: StateFlow<MangaEntity?> = _selectedManga.asStateFlow()

    // Gemini AI Summary State (Idle, Loading, Success, Error)
    private val _aiSummaryState = MutableStateFlow<AiSummaryState>(AiSummaryState.Idle)
    val aiSummaryState: StateFlow<AiSummaryState> = _aiSummaryState.asStateFlow()

    // Active Manga Reader state
    private val _readingManga = MutableStateFlow<MangaEntity?>(null)
    val readingManga: StateFlow<MangaEntity?> = _readingManga.asStateFlow()

    private val _activeChapter = MutableStateFlow(1)
    val activeChapter: StateFlow<Int> = _activeChapter.asStateFlow()

    // Reader UI configurations
    private val _isVerticalWebtoonMode = MutableStateFlow(true)
    val isVerticalWebtoonMode: StateFlow<Boolean> = _isVerticalWebtoonMode.asStateFlow()

    // Myket billing screen state
    private val _showMyketBillingDialog = MutableStateFlow(false)
    val showMyketBillingDialog: StateFlow<Boolean> = _showMyketBillingDialog.asStateFlow()

    private val _selectedSkuToBuy = MutableStateFlow<MyketBillingHelper.SkuDetails?>(null)
    val selectedSkuToBuy: StateFlow<MyketBillingHelper.SkuDetails?> = _selectedSkuToBuy.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        // Initialize setup and seeds
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val current = database.mangaDao().getAllMangas().first()
                if (current.isEmpty()) {
                    Log.d(LOG_TAG, "Seeding database with popular mangas and team members...")
                    repository.seedDatabase()
                }
            }

            // Bind to Myket billing service for IAP memberships
            myketHelper.startSetup { result ->
                if (result.isSuccess) {
                    Log.d(LOG_TAG, "Myket Billing Service Connected inside MangaViewModel.")
                    queryMyketOwnedPurchases()
                } else {
                    Log.e(LOG_TAG, "Failed to connect to Myket service: ${result.message}")
                }
            }
        }
    }

    private fun queryMyketOwnedPurchases() {
        viewModelScope.launch {
            val dbPurchases = userPurchases.first()
            val ownedSkusFromDb = dbPurchases.map { it.sku }

            myketHelper.queryInventory(
                skus = listOf(myketHelper.SKU_VIP_1MONTH, myketHelper.SKU_VIP_3MONTH, myketHelper.SKU_VIP_LIFETIME),
                currentlyOwnedSkus = ownedSkusFromDb
            ) { result, purchases ->
                if (result.isSuccess) {
                    Log.d(LOG_TAG, "Myket query returned ${purchases.size} verified purchases.")
                    viewModelScope.launch(Dispatchers.IO) {
                        purchases.forEach { purchase ->
                            val alreadySaved = ownedSkusFromDb.contains(purchase.sku)
                            if (!alreadySaved) {
                                val skuName = myketHelper.skuDetailsMap[purchase.sku]?.title ?: "اشتراک ویژه"
                                repository.addPurchase(
                                    UserPurchase(
                                        sku = purchase.sku,
                                        purchaseTime = purchase.purchaseTime,
                                        token = purchase.purchaseToken,
                                        orderId = purchase.orderId,
                                        skuNameFa = skuName
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun selectTab(tabIndex: Int) {
        _activeTab.value = tabIndex
        if (tabIndex != 0) {
            _selectedManga.value = null
        }
    }

    fun selectManga(manga: MangaEntity?) {
        _selectedManga.value = manga
        if (manga != null) {
            loadAiStorySummary(manga)
        } else {
            _aiSummaryState.value = AiSummaryState.Idle
        }
    }

    fun updateSiteDomain(domain: String) {
        _siteDomain.value = domain
        sharedPrefs.edit().putString("domain_url", domain).apply()
        // Simulate dynamic testing of domain REST API structure
        viewModelScope.launch {
            _isDomainConnected.value = false
            kotlinx.coroutines.delay(1200) // Realistic server ping check
            _isDomainConnected.value = true
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleBookmark(id: Int, isFav: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleBookmark(id, isFav)
        }
    }

    fun openReader(manga: MangaEntity, chapter: Int = 1) {
        // VIP Check: Early chapters (e.g., chapter > 3) are locked for non-VIPs
        if (chapter > 3 && manga.isPremium && !isVipActive.value) {
            triggerMyketPurchase(myketHelper.SKU_VIP_1MONTH)
            return
        }
        _readingManga.value = manga
        _activeChapter.value = chapter
    }

    fun closeReader() {
        _readingManga.value = null
    }

    fun setChapter(chapter: Int) {
        val manga = _readingManga.value ?: return
        if (chapter > 3 && manga.isPremium && !isVipActive.value) {
            triggerMyketPurchase(myketHelper.SKU_VIP_1MONTH)
            return
        }
        if (chapter in 1..manga.chaptersCount) {
            _activeChapter.value = chapter
            saveReadingProgress(manga.id, chapter, 0.0f)
        }
    }

    fun toggleReaderDirection() {
        _isVerticalWebtoonMode.value = !_isVerticalWebtoonMode.value
    }

    fun saveReadingProgress(mangaId: Int, chapter: Int, progress: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveReadHistory(mangaId, chapter, progress)
        }
    }

    fun isBookmarked(mangaId: Int): Flow<Boolean> {
        return repository.isBookmarked(mangaId)
    }

    fun getHistoryForManga(mangaId: Int): Flow<ReadHistory?> {
        return repository.getHistoryForManga(mangaId)
    }

    fun triggerMyketPurchase(sku: String) {
        val details = myketHelper.skuDetailsMap[sku]
        if (details != null) {
            _selectedSkuToBuy.value = details
            _showMyketBillingDialog.value = true
        }
    }

    fun closeMyketCheckout() {
        _showMyketBillingDialog.value = false
        _selectedSkuToBuy.value = null
    }

    fun completeSimulatedPurchase(sku: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mockPurchase = myketHelper.generateSuccessfulPurchase(
                    sku = sku,
                    developerPayload = "payload_${System.currentTimeMillis()}"
                )

                val skuDetails = myketHelper.skuDetailsMap[sku]
                val farsiTitle = skuDetails?.title ?: "اشتراک ویژه مانگاتا"

                repository.addPurchase(
                    UserPurchase(
                        sku = sku,
                        purchaseTime = mockPurchase.purchaseTime,
                        token = mockPurchase.purchaseToken,
                        orderId = mockPurchase.orderId,
                        skuNameFa = farsiTitle
                    )
                )

                withContext(Dispatchers.Main) {
                    closeMyketCheckout()
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Error writing purchase record to DB: ${e.message}")
            }
        }
    }

    fun cancelVipSubscription() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllPurchases()
        }
    }

    // Role level and translation collaborator assignments
    fun addNewTeamMember(name: String, role: String, works: String, level: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTeamMember(
                TeamMember(
                    name = name,
                    roleFa = role,
                    levelCode = level,
                    assignedWorks = works
                )
            )
        }
    }

    fun deleteMember(member: TeamMember) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeTeamMember(member)
        }
    }

    private fun loadAiStorySummary(manga: MangaEntity) {
        _aiSummaryState.value = AiSummaryState.Loading
        viewModelScope.launch {
            try {
                val summary = withContext(Dispatchers.IO) {
                    GeminiClient.getSummary(
                        "مانهوا/مانگا: ${manga.titleFa} \n" +
                        "توضیحات کلی: ${manga.descriptionFa} \n" +
                        "نظرات خوانندگان: ${manga.reviewsJson} \n" +
                        "لطفا تحلیل نهایی و جذابیت‌های کلیدی آرت و روایت داستان را با قلم حماسی ترجمه مجدد و ارزیابی عمیق به زبان فارسی ارائه بده."
                    )
                }
                _aiSummaryState.value = AiSummaryState.Success(summary)
            } catch (e: Exception) {
                _aiSummaryState.value = AiSummaryState.Error("خطایی در اتصال با جمینای رخ داد.")
            }
        }
    }
}

sealed class AiSummaryState {
    object Idle : AiSummaryState()
    object Loading : AiSummaryState()
    data class Success(val summary: String) : AiSummaryState()
    data class Error(val message: String) : AiSummaryState()
}
