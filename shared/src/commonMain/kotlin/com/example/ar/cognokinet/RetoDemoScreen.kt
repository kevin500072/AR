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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ar.cognokinet.domain.EvaluationStatus

@Composable
fun RetoDemoScreen(
    appState: CognokinetAppState,
    initialStationId: Int = 1,
    onBack: () -> Unit,
    onCompleteSimulatedSession: () -> Unit
) {
    var currentStation by remember { mutableStateOf(initialStationId.coerceIn(1, 5)) }
    val activeExercise = ExerciseCatalog.getById(currentStation)
    val activeStudent = appState.activeProfile
    val activeProgress = appState.activeProgress

    val pendingReq = appState.evaluationRequests.firstOrNull {
        it.studentId == activeStudent?.id && it.stationId == currentStation && it.status == EvaluationStatus.PENDING
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CognokinetColors.NatureBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
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
                                .background(CognokinetColors.ForestLight, CircleShape)
                                .clickable { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("←", color = CognokinetColors.Forest, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text("Guía de Retos y Ejercicios", color = CognokinetColors.Graphite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Text("🏃", fontSize = 20.sp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Selector de Estaciones (Chips) con validación de bloqueo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExerciseCatalog.list.forEach { ex ->
                        val isSel = ex.id == currentStation
                        val isUnlocked = StationUnlockRules.isUnlocked(ex.id, activeProgress.completedStationIds)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .background(
                                    when {
                                        isSel -> CognokinetColors.Forest
                                        isUnlocked -> Color(0xFFE8E2D0)
                                        else -> Color(0xFFCFD8DC)
                                    },
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSel) CognokinetColors.ForestDark else Color(0xFFD5C29E),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    if (isUnlocked) {
                                        currentStation = ex.id
                                    } else {
                                        appState.snackbarMessage = "Estación bloqueada. Completa la anterior."
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isUnlocked) "E${ex.id}" else "🔒${ex.id}",
                                color = if (isSel) Color.White else Color(0xFF5C3A21),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // TARJETA DE ILUSTRACIÓN Y DETALLES DEL EJERCICIO
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(20.dp))
                        .background(CognokinetColors.Parchment, RoundedCornerShape(20.dp))
                        .border(2.dp, CognokinetColors.EarthWood, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(16.dp))
                                    .border(2.dp, CognokinetColors.LeafGreen, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                val exerciseEmoji = when (activeExercise.id) {
                                    1 -> "⬆️"
                                    2 -> "🪢"
                                    3 -> "🎯"
                                    4 -> "⚽"
                                    else -> "🏀"
                                }
                                Text(exerciseEmoji, fontSize = 26.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(activeExercise.name, color = CognokinetColors.Graphite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("Estación ${activeExercise.order} de ${ExerciseCatalog.list.size}", color = CognokinetColors.Forest, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(activeExercise.description, color = Color(0xFF5C3A21), fontSize = 13.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        // VISUALIZADOR ANIMADO DE MODELO 3D FBX
                        FBXModelViewer(
                            modelFileName = activeExercise.modelFileName,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Pasos de Ejecución:", color = CognokinetColors.Graphite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        activeExercise.steps.forEachIndexed { index, step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("${index + 1}. ", color = CognokinetColors.Forest, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(step, color = Color(0xFF4B5E50), fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFEF9E7), RoundedCornerShape(14.dp))
                                .border(1.dp, CognokinetColors.SunAmber, RoundedCornerShape(14.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("📋 Criterios que evaluará el Profesor:", color = Color(0xFF5D4037), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                activeExercise.suggestedCriteria.forEach { crit ->
                                    Text("• $crit", color = Color(0xFF5D4037), fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val isUnlockedCurrent = StationUnlockRules.isUnlocked(currentStation, activeProgress.completedStationIds)

                        if (!isUnlockedCurrent) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .background(Color(0xFFCFD8DC), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🔒 Estación Bloqueada", color = Color(0xFF37474F), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (pendingReq != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .background(Color(0xFFE8E2D0), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⏳ Esperando evaluación del profesor...", color = Color(0xFF5C3A21), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    appState.submitEvaluationRequest(currentStation)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text("📩 Solicitar Evaluación al Profesor", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
