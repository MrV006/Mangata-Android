package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "team_members")
data class TeamMember(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val roleFa: String, // "مدیر کل", "مترجم", "تایپیست", "کلینر", "ادیتور"
    val levelCode: Int, // 1 for Super Admin, 2 for Core Editors, 3 for team members (translator / typesetter / cleaner)
    val assignedWorks: String, // comma separated titles of mangas
    val rating: Double = 5.0,
    val statusText: String = "فعال"
)
