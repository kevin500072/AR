package com.example.ar.cognokinet.domain

import kotlinx.serialization.Serializable

@Serializable
data class Teacher(
    val id: String = IdGenerator.newId(),
    val displayName: String
)

@Serializable
enum class EnrollmentStatus {
    PENDING,
    ACTIVE,
    REJECTED,
    LEFT
}

@Serializable
data class Classroom(
    val id: String = IdGenerator.newId(),
    val name: String,
    val grade: String,
    val group: String,
    val schoolYear: String,
    val teacherId: String,
    val teacherName: String,
    val joinCode: String,
    val joinOpen: Boolean = true,
    val createdAt: Long = 0L
)

@Serializable
data class Enrollment(
    val classId: String,
    val studentId: String,
    val status: EnrollmentStatus = EnrollmentStatus.PENDING,
    val joinedAt: Long = 0L
)

@Serializable
data class StudentProfile(
    val id: String = IdGenerator.newId(),
    val displayName: String,
    val age: Int,
    val grade: String,
    val avatarId: String,
    val consentGiven: Boolean = false,
    val createdAt: Long = 0L
) {
    val name: String
        get() = displayName
}
