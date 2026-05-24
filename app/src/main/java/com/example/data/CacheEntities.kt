package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "manga_cache")
data class MangaEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val coverImage: String?,
    val createdAt: String
)

@Entity(tableName = "chapter_cache")
data class ChapterEntity(
    @PrimaryKey val id: Int,
    val mangaId: Int,
    val chapterNumber: Double,
    val title: String,
    val imagesJson: String?,
    val zipUrl: String?,
    val uploadedBy: Int,
    val createdAt: String
)

@Entity(tableName = "cached_user")
data class CachedUserEntity(
    @PrimaryKey val id: Int,
    val username: String,
    val email: String,
    val role: String,
    val displayName: String?,
    val token: String?
)
