package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_purchases")
data class UserPurchase(
    @PrimaryKey val sku: String, // SKU representing subscription model (e.g. VIP_1MONTH, VIP_LIFETIME)
    val purchaseTime: Long,
    val token: String, // Myket purchase token
    val orderId: String, // Myket order ID
    val skuNameFa: String // Farsi descriptive name
)
