package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val displayName: String,
    val role: String, // "SUPER_ADMIN", "DEPT_ADMIN", "STAFF", "NORMAL_USER"
    val subRole: String, // "مدیر ترجمه", "مترجم", "تایپیست/ادیتور", "کلینر", "کاربر عادی"
    val walletRial: Long, // Ballance in Tomans
    val walletGiftChapters: Int, // Counter for gift chapters
    val chaptersContributedLastMonth: Int = 0,
    val chaptersContributedThisMonth: Int = 0,
    val storyTokens: Int = 0,
    val customRewardRate: Int? = null // Individual reward chapter overrides
)
