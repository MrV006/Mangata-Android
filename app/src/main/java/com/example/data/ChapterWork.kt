package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapter_works")
data class ChapterWork(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mangaId: Int,
    val mangaTitle: String,
    val chapterNumber: Int,
    val translatorId: Int,
    val translatorName: String,
    val cleanerId: Int,
    val cleanerName: String,
    val editorId: Int,
    val editorName: String,
    val revenueEarned: Long = 0L,
    val cleanerPaid: Long = 0L,
    val editorPaid: Long = 0L,
    val translatorPaid: Long = 0L,
    val platformEarned: Long = 0L
)
