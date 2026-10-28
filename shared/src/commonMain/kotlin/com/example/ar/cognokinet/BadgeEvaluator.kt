package com.example.ar.cognokinet

import com.example.ar.cognokinet.domain.DecisionStatus
import com.example.ar.cognokinet.domain.EvaluationReview
import com.example.ar.cognokinet.domain.StudentProgress

object BadgeEvaluator {
    fun evaluate(progress: StudentProgress, evaluationHistory: List<EvaluationReview>): List<String> {
        val currentBadges = progress.unlockedBadges.toMutableSet()
        val approvedHistory = evaluationHistory.filter { it.decision == DecisionStatus.PASS }

        // Station Badges (1..5)
        for (stationId in 1..5) {
            if (progress.completedStationIds.contains(stationId)) {
                currentBadges.add("badge_$stationId")
            }
        }

        // badge_6: Primer Reto
        if (approvedHistory.isNotEmpty()) {
            currentBadges.add("badge_6")
        }

        // badge_7: Tres Estrellas
        if (approvedHistory.any { it.finalStars >= 3 }) {
            currentBadges.add("badge_7")
        }

        // badge_8: Constancia (3 evaluaciones aprobadas)
        if (approvedHistory.size >= 3) {
            currentBadges.add("badge_8")
        }

        // badge_9: Gran Campeón (5 estaciones completas)
        if (progress.completedStationIds.size >= CognokinetConstants.TOTAL_STATIONS) {
            currentBadges.add("badge_9")
        }

        return currentBadges.toList()
    }
}
