package com.example.ar.cognokinet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ar.cognokinet.domain.Classroom
import com.example.ar.cognokinet.domain.DecisionStatus
import com.example.ar.cognokinet.domain.EvaluationRequest
import com.example.ar.cognokinet.domain.EvaluationStatus
import com.example.ar.cognokinet.domain.IdGenerator

@Composable
fun TeacherScreen(
    appState: CognokinetAppState,
    onBack: () -> Unit
) {
    var isAuthenticated by remember { mutableStateOf(appState.isTeacherPinSet) }
    var emailInput by remember { mutableStateOf("prof.ana@escuela.edu") }
    var passwordInput by remember { mutableStateOf("123456") }

    var selectedTab by remember { mutableStateOf("clases") }

    var classrooms by remember {
        mutableStateOf(
            listOf(
                Classroom(
                    id = "class_1",
                    name = "Educación Física 4º A",
                    grade = "4º",
                    group = "A",
                    schoolYear = "2024-2025",
                    teacherId = "teacher_1",
                    teacherName = appState.teacherName,
                    joinCode = "EF4A24",
                    joinOpen = true
                )
            )
        )
    }
    var activeClassroom by remember { mutableStateOf<Classroom?>(classrooms.firstOrNull()) }

    var showCreateClassDialog by remember { mutableStateOf(false) }
    var newClassName by remember { mutableStateOf("") }
    var newClassGrade by remember { mutableStateOf("4º") }
    var newClassGroup by remember { mutableStateOf("A") }
    var newClassYear by remember { mutableStateOf("2024-2025") }

    var selectedRequestToEvaluate by remember { mutableStateOf<EvaluationRequest?>(null) }
    var evalDecision by remember { mutableStateOf(DecisionStatus.PASS) }
    var evalStars by remember { mutableStateOf(3) }
    var evalBonusAcorns by remember { mutableStateOf(10) }
    var evalBonusLeaves by remember { mutableStateOf(2) }
    var evalComment by remember { mutableStateOf("¡Excelente técnica!") }

    val pendingRequests = appState.evaluationRequests.filter { it.status == EvaluationStatus.PENDING }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CognokinetColors.SurfaceGreen)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar with Status Bar Padding to prevent clock overlap
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(CognokinetColors.ForestLight, CircleShape)
                                .border(1.dp, CognokinetColors.Forest.copy(alpha = 0.3f), CircleShape)
                                .clickable { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("←", color = CognokinetColors.Forest, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            "Panel de la Profesora",
                            color = CognokinetColors.Graphite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFFF8E1), CircleShape)
                            .border(1.dp, Color(0xFFFFD54F), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎓", fontSize = 20.sp)
                    }
                }
            }

            if (!isAuthenticated) {
                // LOGIN FORM FOR TEACHER
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CognokinetColors.Parchment),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Iniciar Sesión - Profesora",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = CognokinetColors.Graphite
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Correo institucional") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Contraseña") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = {
                                    if (emailInput.isNotBlank() && passwordInput.length >= 6) {
                                        appState.setTeacherProfile(
                                            appState.teacherName,
                                            appState.teacherGrade,
                                            appState.teacherSubject,
                                            passwordInput
                                        )
                                        isAuthenticated = true
                                    } else {
                                        appState.snackbarMessage = "Ingresa correo y contraseña válida (mínimo 6 caracteres)"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text("Entrar al Panel", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // DASHBOARD VIEW
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // SCROLLABLE HORIZONTAL TABS (Prevents text wrapping and layout breaking)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val tabsList = listOf(
                            "clases" to "🏫 Clases",
                            "alumnos" to "👥 Alumnos",
                            "progreso" to "📊 Progreso",
                            "evaluaciones" to "📩 Evaluaciones (${pendingRequests.size})",
                            "rubricas" to "📝 Rúbricas"
                        )

                        tabsList.forEach { (key, label) ->
                            val isSel = selectedTab == key
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSel) CognokinetColors.Forest else Color(0xFFF0F4F1),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSel) CognokinetColors.ForestDark else Color(0xFFE0E0E0),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable { selectedTab = key }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) Color.White else Color(0xFF424242),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                    // CONTENT BODY FOR SELECTED TAB
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        when (selectedTab) {
                            "clases" -> {
                                // Clear white plus icon on dark green button
                                Button(
                                    onClick = { showCreateClassDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(Color.White.copy(alpha = 0.25f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("+", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            "Crear Clase de Educación Física",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                classrooms.forEach { cls ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CognokinetColors.Parchment),
                                        shape = RoundedCornerShape(18.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .shadow(4.dp, RoundedCornerShape(18.dp))
                                            .clickable { activeClassroom = cls }
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                cls.name,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CognokinetColors.Graphite
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                "${cls.grade} ${cls.group} · Ciclo ${cls.schoolYear}",
                                                fontSize = 13.sp,
                                                color = Color(0xFF5C3A21)
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "Código de ingreso: ${cls.joinCode}",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = CognokinetColors.Forest
                                                )

                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            if (cls.joinOpen) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                                            CircleShape
                                                        )
                                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        if (cls.joinOpen) "🟢 Registro Abierto" else "🔴 Registro Cerrado",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (cls.joinOpen) Color(0xFF2E7D32) else Color(0xFFC62828)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            "evaluaciones" -> {
                                Text(
                                    "Cola de Evaluaciones Pendientes:",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CognokinetColors.Graphite
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                if (pendingRequests.isEmpty()) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("🎉", fontSize = 36.sp)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                "¡Al día! No hay solicitudes pendientes por evaluar.",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CognokinetColors.Graphite,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    pendingRequests.forEach { req ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(14.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(14.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(req.studentName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                    Text(
                                                        "Estación ${req.stationId}: ${ExerciseCatalog.getById(req.stationId).name}",
                                                        fontSize = 12.sp,
                                                        color = CognokinetColors.Forest,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }

                                                Button(
                                                    onClick = { selectedRequestToEvaluate = req },
                                                    colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text("Evaluar ➔", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            "rubricas" -> {
                                RubricEditorScreen(
                                    classId = activeClassroom?.id ?: "class_1",
                                    stationId = 1,
                                    onSaveRubric = { appState.snackbarMessage = "Rúbrica guardada exitosamente" },
                                    onResetToDefault = { appState.snackbarMessage = "Rúbrica restablecida" },
                                    onBack = { selectedTab = "clases" }
                                )
                            }

                            else -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            "Lista de alumnos y progreso de la clase",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CognokinetColors.Graphite
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Selecciona un alumno para revisar su expediente individual, avances en las estaciones e insignias ganadas.",
                                            fontSize = 13.sp,
                                            color = Color(0xFF616161)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // CREATE CLASS DIALOG
        if (showCreateClassDialog) {
            AlertDialog(
                onDismissRequest = { showCreateClassDialog = false },
                title = { Text("Crear Nueva Clase de EF") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newClassName,
                            onValueChange = { newClassName = it },
                            label = { Text("Nombre de la Clase") },
                            placeholder = { Text("ej. Educación Física 4º B") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newClassGrade,
                                onValueChange = { newClassGrade = it },
                                label = { Text("Grado") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = newClassGroup,
                                onValueChange = { newClassGroup = it },
                                label = { Text("Grupo") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newClassName.isNotBlank()) {
                                val code = "EF" + (100..999).random()
                                val newCls = Classroom(
                                    id = IdGenerator.newId(),
                                    name = newClassName,
                                    grade = newClassGrade,
                                    group = newClassGroup,
                                    schoolYear = newClassYear,
                                    teacherId = "teacher_1",
                                    teacherName = appState.teacherName,
                                    joinCode = code,
                                    joinOpen = true
                                )
                                classrooms = classrooms + newCls
                                activeClassroom = newCls
                                showCreateClassDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Crear Clase", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateClassDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // EVALUATION DIALOG
        if (selectedRequestToEvaluate != null) {
            val req = selectedRequestToEvaluate!!
            AlertDialog(
                onDismissRequest = { selectedRequestToEvaluate = null },
                title = { Text("Evaluar a ${req.studentName} - Estación ${req.stationId}") },
                text = {
                    Column {
                        Text("Decisión de Evaluación:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = evalDecision == DecisionStatus.PASS,
                                onClick = { evalDecision = DecisionStatus.PASS },
                                label = { Text("PASA a siguiente") }
                            )
                            FilterChip(
                                selected = evalDecision == DecisionStatus.REPEAT,
                                onClick = { evalDecision = DecisionStatus.REPEAT },
                                label = { Text("Debe REPETIR") }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Estrellas Otorgadas (1 a 3):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..3).forEach { st ->
                                FilterChip(
                                    selected = evalStars == st,
                                    onClick = { evalStars = st },
                                    label = { Text("$st ★") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = evalComment,
                            onValueChange = { evalComment = it },
                            label = { Text("Comentario (Obligatorio si repite)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (evalDecision == DecisionStatus.REPEAT && evalComment.isBlank()) {
                                appState.snackbarMessage = "El comentario es obligatorio para repetir la estación"
                            } else {
                                appState.teacherApproveEvaluation(
                                    requestId = req.id,
                                    stars = evalStars,
                                    bonusAcorns = evalBonusAcorns,
                                    bonusLeaves = evalBonusLeaves,
                                    comment = evalComment,
                                    decision = evalDecision
                                )
                                selectedRequestToEvaluate = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Guardar Evaluación", color = Color.White)
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
