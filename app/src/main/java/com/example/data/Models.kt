package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String, // Exam, Admission, General, Academic, Sports
    val date: String,
    val description: String,
    val pdfUrl: String,
    val isImportant: Boolean = false,
    val isBookmarked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "results")
data class ResultEntity(
    @PrimaryKey val id: String,
    val title: String,
    val course: String, // UG, PG, B.Tech, B.Ed, Vocational
    val session: String, // e.g. 2023-2026
    val publishDate: String,
    val pdfUrl: String,
    val isBookmarked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "pyqs")
data class PyqEntity(
    @PrimaryKey val id: String,
    val title: String,
    val course: String, // B.A, B.Sc, B.Com, BCA, BBA, M.A, M.Sc, M.Com, B.Ed
    val department: String,
    val year: String, // 2025, 2024, 2023, 2022
    val semester: String, // Sem 1, Sem 2, Sem 3, Sem 4, Sem 5, Sem 6
    val pdfUrl: String,
    val isBookmarked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "admissions")
data class AdmissionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String, // UG Admission, PG Admission, Ph.D Entrance
    val academicYear: String,
    val lastDate: String,
    val linkUrl: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "scholarships")
data class ScholarshipEntity(
    @PrimaryKey val id: String,
    val title: String,
    val provider: String, // Bihar Govt, NSP, University
    val amount: String,
    val deadline: String,
    val eligibility: String,
    val applyUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "banners")
data class BannerEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val actionRoute: String
)

@Entity(tableName = "user_notifications")
data class UserNotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val date: String,
    val type: String, // Notice, Result, Admission, System
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "student_suggestions")
data class StudentSuggestionEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val studentName: String,
    val studentEmail: String,
    val suggestionText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = ""
)

