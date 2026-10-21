package com.example.ar.cognokinet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InicioScreen(
    appState: CognokinetAppState,
    onNavigateToStudentSetup: () -> Unit,
    onNavigateToTeacherSetup: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val bgSkyGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFDF7),
            Color(0xFFFBF5E5),
            CognokinetColors.NatureBg
        )
    )

    val hasActiveProfile = appState.activeProfile != null
    val primaryButtonText = if (hasActiveProfile) "Continuar" else "Crear Explorador"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgSkyGradient)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. CABECERA: LOGO & SUPERVISIÓN
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Tarjeta de Título Principal CognoKinet
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .shadow(10.dp, RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(24.dp))
                        .border(1.dp, Color(0xFFE8E2D2), RoundedCornerShape(24.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Ícono de Montaña y Hoja
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFEAF5EB))
                                .border(1.dp, Color(0xFFCDE5CF), RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⛰️", fontSize = 28.sp)

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .size(22.dp)
                                    .background(CognokinetColors.Forest, CircleShape)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🍃", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Título CognoKinet
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Cogno",
                                color = CognokinetColors.ForestDeep,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Kinet",
                                color = CognokinetColors.Forest,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Badge de Evaluación Motriz RA
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF3EFE4), RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFFE3DCCB), RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🥽 Evaluación y Rehabilitación Motriz RA",
                                color = Color(0xFF526354),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Aviso de Supervisión Médica / Adulto
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .background(Color(0xFFEAF5EB), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFCDE5CF), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🛡️ ", fontSize = 16.sp)
                        Text(
                            text = "Requiere supervisión de un adulto y espacio libre (2x2m)",
                            color = Color(0xFF234326),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 2. BOTONES PRINCIPALES DE ACCIÓN Y ROLES
            Column(
                modifier = Modifier.fillMaxWidth(0.88f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Selector de Perfil si existen varios alumnos
                if (appState.studentProfiles.size > 1) {
                    Text("Seleccionar Alumno Activo:", color = Color(0xFF5C3A21), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        appState.studentProfiles.forEach { profile ->
                            val isSel = profile.id == appState.activeProfileId
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSel) CognokinetColors.Forest else Color.White,
                                        CircleShape
                                    )
                                    .border(1.dp, if (isSel) CognokinetColors.ForestDark else Color(0xFFDCD5C5), CircleShape)
                                    .clickable { appState.activeProfileId = profile.id }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${StudentAvatars.getEmoji(profile.avatarId)} ${profile.name}",
                                    color = if (isSel) Color.White else Color(0xFF5C3A21),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Botón Principal (Crear Explorador / Continuar)
                Action3DButton(
                    text = primaryButtonText,
                    icon = "🎒",
                    bgColor = CognokinetColors.Forest,
                    bevelColor = CognokinetColors.ForestDark,
                    textColor = Color.White,
                    onClick = {
                        if (hasActiveProfile) {
                            onNavigateToMap()
                        } else {
                            onNavigateToStudentSetup()
                        }
                    }
                )

                // Botón Secundario: Soy Profesor
                Action3DButton(
                    text = "Soy Profesor",
                    icon = "🎓",
                    bgColor = CognokinetColors.SunAmber,
                    bevelColor = CognokinetColors.SunDark,
                    textColor = CognokinetColors.Graphite,
                    onClick = onNavigateToTeacherSetup
                )
            }

            // 3. SECCIÓN INFERIOR: BOTÓN DE AJUSTES & VERSIÓN PROTOCOLO
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(0.88f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .alpha(if (hasActiveProfile) 1.0f else 0.4f)
                            .shadow(6.dp, RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF9C6D47), Color(0xFF7B4E2E))
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .border(1.dp, Color(0xFFB38760), RoundedCornerShape(16.dp))
                            .clickable {
                                if (hasActiveProfile) {
                                    onNavigateToSettings()
                                } else {
                                    appState.snackbarMessage = "Primero crea tu perfil"
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚙️", fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Versión de Protocolo Fisiokinético v1
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✓ ", color = CognokinetColors.Forest, fontSize = 12.sp)
                    Text(
                        text = "Protocolo Fisiokinético Infantil Ver. ${CognokinetConstants.PROTOCOL_VERSION}",
                        color = Color(0xFF4B5E50),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // SNACKBAR PARA AVISO
        if (appState.snackbarMessage != null) {
            Snackbar(
                action = {
                    TextButton(onClick = { appState.snackbarMessage = null }) {
                        Text("OK", color = Color.White)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(appState.snackbarMessage ?: "", color = Color.White)
            }
        }
    }
}

@Composable
private fun Action3DButton(
    text: String,
    icon: String,
    bgColor: Color,
    bevelColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .shadow(6.dp, RoundedCornerShape(22.dp))
            .background(bgColor, RoundedCornerShape(22.dp))
            .border(2.dp, bevelColor, RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.CenterStart
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
                        .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = text,
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text("➔", color = textColor.copy(alpha = 0.8f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
