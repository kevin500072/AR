package com.example.ar.cognokinet

import com.example.ar.cognokinet.domain.StudentProfile
import com.example.ar.cognokinet.domain.StudentProgress

typealias StudentProfile = StudentProfile
typealias StudentProgress = StudentProgress

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
