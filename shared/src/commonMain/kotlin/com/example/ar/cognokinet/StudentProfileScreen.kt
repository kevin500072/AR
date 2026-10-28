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
fun StudentProfileScreen(
    appState: CognokinetAppState,
    onBackToHome: () -> Unit,
    onGoToMap: () -> Unit
) {
    var isCreatingNew by remember { mutableStateOf(appState.studentProfiles.isEmpty()) }
    var editingProfileId by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showValidationError by remember { mutableStateOf(false) }

    // Form States
    var nameInput by remember { mutableStateOf("") }
    var ageInput by remember { mutableStateOf("8") }
    var gradeInput by remember { mutableStateOf("3º Grado") }
    var selectedAvatarId by remember { mutableStateOf("fox") }
    var consentChecked by remember { mutableStateOf(false) }

    val activeProfile = appState.activeProfile
    val activeProgress = appState.activeProgress

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
                                .clickable { onBackToHome() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("←", color = CognokinetColors.Forest, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text("Perfil del Alumno / Explorador", color = CognokinetColors.Graphite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Text("👤", fontSize = 20.sp)
                }
            }

            // Scrollable Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isCreatingNew || editingProfileId != null) {
                    // FORMULARIO DE ALTA / EDICIÓN
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(24.dp))
                            .background(CognokinetColors.Parchment, RoundedCornerShape(24.dp))
                            .border(2.dp, CognokinetColors.EarthWood, RoundedCornerShape(24.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = if (editingProfileId != null) "Editar Datos del Alumno" else "Registro de Nuevo Alumno",
                                color = CognokinetColors.Graphite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // MENSAJE DE ERROR DE VALIDACIÓN
                            if (showValidationError && nameInput.isBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFFEBEE), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0xFFE53935), RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("⚠️ ", fontSize = 16.sp)
                                        Text(
                                            text = "Por favor, completa los datos necesarios para continuar (Nombre o Apodo).",
                                            color = Color(0xFFC62828),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Nombre / Apodo
                            Text("Nombre o Apodo del Alumno (*):", color = Color(0xFF5C3A21), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = {
                                    nameInput = it
                                    if (it.isNotBlank()) showValidationError = false
                                },
                                placeholder = { Text("ej. Lucas, Sofía, Mateo...") },
                                isError = showValidationError && nameInput.isBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Edad y Grado Row
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Edad:", color = Color(0xFF5C3A21), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = ageInput,
                                        onValueChange = { ageInput = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        singleLine = true
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Grado Escolar:", color = Color(0xFF5C3A21), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = gradeInput,
                                        onValueChange = { gradeInput = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        singleLine = true
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Avatar Ilustrado
                            Text("Elige un Avatar Ilustrado:", color = Color(0xFF5C3A21), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                StudentAvatars.list.forEach { avatar ->
                                    val isSel = avatar.id == selectedAvatarId
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .background(
                                                if (isSel) CognokinetColors.Forest else Color.White,
                                                CircleShape
                                            )
                                            .border(
                                                2.dp,
                                                if (isSel) CognokinetColors.ForestDark else Color(0xFFDCD5C5),
                                                CircleShape
                                            )
                                            .clickable { selectedAvatarId = avatar.id },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(avatar.emoji, fontSize = 22.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Consentimiento Tutor
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { consentChecked = !consentChecked }
                            ) {
                                Checkbox(
                                    checked = consentChecked,
                                    onCheckedChange = { consentChecked = it }
                                )
                                Text(
                                    text = "Declaro contar con la autorización del padre/tutor para la evaluación motriz.",
                                    color = Color(0xFF4B5E50),
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Botón Guardar
                            Button(
                                onClick = {
                                    val age = ageInput.toIntOrNull() ?: 8
                                    if (nameInput.isBlank() || age !in 3..18 || !consentChecked) {
                                        showValidationError = true
                                        appState.snackbarMessage = "Nombre requerido, edad entre 3 y 18, y consentimiento obligatorio."
                                    } else {
                                        showValidationError = false
                                        val age = ageInput.toIntOrNull() ?: 8
                                        val editingId = editingProfileId
                                        if (editingId != null) {
                                            val existing = appState.studentProfiles.firstOrNull { it.id == editingId }
                                            if (existing != null) {
                                                appState.updateStudentProfile(
                                                    existing.copy(
                                                        displayName = nameInput,
                                                        age = age,
                                                        grade = gradeInput,
                                                        avatarId = selectedAvatarId,
                                                        consentGiven = consentChecked
                                                    )
                                                )
                                            }
                                            editingProfileId = null
                                        } else {
                                            appState.createStudentProfile(
                                                name = nameInput,
                                                age = age,
                                                grade = gradeInput,
                                                avatarId = selectedAvatarId,
                                                consentGiven = consentChecked
                                            )
                                        }
                                        isCreatingNew = false
                                        onGoToMap()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text("✓ Guardar Perfil de Explorador", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }

                            if (appState.studentProfiles.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = {
                                        isCreatingNew = false
                                        editingProfileId = null
                                        showValidationError = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Cancelar", color = Color(0xFF5C3A21))
                                }
                            }
                        }
                    }
                } else if (activeProfile != null) {
                    // VISTA DE PERFIL ACTUAL Y SELECTOR DE ALUMNOS
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(24.dp))
                            .background(CognokinetColors.Parchment, RoundedCornerShape(24.dp))
                            .border(2.dp, CognokinetColors.EarthWood, RoundedCornerShape(24.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            // Header Alumno Activo
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(CognokinetColors.ForestLight, CircleShape)
                                        .border(2.dp, CognokinetColors.Forest, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(StudentAvatars.getEmoji(activeProfile.avatarId), fontSize = 32.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(activeProfile.name, color = CognokinetColors.Graphite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text("${activeProfile.age} años · ${activeProfile.grade}", color = Color(0xFF5C3A21), fontSize = 12.sp)
                                    Text("Nivel ${activeProgress.level} · ${activeProgress.xp} XP", color = CognokinetColors.Forest, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Selector de Alumnos Registrados
                            if (appState.studentProfiles.size > 1) {
                                Text("Cambiar de Alumno Activo:", color = Color(0xFF5C3A21), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    appState.studentProfiles.forEach { profile ->
                                        val isSelected = profile.id == appState.activeProfileId
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isSelected) CognokinetColors.Forest else Color(0xFFE8E2D0),
                                                    CircleShape
                                                )
                                                .clickable { appState.activeProfileId = profile.id }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "${StudentAvatars.getEmoji(profile.avatarId)} ${profile.name}",
                                                color = if (isSelected) Color.White else Color(0xFF5C3A21),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                            }

                            // Botones de Edición / Borrado
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        editingProfileId = activeProfile.id
                                        nameInput = activeProfile.name
                                        ageInput = activeProfile.age.toString()
                                        gradeInput = activeProfile.grade
                                        selectedAvatarId = activeProfile.avatarId
                                        showValidationError = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C6D58)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("✏️ Editar", color = Color.White, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { showDeleteConfirm = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("🗑️ Borrar", color = Color.White, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Nuevo Registro
                            Button(
                                onClick = {
                                    nameInput = ""
                                    ageInput = "8"
                                    gradeInput = "3º Grado"
                                    showValidationError = false
                                    isCreatingNew = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8E2D0)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("➕ Registrar Nuevo Alumno", color = Color(0xFF5C3A21), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Ir al Sendero
                            Button(
                                onClick = onGoToMap,
                                colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Text("🏃 Ir al Sendero de Estaciones ➔", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Modal de Confirmación de Borrado
        if (showDeleteConfirm && activeProfile != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Eliminar Perfil") },
                text = { Text("¿Deseas eliminar el perfil de '${activeProfile.name}'? Se borrarán sus datos guardados localmente.") },
                confirmButton = {
                    Button(
                        onClick = {
                            appState.deleteStudentProfile(activeProfile.id)
                            showDeleteConfirm = false
                            if (appState.studentProfiles.isEmpty()) {
                                isCreatingNew = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A))
                    ) {
                        Text("Eliminar", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
