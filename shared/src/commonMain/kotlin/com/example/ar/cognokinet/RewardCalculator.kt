package com.example.ar.cognokinet

import com.example.ar.cognokinet.domain.DecisionStatus

data class RewardResult(
    val acornsGained: Int,
    val leavesGained: Int,
    val xpGained: Int
)

object RewardCalculator {
    fun calculate(
        exercise: ExerciseItem,
        finalStars: Int,
        bonusAcorns: Int = 0,
        bonusLeaves: Int = 0,
        previousBestStars: Int = 0,
        decision: DecisionStatus = DecisionStatus.PASS
    ): RewardResult {
        if (decision == DecisionStatus.REPEAT || finalStars <= 0) {
            return RewardResult(0, 0, 0)
        }

        val clampedStars = finalStars.coerceIn(1, 3)
        val clampedBonusAcorns = bonusAcorns.coerceIn(0, 20)
        val clampedBonusLeaves = bonusLeaves.coerceIn(0, 20)

        val starMultiplier = when (clampedStars) {
            1 -> 1.0f
            2 -> 1.25f
            else -> 1.5f
        }

        val isFirstCompletion = previousBestStars == 0

        if (isFirstCompletion) {
            val acorns = ((exercise.baseRewardAcorns * starMultiplier).toInt() + clampedBonusAcorns).coerceAtLeast(0)
            val leaves = ((exercise.baseRewardLeaves * starMultiplier).toInt() + clampedBonusLeaves).coerceAtLeast(0)
            val xp = 100
            return RewardResult(acornsGained = acorns, leavesGained = leaves, xpGained = xp)
        } else {
            // Repetition: 25% base or difference if stars improved
            val prevMultiplier = when (previousBestStars.coerceIn(1, 3)) {
                1 -> 1.0f
                2 -> 1.25f
                else -> 1.5f
            }
            val multiplierDiff = (starMultiplier - prevMultiplier).coerceAtLeast(0.25f)
            val acorns = ((exercise.baseRewardAcorns * multiplierDiff).toInt() + clampedBonusAcorns).coerceAtLeast(0)
            val leaves = ((exercise.baseRewardLeaves * multiplierDiff).toInt() + clampedBonusLeaves).coerceAtLeast(0)
            val xp = 30
            return RewardResult(acornsGained = acorns, leavesGained = leaves, xpGained = xp)
        }
    }
}
