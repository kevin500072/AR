package com.example.ar.cognokinet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class CognokinetScreen {
    HOME,
    MAP,
    RETO_DEMO,
    RESULTS,
    PROFILE,
    STUDENT_PROFILE,
    SHOP,
    TEACHER_DASHBOARD,
    SETTINGS
}

enum class NavigationTab {
    MAP,
    CHALLENGES,
    ACHIEVEMENTS,
    SHOP,
    SETTINGS
}

data class EvaluationRequest(
    val id: String,
    val studentId: String,
    val studentName: String,
    val exerciseId: Int,
    val requestedAt: Long = 0L,
    var status: String = "PENDIENTE", // "PENDIENTE", "APROBADO", "REPETIR"
    var stars: Int = 0,
    var comment: String = ""
)

class CognokinetAppState {
    var currentScreen by mutableStateOf(CognokinetScreen.HOME)
    var selectedTab by mutableStateOf(NavigationTab.MAP)

    // Rol activo ("STUDENT" o "TEACHER")
    var activeRole by mutableStateOf("STUDENT")
    var teacherPin by mutableStateOf("1234")

    // Snackbar para avisos (ej. "Primero crea tu perfil")
    var snackbarMessage by mutableStateOf<String?>(null)

    // Lista de perfiles de alumnos almacenados
    val studentProfiles = mutableStateListOf<StudentProfile>()
    var activeProfileId by mutableStateOf<String?>(null)

    // Mapa de progresos por alumno
    private val progressMap = mutableMapOf<String, StudentProgress>()

    // Inventario de artículos comprados
    val unlockedInventoryItems = mutableStateListOf<String>()

    // Configuración actual del Avatar
    var currentAvatarConfig by mutableStateOf(AvatarConfig())

    var selectedStation by mutableStateOf(1)

    // Cola de solicitudes de evaluación para el Profesor
    val evaluationRequests = mutableStateListOf<EvaluationRequest>()

    // Ajustes 3JS Motrices
    var stimulusSpeed by mutableStateOf(1.0f) // 0.5x to 2.0x
    var jumpPrecisionCm by mutableStateOf(15) // ±15cm
    var seriesTimeSec by mutableStateOf(90) // 1:30 min
    var simultaneousTargets by mutableStateOf(2) // 1, 2, 4
    var calmModeActive by mutableStateOf(true)
    var classroomModeActive by mutableStateOf(true)
    var lateralityEvaluationActive by mutableStateOf(true)
    var aiSkeletonTrackingActive by mutableStateOf(true)

    val activeProfile: StudentProfile?
        get() = studentProfiles.firstOrNull { it.id == activeProfileId }

    val activeProgress: StudentProgress
        get() {
            val id = activeProfileId ?: return StudentProgress.inicial("guest")
            return progressMap.getOrPut(id) { StudentProgress.inicial(id) }
        }

    fun navigateTo(screen: CognokinetScreen) {
        // NavGuard: Si no hay perfil activo y se intenta acceder a pantallas protegidas, exige crear perfil
        if (activeProfile == null && screen != CognokinetScreen.HOME && screen != CognokinetScreen.STUDENT_PROFILE) {
            snackbarMessage = "Primero crea tu perfil"
            currentScreen = CognokinetScreen.HOME
            return
        }
        currentScreen = screen
    }

    fun selectNavigationTab(tab: NavigationTab) {
        selectedTab = tab
        when (tab) {
            NavigationTab.MAP -> navigateTo(CognokinetScreen.MAP)
            NavigationTab.CHALLENGES -> navigateTo(CognokinetScreen.RETO_DEMO)
            NavigationTab.ACHIEVEMENTS -> navigateTo(CognokinetScreen.PROFILE)
            NavigationTab.SHOP -> navigateTo(CognokinetScreen.SHOP)
            NavigationTab.SETTINGS -> navigateTo(CognokinetScreen.SETTINGS)
        }
    }

    fun createStudentProfile(
        name: String,
        age: Int,
        grade: String,
        avatarId: String,
        consentGiven: Boolean
    ): StudentProfile {
        val newId = "student_${studentProfiles.size + 1}"
        val profile = StudentProfile(
            id = newId,
            name = name,
            age = age,
            grade = grade,
            avatarId = avatarId,
            consentGiven = consentGiven
        )
        studentProfiles.add(profile)
        progressMap[newId] = StudentProgress.inicial(newId)
        activeProfileId = newId
        return profile
    }

