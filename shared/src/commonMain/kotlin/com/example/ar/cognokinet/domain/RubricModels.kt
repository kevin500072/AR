package com.example.ar.cognokinet.domain

import kotlinx.serialization.Serializable

@Serializable
data class RubricLevel(
    val points: Int, // 1..4
    val label: String, // "Inicial", "En proceso", "Logrado", "Destacado"
    val descriptor: String
)

@Serializable
data class RubricCriterion(
    val id: String = IdGenerator.newId(),
    val name: String,
    val weight: Double, // Weight e.g. 25.0
    val levels: List<RubricLevel>
)

@Serializable
data class Rubric(
    val id: String = IdGenerator.newId(),
    val classId: String,
    val stationId: Int,
    val criteria: List<RubricCriterion>
)

@Serializable
data class CriterionScore(
    val criterionId: String,
    val points: Int // 1..4
)
