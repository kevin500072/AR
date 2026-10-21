package com.example.ar.cognokinet

data class StudentProfile(
    val id: String,
    val name: String,
    val age: Int,
    val grade: String,
    val avatarId: String,
    val consentGiven: Boolean = true,
    val createdAt: Long = 0L
)

data class StudentProgress(
    val profileId: String,
    val level: Int = CognokinetConstants.INITIAL_LEVEL,
    val xp: Int = CognokinetConstants.INITIAL_XP,
    val acorns: Int = CognokinetConstants.INITIAL_ACORNS,
    val leaves: Int = 0,
    val completedStations: Int = 0,
    val unlockedBadges: List<String> = emptyList(),
    val hasTournamentData: Boolean = false,
    val tournamentName: String? = null
) {
    companion object {
        fun inicial(profileId: String): StudentProgress {
            return StudentProgress(
                profileId = profileId,
                level = CognokinetConstants.INITIAL_LEVEL,
                xp = CognokinetConstants.INITIAL_XP,
                acorns = CognokinetConstants.INITIAL_ACORNS,
                leaves = 0,
                completedStations = 0,
                unlockedBadges = emptyList(),
                hasTournamentData = false,
                tournamentName = null
            )
        }
    }
}

data class AvatarOption(
    val id: String,
    val name: String,
    val emoji: String
)

object StudentAvatars {
    val list = listOf(
        AvatarOption("fox", "Zorro", "🦊"),
        AvatarOption("bear", "Oso", "🐻"),
        AvatarOption("owl", "Búho", "🦉"),
        AvatarOption("raccoon", "Mapache", "🦝"),
        AvatarOption("squirrel", "Ardilla", "🐿️"),
        AvatarOption("rabbit", "Conejo", "🐰")
    )

    fun getEmoji(avatarId: String): String {
        return list.firstOrNull { it.id == avatarId }?.emoji ?: "👤"
    }
}
