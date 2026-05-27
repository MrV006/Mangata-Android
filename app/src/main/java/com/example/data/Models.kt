package com.example.data

import com.google.gson.annotations.SerializedName

// Generic API wrapper
data class ApiResponse<T>(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: T?
)

// Authentication
data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String
)

data class UserData(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("token") val token: String?
)

// Manhwas & Chapters
data class MangaItem(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("cover_image") val coverImage: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("genres") val genres: String? = null,
    @SerializedName("release_year") val releaseYear: String? = null,
    @SerializedName("main_characters") val mainCharacters: String? = null,
    @SerializedName("author") val author: String? = null
)

data class ChapterItem(
    @SerializedName("id") val id: Int,
    @SerializedName("manga_id") val mangaId: Int,
    @SerializedName("chapter_number") val chapterNumber: Double,
    @SerializedName("title") val title: String,
    @SerializedName("images_json") val imagesJson: String?,
    @SerializedName("zip_url") val zipUrl: String?,
    @SerializedName("uploaded_by") val uploadedBy: Int,
    @SerializedName("created_at") val createdAt: String
)

// Recruitment Exams
data class ExamItem(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("username") val username: String?,
    @SerializedName("file_name") val fileName: String,
    @SerializedName("file_url") val fileUrl: String,
    @SerializedName("status") val status: String, // Pending, Accepted, Rejected
    @SerializedName("score") val score: Int?,
    @SerializedName("created_at") val createdAt: String
)

data class GradeExamRequest(
    @SerializedName("admin_id") val adminId: Int,
    @SerializedName("exam_id") val examId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("score") val score: Int
)

data class StaffAssignmentRequest(
    @SerializedName("admin_id") val adminId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("manga_id") val mangaId: Int,
    @SerializedName("role") val role: String
)

data class SessionValidResponse(
    @SerializedName("valid") val valid: Boolean,
    @SerializedName("role") val role: String
)

data class AppSettingsResponse(
    @SerializedName("force_update_app_active") val forceUpdateAppActive: String,
    @SerializedName("force_update_app_url") val forceUpdateAppUrl: String,
    @SerializedName("force_update_app_msg") val forceUpdateAppMsg: String,
    @SerializedName("force_update_web_active") val forceUpdateWebActive: String,
    @SerializedName("force_update_web_version") val forceUpdateWebVersion: String,
    @SerializedName("force_update_web_msg") val forceUpdateWebMsg: String
)
