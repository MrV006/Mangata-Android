package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_settings")
data class SystemSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val baseChapterPrice: Int = 400, // Tomans per chapter
    val discountPercent50: Int = 20, // 20% discount if bulk buying 50 chapters
    val discountPercent100: Int = 40, // 40% discount if bulk buying 100 chapters
    val defaultStaffRewardChapters: Int = 5, // Chapters gifted for each work contribution
    val minChaptersForStoryToken: Int = 40, // Threshold to get story tokens
    val storyTokensAwarded: Int = 2, // Free story token count if threshold met
    val maxVideoStoryDurationSeconds: Int = 30, // Default duration limit
    val shareCleanerPct: Int = 30,
    val shareEditorPct: Int = 30,
    val shareTranslatorPct: Int = 20,
    val sharePlatformPct: Int = 20
)
