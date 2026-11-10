package com.example.ar.cognokinet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ar.cognokinet.domain.DecisionStatus
import com.example.ar.cognokinet.domain.EvaluationRequest
import com.example.ar.cognokinet.domain.EvaluationReview
import com.example.ar.cognokinet.domain.EvaluationStatus
import com.example.ar.cognokinet.domain.IdGenerator
import com.example.ar.cognokinet.domain.SessionResult
import com.example.ar.cognokinet.domain.StudentProfile
import com.example.ar.cognokinet.domain.StudentProgress

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

class CognokinetAppState {
    var currentScreen by mutableStateOf(CognokinetScreen.HOME)
    var selectedTab by mutableStateOf(NavigationTab.MAP)

    // Control de audio y música del bosque
    var isMusicEnabled by mutableStateOf(true)
    var isMusicPlaying by mutableStateOf(false)

    // Sesión de cuenta remota autenticada con Google.
    var onlineAuthToken by mutableStateOf<String?>(null)
    var onlineRefreshToken by mutableStateOf<String?>(null)
    var onlineUserId by mutableStateOf<String?>(null)
    var onlineRole by mutableStateOf<String?>(null)
    var onlineClassId by mutableStateOf<String?>(null)
    var onlineProgressReady by mutableStateOf(false)

    // Rol activo ("STUDENT" o "TEACHER")
    var activeRole by mutableStateOf("STUDENT")
    var teacherPinHash by mutableStateOf("")
    var isTeacherPinSet by mutableStateOf(false)
    var teacherName by mutableStateOf("Prof. Guía")
    var teacherGrade by mutableStateOf("3º A de Primaria")
    var teacherSubject by mutableStateOf("Educación Física")

    fun setTeacherProfile(name: String, grade: String, subject: String, pin: String): Boolean {
        if (pin.length < 4) return false
        if (name.isBlank()) return false
        teacherName = name
        teacherGrade = grade.ifBlank { "3º de Primaria" }
        teacherSubject = subject.ifBlank { "Educación Física" }
        teacherPinHash = pin.hashCode().toString()
        isTeacherPinSet = true
        return true
    }

    fun verifyTeacherPin(pin: String): Boolean {
        if (!isTeacherPinSet) return pin == "1234"
        return teacherPinHash == pin.hashCode().toString()
    }

    // Snackbar para avisos
    var snackbarMessage by mutableStateOf<String?>(null)

    // Lista de perfiles de alumnos almacenados
    val studentProfiles = mutableStateListOf<StudentProfile>()
    var activeProfileId by mutableStateOf<String?>(null)

    // Mapa de progresos por alumno (Observable)
    val progressMap = mutableStateMapOf<String, StudentProgress>()

    fun signOutOnlineAccount() {
        val previousUserId = onlineUserId
        val previousRole = onlineRole
        onlineAuthToken = null
        onlineRefreshToken = null
        onlineUserId = null
        onlineRole = null
        onlineClassId = null
        onlineProgressReady = false
        activeRole = "STUDENT"
        if (previousRole == "STUDENT" && previousUserId != null) {
            studentProfiles.removeAll { it.id == previousUserId }
            progressMap.remove(previousUserId)
            if (activeProfileId == previousUserId) {
                activeProfileId = studentProfiles.firstOrNull()?.id
            }
        }
        currentScreen = CognokinetScreen.HOME
    }

    var selectedStation by mutableStateOf(1)

    // Cola de solicitudes de evaluación y revisiones para el Profesor
    val evaluationRequests = mutableStateListOf<EvaluationRequest>()
    val evaluationReviews = mutableStateListOf<EvaluationReview>()

    // Ajustes 3JS Motrices
    var stimulusSpeed by mutableStateOf(1.0f)
    var jumpPrecisionCm by mutableStateOf(15)
    var seriesTimeSec by mutableStateOf(90)
    var simultaneousTargets by mutableStateOf(2)
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

    fun updateActiveProgress(update: (StudentProgress) -> StudentProgress) {
        val id = activeProfileId ?: return
        val current = progressMap.getOrPut(id) { StudentProgress.inicial(id) }
        progressMap[id] = update(current)
    }

    val unlockedInventoryItems: List<String>
        get() = activeProgress.unlockedInventoryItems

    val currentAvatarConfig: AvatarConfig
        get() = activeProgress.avatarConfig

    fun updateAvatarConfig(newConfig: AvatarConfig) {
        updateActiveProgress { it.copy(avatarConfig = newConfig) }
    }

