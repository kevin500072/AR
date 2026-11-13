package com.example.ar.cognokinet

import androidx.compose.runtime.Composable
import com.example.ar.cognokinet.domain.Classroom
import com.example.ar.cognokinet.domain.EvaluationRequest
import com.example.ar.cognokinet.domain.EvaluationReview
import com.example.ar.cognokinet.domain.StudentProfile
import com.example.ar.cognokinet.domain.StudentProgress
import kotlinx.serialization.Serializable

@Serializable
data class LocalGameSnapshot(
    val studentProfiles: List<StudentProfile> = emptyList(),
    val activeProfileId: String? = null,
    val progress: List<StudentProgress> = emptyList(),
    val teacherName: String = "Prof. Guía",
    val teacherGrade: String = "3º A de Primaria",
    val teacherSubject: String = "Educación Física",
    val classrooms: List<Classroom> = emptyList(),
    val evaluationRequests: List<EvaluationRequest> = emptyList(),
    val evaluationReviews: List<EvaluationReview> = emptyList(),
    val isMusicEnabled: Boolean = true
)

interface LocalGameStore {
    fun load(): String?
    fun save(snapshotJson: String)
    fun clear()
}

@Composable
expect fun rememberLocalGameStore(): LocalGameStore
