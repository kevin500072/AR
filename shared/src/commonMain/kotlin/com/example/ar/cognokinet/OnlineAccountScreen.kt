package com.example.ar.cognokinet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun OnlineAccountScreen(appState: CognokinetAppState, onBack: () -> Unit) {
    var role by remember { mutableStateOf("STUDENT") }
    var joinCode by remember { mutableStateOf("") }
    var studentName by remember { mutableStateOf("") }
    var studentAge by remember { mutableStateOf("8") }
    var grade by remember { mutableStateOf("") }
    var consent by remember { mutableStateOf(false) }
    var returningAccount by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize().background(CognokinetColors.SurfaceGreen).padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Cuenta de clase", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = CognokinetColors.ForestDeep)
            Spacer(Modifier.height(8.dp))
            Text("Usa Google para crear una identidad única. Tu progreso de juego permanece disponible localmente.", color = CognokinetColors.Graphite)
            Spacer(Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { role = "STUDENT" }, colors = ButtonDefaults.buttonColors(containerColor = if (role == "STUDENT") CognokinetColors.Forest else Color.Gray)) {
                    Text("Alumno")
                }
                Button(onClick = { role = "TEACHER" }, colors = ButtonDefaults.buttonColors(containerColor = if (role == "TEACHER") CognokinetColors.Forest else Color.Gray)) {
                    Text("Profesor")
                }
            }

            if (role == "STUDENT") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = returningAccount, onCheckedChange = { returningAccount = it })
                    Text("Ya tengo una cuenta de alumno")
                }
                if (returningAccount) {
                    Text("Inicia sesión con la misma cuenta Google para recuperar tu perfil y progreso.")
                } else {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(studentName, { studentName = it }, label = { Text("Nombre del alumno") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(studentAge, { studentAge = it.filter(Char::isDigit).take(2) }, label = { Text("Edad (3 a 18)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(grade, { grade = it }, label = { Text("Grado y grupo") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(joinCode, { joinCode = it.uppercase().take(6) }, label = { Text("Código de clase") }, modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = consent, onCheckedChange = { consent = it })
                        Text("Tengo autorización de mi madre, padre o tutor.")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            GoogleSignInButton(
                onIdToken = { idToken ->
                    if (role == "STUDENT" && !returningAccount &&
                        (studentName.isBlank() || grade.isBlank() || joinCode.length != 6 ||
                            (studentAge.toIntOrNull() ?: 0) !in 3..18 || !consent)
                    ) {
                        message = "Completa los datos y el consentimiento para unirte a una clase."
                    } else {
                        loading = true
                        message = null
                        scope.launch {
                            try {
                                val result = OnlineClassApi.signInWithGoogle(
                                    OnlineGoogleLoginRequest(
                                        idToken = idToken,
                                        role = role,
                                        joinCode = joinCode,
                                        displayName = if (returningAccount) "" else studentName,
                                        age = if (returningAccount) 0 else (studentAge.toIntOrNull() ?: 8),
                                        grade = if (returningAccount) "" else grade,
                                        avatarId = "fox",
                                        consentGiven = consent
                                    )
                                )
                                appState.onlineAuthToken = result.token
                                appState.onlineUserId = result.userId
                                appState.onlineRole = result.role
                                appState.onlineClassId = result.classId
                                appState.activeRole = result.role
                                if (result.role == "STUDENT") {
                                    val profile = com.example.ar.cognokinet.domain.StudentProfile(
                                        id = result.userId,
                                        displayName = result.displayName.ifBlank { studentName.ifBlank { "Explorador" } },
                                        age = result.age.takeIf { it > 0 } ?: (studentAge.toIntOrNull() ?: 8),
                                        grade = result.grade.ifBlank { grade.ifBlank { "Mi clase" } },
                                        avatarId = result.avatarId.ifBlank { "fox" },
                                        consentGiven = result.consentGiven || consent
                                    )
                                    val localIndex = appState.studentProfiles.indexOfFirst { it.id == result.userId }
                                    if (localIndex >= 0) appState.studentProfiles[localIndex] = profile
                                    else appState.studentProfiles.add(profile)
                                    appState.progressMap[result.userId] = result.progress
                                        ?: com.example.ar.cognokinet.domain.StudentProgress.inicial(result.userId)
                                    appState.activeProfileId = result.userId
                                    appState.navigateTo(CognokinetScreen.HOME)
                                } else {
                                    appState.teacherName = result.displayName.ifBlank { appState.teacherName }
                                    appState.navigateTo(CognokinetScreen.TEACHER_DASHBOARD)
                                }
                            } catch (error: Exception) {
                                message = error.message ?: "No se pudo conectar con el servidor de clase."
                            } finally {
                                loading = false
                            }
                        }
                    }
                },
                onError = { message = it },
                modifier = Modifier.fillMaxWidth()
            )
            if (loading) Text("Conectando con Google y el servidor…", modifier = Modifier.padding(top = 10.dp))
            message?.let { Text(it, color = Color(0xFFB3261E), modifier = Modifier.padding(top = 10.dp)) }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onBack) { Text("Volver") }
        }
    }
}
