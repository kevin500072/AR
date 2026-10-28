package com.example.ar.cognokinet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ar.cognokinet.domain.DecisionStatus
import com.example.ar.cognokinet.domain.EvaluationReview
import com.example.ar.cognokinet.domain.StudentProfile
import com.example.ar.cognokinet.domain.StudentProgress

@Composable
fun StudentDetailScreen(
    student: StudentProfile,
    progress: StudentProgress,
    evaluationHistory: List<EvaluationReview>,
    onEvaluatePending: () -> Unit,
    onRemoveFromClass: () -> Unit,
    onBack: () -> Unit
) {
    var showRemoveConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CognokinetColors.SurfaceGreen)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Header Bar
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
                                .size(48.dp)
                                .background(CognokinetColors.ForestLight, CircleShape)
                                .clickable { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("←", color = CognokinetColors.Forest, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text("Expediente del Alumno", color = CognokinetColors.Graphite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Text("👤", fontSize = 22.sp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header Card: Avatar, Name, Age, Grade
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = CognokinetColors.Parchment)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(CognokinetColors.ForestLight, CircleShape)
                                .border(2.dp, CognokinetColors.Forest, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(StudentAvatars.getEmoji(student.avatarId), fontSize = 34.sp)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(student.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CognokinetColors.Graphite)
                            Text("${student.age} años · ${student.grade}", fontSize = 13.sp, color = Color(0xFF5C3A21))
                            Text("Estado: Activo en clase", fontSize = 12.sp, color = CognokinetColors.Forest, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Overview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Resumen de Progreso", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CognokinetColors.Graphite)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Nivel", fontSize = 11.sp, color = Color.Gray)
                                Text("${progress.level}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CognokinetColors.Forest)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("XP Total", fontSize = 11.sp, color = Color.Gray)
                                Text("${progress.xp}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CognokinetColors.Forest)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Estaciones", fontSize = 11.sp, color = Color.Gray)
                                Text("${progress.completedStationIds.size}/5", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CognokinetColors.Forest)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Insignias", fontSize = 11.sp, color = Color.Gray)
                                Text("${progress.unlockedBadges.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CognokinetColors.Forest)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🌰 Bellotas: ${progress.acorns}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5D4037))
                            Text("🍃 Hojas: ${progress.leaves}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CognokinetColors.ForestDark)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Evaluation History
                Text("Historial de Evaluaciones con Rúbrica:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CognokinetColors.Graphite)
                Spacer(modifier = Modifier.height(6.dp))

                if (evaluationHistory.isEmpty()) {
                    Text("Sin evaluaciones registradas aún.", fontSize = 12.sp, color = Color.Gray)
                } else {
                    evaluationHistory.forEach { rev ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Estación ${rev.stationId}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        if (rev.decision == DecisionStatus.PASS) "PASÓ" else "REPITE",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (rev.decision == DecisionStatus.PASS) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                }
                                Text("Estrellas: " + "★".repeat(rev.finalStars) + "☆".repeat(3 - rev.finalStars), color = CognokinetColors.SunAmber, fontSize = 13.sp)
                                if (rev.comment.isNotBlank()) {
                                    Text("Comentario: ${rev.comment}", fontSize = 11.sp, color = Color(0xFF424242))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Button(
                    onClick = onEvaluatePending,
                    colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("✏️ Evaluar Estación Pendiente", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showRemoveConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFBA1A1A)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🗑️ Quitar de la clase", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Remove Confirmation Dialog
        if (showRemoveConfirm) {
            AlertDialog(
                onDismissRequest = { showRemoveConfirm = false },
                title = { Text("Quitar alumno de la clase") },
                text = { Text("¿Deseas quitar a '${student.name}' de esta clase? Se eliminarán sus registros asociados a esta clase.") },
                confirmButton = {
                    Button(
                        onClick = {
                            onRemoveFromClass()
                            showRemoveConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A))
                    ) {
                        Text("Quitar", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRemoveConfirm = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
