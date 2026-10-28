package com.example.ar.cognokinet

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ar.cognokinet.domain.SessionResult

@Composable
fun ResultsScreen(
    appState: CognokinetAppState,
    onContinueToMap: () -> Unit,
    onBack: () -> Unit
) {
    val progress = appState.activeProgress
    val result = progress.lastSessionResult
    val exerciseName = if (result != null) ExerciseCatalog.getById(result.stationId).name else ""

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CognokinetColors.NatureBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. BARRA SUPERIOR
            ResultsHeaderBar(onBack = onBack)

            // 2. CONTENIDO DESPLAZABLE
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Banner de Celebración
                Box(
                    modifier = Modifier
                        .background(Color(0xFFD1C4E9).copy(alpha = 0.5f), CircleShape)
                        .border(1.dp, CognokinetColors.Forest.copy(alpha = 0.3f), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("🌿 EVALUACIÓN MOTRIZ COMPLETADA", color = CognokinetColors.ForestDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (result != null) "¡Reto $exerciseName Superado!" else "¡Gran Sesión!",
                    color = CognokinetColors.Graphite,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (result != null) "Estación ${result.stationId} Evaluada por el Profesor" else "Sin resultados recientes",
                    color = Color(0xFF4F6354),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. MEDALLA Y ESTRELLAS REALES
                NobleWoodMedal(result = result)

                Spacer(modifier = Modifier.height(16.dp))

                // 4. PROGRESS BAR DE EXPERIENCIA (XP) REAL
                XpProgressCard(progress = progress, result = result)

                Spacer(modifier = Modifier.height(14.dp))

                // 5. RECOMPENSAS GANADAS (Bellotas y Hojas)
                if (result != null) {
                    RewardsSummaryCard(result = result)
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 6. MENSAJE DEL PROFESOR / ZORRO GUÍA
                FoxGuideMessageCard(comment = result?.teacherComment ?: "¡Sigue practicando con entusiasmo!")

                Spacer(modifier = Modifier.height(16.dp))

                // 7. BOTONES INFERIORES DE ACCIÓN
                Button(
                    onClick = onContinueToMap,
                    colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Continuar al Mapa ➔", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onContinueToMap,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C6D58)),
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("🗺️ Volver al Sendero", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ResultsHeaderBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CognokinetColors.NatureBg.copy(alpha = 0.95f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color(0xFFA5D6A7), CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("←", color = CognokinetColors.Forest, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text("Resultados del Reto", color = CognokinetColors.Graphite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(CognokinetColors.Forest, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🌲", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun NobleWoodMedal(result: SessionResult?) {
    val stars = result?.stars ?: 3
    val starString = "★".repeat(stars) + "☆".repeat(3 - stars)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .shadow(10.dp, CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF7A4E2D), Color(0xFF5D3A20), Color(0xFF3E2311))
                    ),
                    CircleShape
                )
                .border(3.dp, Color(0xFF4A2D16), CircleShape)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFEFE9DB), Color(0xFFDFD6C2), Color(0xFFCAC0A8))
                        ),
                        CircleShape
                    )
                    .border(1.dp, Color(0xFFC5B89C), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .shadow(4.dp, CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(CognokinetColors.Forest, Color(0xFF34D399))
                                ),
                                CircleShape
                            )
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍃", fontSize = 26.sp)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(starString, color = CognokinetColors.SunAmber, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .shadow(4.dp, CircleShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF6D4C41), Color(0xFF5D4037), Color(0xFF4E342E))
                    ),
                    CircleShape
                )
                .border(1.dp, Color(0xFF8D6E63), CircleShape)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text("Nivel ${result?.levelAfter ?: 1} · ${result?.xpGained ?: 0} XP Ganados", color = Color(0xFFFFECB3), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun XpProgressCard(progress: StudentProgress, result: SessionResult?) {
    val currentXp = progress.xp
    val nextLevelXp = progress.level * CognokinetConstants.XP_PER_LEVEL
    val prevLevelXp = (progress.level - 1) * CognokinetConstants.XP_PER_LEVEL
    val xpInLevel = currentXp - prevLevelXp
    val xpSpan = nextLevelXp - prevLevelXp
    val fraction = if (xpSpan > 0) (xpInLevel.toFloat() / xpSpan.toFloat()).coerceIn(0f, 1f) else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(18.dp))
            .background(CognokinetColors.NatureBgSoft, RoundedCornerShape(18.dp))
            .border(2.dp, Color(0xFFD7CCC8), RoundedCornerShape(18.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🌱 ", fontSize = 16.sp)
                Text("Progreso de Nivel ${progress.level}", color = CognokinetColors.Graphite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .background(CognokinetColors.SunAmber.copy(alpha = 0.3f), CircleShape)
                    .border(1.dp, CognokinetColors.SunAmber, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("✦ +${result?.xpGained ?: 100} XP", color = Color(0xFF5D4037), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(CircleShape)
                .background(Color(0xFFE6DFD1))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(
                        Brush.horizontalGradient(
                            listOf(CognokinetColors.ForestDark, CognokinetColors.Forest, CognokinetColors.LeafGreen)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$currentXp XP Total", color = Color(0xFF68766C), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("Meta Siguiente: $nextLevelXp XP", color = CognokinetColors.Forest, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RewardsSummaryCard(result: SessionResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFFFFF8E1), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFFFE082), RoundedCornerShape(16.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🌰", fontSize = 24.sp)
                Text("+${result.acornsGained} Bellotas", color = Color(0xFF5D4037), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFFE8F5E9), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFA5D6A7), RoundedCornerShape(16.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🍃", fontSize = 24.sp)
                Text("+${result.leavesGained} Hojas", color = CognokinetColors.ForestDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FoxGuideMessageCard(comment: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp))
            .background(CognokinetColors.NatureBgSoft, RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFFE2DCCE), RoundedCornerShape(18.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(Color(0xFFE67E22), RoundedCornerShape(16.dp))
                    .border(2.dp, Color.White, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🦊", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text("Comentario del Profesor / Zorro 🧣", color = CognokinetColors.Graphite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(comment, color = Color(0xFF4D5A50), fontSize = 12.sp)
            }
        }
    }
}
