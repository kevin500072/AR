package com.example.ar.cognokinet.domain

import com.example.ar.cognokinet.AvatarConfig
import com.example.ar.cognokinet.CognokinetConstants
import kotlinx.serialization.Serializable

@Serializable
data class SessionResult(
    val stationId: Int,
    val stars: Int,
    val xpGained: Int,
    val acornsGained: Int,
    val leavesGained: Int,
    val levelBefore: Int,
    val levelAfter: Int,
    val teacherComment: String,
    val newBadges: List<String> = emptyList(),
    val rubricSummary: String = ""
)

@Serializable
data class StudentProgress(
    val profileId: String,
    val level: Int = CognokinetConstants.INITIAL_LEVEL,
    val xp: Int = CognokinetConstants.INITIAL_XP,
    val acorns: Int = CognokinetConstants.INITIAL_ACORNS,
    val leaves: Int = 0,
    val completedStationIds: Set<Int> = emptySet(),
    val bestStarsByStation: Map<Int, Int> = emptyMap(),
    val unlockedBadges: List<String> = emptyList(),
    val avatarConfig: AvatarConfig = AvatarConfig(),
    val unlockedInventoryItems: List<String> = emptyList(),
    val lastSessionResult: SessionResult? = null,
    val hasTournamentData: Boolean = false,
    val tournamentName: String? = null
) {
    val completedStationsCount: Int
        get() = completedStationIds.size

    val currentStation: Int
        get() = (completedStationIds.size + 1).coerceAtMost(CognokinetConstants.TOTAL_STATIONS)

    val isPathCompleted: Boolean
        get() = completedStationIds.size >= CognokinetConstants.TOTAL_STATIONS

    companion object {
        fun levelForXp(xp: Int): Int {
            return CognokinetConstants.levelFor(xp)
        }

        fun inicial(profileId: String): StudentProgress {
            return StudentProgress(
                profileId = profileId,
                level = CognokinetConstants.INITIAL_LEVEL,
                xp = CognokinetConstants.INITIAL_XP,
                acorns = CognokinetConstants.INITIAL_ACORNS,
                leaves = 0,
                completedStationIds = emptySet(),
                bestStarsByStation = emptyMap(),
                unlockedBadges = emptyList(),
                avatarConfig = AvatarConfig(),
                unlockedInventoryItems = emptyList(),
                lastSessionResult = null,
                hasTournamentData = false,
                tournamentName = null
            )
        }
    }
}
