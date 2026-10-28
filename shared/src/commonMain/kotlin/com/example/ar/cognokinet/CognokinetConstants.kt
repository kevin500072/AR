package com.example.ar.cognokinet

object CognokinetConstants {
    const val PROTOCOL_VERSION = "1"
    const val INITIAL_LIVES = 5
    const val INITIAL_ACORNS = 0
    const val INITIAL_LEVEL = 1
    const val INITIAL_XP = 0
    const val TOTAL_STATIONS = 5
    const val XP_PER_LEVEL = 200

    val TOTAL_BADGES: Int
        get() = BadgeCatalog.ALL_BADGES.size

    fun levelFor(xp: Int): Int = xp / XP_PER_LEVEL + 1
}
