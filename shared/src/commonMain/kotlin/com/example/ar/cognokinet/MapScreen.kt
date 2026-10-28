package com.example.ar.cognokinet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun MapScreen(
    appState: CognokinetAppState,
    onStartChallenge: (Int) -> Unit,
    onOpenProfile: () -> Unit
) {
    var showModal by remember { mutableStateOf(false) }
    var selectedModalStation by remember { mutableStateOf(1) }
    val activeProgress = appState.activeProgress
    val activeProfile = appState.activeProfile

    var showWelcomeBubble by remember { mutableStateOf(activeProgress.completedStationsCount == 0) }

    LaunchedEffect(showWelcomeBubble) {
        if (showWelcomeBubble) {
            delay(6000)
            showWelcomeBubble = false
        }
    }

    val stations = ExerciseCatalog.list.map { ex ->
        StationInfo(
            id = ex.id,
            title = "Estación ${ex.id}: ${ex.name}",
            description = ex.description,
            goal = ex.targetGoal,
            isUnlocked = activeProgress.completedStationsCount >= (ex.id - 1)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE4EFE3))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp)
        ) {
            MapHeaderBar(appState = appState, onOpenProfile = onOpenProfile)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CoordinationProgressCard(progress = activeProgress)

                Spacer(modifier = Modifier.height(8.dp))

                StitchWindingTrailMap(
                    stations = stations,
                    completedStations = activeProgress.completedStationsCount,
                    currentStation = activeProgress.currentStation,
                    isPathCompleted = activeProgress.isPathCompleted,
                    showWelcomeBubble = showWelcomeBubble && activeProgress.completedStationsCount == 0,
                    onDismissBubble = { showWelcomeBubble = false },
                    onStationClick = { stationId ->
                        selectedModalStation = stationId
                        showModal = true
                    }
                )



                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (showModal) {
            val station = stations.firstOrNull { it.id == selectedModalStation } ?: stations[0]
            StationModalDialog(
                station = station,
                onClose = { showModal = false },
                onStart = {
                    if (station.isUnlocked) {
                        showModal = false
                        onStartChallenge(station.id)
                    }
                }
            )
        }
    }
}

@Composable
private fun MapHeaderBar(appState: CognokinetAppState, onOpenProfile: () -> Unit) {
    val progress = appState.activeProgress
    val profile = appState.activeProfile

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp)
            .background(Color(0xFFF8FAF6).copy(alpha = 0.9f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderPill(icon = "🍃", text = "${progress.leaves}", badge = null, color = Color(0xFF1D321F))
                HeaderPill(icon = "🌰", text = "${progress.acorns}", badge = null, color = Color(0xFF5D4037))
                HeaderPill(icon = "🧭", text = "Niv. ${progress.level} Explorador", badge = null, color = Color(0xFF4E342E))
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD7E8D3))
                    .border(2.dp, CognokinetColors.Forest, CircleShape)
                    .clickable { onOpenProfile() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (profile != null) StudentAvatars.getEmoji(profile.avatarId) else "👤",
                    fontSize = 22.sp
                )
            }
        }
    }
}

@Composable
private fun HeaderPill(icon: String, text: String, badge: String?, color: Color) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .background(Color.White, CircleShape)
            .border(1.dp, Color(0xFFC2E0B8), CircleShape)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            if (badge != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(0xFFE8F5E9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(badge, color = CognokinetColors.Forest, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun CoordinationProgressCard(progress: StudentProgress) {
    val totalStations = CognokinetConstants.TOTAL_STATIONS
    val percentInt = ((progress.completedStationsCount.toFloat() / totalStations) * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
                .background(Color(0xFFEADBC0).copy(alpha = 0.95f), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFD5C29E), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(CognokinetColors.Forest, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏋️", fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Progreso Coordinación",
                                color = Color(0xFF2A1D13),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (progress.isPathCompleted) "¡Sendero completado!" else "${progress.completedStationsCount} de $totalStations Estaciones",
                                color = Color(0xFF6D5A4E),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFF57606F), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("$percentInt%", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.8f))
                        .border(1.dp, Color(0xFFDAC49E), CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(percentInt / 100f)
                            .background(CognokinetColors.LeafGreen)
                    )
                }
            }
        }
    }
}