    fun navigateTo(screen: CognokinetScreen) {
        if (activeProfile == null && screen != CognokinetScreen.HOME && screen != CognokinetScreen.STUDENT_PROFILE && screen != CognokinetScreen.TEACHER_DASHBOARD) {
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
        val newId = IdGenerator.newId()
        val profile = StudentProfile(
            id = newId,
            displayName = name,
            age = age,
            grade = grade,
            avatarId = avatarId,
            consentGiven = consentGiven,
            createdAt = 0L
        )
        studentProfiles.add(profile)
        progressMap[newId] = StudentProgress.inicial(newId)
        activeProfileId = newId

        // La música del bosque inicia inmediatamente DESPUÉS de que el alumno registra su perfil
        isMusicPlaying = true

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
        evaluationRequests.removeAll { it.studentId == profileId }
        if (activeProfileId == profileId) {
            activeProfileId = studentProfiles.firstOrNull()?.id
            if (activeProfileId == null) {
                isMusicPlaying = false
            }
        }
    }

    fun purchaseShopItem(item: ShopItem): Boolean {
        val p = activeProgress
        if (p.acorns >= item.priceAcorns && p.leaves >= item.priceLeaves && !p.unlockedInventoryItems.contains(item.id)) {
            val updatedInventory = p.unlockedInventoryItems + item.id
            val newAvatarConfig = when (item.category) {
                "gorra" -> p.avatarConfig.copy(hatId = item.itemId)
                "camisa" -> p.avatarConfig.copy(shirtColorHex = item.colorHex ?: "#2E7D32")
                "pantalon" -> p.avatarConfig.copy(pantsColorHex = item.colorHex ?: "#1565C0")
                "calzado" -> p.avatarConfig.copy(shoesColorHex = item.colorHex ?: "#3E2723")
                "accesorio" -> p.avatarConfig.copy(accessoryId = item.itemId)
                else -> p.avatarConfig
            }

            updateActiveProgress {
                it.copy(
                    acorns = it.acorns - item.priceAcorns,
                    leaves = it.leaves - item.priceLeaves,
                    unlockedInventoryItems = updatedInventory,
                    avatarConfig = newAvatarConfig
                )
            }
            return true
        }
        return false
    }

    fun submitEvaluationRequest(stationId: Int) {
        val student = activeProfile ?: return
        val currentP = progressMap.getOrPut(student.id) { StudentProgress.inicial(student.id) }

        if (!StationUnlockRules.isUnlocked(stationId, currentP.completedStationIds)) return

        val existingPending = evaluationRequests.firstOrNull { it.studentId == student.id && it.stationId == stationId && it.status == EvaluationStatus.PENDING }
        if (existingPending == null) {
            evaluationRequests.add(
                EvaluationRequest(
                    id = IdGenerator.newId(),
                    studentId = student.id,
                    studentName = student.name,
                    stationId = stationId,
                    status = EvaluationStatus.PENDING,
                    requestedAt = 0L
                )
            )
        }
    }

    fun requestRepeatEvaluation(stationId: Int, comment: String) {
        submitEvaluationRequest(stationId)
    }

    fun teacherApproveEvaluation(
        requestId: String,
        stars: Int,
        bonusAcorns: Int,
        bonusLeaves: Int,
        comment: String,
        decision: DecisionStatus = DecisionStatus.PASS
    ) {
        val index = evaluationRequests.indexOfFirst { it.id == requestId }
        if (index < 0) return
        val req = evaluationRequests[index]

        val status = if (decision == DecisionStatus.PASS) EvaluationStatus.PASSED else EvaluationStatus.REPEAT
        val updatedReq = req.copy(status = status, stars = stars, comment = comment)
        evaluationRequests[index] = updatedReq

        val studentId = req.studentId
        val currentP = progressMap.getOrPut(studentId) { StudentProgress.inicial(studentId) }
        val exercise = ExerciseCatalog.getById(req.stationId)

        val previousBest = currentP.bestStarsByStation[req.stationId] ?: 0
        val reward = RewardCalculator.calculate(exercise, stars, bonusAcorns, bonusLeaves, previousBest, decision)

        val updatedCompleted = if (decision == DecisionStatus.PASS) currentP.completedStationIds + req.stationId else currentP.completedStationIds
        val updatedBestStars = if (decision == DecisionStatus.PASS) {
            currentP.bestStarsByStation + (req.stationId to maxOf(previousBest, stars))
        } else {
            currentP.bestStarsByStation
        }

        val updatedXp = currentP.xp + reward.xpGained
        val updatedLevel = StudentProgress.levelForXp(updatedXp)

        val review = EvaluationReview(
            requestId = requestId,
            classId = req.classId,
            stationId = req.stationId,
            scores = emptyList(),
            totalPercent = if (decision == DecisionStatus.PASS) 85.0 else 30.0,
            suggestedStars = stars,
            finalStars = stars,
            decision = decision,
            bonusAcorns = bonusAcorns,
            bonusLeaves = bonusLeaves,
            comment = comment,
            reviewedAt = 0L
        )
        evaluationReviews.add(review)

        val tempProgress = currentP.copy(
            level = updatedLevel,
            xp = updatedXp,
            acorns = currentP.acorns + reward.acornsGained,
            leaves = currentP.leaves + reward.leavesGained,
            completedStationIds = updatedCompleted,
            bestStarsByStation = updatedBestStars
        )

        val newBadges = BadgeEvaluator.evaluate(tempProgress, evaluationReviews.toList())

        val sessionResult = SessionResult(
            stationId = req.stationId,
            stars = stars,
            xpGained = reward.xpGained,
            acornsGained = reward.acornsGained,
            leavesGained = reward.leavesGained,
            levelBefore = currentP.level,
            levelAfter = updatedLevel,
            teacherComment = comment,
            newBadges = newBadges.filter { !currentP.unlockedBadges.contains(it) }
        )

        progressMap[studentId] = tempProgress.copy(
            unlockedBadges = newBadges,
            lastSessionResult = sessionResult
        )
    }
}
