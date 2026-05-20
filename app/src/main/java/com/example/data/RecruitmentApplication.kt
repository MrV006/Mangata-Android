package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recruitment_applications")
data class RecruitmentApplication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val messengerId: String, // Telegram, Rubika, Bale ID
    val specialty: String, // "مترجم", "تایپیست/ادیتور", "کلینر"
    val testFileName: String, // raw file downloaded
    val uploadedWorkName: String, // file uploaded by applicant
    val status: String, // "PENDING", "APPROVED", "REJECTED"
    val dateSubmitted: String
)
