package com.example.ar.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Renderiza la figura gráfica 3D y brillante de cada caramelo.
 */
@Composable
fun CandyView(
    candy: Candy,
    isSelected: Boolean,
    isHinted: Boolean,
    modifier: Modifier = Modifier
) {
    // Animación de pulso para caramelo seleccionado
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val currentScale = if (isSelected) pulseScale else 1.0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .scale(currentScale),
        contentAlignment = Alignment.Center
    ) {
        // Fondo / Resplandor si está seleccionado
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.95f)
                    .clip(CircleShape)
                    .background(Color(0x88FFEB3B))
                    .border(3.dp, Color(0xFFFFEB3B), CircleShape)
            )
        }

        // Borde indicador de pista
        if (isHinted) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.95f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(3.dp, Color(0xFF00E676), RoundedCornerShape(12.dp))
            )
        }

        // Dibujo vectorial del caramelo con brillo 3D
        Canvas(modifier = Modifier.fillMaxSize(0.82f)) {
            val type = candy.type
            val width = size.width
            val height = size.height

            val gradient = Brush.radialGradient(
                colors = listOf(type.glossColor, type.primaryColor, type.secondaryColor),
                center = Offset(width * 0.35f, height * 0.35f),
                radius = width * 0.75f
            )

            when (type) {
                CandyType.RED -> drawHeartCandy(gradient, type.glossColor)
                CandyType.BLUE -> drawBerryCandy(gradient, type.glossColor)
                CandyType.GREEN -> drawAppleCandy(gradient, type.glossColor)
                CandyType.YELLOW -> drawLemonCandy(gradient, type.glossColor)
                CandyType.PURPLE -> drawGrapeCandy(gradient, type.glossColor)
                CandyType.ORANGE -> drawOrangeCandy(gradient, type.glossColor)
            }
        }
    }
}

private fun DrawScope.drawBerryCandy(gradient: Brush, glossColor: Color) {
    val radius = size.minDimension / 2f
    val center = Offset(size.width / 2f, size.height / 2f)

    drawCircle(brush = gradient, radius = radius, center = center)
    // Brillo superior brillante
    drawCircle(
        color = glossColor.copy(alpha = 0.8f),
        radius = radius * 0.28f,
        center = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f)
    )
}

private fun DrawScope.drawAppleCandy(gradient: Brush, glossColor: Color) {
    val corner = size.width * 0.25f
    drawRoundRect(
        brush = gradient,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner)
    )
    // Reflejo diagonal de brillo
    val path = Path().apply {
        moveTo(size.width * 0.2f, size.height * 0.2f)
        lineTo(size.width * 0.6f, size.height * 0.2f)
        lineTo(size.width * 0.2f, size.height * 0.6f)
        close()
    }
    drawPath(path, glossColor.copy(alpha = 0.5f))
}

private fun DrawScope.drawLemonCandy(gradient: Brush, glossColor: Color) {
    val path = Path().apply {
        moveTo(size.width / 2f, 0f)
        lineTo(size.width, size.height / 2f)
        lineTo(size.width / 2f, size.height)
        lineTo(0f, size.height / 2f)
        close()
    }
    drawPath(path, gradient)

    // Brillo central
    drawCircle(
        color = glossColor.copy(alpha = 0.75f),
        radius = size.width * 0.18f,
        center = Offset(size.width * 0.35f, size.height * 0.35f)
    )
}

private fun DrawScope.drawGrapeCandy(gradient: Brush, glossColor: Color) {
    val radiusX = size.width * 0.45f
    val radiusY = size.height * 0.38f
    drawOval(
        brush = gradient,
        topLeft = Offset((size.width - radiusX * 2) / 2f, (size.height - radiusY * 2) / 2f),
        size = Size(radiusX * 2, radiusY * 2)
    )
    drawCircle(
        color = glossColor.copy(alpha = 0.8f),
        radius = radiusX * 0.25f,
        center = Offset(size.width * 0.35f, size.height * 0.35f)
    )
}

private fun DrawScope.drawOrangeCandy(gradient: Brush, glossColor: Color) {
    val path = Path().apply {
        moveTo(size.width / 2f, size.height * 0.05f)
        lineTo(size.width * 0.95f, size.height * 0.85f)
        lineTo(size.width * 0.05f, size.height * 0.85f)
        close()
    }
    drawPath(path, gradient)
    drawCircle(
        color = glossColor.copy(alpha = 0.8f),
        radius = size.width * 0.15f,
        center = Offset(size.width * 0.5f, size.height * 0.45f)
    )
}

private fun DrawScope.drawHeartCandy(gradient: Brush, glossColor: Color) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w / 2f, h * 0.8f)
        cubicTo(w * 0.1f, h * 0.5f, 0f, h * 0.25f, w * 0.25f, h * 0.1f)
        cubicTo(w * 0.45f, 0f, w / 2f, h * 0.25f, w / 2f, h * 0.25f)
        cubicTo(w / 2f, h * 0.25f, w * 0.55f, 0f, w * 0.75f, h * 0.1f)
        cubicTo(w, h * 0.25f, w * 0.9f, h * 0.5f, w / 2f, h * 0.8f)
        close()
    }
    drawPath(path, gradient)
    drawCircle(
        color = glossColor.copy(alpha = 0.75f),
        radius = w * 0.12f,
        center = Offset(w * 0.32f, h * 0.25f)
    )
}