    fun updateStudentProfile(profile: StudentProfile) {
        val index = studentProfiles.indexOfFirst { it.id == profile.id }
        if (index >= 0) {
            studentProfiles[index] = profile
        }
    }

    fun deleteStudentProfile(profileId: String) {
        studentProfiles.removeAll { it.id == profileId }
        progressMap.remove(profileId)
        if (activeProfileId == profileId) {
            activeProfileId = studentProfiles.firstOrNull()?.id
        }
    }

    fun purchaseShopItem(item: ShopItem): Boolean {
        val p = activeProgress
        if (p.acorns >= item.priceAcorns && p.leaves >= item.priceLeaves && !unlockedInventoryItems.contains(item.id)) {
            val updatedP = p.copy(
                acorns = p.acorns - item.priceAcorns,
                leaves = p.leaves - item.priceLeaves
            )
            val id = activeProfileId ?: "guest"
            progressMap[id] = updatedP
            unlockedInventoryItems.add(item.id)

            // Equipar automáticamente
            currentAvatarConfig = when (item.category) {
                "gorra" -> currentAvatarConfig.copy(hatId = item.itemId)
                "camisa" -> currentAvatarConfig.copy(shirtColorHex = item.colorHex ?: "#2E7D32")
                "pantalon" -> currentAvatarConfig.copy(pantsColorHex = item.colorHex ?: "#1565C0")
                "calzado" -> currentAvatarConfig.copy(shoesColorHex = item.colorHex ?: "#3E2723")
                "accesorio" -> currentAvatarConfig.copy(accessoryId = item.itemId)
                else -> currentAvatarConfig
            }
            return true
        }
        return false
    }

    fun submitEvaluationRequest(exerciseId: Int) {
        val student = activeProfile ?: return
        val existingPending = evaluationRequests.firstOrNull { it.studentId == student.id && it.exerciseId == exerciseId && it.status == "PENDIENTE" }
        if (existingPending == null) {
            evaluationRequests.add(
                EvaluationRequest(
                    id = "req_${evaluationRequests.size + 1}",
                    studentId = student.id,
                    studentName = student.name,
                    exerciseId = exerciseId
                )
            )
        }
    }

    fun teacherApproveEvaluation(requestId: String, stars: Int, bonusAcorns: Int, bonusLeaves: Int, comment: String) {
        val req = evaluationRequests.firstOrNull { it.id == requestId } ?: return
        req.status = "APROBADO"
        req.stars = stars
        req.comment = comment

        val studentId = req.studentId
        val currentP = progressMap.getOrPut(studentId) { StudentProgress.inicial(studentId) }

        val awardedAcorns = when (stars) {
            1 -> 20
            2 -> 30
            else -> 40
        } + bonusAcorns

        val awardedLeaves = when (stars) {
            1 -> 3
            2 -> 4
            else -> 5
        } + bonusLeaves

        val updatedCompleted = (req.exerciseId).coerceAtLeast(currentP.completedStations)
        val updatedXp = currentP.xp + 100
        val updatedLevel = (updatedXp / 200) + 1

        val newBadges = currentP.unlockedBadges.toMutableList()
        val badgeKey = "badge_${req.exerciseId}"
        if (!newBadges.contains(badgeKey)) {
            newBadges.add(badgeKey)
        }

        progressMap[studentId] = currentP.copy(
            level = updatedLevel,
            xp = updatedXp,
            acorns = currentP.acorns + awardedAcorns,
            leaves = currentP.leaves + awardedLeaves,
            completedStations = updatedCompleted,
            unlockedBadges = newBadges
        )
    }

    fun simulateSessionCompletion(stationId: Int) {
        val currentP = activeProgress
        val updatedCompleted = (stationId).coerceAtLeast(currentP.completedStations)
        val updatedXp = currentP.xp + 100
        val updatedAcorns = currentP.acorns + 30
        val updatedLeaves = currentP.leaves + 4
        val updatedLevel = (updatedXp / 200) + 1

        val newBadges = currentP.unlockedBadges.toMutableList()
        val badgeKey = "badge_${stationId}"
        if (!newBadges.contains(badgeKey)) {
            newBadges.add(badgeKey)
        }

        val id = activeProfileId ?: "guest"
        progressMap[id] = currentP.copy(
            level = updatedLevel,
            xp = updatedXp,
            acorns = updatedAcorns,
            leaves = updatedLeaves,
            completedStations = updatedCompleted,
            unlockedBadges = newBadges
        )
    }
}
