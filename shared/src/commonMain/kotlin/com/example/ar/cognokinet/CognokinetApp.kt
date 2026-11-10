package com.example.ar.cognokinet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun CognokinetApp(
    appState: CognokinetAppState = remember { CognokinetAppState() }
) {
    val sessionStore = rememberOnlineSessionStore()
    val currentProgress = appState.activeProgress

    LaunchedEffect(sessionStore) {
        val cached = sessionStore.load()?.let(OnlineClassApi::decodeSession) ?: return@LaunchedEffect
        val refreshToken = cached.refreshToken ?: run {
            sessionStore.clear()
            return@LaunchedEffect
        }
        val refreshResult = runCatching { OnlineClassApi.refreshSession(refreshToken) }
        val restored = refreshResult.getOrElse { cached }

        if (restored.role == "STUDENT") {
            val latestProgress = if (refreshResult.isSuccess) {
                runCatching { OnlineClassApi.getProgress(restored.token) }
            } else {
                Result.failure(Exception("Sin conexión"))
            }
            val completeSession = latestProgress.getOrNull()?.let { restored.copy(progress = it) } ?: restored
            val profile = com.example.ar.cognokinet.domain.StudentProfile(
                id = completeSession.userId,
                displayName = completeSession.displayName,
                age = completeSession.age,
                grade = completeSession.grade,
                avatarId = completeSession.avatarId,
                consentGiven = completeSession.consentGiven
            )
            val index = appState.studentProfiles.indexOfFirst { it.id == profile.id }
            if (index >= 0) appState.studentProfiles[index] = profile else appState.studentProfiles.add(profile)
            appState.progressMap[profile.id] = completeSession.progress
                ?: com.example.ar.cognokinet.domain.StudentProgress.inicial(profile.id)
            appState.activeProfileId = profile.id
            appState.onlineProgressReady = latestProgress.isSuccess
            appState.currentScreen = CognokinetScreen.MAP
            if (refreshResult.isSuccess) sessionStore.save(OnlineClassApi.encodeSession(completeSession))
            if (refreshResult.isFailure) {
                appState.snackbarMessage = "Sin conexión: se usará el progreso guardado en este dispositivo."
            }
        } else {
            appState.teacherName = restored.displayName.ifBlank { appState.teacherName }
            appState.onlineProgressReady = false
            appState.currentScreen = CognokinetScreen.TEACHER_DASHBOARD
            if (refreshResult.isSuccess) sessionStore.save(OnlineClassApi.encodeSession(restored))
            if (refreshResult.isFailure) {
                appState.snackbarMessage = "Sin conexión: se restauró la cuenta guardada en este dispositivo."
            }
        }
        appState.onlineAuthToken = restored.token
        appState.onlineRefreshToken = restored.refreshToken
        appState.onlineUserId = restored.userId
        appState.onlineRole = restored.role
        appState.onlineClassId = restored.classId
        appState.activeRole = restored.role
    }

    LaunchedEffect(appState.onlineRefreshToken, appState.onlineRole) {
        var refreshToken = appState.onlineRefreshToken ?: return@LaunchedEffect
        while (true) {
            delay(10 * 60 * 1000L)
            try {
                val renewed = OnlineClassApi.refreshSession(refreshToken)
                refreshToken = renewed.refreshToken ?: break
                appState.onlineAuthToken = renewed.token
                appState.onlineRefreshToken = refreshToken
                appState.onlineUserId = renewed.userId
                appState.onlineRole = renewed.role
                appState.onlineClassId = renewed.classId
                val localProgress = appState.activeProfile?.let { appState.progressMap[it.id] }
                sessionStore.save(OnlineClassApi.encodeSession(
                    if (localProgress != null && renewed.role == "STUDENT") renewed.copy(progress = localProgress) else renewed
                ))
            } catch (_: Exception) {
                appState.snackbarMessage = "No se pudo renovar la sesión. Vuelve a iniciar sesión con Google."
                break
            }
        }
    }

    LaunchedEffect(appState.onlineAuthToken, currentProgress, appState.onlineProgressReady) {
        val token = appState.onlineAuthToken
        if (token != null && appState.onlineRole == "STUDENT" && appState.activeProfile != null) {
            delay(500)
            sessionStore.load()?.let(OnlineClassApi::decodeSession)?.let { cached ->
                sessionStore.save(OnlineClassApi.encodeSession(cached.copy(progress = currentProgress)))
            }
            if (appState.onlineProgressReady) {
                runCatching { OnlineClassApi.saveProgress(token, currentProgress) }
                    .onFailure { appState.snackbarMessage = "No se pudo sincronizar el progreso todavía." }
            }
        }
    }

    // Control de música: sólo se reproduce cuando el alumno ya registró su perfil
    ForestAudioPlayerController(
        isMusicEnabled = appState.isMusicEnabled,
        isMusicPlaying = appState.isMusicPlaying && appState.activeProfile != null
    )

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                val hasProfile = appState.activeProfile != null
                val isFullScreen = !hasProfile ||
                        appState.currentScreen == CognokinetScreen.HOME ||
                        appState.currentScreen == CognokinetScreen.STUDENT_PROFILE ||
                        appState.currentScreen == CognokinetScreen.TEACHER_DASHBOARD

                // SCREEN SWITCHING con Edge-to-Edge Navigation Bars padding dinámico
                val bottomBarPadding = if (!isFullScreen) WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 56.dp else 0.dp

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = bottomBarPadding)
                ) {
                    when (appState.currentScreen) {
                        CognokinetScreen.HOME -> {
                            InicioScreen(
                                appState = appState,
                                onNavigateToStudentSetup = { appState.navigateTo(CognokinetScreen.STUDENT_PROFILE) },
                                onNavigateToTeacherSetup = { appState.navigateTo(CognokinetScreen.TEACHER_DASHBOARD) },
                                onNavigateToMap = { appState.navigateTo(CognokinetScreen.MAP) },
                                onNavigateToSettings = { appState.navigateTo(CognokinetScreen.SETTINGS) }
                            )
                        }
                        CognokinetScreen.STUDENT_PROFILE -> {
                            StudentProfileScreen(
                                appState = appState,
                                sessionStore = sessionStore,
                                onBackToHome = { appState.navigateTo(CognokinetScreen.HOME) },
                                onGoToMap = { appState.navigateTo(CognokinetScreen.MAP) }
                            )
                        }
                        CognokinetScreen.MAP -> {
                            MapScreen(
                                appState = appState,
                                onStartChallenge = { stationId ->
                                    appState.selectedStation = stationId
                                    appState.navigateTo(CognokinetScreen.RETO_DEMO)
                                },
                                onOpenProfile = { appState.navigateTo(CognokinetScreen.STUDENT_PROFILE) }
                            )
                        }
                        CognokinetScreen.RETO_DEMO -> {
                            RetoDemoScreen(
                                appState = appState,
                                initialStationId = appState.selectedStation,
                                onBack = { appState.navigateTo(CognokinetScreen.MAP) },
                                onCompleteSimulatedSession = { appState.navigateTo(CognokinetScreen.RESULTS) }
                            )
                        }
                        CognokinetScreen.RESULTS -> {
                            ResultsScreen(
                                appState = appState,
                                onContinueToMap = { appState.navigateTo(CognokinetScreen.MAP) },
                                onBack = { appState.navigateTo(CognokinetScreen.MAP) }
                            )
                        }
                        CognokinetScreen.PROFILE -> {
                            ProfileScreen(
                                appState = appState,
                                onOpenProfileSetup = { appState.navigateTo(CognokinetScreen.STUDENT_PROFILE) },
                                onBack = { appState.navigateTo(CognokinetScreen.MAP) }
                            )
                        }
                        CognokinetScreen.SHOP -> {
                            ShopScreen(
                                appState = appState,
                                onGoToMap = { appState.navigateTo(CognokinetScreen.MAP) },
                                onBack = { appState.navigateTo(CognokinetScreen.MAP) }
                            )
                        }
                        CognokinetScreen.TEACHER_DASHBOARD -> {
                            TeacherScreen(
                                appState = appState,
                                sessionStore = sessionStore,
                                onBack = { appState.navigateTo(CognokinetScreen.HOME) }
                            )
                        }
                        CognokinetScreen.SETTINGS -> {
                            SettingsScreen(
                                appState = appState,
                                onBack = { appState.navigateTo(CognokinetScreen.MAP) }
                            )
                        }
                    }
                }

                // BOTTOM NAVIGATION BAR (Solo si hay perfil activo)
                if (!isFullScreen && hasProfile) {
                    Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                        CognokinetBottomBar(
                            currentScreen = appState.currentScreen,
                            onTabSelected = { tab -> appState.selectNavigationTab(tab) }
                        )
                    }
                }
            }
        }
    }
}
