package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey val mangaId: Int,
    val bookmarkedAt: Long = System.currentTimeMillis()
)
