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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
    var selectedTab by remember { mutableStateOf("motriz") } // "general" or "motriz"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEDF7EE))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. TOP HEADER BAR
            SettingsHeaderBar(appState = appState)

            // 2. MAIN SCROLLABLE CONTENT
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                                .padding(14.dp)
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
                                            .size(40.dp)
                                            .background(CognokinetColors.Forest, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🌲", fontSize = 20.sp)
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Ajustes del Sendero", color = CognokinetColors.Graphite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        Text("Configuración Kinemática y Parámetros 3JS", color = Color(0xFF5C3A21), fontSize = 11.sp)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFF5C3A21), CircleShape)
                                        .clickable { onBack() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("✕", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Pestañas (General vs Coordinación AR)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8E2D0).copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TabButton(
                                text = "🔊 General",
                                isSelected = selectedTab == "general",
                                onClick = { selectedTab = "general" },
                                modifier = Modifier.weight(1f)
                            )
                            TabButton(
                                text = "🏃 Coordinación AR",
                                isSelected = selectedTab == "motriz",
                                onClick = { selectedTab = "motriz" },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Cuerpo según la pestaña seleccionada
                        Column(modifier = Modifier.padding(14.dp)) {
                            if (selectedTab == "general") {
                                GeneralSettingsContent()
                            } else {
                                MotrizSettingsContent(appState = appState, onSave = onBack)
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
private fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
            .background(
                if (isSelected) Color(0xFFF6F3E8) else Color(0xFFE8E2D0),
                RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
            )
            .border(
                1.dp,
                if (isSelected) CognokinetColors.EarthWood else Color.Transparent,
                RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) CognokinetColors.ForestDark else Color(0xFF5C3A21),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GeneralSettingsContent() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(14.dp))
                .border(1.dp, CognokinetColors.EarthWood.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SwitchRow(title = "🌲 Sonidos del Bosque (Música)", checked = true, onCheckedChange = {})
                SwitchRow(title = "🔊 Efectos de Movimiento", checked = true, onCheckedChange = {})
            }
        }
    }
}

@Composable
private fun MotrizSettingsContent(appState: CognokinetAppState, onSave: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // TARJETA MOTOR 3JS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(14.dp))
                .border(2.dp, CognokinetColors.Forest.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CognokinetColors.Forest)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("📊 Variables de Coordinación - Motor 3JS", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SliderRow(
                        title = "🚀 Velocidad de Estímulos",
                        valueText = "${appState.stimulusSpeed}x",
                        value = appState.stimulusSpeed,
                        range = 0.5f..2.0f,
                        onValueChange = { appState.stimulusSpeed = (it * 10).toInt() / 10f }
                    )

                    SliderRow(
                        title = "🎯 Precisión de Salto",
                        valueText = "±${appState.jumpPrecisionCm} cm",
                        value = appState.jumpPrecisionCm.toFloat(),
                        range = 5f..25f,
                        onValueChange = { appState.jumpPrecisionCm = it.toInt() }
                    )

                    Column {
                        Text("📐 Estímulos Simultáneos", color = CognokinetColors.Graphite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TargetPill("1 Hito", "Básico", appState.simultaneousTargets == 1) { appState.simultaneousTargets = 1 }
                            TargetPill("2 Hitos", "Recomendado", appState.simultaneousTargets == 2) { appState.simultaneousTargets = 2 }
                            TargetPill("4 Hitos", "Desafío", appState.simultaneousTargets == 4) { appState.simultaneousTargets = 4 }
                        }
                    }
                }
            }
        }

        // BIOMECÁNICA DE CAMPO SWITCHES (con textos flexibles que no se cortan)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                .border(1.dp, CognokinetColors.EarthWood.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("🌿 MODOS Y BIOMECÁNICA DE CAMPO", color = Color(0xFF5C3A21), fontSize = 12.sp, fontWeight = FontWeight.Bold)

                SwitchRow("🧘 Modo Calma / Inhibición Motriz", appState.calmModeActive) { appState.calmModeActive = it }
                SwitchRow("🏫 Modo Aula / Reducido (1.5 m²)", appState.classroomModeActive) { appState.classroomModeActive = it }
                SwitchRow("↔️ Evaluación de Lateralidad (Izquierda / Derecha)", appState.lateralityEvaluationActive) { appState.lateralityEvaluationActive = it }
                SwitchRow("🧠 Conteo Esquelético IA Articular", appState.aiSkeletonTrackingActive) { appState.aiSkeletonTrackingActive = it }
            }
        }

        // BOTONES ACCIÓN
        Button(
            onClick = {
                appState.stimulusSpeed = 1.0f
                appState.jumpPrecisionCm = 15
                appState.simultaneousTargets = 2
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF6F3E8)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .border(1.dp, CognokinetColors.EarthWood.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
        ) {
            Text("🔄 Restablecer a Valores Clínicos por Defecto", color = Color(0xFF5C3A21), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = onSave,
            colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
        ) {
            Text("✓ Aplicar al Próximo Ejercicio", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = CognokinetColors.Graphite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
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

@Composable
private fun SliderRow(
    title: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = CognokinetColors.Graphite, fontSize = 13.sp, fontWeight = FontWeight.Bold)

            Box(
                modifier = Modifier
                    .background(Color(0xFFE8F5E9), CircleShape)
                    .border(1.dp, CognokinetColors.Forest.copy(alpha = 0.3f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(valueText, color = CognokinetColors.ForestDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = CognokinetColors.Forest,
                activeTrackColor = CognokinetColors.Forest
            )
        )
    }
}

@Composable
private fun TargetPill(title: String, subtitle: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .heightIn(min = 44.dp)
            .background(
                if (isSelected) CognokinetColors.Forest else Color(0xFFF6F3E8),
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                if (isSelected) CognokinetColors.ForestDark else CognokinetColors.EarthWood.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = if (isSelected) Color.White else Color(0xFF5C3A21), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = if (isSelected) Color(0xFFE8F5E9) else Color(0xFF5C3A21).copy(alpha = 0.7f), fontSize = 9.sp)
        }
    }
}
