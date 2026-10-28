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
import com.example.ar.cognokinet.domain.IdGenerator
import com.example.ar.cognokinet.domain.Rubric
import com.example.ar.cognokinet.domain.RubricCriterion
import com.example.ar.cognokinet.domain.RubricLevel
import kotlin.math.abs

@Composable
fun RubricEditorScreen(
    classId: String = "class_default",
    stationId: Int = 1,
    initialRubric: Rubric? = null,
    onSaveRubric: (Rubric) -> Unit,
    onResetToDefault: () -> Unit,
    onBack: () -> Unit
) {
    var selectedStation by remember { mutableStateOf(stationId.coerceIn(1, 5)) }

    // Active Rubric State
    var currentRubric by remember(selectedStation, initialRubric) {
        mutableStateOf(initialRubric ?: RubricDefaults.forStation(selectedStation, classId))
    }

    var showResetDialog by remember { mutableStateOf(false) }

    val totalWeight = currentRubric.criteria.sumOf { it.weight }
    val isWeightValid = abs(totalWeight - 100.0) < 0.1

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

                        Text("Editor de Rúbricas por Estación", color = CognokinetColors.Graphite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Text("📝", fontSize = 22.sp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Station Selector Tabs (1..5)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..5).forEach { st ->
                        val isSel = st == selectedStation
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .background(
                                    if (isSel) CognokinetColors.Forest else Color(0xFFE8E2D0),
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSel) CognokinetColors.ForestDark else Color(0xFFD5C29E),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedStation = st
                                    currentRubric = RubricDefaults.forStation(st, classId)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Estación $st",
                                color = if (isSel) Color.White else Color(0xFF5C3A21),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Exercise Info Header
                val exercise = ExerciseCatalog.getById(selectedStation)
                Card(
                    colors = CardDefaults.cardColors(containerColor = CognokinetColors.Parchment),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "Estación $selectedStation: ${exercise.name}",
                            color = CognokinetColors.Graphite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(exercise.description, color = Color(0xFF5C3A21), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Weight Indicator Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isWeightValid) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (isWeightValid) Color(0xFF4CAF50) else Color(0xFFE53935),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isWeightValid) "✓ Suma de pesos válida" else "⚠️ La suma de pesos debe ser exactamente 100%",
                            color = if (isWeightValid) Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "${totalWeight.toInt()}%",
                            color = if (isWeightValid) Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Criteria List Editor
                Text("Criterios de Evaluación (${currentRubric.criteria.size}/6):", color = CognokinetColors.Graphite, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                currentRubric.criteria.forEachIndexed { cIndex, criterion ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Criterio ${cIndex + 1}",
                                    color = CognokinetColors.Forest,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                if (currentRubric.criteria.size > 1) {
                                    IconButton(
                                        onClick = {
                                            val updatedList = currentRubric.criteria.filterIndexed { idx, _ -> idx != cIndex }
                                            currentRubric = currentRubric.copy(criteria = updatedList)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Text("🗑️", fontSize = 16.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = criterion.name,
                                onValueChange = { newName ->
                                    val updatedCriteria = currentRubric.criteria.toMutableList()
                                    updatedCriteria[cIndex] = criterion.copy(name = newName)
                                    currentRubric = currentRubric.copy(criteria = updatedCriteria)
                                },
                                label = { Text("Nombre del Criterio") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Peso (%):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5C3A21))
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = criterion.weight.toInt().toString(),
                                    onValueChange = { weightStr ->
                                        val newW = weightStr.toDoubleOrNull() ?: 0.0
                                        val updatedCriteria = currentRubric.criteria.toMutableList()
                                        updatedCriteria[cIndex] = criterion.copy(weight = newW)
                                        currentRubric = currentRubric.copy(criteria = updatedCriteria)
                                    },
                                    modifier = Modifier.width(90.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Niveles de Desempeño (1 a 4):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5C3A21))

                            criterion.levels.forEachIndexed { lIndex, level ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("${level.points} pts - ${level.label}: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CognokinetColors.Forest)
                                    OutlinedTextField(
                                        value = level.descriptor,
                                        onValueChange = { newDesc ->
                                            val updatedLevels = criterion.levels.toMutableList()
                                            updatedLevels[lIndex] = level.copy(descriptor = newDesc)
                                            val updatedCriteria = currentRubric.criteria.toMutableList()
                                            updatedCriteria[cIndex] = criterion.copy(levels = updatedLevels)
                                            currentRubric = currentRubric.copy(criteria = updatedCriteria)
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Add Criterion Button
                if (currentRubric.criteria.size < 6) {
                    Button(
                        onClick = {
                            val newCriterion = RubricCriterion(
                                id = IdGenerator.newId(),
                                name = "Nuevo Criterio",
                                weight = 0.0,
                                levels = listOf(
                                    RubricLevel(1, "Inicial", "Nivel inicial"),
                                    RubricLevel(2, "En proceso", "En proceso de logro"),
                                    RubricLevel(3, "Logrado", "Logra la técnica correctamente"),
                                    RubricLevel(4, "Destacado", "Muestra nivel destacado")
                                )
                            )
                            currentRubric = currentRubric.copy(criteria = currentRubric.criteria + newCriterion)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8E2D0)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("➕ Añadir Criterio", color = Color(0xFF5C3A21), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("🔄 Restablecer", fontSize = 13.sp, color = Color(0xFF5C3A21))
                    }

                    Button(
                        onClick = {
                            if (isWeightValid) {
                                onSaveRubric(currentRubric)
                            }
                        },
                        enabled = isWeightValid,
                        colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("💾 Guardar Rúbrica", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Confirmation Reset Dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Restablecer Rúbrica") },
                text = { Text("¿Deseas volver a cargar los criterios por defecto para la Estación $selectedStation?") },
                confirmButton = {
                    Button(
                        onClick = {
                            currentRubric = RubricDefaults.forStation(selectedStation, classId)
                            onResetToDefault()
                            showResetDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A))
                    ) {
                        Text("Restablecer", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
