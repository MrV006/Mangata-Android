package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaDao {
    @Query("SELECT * FROM mangas")
    fun getAllMangas(): Flow<List<MangaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMangas(mangas: List<MangaEntity>)

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
}
