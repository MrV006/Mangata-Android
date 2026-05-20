package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "read_history")
data class ReadHistory(
    @PrimaryKey val mangaId: Int,
    val currentChapter: Int,
    val scrollPercent: Float, // exact reading percentage scroll position
    val lastReadTimestamp: Long = System.currentTimeMillis()
)
