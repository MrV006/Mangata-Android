package com.example.data

import com.example.network.MangaApiService
import com.example.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class MangaRepository(private val db: MangaDatabase) {
    private val api: MangaApiService = RetrofitClient.apiService
    private val dao = db.mangaDao()

    // 1. Session and Auth Flows
    suspend fun getCurrentUser(): CachedUserEntity? {
        return dao.getCurrentUserProfile()
    }

    suspend fun checkSession(userId: Int, token: String): Result<SessionValidResponse> {
        return try {
            val response = api.checkSession(userId, token)
            if (response.status == "success" && response.data != null) {
                // If role changed on DB, we can update local database here too
                val cached = dao.getCurrentUserProfile()
                if (cached != null && cached.role != response.data.role) {
                    dao.saveUserProfile(cached.copy(role = response.data.role))
                }
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "نشست معتبر یافت نشد."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        dao.clearUserProfile()
    }

    suspend fun login(username: String,password: String): Result<UserData> {
        return try {
            val response = api.login(LoginRequest(username, password))
            if (response.status == "success" && response.data != null) {
                // Save user profile locally to persist session across restarts
                val user = response.data
                dao.saveUserProfile(
                    CachedUserEntity(
                        id = user.userId,
                        username = user.username,
                        email = user.email,
                        role = user.role,
                        displayName = user.displayName,
                        token = user.token
                    )
                )
                Result.success(user)
            } else {
                Result.failure(Exception(response.message ?: "نام کاربری یا رمز عبور اشتباه است."))
            }
        } catch (e: Exception) {
            // Check if we have a locally cached user profile that matches
            val localUser = dao.getCurrentUserProfile()
            if (localUser != null && localUser.username.equals(username, ignoreCase = true)) {
                // Let them in from cache if server is offline
                Result.success(
                    UserData(
                        userId = localUser.id,
                        username = localUser.username,
                        email = localUser.email,
                        role = localUser.role,
                        displayName = localUser.displayName,
                        token = localUser.token
                    )
                )
            } else {
                Result.failure(Exception("ارتباط با سایت برقرار نشد و هیچ حساب محلی قبلی با این مشخصات یافت نگردید."))
            }
        }
    }

    suspend fun register(username: String, email: String, password: String, role: String): Result<UserData> {
        return try {
            val response = api.register(RegisterRequest(username, email, password, role))
            if (response.status == "success" && response.data != null) {
                val user = response.data
                dao.saveUserProfile(
                    CachedUserEntity(
                        id = user.userId,
                        username = user.username,
                        email = user.email,
                        role = user.role,
                        displayName = user.displayName,
                        token = user.token
                    )
                )
                Result.success(user)
            } else {
                Result.failure(Exception(response.message ?: "خطا در ثبت نام در سایت."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در برقراری ارتباط با وردپرس: " + e.localizedMessage))
        }
    }

    // 2. Fetch Manhwas (Syncing Network with Database cache)
    suspend fun getManhwas(
        search: String? = null,
        genre: String? = null,
        year: String? = null,
        character: String? = null
    ): Result<List<MangaItem>> {
        return try {
            val response = api.getManhwas(search, genre, year, character)
            if (response.status == "success" && response.data != null) {
                // Populate local db cache
                val entities = response.data.map {
                    MangaEntity(
                        id = it.id,
                        title = it.title,
                        description = it.description,
                        coverImage = it.coverImage,
                        createdAt = it.createdAt,
                        genres = it.genres,
                        releaseYear = it.releaseYear,
                        mainCharacters = it.mainCharacters,
                        author = it.author
                    )
                }
                if (search == null && genre == null && year == null && character == null) {
                    dao.clearMangaCache()
                    dao.cacheMangas(entities)
                }
                Result.success(response.data)
            } else {
                Result.failure(Exception("خطا در بارگذاری لیست مانهواها از سایت."))
            }
        } catch (e: Exception) {
            // Read from cache if offline
            val cached = dao.getCachedMangas()
            if (cached.isNotEmpty()) {
                val items = cached.map {
                    MangaItem(
                        id = it.id,
                        title = it.title,
                        description = it.description,
                        coverImage = it.coverImage,
                        createdAt = it.createdAt,
                        genres = it.genres,
                        releaseYear = it.releaseYear,
                        mainCharacters = it.mainCharacters,
                        author = it.author
                    )
                }
                val filtered = items.filter { item ->
                    val matchSearch = search == null || item.title.contains(search, ignoreCase = true) || item.description.contains(search, ignoreCase = true) || (item.author != null && item.author.contains(search, ignoreCase = true))
                    val matchGenre = genre == null || (item.genres != null && item.genres.contains(genre, ignoreCase = true))
                    val matchYear = year == null || item.releaseYear == year
                    val matchChar = character == null || (item.mainCharacters != null && item.mainCharacters.contains(character, ignoreCase = true))
                    matchSearch && matchGenre && matchYear && matchChar
                }
                Result.success(filtered)
            } else {
                Result.failure(Exception("عدم دسترسی به شبکه و اطلاعات کَش شده."))
            }
        }
    }

    // 3. Create Manga Entry (Manager mode)
    suspend fun createManhwa(title: String, desc: String, coverUrl: String): Result<String> {
        return try {
            val payload = mapOf("title" to title, "description" to desc, "cover_image" to coverUrl)
            val response = api.createManhwa(payload)
            if (response.status == "success") {
                Result.success("مانهوا با موفقیت افزوده شد.")
            } else {
                Result.failure(Exception(response.message ?: "خطا در ثبت مانهوا."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("عدم برقراری ارتباط با وبسایت: " + e.localizedMessage))
        }
    }

    // 4. Fetch Chapters (Syncing Network with cache)
    suspend fun getChapters(mangaId: Int): Result<List<ChapterItem>> {
        return try {
            val response = api.getChapters(mangaId)
            if (response.status == "success" && response.data != null) {
                val entities = response.data.map {
                    ChapterEntity(it.id, it.mangaId, it.chapterNumber, it.title, it.imagesJson, it.zipUrl, it.uploadedBy, it.createdAt)
                }
                dao.cacheChapters(entities)
                Result.success(response.data)
            } else {
                Result.failure(Exception("خطا در بارگذاری فصل مانهوا."))
            }
        } catch (e: Exception) {
            val cached = dao.getCachedChapters(mangaId)
            if (cached.isNotEmpty()) {
                val items = cached.map {
                    ChapterItem(it.id, it.mangaId, it.chapterNumber, it.title, it.imagesJson, it.zipUrl, it.uploadedBy, it.createdAt)
                }
                Result.success(items)
            } else {
                Result.failure(Exception("دسترسی به چپترها امکان پذیر نیست."))
            }
        }
    }

    // 5. ZIP Chapter Upload and Extraction
    suspend fun uploadChapterZip(
        file: File,
        mangaId: Int,
        chapterNumber: Double,
        title: String,
        userId: Int
    ): Result<String> {
        return try {
            val reqFile = file.asRequestBody("application/zip".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("zip_file", file.name, reqFile)

            val mIdPart = mangaId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val chNumPart = chapterNumber.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val uIdPart = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.uploadChapterZip(filePart, mIdPart, chNumPart, titlePart, uIdPart)
            if (response.status == "success") {
                Result.success("فایل زیپ با موفقیت آپلود شده و تمام صفحات در ریدر وب‌سایت و اپلیکیشن آماده شد.")
            } else {
                Result.failure(Exception(response.message ?: "خطا در استخراج فایل زیپ."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در اتصال یا پردازش آپلود زیپ مانهوا: " + e.localizedMessage))
        }
    }

    // 6. Exam Upload (Recruitment Portal)
    suspend fun uploadExamFile(file: File, userId: Int): Result<String> {
        return try {
            val reqFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("exam_file", file.name, reqFile)
            val userIdPart = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.uploadExamFile(filePart, userIdPart)
            if (response.status == "success") {
                Result.success("پاسخ آزمون شما با ردیف دیتابیس ثبت شد و برای مدیریت کل فرستاده گردید.")
            } else {
                Result.failure(Exception(response.message ?: "خطا در ثبت پاسخ آزمون."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در آپلود آزمون به سیستم: " + e.localizedMessage))
        }
    }

    // 7. Get Exam List (Superadmin Only)
    suspend fun getExams(userId: Int): Result<List<ExamItem>> {
        return try {
            val response = api.getExams(userId)
            if (response.status == "success" && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "خطا در بازیابی لیست آزمون‌ها."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطای عدم برقراری ارتباط با پایگاه داده ادمین: " + e.localizedMessage))
        }
    }

    // 8. Grade Exam
    suspend fun gradeExam(adminId: Int, examId: Int, status: String, score: Int): Result<String> {
        return try {
            val response = api.gradeExam(GradeExamRequest(adminId, examId, status, score))
            if (response.status == "success") {
                Result.success("آزمون نمره‌دهی شد و وضعیت متقاضی ویرایش گردید.")
            } else {
                Result.failure(Exception(response.message ?: "خطا در ثبت نمره آزمون."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در اتصال به پنل تایید نمرات: " + e.localizedMessage))
        }
    }

    // 9. Assign Staff to Manhwa Work
    suspend fun assignStaff(adminId: Int, staffId: Int, mangaId: Int, role: String): Result<String> {
        return try {
            val response = api.assignStaff(StaffAssignmentRequest(adminId, staffId, mangaId, role))
            if (response.status == "success") {
                Result.success("مترجم/طراح با موفقیت به مانهوا اختصاص داده شد.")
            } else {
                Result.failure(Exception(response.message ?: "خطا در تخصیص عضو تیم."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در برقراری ارتباط با پلتفرم تیم ترجمه: " + e.localizedMessage))
        }
    }

    // 10. Get App Settings (for Force Update)
    suspend fun getAppSettings(): Result<AppSettingsResponse> {
        return try {
            val response = api.getSettings()
            if (response.status == "success" && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "خطا در بارگذاری تنظیمات."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 11. Clear Manga Cache safely (excluding user profile)
    suspend fun clearMangaCache() {
        dao.clearMangaCache()
        dao.clearChapterCache()
    }

    // 12. Bookmarks Logic
    suspend fun getBookmarks(userId: Int): Result<List<BookmarkItem>> {
        return try {
            val response = api.listBookmarks(userId)
            if (response.status == "success" && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "خطا در دریافت لیست نشانک‌ها"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleBookmark(userId: Int, mangaId: Int): Result<String> {
        return try {
            val payload = mapOf("user_id" to userId, "manga_id" to mangaId)
            val response = api.toggleBookmark(payload)
            if (response.status == "success" && response.data != null) {
                Result.success(response.data["message"] ?: "تغییر وضعیت نشانک انجام شد.")
            } else {
                Result.failure(Exception(response.message ?: "خطا در برقراری وضعیت نشانک"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBookmarkStatus(userId: Int, mangaId: Int, status: String): Result<String> {
        return try {
            val payload = mapOf("user_id" to userId.toString(), "manga_id" to mangaId.toString(), "status" to status)
            val response = api.updateBookmarkStatus(payload)
            if (response.status == "success") {
                Result.success(response.message ?: "وضعیت نشانک مانهوا با موفقیت آپدیت شد.")
            } else {
                Result.failure(Exception(response.message ?: "خطا در به روزرسانی نشانک"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 13. Wallet Logic
    suspend fun walletGetBalance(userId: Int): Result<Int> {
        return try {
            val response = api.getWalletBalance(userId)
            if (response.status == "success" && response.data != null) {
                val balance = response.data["wallet_balance"] ?: 0
                Result.success(balance)
            } else {
                Result.failure(Exception(response.message ?: "خطا در دریافت موجودی"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun walletCharge(userId: Int, amount: Int): Result<Int> {
        return try {
            val payload = mapOf("user_id" to userId, "amount" to amount)
            val response = api.chargeWallet(payload)
            if (response.status == "success" && response.data != null) {
                // Here is return new balance, we can also extract or parse it to Int if it has double quotes
                val rawBal = response.data["wallet_balance"] ?: "0"
                val newBal = when(rawBal) {
                    is Number -> rawBal.toInt()
                    else -> rawBal.toString().toIntOrNull() ?: 0
                }
                Result.success(newBal)
            } else {
                Result.failure(Exception(response.message ?: "خطا در شارژ کیف پول"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
