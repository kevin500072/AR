package com.example.ar.cognokinet

import com.example.ar.cognokinet.domain.CriterionScore
import com.example.ar.cognokinet.domain.DecisionStatus
import com.example.ar.cognokinet.domain.EvaluationReview
import com.example.ar.cognokinet.domain.StudentProgress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CognokinetUnitTest {

    @Test
    fun testLevelCalculation() {
        assertEquals(1, CognokinetConstants.levelFor(0))
        assertEquals(1, CognokinetConstants.levelFor(199))
        assertEquals(2, CognokinetConstants.levelFor(200))
        assertEquals(3, CognokinetConstants.levelFor(450))
        assertEquals(1, StudentProgress.levelForXp(0))
        assertEquals(2, StudentProgress.levelForXp(200))
    }

    @Test
    fun testStationUnlockRules() {
        // Station 1 is always unlocked
        assertTrue(StationUnlockRules.isUnlocked(1, emptySet()))

        // Station 2 is locked if station 1 is not passed
        assertFalse(StationUnlockRules.isUnlocked(2, emptySet()))

        // Skipping to Station 3 must fail if Station 2 is missing
        assertFalse(StationUnlockRules.isUnlocked(3, setOf(1)))

        // Station 2 unlocked when station 1 passed
        assertTrue(StationUnlockRules.isUnlocked(2, setOf(1)))

        // Station 3 unlocked when stations 1 and 2 passed
        assertTrue(StationUnlockRules.isUnlocked(3, setOf(1, 2)))
    }

    @Test
    fun testRubricScoringAndStars() {
        val rubric = RubricDefaults.forStation(1, "class_1")
        val criteriaCount = rubric.criteria.size
        assertTrue(criteriaCount > 0)

        // All 4 points -> 100%
        val perfectScores = rubric.criteria.map { CriterionScore(it.id, 4) }
        val perfectPercent = RubricScoring.compute(rubric, perfectScores)
        assertEquals(100.0, perfectPercent, 0.01)
        assertEquals(3, RubricScoring.suggestStars(perfectPercent))
        assertEquals(DecisionStatus.PASS, RubricScoring.suggestDecision(perfectPercent))

        // All 1 point -> 25%
        val lowScores = rubric.criteria.map { CriterionScore(it.id, 1) }
        val lowPercent = RubricScoring.compute(rubric, lowScores)
        assertEquals(25.0, lowPercent, 0.01)
        assertEquals(0, RubricScoring.suggestStars(lowPercent))
        assertEquals(DecisionStatus.REPEAT, RubricScoring.suggestDecision(lowPercent))

        // All 3 points -> 75% -> 2 stars
        val midScores = rubric.criteria.map { CriterionScore(it.id, 3) }
        val midPercent = RubricScoring.compute(rubric, midScores)
        assertEquals(75.0, midPercent, 0.01)
        assertEquals(2, RubricScoring.suggestStars(midPercent))
    }

    @Test
    fun testRubricDefaultsWeightSum() {
        val rubric = RubricDefaults.forStation(1, "class_1")
        val weightSum = rubric.criteria.sumOf { it.weight }
        assertEquals(100.0, weightSum, 0.001)

        val allRubrics = RubricDefaults.forAllStations("class_1")
        assertEquals(5, allRubrics.size)
        allRubrics.forEach { r ->
            assertEquals(100.0, r.criteria.sumOf { it.weight }, 0.001)
        }
    }

    @Test
    fun testRewardCalculationWithDecision() {
        val exercise = ExerciseCatalog.getById(1)

        // REPEAT decision gives 0 rewards
        val repeatReward = RewardCalculator.calculate(
            exercise = exercise,
            finalStars = 2,
            bonusAcorns = 10,
            bonusLeaves = 5,
            previousBestStars = 0,
            decision = DecisionStatus.REPEAT
        )
        assertEquals(0, repeatReward.acornsGained)
        assertEquals(0, repeatReward.leavesGained)
        assertEquals(0, repeatReward.xpGained)

        // PASS decision gives rewards
        val passReward = RewardCalculator.calculate(
            exercise = exercise,
            finalStars = 3,
            bonusAcorns = 10,
            bonusLeaves = 5,
            previousBestStars = 0,
            decision = DecisionStatus.PASS
        )
        assertTrue(passReward.acornsGained > 0)
        assertTrue(passReward.leavesGained > 0)
        assertEquals(100, passReward.xpGained)
    }

    @Test
    fun testBadgeEvaluationWithReviews() {
        val progress = StudentProgress.inicial("student_1").copy(
            completedStationIds = setOf(1, 2, 3, 4, 5)
        )

        val reviews = listOf(
            EvaluationReview(
                requestId = "req_1",
                classId = "class_1",
                stationId = 1,
                scores = emptyList(),
                totalPercent = 90.0,
                suggestedStars = 3,
                finalStars = 3,
                decision = DecisionStatus.PASS,
                comment = "Excelente"
            ),
            EvaluationReview(
                requestId = "req_2",
                classId = "class_1",
                stationId = 2,
                scores = emptyList(),
                totalPercent = 80.0,
                suggestedStars = 2,
                finalStars = 2,
                decision = DecisionStatus.PASS,
                comment = "Muy bien"
            ),
            EvaluationReview(
                requestId = "req_3",
                classId = "class_1",
                stationId = 3,
                scores = emptyList(),
                totalPercent = 85.0,
                suggestedStars = 3,
                finalStars = 3,
                decision = DecisionStatus.PASS,
                comment = "Genial"
            )
        )

        val badges = BadgeEvaluator.evaluate(progress, reviews)
        assertTrue(badges.contains("badge_1"))
        assertTrue(badges.contains("badge_5"))
        assertTrue(badges.contains("badge_6")) // Primer reto
        assertTrue(badges.contains("badge_7")) // 3 estrellas
        assertTrue(badges.contains("badge_8")) // Constancia (3 aprobadas)
        assertTrue(badges.contains("badge_9")) // Gran Campeón (5 completas)
    }
}