@Composable
private fun StitchWindingTrailMap(
    stations: List<StationInfo>,
    completedStations: Int,
    currentStation: Int,
    isPathCompleted: Boolean,
    showWelcomeBubble: Boolean,
    onDismissBubble: () -> Unit,
    onStationClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .height(710.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val path = Path().apply {
                moveTo(w * 0.5f, h * 0.91f)
                cubicTo(w * 0.5f, h * 0.80f, w * 0.67f, h * 0.73f, w * 0.53f, h * 0.61f)
                cubicTo(w * 0.39f, h * 0.49f, w * 0.35f, h * 0.41f, w * 0.51f, h * 0.28f)
                cubicTo(w * 0.64f, h * 0.18f, w * 0.44f, h * 0.11f, w * 0.5f, h * 0.03f)
            }

            drawPath(path = path, color = Color(0xFFC2D6BF), style = Stroke(width = 48f))
            drawPath(path = path, color = Color(0xFFD3BD98), style = Stroke(width = 36f))
            drawPath(
                path = path,
                color = Color(0xFFBCA075),
                style = Stroke(width = 8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 14f), 0f))
            )
        }

        Text("🌲", fontSize = 48.sp, modifier = Modifier.align(Alignment.TopStart).offset(x = 10.dp, y = 20.dp))
        Text("🌲", fontSize = 54.sp, modifier = Modifier.align(Alignment.TopEnd).offset(x = (-10).dp, y = 140.dp))
        Text("🌱", fontSize = 40.sp, modifier = Modifier.align(Alignment.BottomStart).offset(x = 10.dp, y = (-120).dp))
        Text("🌿", fontSize = 38.sp, modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-10).dp, y = (-180).dp))

        // Estación 5 (Arriba)
        Box(modifier = Modifier.align(Alignment.TopCenter).offset(y = 20.dp)) {
            StationNodeWithMarker(
                station = stations[4],
                isCurrent = currentStation == 5,
                isPathCompleted = isPathCompleted,
                onClick = { onStationClick(5) }
            )
        }

        // Estación 4 (Derecha)
        Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = (-40).dp, y = 160.dp)) {
            StationNodeWithMarker(
                station = stations[3],
                isCurrent = currentStation == 4,
                isPathCompleted = isPathCompleted,
                onClick = { onStationClick(4) }
            )
        }

        // Estación 3 (Izquierda)
        Box(modifier = Modifier.align(Alignment.CenterStart).offset(x = 30.dp, y = (-40).dp)) {
            StationNodeWithMarker(
                station = stations[2],
                isCurrent = currentStation == 3,
                isPathCompleted = isPathCompleted,
                onClick = { onStationClick(3) }
            )
        }

        // Estación 2 (Centro)
        Box(modifier = Modifier.align(Alignment.Center).offset(y = 80.dp)) {
            StationNodeWithMarker(
                station = stations[1],
                isCurrent = currentStation == 2,
                isPathCompleted = isPathCompleted,
                onClick = { onStationClick(2) }
            )
        }

        // Estación 1 (Abajo)
        Box(modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-30).dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (showWelcomeBubble) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.clickable { onDismissBubble() }
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFDECFB5), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("¡Tu aventura empieza aquí! 🐾", color = Color(0xFF4E342E), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFFFFE0B2), CircleShape)
                                .border(2.dp, Color(0xFFE65100), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🦊", fontSize = 24.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }

                StationNodeWithMarker(
                    station = stations[0],
                    isCurrent = currentStation == 1,
                    isPathCompleted = isPathCompleted,
                    onClick = { onStationClick(1) }
                )
            }
        }
    }
}

@Composable
private fun StationNodeWithMarker(
    station: StationInfo,
    isCurrent: Boolean,
    isPathCompleted: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        if (isCurrent && !isPathCompleted) {
            Box(
                modifier = Modifier
                    .background(CognokinetColors.Forest, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("📍 ¡Estás aquí!", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
        } else if (isPathCompleted && station.id == 5) {
            Box(
                modifier = Modifier
                    .background(CognokinetColors.SunAmber, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("🏆 ¡Completado!", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        Box(
            modifier = Modifier
                .size(if (isCurrent) 76.dp else 56.dp)
                .shadow(6.dp, RoundedCornerShape(20.dp))
                .background(
                    if (station.isUnlocked) CognokinetColors.LeafGreen else Color(0xFF57606F),
                    RoundedCornerShape(20.dp)
                )
                .border(3.dp, if (station.isUnlocked) Color(0xFF5D4037) else Color(0xFF474F5C), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (station.isUnlocked) {
                Text("${station.id}", color = Color.White, fontSize = if (isCurrent) 32.sp else 24.sp, fontWeight = FontWeight.Bold)
            } else {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFF6D4C41), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF533931), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔒", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.9f), CircleShape)
                .border(1.dp, Color(0xFFD5DDD3), CircleShape)
                .padding(horizontal = 10.dp, vertical = 2.dp)
        ) {
            Text(
                text = station.title,
                color = if (station.isUnlocked) CognokinetColors.ForestDark else Color(0xFF556754),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}



@Composable
private fun StationModalDialog(
    station: StationInfo,
    onClose: () -> Unit,
    onStart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x88132815)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .shadow(16.dp, RoundedCornerShape(28.dp))
                .background(Color(0xFFFCFBF7), RoundedCornerShape(28.dp))
                .border(2.dp, Color(0xFFD7C4A5), RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .size(32.dp)
                        .background(Color(0xFF8D6E63), CircleShape)
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(if (station.isUnlocked) Color(0xFFE8F5E9) else Color(0xFFECEFF1), RoundedCornerShape(20.dp))
                        .border(2.dp, if (station.isUnlocked) CognokinetColors.LeafGreen else Color(0xFF90A4AE), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (station.isUnlocked) "🥾" else "🔒", fontSize = 30.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (station.isUnlocked) "¡DISPONIBLE!" else "ESTACIÓN BLOQUEADA",
                    color = if (station.isUnlocked) CognokinetColors.Forest else Color(0xFF78909C),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = station.title,
                    color = Color(0xFF1D321F),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (station.isUnlocked) station.description else "Debes completar las estaciones anteriores para desbloquear este reto.",
                    color = Color(0xFF4A5D4E),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (station.isUnlocked) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEFE8DA), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFDECFB5), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("⭐ Meta: ${station.goal}", color = Color(0xFF3A2B1E), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onStart,
                    enabled = station.isUnlocked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (station.isUnlocked) CognokinetColors.Forest else Color(0xFFB0BEC5)
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (station.isUnlocked) "🏃 Ver Demostración y Ejercicio 3D" else "🔒 Estación Bloqueada",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

data class StationInfo(
    val id: Int,
    val title: String,
    val description: String,
    val goal: String,
    val isUnlocked: Boolean
)
