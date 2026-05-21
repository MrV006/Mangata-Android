package com.example.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class ServerChapterPurchase(
    val mangaId: Int,
    val chapterNumber: Int
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val displayName: String,
    val password: String
)

data class SyncRequest(
    val username: String,
    val walletRial: Long,
    val walletGiftChapters: Int,
    val purchasedChaptersJson: String
)

data class PurchaseRequest(
    val userId: Int,
    val mangaId: Int,
    val chapterNumber: Int,
    val price: Long,
    val isGiftUse: Boolean
)

data class UserResponse(
    val id: Int,
    val username: String,
    val displayName: String,
    val role: String,
    val subRole: String,
    val walletRial: Long,
    val walletGiftChapters: Int,
    val purchasedChaptersJson: String,
    val error: String? = null
)

data class PurchaseResponse(
    val success: Boolean,
    val walletRial: Long,
    val walletGiftChapters: Int,
    val purchasedChaptersJson: String,
    val errorMessage: String? = null
)

interface MangaApiService {
    // This connects to the custom WordPress REST API endpoint defined in our Mangata theme
    @GET("wp-json/mangata/v1/mangas")
    suspend fun getMangas(): List<com.example.data.MangaEntity>

    @POST("wp-json/mangata/v1/login")
    suspend fun loginUser(@Body req: LoginRequest): UserResponse

    @POST("wp-json/mangata/v1/register")
    suspend fun registerUser(@Body req: RegisterRequest): UserResponse

    @POST("wp-json/mangata/v1/sync")
    suspend fun syncUserData(@Body req: SyncRequest): UserResponse

    @POST("wp-json/mangata/v1/purchase")
    suspend fun purchaseChapterOnServer(@Body req: PurchaseRequest): PurchaseResponse
}
