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
    val purchasedChaptersJson: String,
    val bookmarksJson: String? = null,
    val readHistoryJson: String? = null
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
    val bookmarksJson: String? = null,
    val readHistoryJson: String? = null,
    val error: String? = null
)

data class PurchaseResponse(
    val success: Boolean,
    val walletRial: Long,
    val walletGiftChapters: Int,
    val purchasedChaptersJson: String,
    val errorMessage: String? = null
)

data class AdminSettingsRequest(
    val baseChapterPrice: Int,
    val discountPercent50: Int,
    val discountPercent100: Int,
    val defaultStaffRewardChapters: Int,
    val minChaptersForStoryToken: Int,
    val storyTokensAwarded: Int,
    val maxVideoStoryDurationSeconds: Int,
    val shareCleanerPct: Int,
    val shareEditorPct: Int,
    val shareTranslatorPct: Int,
    val sharePlatformPct: Int,
    val isTranslatorTestUploaded: Boolean,
    val isCleanerTestUploaded: Boolean,
    val isTypistTestUploaded: Boolean,
    val requiredVersion: Int,
    val featuredMangaIdsJson: String,
    val startsFromZeroMangaIdsJson: String
)

data class UpdateUserRequest(
    val id: Int,
    val role: String? = null,
    val subRole: String? = null,
    val walletRial: Long? = null,
    val walletGiftChapters: Int? = null,
    val customRewardRate: Int? = null
)

data class UpdateRecruitmentRequest(
    val id: Int,
    val status: String
)

data class AddRecruitmentRequest(
    val fullName: String,
    val messengerId: String,
    val specialty: String,
    val testFileName: String,
    val uploadedWorkName: String
)

data class AdminMangaSaveRequest(
    val id: Int? = null,
    val titleFa: String,
    val titleEn: String,
    val descriptionFa: String,
    val coverUrl: String,
    val bannerUrl: String,
    val type: String = "مانهوا",
    val status: String = "در حال انتشار",
    val genres: String = "فانتزی, اکشن",
    val author: String = "نامشخص",
    val translatorTeam: String = "تیم مانگاتا",
    val chaptersCount: Int = 10,
    val isPremium: Boolean,
    val pagesJson: String
)

data class AdminMangaDeleteRequest(
    val id: Int
)

data class GenericAdminResponse(
    val success: Boolean,
    val id: Int? = null,
    val error: String? = null
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

    @GET("wp-json/mangata/v1/admin/get-settings")
    suspend fun getAdminSettings(): com.example.data.SystemSettingsEntity

    @POST("wp-json/mangata/v1/admin/update-settings")
    suspend fun updateAdminSettings(@Body req: AdminSettingsRequest): com.example.data.SystemSettingsEntity

    @GET("wp-json/mangata/v1/admin/get-users")
    suspend fun getAdminUsers(): List<com.example.data.UserAccount>

    @POST("wp-json/mangata/v1/admin/update-user")
    suspend fun updateAdminUser(@Body req: UpdateUserRequest): GenericAdminResponse

    @GET("wp-json/mangata/v1/admin/get-recruitments")
    suspend fun getAdminRecruitments(): List<com.example.data.RecruitmentApplication>

    @POST("wp-json/mangata/v1/admin/add-recruitment")
    suspend fun addAdminRecruitment(@Body req: AddRecruitmentRequest): com.example.data.RecruitmentApplication

    @POST("wp-json/mangata/v1/admin/update-recruitment")
    suspend fun updateAdminRecruitment(@Body req: UpdateRecruitmentRequest): GenericAdminResponse

    @POST("wp-json/mangata/v1/admin/save-manga")
    suspend fun saveManga(@Body req: AdminMangaSaveRequest): GenericAdminResponse

    @POST("wp-json/mangata/v1/admin/delete-manga")
    suspend fun deleteManga(@Body req: AdminMangaDeleteRequest): GenericAdminResponse
}
