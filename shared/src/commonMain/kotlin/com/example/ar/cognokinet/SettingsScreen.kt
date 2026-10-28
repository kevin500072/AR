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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    appState: CognokinetAppState,
    onBack: () -> Unit
) {
    var soundEffectsEnabled by remember { mutableStateOf(true) }
    var hapticFeedbackEnabled by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEDF7EE))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. TOP HEADER BAR
            SettingsHeaderBar(appState = appState)

            // 2. MAIN CONTENT
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(20.dp))
                        .background(Color(0xFFF6F3E8), RoundedCornerShape(20.dp))
                        .border(2.dp, CognokinetColors.EarthWood, RoundedCornerShape(20.dp))
                ) {
                    Column {
                        // Cabecera del Panel
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFEFE9D7))
                                .border(1.dp, CognokinetColors.EarthWood.copy(alpha = 0.3f))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(CognokinetColors.Forest, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("⚙️", fontSize = 22.sp)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Ajustes Generales",
                                            color = CognokinetColors.Graphite,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Configuración de Sonido y Audio del Juego",
                                            color = Color(0xFF5C3A21),
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(Color(0xFF5C3A21), CircleShape)
                                        .clickable { onBack() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("✕", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Opciones Simplificadas de Sonido
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(16.dp))
                                    .border(1.dp, CognokinetColors.EarthWood.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text(
                                        "🔊 AUDIO Y MÚSICA",
                                        color = Color(0xFF5C3A21),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    SwitchRow(
                                        title = "🌲 Sonido del Bosque (Música)",
                                        subtitle = "Música de fondo del sendero",
                                        checked = appState.isMusicEnabled,
                                        onCheckedChange = { appState.isMusicEnabled = it }
                                    )

                                    SwitchRow(
                                        title = "🔔 Efectos de Sonido (SFX)",
                                        subtitle = "Sonidos de botones y celebraciones",
                                        checked = soundEffectsEnabled,
                                        onCheckedChange = { soundEffectsEnabled = it }
                                    )

                                    SwitchRow(
                                        title = "📳 Respuesta Háptica",
                                        subtitle = "Vibración en toques y alertas",
                                        checked = hapticFeedbackEnabled,
                                        onCheckedChange = { hapticFeedbackEnabled = it }
                                    )
                                }
                            }

                            Button(
                                onClick = onBack,
                                colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text("✓ Guardar y Volver", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SettingsHeaderBar(appState: CognokinetAppState) {
    val progress = appState.activeProgress

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEDF7EE).copy(alpha = 0.9f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PillStat("🍃", "${progress.leaves}", CognokinetColors.Forest)
                PillStat("🌰", "${progress.acorns}", CognokinetColors.EarthWood)
                PillStat("🧭", "Niv. ${progress.level}", CognokinetColors.ForestDark)
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(CognokinetColors.Forest, CircleShape)
                    .border(2.dp, CognokinetColors.EarthWood, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🥾", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun PillStat(icon: String, text: String, color: Color) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .background(Color(0xFFF6F3E8), CircleShape)
            .border(1.dp, CognokinetColors.EarthWood.copy(alpha = 0.3f), CircleShape)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = CognokinetColors.Graphite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = CognokinetColors.Forest
            )
        )
    }
}
