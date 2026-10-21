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
    var showWelcomeBubble by remember { mutableStateOf(appState.activeProgress.completedStations == 0) }

    val activeProgress = appState.activeProgress
    val activeProfile = appState.activeProfile

    // Auto-dismiss welcome bubble after 6 seconds
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
            isUnlocked = activeProgress.completedStations >= (ex.id - 1)
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
                .padding(bottom = 60.dp) // Leave space for bottom bar + insets
        ) {
            // 1. CABECERA SUPERIOR
            MapHeaderBar(appState = appState, onOpenProfile = onOpenProfile)

            // 2. CONTENIDO PRINCIPAL CON SCROLL
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tarjeta de Progreso Motriz & Evento
                CoordinationProgressCard(progress = activeProgress)

                Spacer(modifier = Modifier.height(8.dp))

                // Canvas del Sendero Sinuoso (Stitch Version)
                StitchWindingTrailMap(
                    stations = stations,
                    completedStations = activeProgress.completedStations,
                    showWelcomeBubble = showWelcomeBubble,
                    onDismissBubble = { showWelcomeBubble = false },
                    onStationClick = { stationId ->
                        selectedModalStation = stationId
                        showModal = true
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tarjeta Flotante Cámara Kinemática AR
                ArCalibrationBottomCard(onCalibrate = { onStartChallenge(1) })

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // 3. MODAL INTERACTIVO DE NIVEL
        if (showModal) {
            val station = stations.firstOrNull { it.id == selectedModalStation } ?: stations[0]
            StationModalDialog(
                station = station,
                onClose = { showModal = false },
                onStart = {
                    showModal = false
                    onStartChallenge(station.id)
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

            // Avatar Circular de Explorador
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
    val percentInt = ((progress.completedStations.toFloat() / totalStations) * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Tarjeta de Arena Suave
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
                                text = "${progress.completedStations} de $totalStations Estaciones",
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

        // Banner de Torneo (solo si existen datos de torneo)
        if (progress.hasTournamentData && progress.tournamentName != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFD8E6D3), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏆 ", fontSize = 14.sp)
                    Text(
                        text = progress.tournamentName,
                        color = CognokinetColors.Forest,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text("⏱️ 1D / 30m", color = Color(0xFF5D4037), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StitchWindingTrailMap(
    stations: List<StationInfo>,
    completedStations: Int,
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
        // SVG Sendero Sinuoso de Tierra y Piedra
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Sendero M 180 650 C 180 570, 240 520, 190 435 C 140 350, 125 290, 185 200 C 230 130, 160 80, 180 20
            val path = Path().apply {
                moveTo(w * 0.5f, h * 0.91f)
                cubicTo(w * 0.5f, h * 0.80f, w * 0.67f, h * 0.73f, w * 0.53f, h * 0.61f)
                cubicTo(w * 0.39f, h * 0.49f, w * 0.35f, h * 0.41f, w * 0.51f, h * 0.28f)
                cubicTo(w * 0.64f, h * 0.18f, w * 0.44f, h * 0.11f, w * 0.5f, h * 0.03f)
            }

            // 1. Borde de hierba oscura del sendero
            drawPath(
                path = path,
                color = Color(0xFFC2D6BF),
                style = Stroke(width = 48f)
            )

            // 2. Tierra firme / Sendero pedregoso
            drawPath(
                path = path,
                color = Color(0xFFD3BD98),
                style = Stroke(width = 36f)
            )

            // 3. Huella y piedras centrales del sendero
            drawPath(
                path = path,
                color = Color(0xFFBCA075),
                style = Stroke(
                    width = 8f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 14f), 0f)
                )
            )
        }

        // Texturas de Bosque (Pinos, Arbustos) en los costados
        Text("🌲", fontSize = 48.sp, modifier = Modifier.align(Alignment.TopStart).offset(x = 10.dp, y = 20.dp))
        Text("🌲", fontSize = 54.sp, modifier = Modifier.align(Alignment.TopEnd).offset(x = (-10).dp, y = 140.dp))
        Text("🌱", fontSize = 40.sp, modifier = Modifier.align(Alignment.BottomStart).offset(x = 10.dp, y = (-120).dp))
        Text("🌿", fontSize = 38.sp, modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-10).dp, y = (-180).dp))

        // ================= ESTACIÓN 5 (Arriba) =================
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 20.dp)
        ) {
            StationNodeButton(
                station = stations[4],
                onClick = { onStationClick(5) }
            )
        }

        // ================= ESTACIÓN 4 (Derecha) =================
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-40).dp, y = 160.dp)
        ) {
            StationNodeButton(
                station = stations[3],
                onClick = { onStationClick(4) }
            )
        }

        // ================= ESTACIÓN 3 (Izquierda) =================
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 30.dp, y = (-40).dp)
        ) {
            StationNodeButton(
                station = stations[2],
                onClick = { onStationClick(3) }
            )
        }

        // ================= ESTACIÓN 2 (Centro) =================
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 80.dp)
        ) {
            StationNodeButton(
                station = stations[1],
                onClick = { onStationClick(2) }
            )
        }

        // ================= ESTACIÓN 1 (Abajo - Ubicación Actual) =================
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-30).dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Burbuja Mascota Zorro Guía
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

                // Badge ¡Estás aquí!
                Box(
                    modifier = Modifier
                        .background(CognokinetColors.Forest, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text("📍 ¡Estás aquí!", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Botón Gigante Verde Estación 1
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(10.dp, RoundedCornerShape(28.dp))
                        .background(CognokinetColors.LeafGreen, RoundedCornerShape(28.dp))
                        .border(4.dp, Color(0xFF5D4037), RoundedCornerShape(28.dp))
                        .clickable { onStationClick(1) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("1", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color(0xFFA5D6A7), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("1. Salto Vertical", color = Color(0xFF1B5E20), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StationNodeButton(
    station: StationInfo,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .background(
                    if (station.isUnlocked) CognokinetColors.LeafGreen else Color(0xFF57606F),
                    RoundedCornerShape(16.dp)
                )
                .border(2.dp, if (station.isUnlocked) Color(0xFF5D4037) else Color(0xFF474F5C), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (station.isUnlocked) {
                Text("${station.id}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
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
private fun ArCalibrationBottomCard(onCalibrate: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(Color(0xFFFCFBF7), RoundedCornerShape(16.dp))
            .border(2.dp, Color(0xFFD7C4A5), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFEFE8DA), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFDECFB5), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🥽", fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Cámara Kinemática AR",
                        color = Color(0xFF2A1D13),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Calibrar seguimiento corporal",
                        color = Color(0xFF6D5A4E),
                        fontSize = 12.sp
                    )
                }
            }

            Button(
                onClick = onCalibrate,
                colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text("Calibrar ⚙️", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
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
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(20.dp))
                        .border(2.dp, CognokinetColors.LeafGreen, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🥾", fontSize = 30.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (station.isUnlocked) "¡DISPONIBLE!" else "VISTA PREVIA",
                    color = CognokinetColors.Forest,
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
                    text = station.description,
                    color = Color(0xFF4A5D4E),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .background(Color(0xFFEFE8DA), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFDECFB5), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("⭐ Meta: ${station.goal}", color = Color(0xFF3A2B1E), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("🏃 Ver Demostración y Ejercicio 3D", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
