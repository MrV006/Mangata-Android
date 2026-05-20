package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "webtoon_stories")
data class StoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val staffId: Int,
    val staffName: String,
    val staffRole: String,
    val mediaUrl: String, // Path to image
    val caption: String,
    val uploadTime: Long // timestamp for auto pruning
)
