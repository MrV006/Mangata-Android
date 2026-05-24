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

    @GET("manhwa/list")
    suspend fun getManhwas(): ApiResponse<List<MangaItem>>

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
}
