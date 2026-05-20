package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapter_purchase_records")
data class ChapterPurchaseRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val mangaId: Int,
    val chapterNumber: Int,
    val purchaseTime: Long
)
