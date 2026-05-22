package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaDao {
    @Query("SELECT * FROM mangas")
    fun getAllMangas(): Flow<List<MangaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMangas(mangas: List<MangaEntity>)

    @Query("DELETE FROM mangas WHERE id = :id")
    suspend fun deleteMangaById(id: Int)

    @Query("SELECT * FROM mangas WHERE id = :id")
    fun getMangaById(id: Int): Flow<MangaEntity?>

    @Query("SELECT * FROM mangas WHERE id = :id")
    suspend fun getMangaByIdOneShot(id: Int): MangaEntity?

    @Query("SELECT * FROM bookmarks")
    fun getBookmarks(): Flow<List<Bookmark>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE mangaId = :mangaId)")
    fun isBookmarked(mangaId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark)

    @Query("DELETE FROM bookmarks WHERE mangaId = :mangaId")
    suspend fun deleteBookmark(mangaId: Int)

    @Query("SELECT * FROM read_history ORDER BY lastReadTimestamp DESC")
    fun getReadHistory(): Flow<List<ReadHistory>>

    @Query("SELECT * FROM read_history WHERE mangaId = :mangaId")
    fun getHistoryForManga(mangaId: Int): Flow<ReadHistory?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadHistory(history: ReadHistory)

    @Query("DELETE FROM read_history WHERE mangaId = :mangaId")
    suspend fun deleteHistoryForManga(mangaId: Int)

    // Team management queries
    @Query("SELECT * FROM team_members ORDER BY levelCode ASC")
    fun getAllTeamMembers(): Flow<List<TeamMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeamMember(member: TeamMember)

    @Delete
    suspend fun deleteTeamMember(member: TeamMember)

    // Myket billing queries
    @Query("SELECT * FROM user_purchases")
    fun getPurchases(): Flow<List<UserPurchase>>

    @Query("SELECT EXISTS(SELECT 1 FROM user_purchases WHERE sku = :sku)")
    fun hasActivePurchase(sku: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: UserPurchase)

    @Query("DELETE FROM user_purchases WHERE sku = :sku")
    suspend fun deletePurchase(sku: String)

    @Query("DELETE FROM user_purchases")
    suspend fun clearPurchases()

    // --- Advanced Features DAO ---

    // User Accounts queries
    @Query("SELECT * FROM user_accounts")
    fun getAllUserAccounts(): Flow<List<UserAccount>>

    @Query("SELECT * FROM user_accounts WHERE id = :id")
    fun getUserAccountById(id: Int): Flow<UserAccount?>

    @Query("SELECT * FROM user_accounts WHERE id = :id")
    suspend fun getUserAccountByIdOneShot(id: Int): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(user: UserAccount)

    // System Settings queries
    @Query("SELECT * FROM system_settings WHERE id = 1")
    fun getSystemSettings(): Flow<SystemSettingsEntity?>

    @Query("SELECT * FROM system_settings WHERE id = 1")
    suspend fun getSystemSettingsOneShot(): SystemSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystemSettings(settings: SystemSettingsEntity)

    // Recruitment queries
    @Query("SELECT * FROM recruitment_applications ORDER BY id DESC")
    fun getAllRecruitments(): Flow<List<RecruitmentApplication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecruitment(app: RecruitmentApplication)

    @Query("DELETE FROM recruitment_applications WHERE id = :id")
    suspend fun deleteRecruitment(id: Int)

    // Story queries
    @Query("SELECT * FROM webtoon_stories ORDER BY uploadTime DESC")
    fun getAllStories(): Flow<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)

    @Query("DELETE FROM webtoon_stories WHERE id = :id")
    suspend fun deleteStory(id: Int)

    @Query("DELETE FROM webtoon_stories WHERE uploadTime < :expiryTime")
    suspend fun pruneStories(expiryTime: Long)

    @Query("DELETE FROM webtoon_stories")
    suspend fun clearAllStories()

    // Chapter Unlock queries
    @Query("SELECT * FROM chapter_purchase_records WHERE userId = :userId")
    fun getPurchasedChapters(userId: Int): Flow<List<ChapterPurchaseRecord>>

    @Query("SELECT EXISTS(SELECT 1 FROM chapter_purchase_records WHERE userId = :userId AND mangaId = :mangaId AND chapterNumber = :chapterNumber)")
    fun isChapterUnlocked(userId: Int, mangaId: Int, chapterNumber: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapterPurchase(record: ChapterPurchaseRecord)

    // Support Ticket queries
    @Query("SELECT * FROM support_tickets ORDER BY id DESC")
    fun getAllSupportTickets(): Flow<List<SupportTicket>>

    @Query("SELECT * FROM support_tickets WHERE userId = :userId ORDER BY id DESC")
    fun getSupportTicketsByUserId(userId: Int): Flow<List<SupportTicket>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupportTicket(ticket: SupportTicket)

    // Chapter Work tracking queries
    @Query("SELECT * FROM chapter_works ORDER BY id DESC")
    fun getAllChapterWorks(): Flow<List<ChapterWork>>

    @Query("SELECT * FROM chapter_works WHERE mangaId = :mangaId AND chapterNumber = :chapterNumber")
    fun getChapterWorkByMangaAndNumber(mangaId: Int, chapterNumber: Int): Flow<ChapterWork?>

    @Query("SELECT * FROM chapter_works WHERE mangaId = :mangaId AND chapterNumber = :chapterNumber")
    suspend fun getChapterWorkByMangaAndNumberOneShot(mangaId: Int, chapterNumber: Int): ChapterWork?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapterWork(work: ChapterWork)
}
