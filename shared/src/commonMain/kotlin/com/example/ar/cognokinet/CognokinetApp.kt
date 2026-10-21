package com.example.ar.cognokinet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CognokinetApp(
    appState: CognokinetAppState = remember { CognokinetAppState() }
) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                val hasProfile = appState.activeProfile != null
                val isFullScreen = !hasProfile ||
                        appState.currentScreen == CognokinetScreen.HOME ||
                        appState.currentScreen == CognokinetScreen.STUDENT_PROFILE ||
                        appState.currentScreen == CognokinetScreen.TEACHER_DASHBOARD

                // SCREEN SWITCHING
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (!isFullScreen) 60.dp else 0.dp)
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
