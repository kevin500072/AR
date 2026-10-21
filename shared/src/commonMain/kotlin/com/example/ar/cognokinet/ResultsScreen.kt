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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResultsScreen(
    onContinueToMap: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CognokinetColors.NatureBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. BARRA SUPERIOR
            ResultsHeaderBar(onBack = onBack)

            // 2. CONTENIDO DESPLAZABLE
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pestañas Internas (Resultados / Premios)
                Row(
                    modifier = Modifier
                        .background(Color(0xFFEAE5D7), CircleShape)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(CognokinetColors.Forest, CircleShape)
                            .padding(horizontal = 18.dp, vertical = 6.dp)
                    ) {
                        Text("✓ Resultados", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 18.dp, vertical = 6.dp)
                    ) {
                        Text("🎖️ Premios del Sendero", color = Color(0xFF5B685E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Banner de Celebración
                Box(
                    modifier = Modifier
                        .background(Color(0xFFD1C4E9).copy(alpha = 0.5f), CircleShape)
                        .border(1.dp, CognokinetColors.Forest.copy(alpha = 0.3f), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("🌿 EVALUACIÓN 3JS COMPLETADA", color = CognokinetColors.ForestDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "¡Primer Reto Superado!",
                    color = CognokinetColors.Graphite,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Estación 1: Salto Vertical Completada",
                    color = Color(0xFF4F6354),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. GRAN MEDALLA CENTRAL DE MADERA NOBLE Y PIEDRA DE RÍO
                NobleWoodMedal()

                Spacer(modifier = Modifier.height(16.dp))

                // 4. PROGRESS BAR DE EXPERIENCIA (XP)
                XpProgressCard()

                Spacer(modifier = Modifier.height(14.dp))

                // 5. CUADRÍCULA BENTO 2x2 DE MÉTRICAS FISIOKINÉTICAS
                BentoMetricsGrid()

                Spacer(modifier = Modifier.height(14.dp))

                // 6. MENSAJE DEL ZORRO GUÍA
                FoxGuideMessageCard()

                Spacer(modifier = Modifier.height(16.dp))

                // 7. BOTONES INFERIORES DE ACCIÓN
                Button(
                    onClick = onContinueToMap,
                    colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Continuar al Mapa ➔", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onContinueToMap,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C6D58)),
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("🗺️ Volver al Sendero", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(2.dp, CircleShape)
                            .background(Color(0xFFFCFBF8), CircleShape)
                            .border(1.dp, Color(0xFFDDD5C5), CircleShape)
                            .clickable { onContinueToMap() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔄", fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ResultsHeaderBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CognokinetColors.NatureBg.copy(alpha = 0.95f))
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
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color(0xFFA5D6A7), CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("←", color = CognokinetColors.Forest, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text("Sesión Motriz al Aire Libre", color = CognokinetColors.Graphite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(CognokinetColors.Forest, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🌲", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun NobleWoodMedal() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Medallón Central
        Box(
            modifier = Modifier
                .size(160.dp)
                .shadow(12.dp, CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF7A4E2D), Color(0xFF5D3A20), Color(0xFF3E2311))
                    ),
                    CircleShape
                )
                .border(3.dp, Color(0xFF4A2D16), CircleShape)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            // Anillo interior
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFEFE9DB), Color(0xFFDFD6C2), Color(0xFFCAC0A8))
                        ),
                        CircleShape
                    )
                    .border(1.dp, Color(0xFFC5B89C), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Círculo Hoja
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .shadow(4.dp, CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(CognokinetColors.Forest, Color(0xFF34D399))
                                ),
                                CircleShape
                            )
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍃", fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("★★★", color = CognokinetColors.SunAmber, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Placa Colgante
        Box(
            modifier = Modifier
                .shadow(6.dp, CircleShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF6D4C41), Color(0xFF5D4037), Color(0xFF4E342E))
                    ),
                    CircleShape
                )
                .border(1.dp, Color(0xFF8D6E63), CircleShape)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text("⭐ Rango Explorador Principiante · Nivel 1 (100/500 XP)", color = Color(0xFFFFECB3), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text("Insignia de Roble Ganada", color = CognokinetColors.Graphite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text("Evaluación Biomecánica 3JS", color = Color(0xFF6A786E), fontSize = 11.sp)
    }
}

@Composable
private fun XpProgressCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(18.dp))
            .background(CognokinetColors.NatureBgSoft, RoundedCornerShape(18.dp))
            .border(2.dp, Color(0xFFD7CCC8), RoundedCornerShape(18.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🌱 ", fontSize = 16.sp)
                Text("Progreso de Coordinación", color = CognokinetColors.Graphite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .background(CognokinetColors.SunAmber.copy(alpha = 0.3f), CircleShape)
                    .border(1.dp, CognokinetColors.SunAmber, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("✦ +100 XP", color = Color(0xFF5D4037), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(CircleShape)
                .background(Color(0xFFE6DFD1))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.2f)
                    .background(
                        Brush.horizontalGradient(
                            listOf(CognokinetColors.ForestDark, CognokinetColors.Forest, CognokinetColors.LeafGreen)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("100 XP", color = Color(0xFF68766C), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("Meta: 500 XP", color = CognokinetColors.Forest, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BentoMetricsGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("PRECISIÓN", "94%", "Excelente", CognokinetColors.LeafGreen, modifier = Modifier.weight(1f))
            MetricCard("TIEMPO", "1:47", "Minutos", CognokinetColors.RiverBlue, modifier = Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("SALTOS TOTALES", "18", "Sin pausas", CognokinetColors.Forest, modifier = Modifier.weight(1f))
            MetricCard("ATERRIZAJE", "A+", "Estabilidad Óptima", CognokinetColors.SunAmber, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    status: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .background(Color(0xFFFCFBF8), RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFE8E1D3), RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(label, color = Color(0xFF4F6354), fontSize = 11.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(4.dp))

            Text(value, color = color, fontSize = 26.sp, fontWeight = FontWeight.Black)

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .background(color.copy(alpha = 0.15f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(status, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FoxGuideMessageCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp))
            .background(CognokinetColors.NatureBgSoft, RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFFE2DCCE), RoundedCornerShape(18.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(Color(0xFFE67E22), RoundedCornerShape(16.dp))
                    .border(2.dp, Color.White, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🦊", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text("¡El Zorro Guía está asombrado! 🧣", color = CognokinetColors.Graphite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("¡Gran comienzo! Has desbloqueado la Estación 2 en el mapa y tu primera insignia.", color = Color(0xFF4D5A50), fontSize = 11.sp)
            }
        }
    }
}
