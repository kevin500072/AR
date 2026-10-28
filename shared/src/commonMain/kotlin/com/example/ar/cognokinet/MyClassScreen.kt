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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ar.cognokinet.domain.Classroom
import com.example.ar.cognokinet.domain.DecisionStatus
import com.example.ar.cognokinet.domain.EnrollmentStatus
import com.example.ar.cognokinet.domain.EvaluationReview
import com.example.ar.cognokinet.domain.EvaluationStatus

@Composable
fun MyClassScreen(
    appState: CognokinetAppState,
    currentClassroom: Classroom? = null,
    enrollmentStatus: EnrollmentStatus = EnrollmentStatus.ACTIVE,
    onJoinClass: (String) -> Unit,
    onLeaveClass: () -> Unit,
    onBack: () -> Unit
) {
    var showJoinDialog by remember { mutableStateOf(false) }
    var showLeaveConfirm by remember { mutableStateOf(false) }
    var joinCodeInput by remember { mutableStateOf("") }

    var selectedReviewForDetail by remember { mutableStateOf<EvaluationReview?>(null) }

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

                        Text("Mi Clase de Educación Física", color = CognokinetColors.Graphite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Text("🏫", fontSize = 22.sp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentClassroom == null) {
                    // Sin clase activa
                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = CognokinetColors.Parchment)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎒", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Todavía no estás en una clase",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = CognokinetColors.Graphite,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Pídele el código de 6 caracteres a tu profesora de Educación Física para unirte a su clase.",
                                fontSize = 13.sp,
                                color = Color(0xFF5C3A21),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { showJoinDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text("🔑 Unirme a una clase", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Con clase activa
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = CognokinetColors.Parchment)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        currentClassroom.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CognokinetColors.Graphite
                                    )
                                    Text(
                                        "${currentClassroom.grade} ${currentClassroom.group} · Ciclo ${currentClassroom.schoolYear}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF5C3A21)
                                    )
                                    Text(
                                        "Profesora: ${currentClassroom.teacherName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CognokinetColors.Forest
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (enrollmentStatus == EnrollmentStatus.ACTIVE) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                            CircleShape
                                        )
                                        .border(
                                            1.dp,
                                            if (enrollmentStatus == EnrollmentStatus.ACTIVE) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                            CircleShape
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        if (enrollmentStatus == EnrollmentStatus.ACTIVE) "✓ Activo" else "⏳ Pendiente",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (enrollmentStatus == EnrollmentStatus.ACTIVE) Color(0xFF2E7D32) else Color(0xFFE65100)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text("Última actualización: Datos sincronizados", fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Summary of My Stations
                    Text("Resumen de Mis Estaciones:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CognokinetColors.Graphite)
                    Spacer(modifier = Modifier.height(8.dp))

                    (1..5).forEach { st ->
                        val exercise = ExerciseCatalog.getById(st)
                        val isPassed = appState.activeProgress.completedStationIds.contains(st)
                        val stars = appState.activeProgress.bestStarsByStation[st] ?: 0
                        val pending = appState.evaluationRequests.firstOrNull { it.studentId == appState.activeProfileId && it.stationId == st && it.status == EvaluationStatus.PENDING }
                        val review = appState.evaluationReviews.firstOrNull { it.stationId == st }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    if (review != null) selectedReviewForDetail = review
                                },
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        when {
                                            isPassed -> "✅"
                                            pending != null -> "⏳"
                                            else -> "📌"
                                        },
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Estación $st: ${exercise.name}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        if (isPassed) {
                                            Text("★".repeat(stars) + "☆".repeat(3 - stars) + " · Aprobado", fontSize = 11.sp, color = CognokinetColors.SunAmber)
                                        } else if (pending != null) {
                                            Text("Pendiente de revisión por la profesora", fontSize = 11.sp, color = Color.Gray)
                                        } else if (review?.decision == DecisionStatus.REPEAT) {
                                            Text("Debes repetir esta estación", fontSize = 11.sp, color = Color(0xFFC62828))
                                        } else {
                                            Text("Disponible", fontSize = 11.sp, color = CognokinetColors.Forest)
                                        }
                                    }
                                }

                                if (review != null) {
                                    Text("Ver Rúbrica ➔", fontSize = 11.sp, color = CognokinetColors.Forest, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Leave Class Button
                    OutlinedButton(
                        onClick = { showLeaveConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFBA1A1A)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🚪 Salir de la clase", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Dialog Join Class
        if (showJoinDialog) {
            AlertDialog(
                onDismissRequest = { showJoinDialog = false },
                title = { Text("Unirme a una clase") },
                text = {
                    Column {
                        Text("Escribe el código de 6 caracteres que te dio tu profesora:", fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = joinCodeInput,
                            onValueChange = { joinCodeInput = it.uppercase() },
                            placeholder = { Text("EJ. ABC123") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (joinCodeInput.isNotBlank()) {
                                onJoinClass(joinCodeInput)
                                showJoinDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest)
                    ) {
                        Text("Unirme", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showJoinDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Dialog Leave Confirmation
        if (showLeaveConfirm) {
            AlertDialog(
                onDismissRequest = { showLeaveConfirm = false },
                title = { Text("Salir de la Clase") },
                text = { Text("¿Estás seguro de que quieres salir de esta clase? Podrás unirte a otra clase usando un código.") },
                confirmButton = {
                    Button(
                        onClick = {
                            onLeaveClass()
                            showLeaveConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A))
                    ) {
                        Text("Sí, Salir", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLeaveConfirm = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Detail View of Rubric Result
        if (selectedReviewForDetail != null) {
            val rev = selectedReviewForDetail!!
            AlertDialog(
                onDismissRequest = { selectedReviewForDetail = null },
                title = { Text("Detalle de Evaluación - Estación ${rev.stationId}") },
                text = {
                    Column {
                        Text("Resultado: ${if (rev.decision == DecisionStatus.PASS) "¡Aprobado! 🎉" else "A Repetir 🔁"}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Estrellas: " + "★".repeat(rev.finalStars) + "☆".repeat(3 - rev.finalStars), color = CognokinetColors.SunAmber, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Comentario de la Profesora:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(rev.comment.ifBlank { "¡Buen trabajo!" }, fontSize = 12.sp, color = Color(0xFF424242))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { selectedReviewForDetail = null },
                        colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest)
                    ) {
                        Text("Cerrar", color = Color.White)
                    }
                }
            )
        }
    }
}
