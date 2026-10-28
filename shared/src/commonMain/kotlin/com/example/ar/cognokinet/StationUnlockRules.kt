package com.example.ar.cognokinet

object StationUnlockRules {
    fun isUnlocked(stationId: Int, passedStationIds: Set<Int>): Boolean {
        if (stationId == 1) return true
        if (stationId < 1 || stationId > CognokinetConstants.TOTAL_STATIONS) return false
        return passedStationIds.contains(stationId - 1)
    }

    fun canRequestEvaluation(stationId: Int, passedStationIds: Set<Int>): Boolean {
        return isUnlocked(stationId, passedStationIds)
    }

    fun canReview(stationId: Int, passedStationIds: Set<Int>): Boolean {
        return isUnlocked(stationId, passedStationIds)
    }
}
