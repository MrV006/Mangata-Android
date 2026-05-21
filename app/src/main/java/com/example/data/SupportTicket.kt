package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "support_tickets")
data class SupportTicket(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val senderUsername: String,
    val title: String,
    val description: String,
    val replyMessage: String? = null,
    val replierName: String? = null,
    val isAnswered: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
