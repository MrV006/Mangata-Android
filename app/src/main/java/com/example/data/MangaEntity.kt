package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mangas")
data class MangaEntity(
    @PrimaryKey val id: Int,
    val titleFa: String,
    val titleEn: String,
    val descriptionFa: String,
    val type: String, // manehwa (مانهوا), manga (مانگا), manhua (مانها)
    val coverUrl: String,
    val bannerUrl: String,
    val rating: Double,
    val status: String, // "در حال انتشار" or "پایان یافته"
    val genres: String, // comma separated
    val author: String,
    val translatorTeam: String,
    val chaptersCount: Int,
    val isPremium: Boolean, // Requires Myket VIP to see advanced chapters
    val reviewsJson: String, // For Gemini summaries
    val pagesJson: String // JSON array of image URLs representing pages for the current chapter
)
