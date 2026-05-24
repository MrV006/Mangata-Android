package com.example.data

import androidx.room.*

@Dao
interface MangaDao {

    // User Profile Cache Queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(user: CachedUserEntity)

    @Query("SELECT * FROM cached_user LIMIT 1")
    suspend fun getCurrentUserProfile(): CachedUserEntity?

    @Query("DELETE FROM cached_user")
    suspend fun clearUserProfile()

    // Manga cache
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheMangas(mangas: List<MangaEntity>)

    @Query("SELECT * FROM manga_cache ORDER BY id DESC")
    suspend fun getCachedMangas(): List<MangaEntity>

    // Chapter cache
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheChapters(chapters: List<ChapterEntity>)

    @Query("SELECT * FROM chapter_cache WHERE mangaId = :mangaId ORDER BY chapterNumber ASC")
    suspend fun getCachedChapters(mangaId: Int): List<ChapterEntity>

    @Query("DELETE FROM manga_cache")
    suspend fun clearMangaCache()

    @Query("DELETE FROM chapter_cache")
    suspend fun clearChapterCache()
}
