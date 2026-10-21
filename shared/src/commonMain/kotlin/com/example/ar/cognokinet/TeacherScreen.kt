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

@Composable
fun TeacherScreen(
    appState: CognokinetAppState,
    onBack: () -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    var isAuthenticated by remember { mutableStateOf(false) }
    var selectedTeacherTab by remember { mutableStateOf("evaluaciones") } // "clases", "evaluaciones", "alumnos"

    var selectedRequestToEvaluate by remember { mutableStateOf<EvaluationRequest?>(null) }
    var starsInput by remember { mutableStateOf(3) }
    var bonusAcornsInput by remember { mutableStateOf(10) }
    var bonusLeavesInput by remember { mutableStateOf(2) }
    var commentInput by remember { mutableStateOf("¡Excelente ejecución de técnica!") }

    val pendingRequests = appState.evaluationRequests.filter { it.status == "PENDIENTE" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CognokinetColors.SurfaceGreen)
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

                        Text("Panel del Profesor / Evaluador", color = CognokinetColors.Graphite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Text("🎓", fontSize = 20.sp)
                }
            }

            if (!isAuthenticated) {
                // AUTENTICACIÓN DE PIN DEL PROFESOR
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .shadow(8.dp, RoundedCornerShape(24.dp))
                            .background(CognokinetColors.Parchment, RoundedCornerShape(24.dp))
                            .border(2.dp, CognokinetColors.EarthWood, RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔐 Acceso Protegido de Profesor", color = CognokinetColors.Graphite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Ingresa el PIN de seguridad (Default: 1234):", color = Color(0xFF5C3A21), fontSize = 12.sp, textAlign = TextAlign.Center)

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = pinInput,
                                onValueChange = { pinInput = it },
                                placeholder = { Text("PIN de 4 dígitos") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth(0.7f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (pinInput == appState.teacherPin || pinInput == "1234") {
                                        isAuthenticated = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(48.dp)
                            ) {
                                Text("Ingresar al Dashboard", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // PANELES DE GESTIÓN DEL PROFESOR
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pestañas del Profesor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "evaluaciones" to "📝 Evaluaciones (${pendingRequests.size})",
                            "clases" to "🏫 Mis Clases",
                            "alumnos" to "👥 Alumnos"
                        ).forEach { (key, label) ->
                            val isSel = selectedTeacherTab == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .background(
                                        if (isSel) CognokinetColors.Forest else Color(0xFFE8E2D0),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedTeacherTab = key },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) Color.White else Color(0xFF5C3A21),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedTeacherTab == "evaluaciones") {
                        // COLA DE EVALUACIONES PENDIENTES
                        Text("Cola de Solicitudes de Evaluación", color = CognokinetColors.Graphite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (pendingRequests.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(16.dp))
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✓ No hay evaluaciones pendientes en este momento.", color = Color(0xFF5C6E5A), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                pendingRequests.forEach { req ->
                                    val exercise = ExerciseCatalog.getById(req.exerciseId)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .shadow(3.dp, RoundedCornerShape(16.dp))
                                            .background(Color.White, RoundedCornerShape(16.dp))
                                            .border(1.dp, Color(0xFFE8E1D3), RoundedCornerShape(16.dp))
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(req.studentName, color = CognokinetColors.Graphite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                                Text("Ejercicio: ${exercise.name}", color = CognokinetColors.Forest, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text("Meta: ${exercise.targetGoal}", color = Color(0xFF5D4037), fontSize = 11.sp)
                                            }

                                            Button(
                                                onClick = { selectedRequestToEvaluate = req },
                                                colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("Evaluar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (selectedTeacherTab == "clases") {
                        // VISTA DE CLASES
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(20.dp))
                                .background(Color.White, RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Clase: 3º A de Primaria", color = CognokinetColors.Graphite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Text("Código de Clase: ABC123", color = CognokinetColors.Forest, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFE8F5E9), CircleShape)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("${appState.studentProfiles.size} Alumnos", color = CognokinetColors.ForestDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { },
                                    colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("➕ Crear Nueva Clase", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // VISTA DE ROSTER DE ALUMNOS
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            appState.studentProfiles.forEach { student ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(3.dp, RoundedCornerShape(16.dp))
                                        .background(Color.White, RoundedCornerShape(16.dp))
                                        .border(1.dp, Color(0xFFE8E1D3), RoundedCornerShape(16.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(StudentAvatars.getEmoji(student.avatarId), fontSize = 28.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(student.name, color = CognokinetColors.Graphite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text("${student.age} años · ${student.grade}", color = Color(0xFF5C3A21), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // DIÁLOGO DE CALIFICACIÓN DE EVALUACIÓN
        if (selectedRequestToEvaluate != null) {
            val req = selectedRequestToEvaluate!!
            val exercise = ExerciseCatalog.getById(req.exerciseId)

            AlertDialog(
                onDismissRequest = { selectedRequestToEvaluate = null },
                title = { Text("Evaluar a ${req.studentName}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Ejercicio: ${exercise.name}", fontWeight = FontWeight.Bold, color = CognokinetColors.Forest)

                        Text("Calificación de Estrellas (1 - 3):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..3).forEach { st ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            if (st <= starsInput) CognokinetColors.SunAmber else Color(0xFFE0E0E0),
                                            CircleShape
                                        )
                                        .clickable { starsInput = st },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("★", color = Color.White, fontSize = 18.sp)
                                }
                            }
                        }

                        Text("Comentario para el Alumno:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = commentInput,
                            onValueChange = { commentInput = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            appState.teacherApproveEvaluation(
                                requestId = req.id,
                                stars = starsInput,
                                bonusAcorns = bonusAcornsInput,
                                bonusLeaves = bonusLeavesInput,
                                comment = commentInput
                            )
                            selectedRequestToEvaluate = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest)
                    ) {
                        Text("Aprobar Nivel", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedRequestToEvaluate = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
