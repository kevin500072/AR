package com.example.ar.cognokinet

import com.example.ar.cognokinet.domain.CriterionScore
import com.example.ar.cognokinet.domain.DecisionStatus
import com.example.ar.cognokinet.domain.Rubric

object RubricScoring {
    fun compute(rubric: Rubric, scores: List<CriterionScore>): Double {
        if (rubric.criteria.isEmpty()) return 0.0
        val scoreMap = scores.associateBy { it.criterionId }
        val totalWeight = rubric.criteria.sumOf { it.weight }
        if (totalWeight <= 0.0) return 0.0

        var weightedPoints = 0.0
        for (criterion in rubric.criteria) {
            val score = scoreMap[criterion.id]?.points?.coerceIn(1, 4) ?: 1
            // Points are 1..4, so percentage for this criterion is score / 4.0
            weightedPoints += (score / 4.0) * criterion.weight
        }

        return (weightedPoints / totalWeight) * 100.0
    }

    fun suggestStars(totalPercent: Double): Int {
        return when {
            totalPercent >= 85.0 -> 3
            totalPercent >= 65.0 -> 2
            totalPercent >= 40.0 -> 1
            else -> 0
        }
    }

    fun suggestDecision(totalPercent: Double): DecisionStatus {
        return if (totalPercent >= 40.0) DecisionStatus.PASS else DecisionStatus.REPEAT
    }

    fun isCommentRequired(
        decision: DecisionStatus,
        comment: String,
        suggestedStars: Int,
        finalStars: Int,
        suggestedDecision: DecisionStatus
    ): Boolean {
        val hasChangedSuggestion = decision != suggestedDecision || finalStars != suggestedStars
        if (decision == DecisionStatus.REPEAT || hasChangedSuggestion) {
            return comment.trim().isEmpty()
        }
        return false
    }
}
