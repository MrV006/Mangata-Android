package com.example.network

import com.example.data.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface MangaApiService {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): ApiResponse<UserData>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): ApiResponse<UserData>

    @GET("auth/check-session")
    suspend fun checkSession(
        @Query("user_id") userId: Int,
        @Query("token") token: String
    ): ApiResponse<SessionValidResponse>

    @GET("settings/get")
    suspend fun getSettings(): ApiResponse<AppSettingsResponse>

    @GET("manhwa/list")
    suspend fun getManhwas(
        @Query("search") search: String? = null,
        @Query("genre") genre: String? = null,
        @Query("year") year: String? = null,
        @Query("character") character: String? = null
    ): ApiResponse<List<MangaItem>>

    @POST("manhwa/create")
    suspend fun createManhwa(
        @Body request: Map<String, String>
    ): ApiResponse<Map<String, String>>

    @GET("chapter/list")
    suspend fun getChapters(
        @Query("manga_id") mangaId: Int?
    ): ApiResponse<List<ChapterItem>>

    @Multipart
    @POST("chapter/upload-zip")
    suspend fun uploadChapterZip(
        @Part zipFile: MultipartBody.Part,
        @Part("manga_id") mangaId: RequestBody,
        @Part("chapter_number") chapterNumber: RequestBody,
        @Part("title") title: RequestBody,
        @Part("user_id") userId: RequestBody
    ): ApiResponse<Map<String, String>>

    @Multipart
    @POST("exam/upload")
    suspend fun uploadExamFile(
        @Part examFile: MultipartBody.Part,
        @Part("user_id") userId: RequestBody
    ): ApiResponse<Map<String, String>>

    @GET("exam/list")
    suspend fun getExams(
        @Query("user_id") userId: Int
    ): ApiResponse<List<ExamItem>>

    @POST("exam/grade")
    suspend fun gradeExam(
        @Body request: GradeExamRequest
    ): ApiResponse<Map<String, String>>

    @POST("staff/assign")
    suspend fun assignStaff(
        @Body request: StaffAssignmentRequest
    ): ApiResponse<Map<String, String>>

    @GET("bookmark/list")
    suspend fun listBookmarks(
        @Query("user_id") userId: Int
    ): ApiResponse<List<BookmarkItem>>

    @POST("bookmark/toggle")
    suspend fun toggleBookmark(
        @Body request: Map<String, Int>
    ): ApiResponse<Map<String, String>>

    @POST("bookmark/update-status")
    suspend fun updateBookmarkStatus(
        @Body request: Map<String, String>
    ): ApiResponse<Map<String, String>>

    @GET("wallet/get")
    suspend fun getWalletBalance(
        @Query("user_id") userId: Int
    ): ApiResponse<Map<String, Int>>

    @POST("wallet/charge")
    suspend fun chargeWallet(
        @Body request: Map<String, Int>
    ): ApiResponse<Map<String, String>>

    // Dynamic Blog & Critique Reviews Endpoints
    @GET("blog/list")
    suspend fun getBlogs(): ApiResponse<List<BlogItem>>

    @GET("review/list")
    suspend fun getReviews(): ApiResponse<List<ReviewItem>>

    @POST("review/create")
    suspend fun createReview(
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): ApiResponse<Map<String, String>>
}