/**
 * Barra superior de estado (Nivel, Puntuación, Barra de Estrellas).
 */
@Composable
fun TopStatsBar(
    level: Int,
    score: Int,
    targetScore: Int,
    movesLeft: Int,
    onPauseClicked: () -> Unit
) {
    val progress = (score.toFloat() / targetScore.toFloat()).coerceIn(0f, 1f)
    val star1 = progress >= 0.33f
    val star2 = progress >= 0.66f
    val star3 = progress >= 1.0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Insignia de Nivel y Puntuación
        Column(horizontalAlignment = Alignment.Start) {
            Box(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFF4081), Color(0xFFAB47BC))
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "NIVEL $level",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Score: $score",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        // Contador de Movimientos en circulo caramelizado
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(6.dp, CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFFFCA28), Color(0xFFF57F17))
                    ),
                    CircleShape
                )
                .border(3.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$movesLeft",
                    color = Color(0xFF3E2723),
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
                Text(
                    text = "MOVS",
                    color = Color(0xFF5D4037),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }

        // Botón de Pausa
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(4.dp, CircleShape)
                .background(Color(0x88000000), CircleShape)
                .clickable { onPauseClicked() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⏸️",
                fontSize = 20.sp
            )
        }
    }

    // Barra de Progreso con Estrellas
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🎯 $targetScore", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(18.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x66000000))
                .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
        ) {
            // Relleno de la barra
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFFEB3B), Color(0xFFFF9800))
                        )
                    )
            )

            // Indicadores de Estrellas
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (star1) "⭐" else "☆", fontSize = 12.sp)
                Text(if (star2) "⭐" else "☆", fontSize = 12.sp)
                Text(if (star3) "⭐" else "☆", fontSize = 12.sp)
            }
        }
    }
}

/**
 * Barra inferior de Boosters y Potenciadores.
 */
@Composable
fun BoosterToolbar(
    engine: CandyCrushEngine,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BoosterType.values().forEach { booster ->
            val count = engine.boosterCounts[booster] ?: 0
            val isActive = engine.activeBooster == booster

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(4.dp, CircleShape)
                    .background(
                        if (isActive) Brush.linearGradient(
                            listOf(
                                Color(0xFF00E676),
                                Color(0xFF00B0FF)
                            )
                        )
                        else Brush.linearGradient(
                            listOf(
                                Color(0xFF8E24AA),
                                Color(0xFF512DA8)
                            )
                        ),
                        CircleShape
                    )
                    .border(
                        2.dp,
                        if (isActive) Color.Yellow else Color.White.copy(alpha = 0.8f),
                        CircleShape
                    )
                    .clickable { engine.selectBooster(booster) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = booster.icon, fontSize = 24.sp)

                // Insignia con el conteo disponible
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(20.dp)
                        .background(Color.Red, CircleShape)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$count",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Botón de Pista
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(4.dp, CircleShape)
                .background(
                    Brush.linearGradient(listOf(Color(0xFFFF9800), Color(0xFFF57F17))),
                    CircleShape
                )
                .border(2.dp, Color.White, CircleShape)
                .clickable { engine.findHint() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "💡", fontSize = 24.sp)
        }
    }
}

/**
 * Diálogo modal para Victoria / Derrota / Pausa.
 */
@Composable
fun GameOverOverlay(
    status: GameStatus,
    score: Int,
    targetScore: Int,
    onNextLevel: () -> Unit,
    onRetry: () -> Unit,
    onResume: () -> Unit
) {
    if (status == GameStatus.PLAYING) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA000000)),
        contentAlignment = Alignment.Center
    ) {
        CardContainer {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                val title = when (status) {
                    GameStatus.LEVEL_COMPLETE -> "¡SUGAR RUSH!"
                    GameStatus.GAME_OVER -> "¡SIN MOVIMIENTOS!"
                    GameStatus.PAUSED -> "JUEGO EN PAUSA"
                    else -> ""
                }

                val emoji = when (status) {
                    GameStatus.LEVEL_COMPLETE -> "🎉 🏆 🍬"
                    GameStatus.GAME_OVER -> "😿 🍭"
                    GameStatus.PAUSED -> "⏸️"
                    else -> ""
                }

                Text(text = emoji, fontSize = 48.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = title,
                    color = Color(0xFFFFD700),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Puntuación final: $score / $targetScore",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                when (status) {
                    GameStatus.LEVEL_COMPLETE -> {
                        GameButton(text = "Siguiente Nivel ➡️", color = Color(0xFF00E676)) {
                            onNextLevel()
                        }
                    }
                    GameStatus.GAME_OVER -> {
                        GameButton(text = "Reintentar 🔄", color = Color(0xFFFF3D00)) {
                            onRetry()
                        }
                    }
                    GameStatus.PAUSED -> {
                        GameButton(text = "Reanudar ▶️", color = Color(0xFF29B6F6)) {
                            onResume()
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        GameButton(text = "Reiniciar Nivel 🔄", color = Color(0xFFFF9800)) {
                            onRetry()
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun CardContainer(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF4A148C), Color(0xFF880E4F))
                ),
                RoundedCornerShape(24.dp)
            )
            .border(4.dp, Color(0xFFFFD700), RoundedCornerShape(24.dp))
    ) {
        content()
    }
}

@Composable
private fun GameButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp))
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp
        )
    }
}
