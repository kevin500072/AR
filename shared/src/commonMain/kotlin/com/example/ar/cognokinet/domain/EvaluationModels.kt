package com.example.ar.cognokinet.domain

import kotlinx.serialization.Serializable

@Serializable
enum class EvaluationStatus {
    PENDING,
    PASSED,
    REPEAT
}

@Serializable
enum class DecisionStatus {
    PASS,
    REPEAT
}

@Serializable
data class EvaluationRequest(
    val id: String = IdGenerator.newId(),
    val studentId: String,
    val classId: String = "",
    val stationId: Int,
    val status: EvaluationStatus = EvaluationStatus.PENDING,
    val requestedAt: Long = 0L,
    val studentName: String = "",
    val stars: Int = 0,
    val comment: String = ""
) {
    val exerciseId: Int
        get() = stationId
}

@Serializable
data class EvaluationReview(
    val requestId: String,
    val classId: String,
    val stationId: Int,
    val scores: List<CriterionScore>,
    val totalPercent: Double,
    val suggestedStars: Int,
    val finalStars: Int,
    val decision: DecisionStatus,
    val bonusAcorns: Int = 0,
    val bonusLeaves: Int = 0,
    val comment: String,
    val reviewedAt: Long = 0L,
    val reviewedByTeacherId: String = ""
)
