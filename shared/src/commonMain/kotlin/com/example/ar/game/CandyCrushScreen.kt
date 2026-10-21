package com.example.ar.game

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun CandyCrushGameScreen(
    engine: CandyCrushEngine = remember { CandyCrushEngine() }
) {
    // Fondo de ensueño tipo Candy Kingdom (Degradado rosa a violeta)
    val kingdomBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF311B92),
            Color(0xFF6A1B9A),
            Color(0xFFAD1457),
            Color(0xFFD81B60)
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(kingdomBackground)
                .systemBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Barra Superior con Nivel, Puntuación, Estrellas y Movimientos
                TopStatsBar(
                    level = engine.level,
                    score = engine.score,
                    targetScore = engine.targetScore,
                    movesLeft = engine.movesLeft,
                    onPauseClicked = { engine.togglePause() }
                )

                // 2. Banner Animado de Combos ("¡DULCE!", "¡DELICIOSO!")
                ComboBanner(engine.comboMessage)

                // 3. Tablero de Juego (Grilla 8x8)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    GameBoard(engine = engine)
                }

                // 4. Barra Inferior de Potenciadores (Boosters) y Pistas
                BoosterToolbar(engine = engine)
            }

            // 5. Capa Superpuesta de Fin de Juego / Victoria / Pausa
            GameOverOverlay(
                status = engine.gameStatus,
                score = engine.score,
                targetScore = engine.targetScore,
                onNextLevel = { engine.startNewLevel(engine.level + 1) },
                onRetry = { engine.restartCurrentLevel() },
                onResume = { engine.togglePause() }
            )
        }
    }
}

/**
 * Muestra el banner flotante al realizar combos consecutivos.
 */
@Composable
private fun ComboBanner(message: String?) {
    var visibleMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(message) {
        if (message != null) {
            visibleMessage = message
            delay(1200)
            visibleMessage = null
        }
    }

    AnimatedVisibility(
        visible = visibleMessage != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFFFEA00), Color(0xFFFF6D00), Color(0xFFFF1744))
                    ),
                    RoundedCornerShape(20.dp)
                )
                .border(2.dp, Color.White, RoundedCornerShape(20.dp))
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = visibleMessage ?: "",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

/**
 * Marco y cuadrícula 8x8 interactiva del tablero.
 */
@Composable
private fun GameBoard(engine: CandyCrushEngine) {
    val rows = engine.rows
    val cols = engine.cols
    val selectedPos = engine.selectedPosition
    val hintPair = engine.hintPair

    // Marco exterior con estilo de tienda de dulces (Borde dorado brillante)
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth(0.96f)
            .shadow(16.dp, RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF4A148C), Color(0xFF880E4F))
                ),
                RoundedCornerShape(20.dp)
            )
            .border(4.dp, Color(0xFFFFD700), RoundedCornerShape(20.dp))
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            for (r in 0 until rows) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (c in 0 until cols) {
                        val pos = Position(r, c)
                        val candy = engine.board[r][c]
                        val isSelected = selectedPos == pos
                        val isHinted = hintPair?.first == pos || hintPair?.second == pos

                        // Casilla con patrón ajedrezado translúcido
                        val cellBg = if ((r + c) % 2 == 0) Color(0x22FFFFFF) else Color(0x11000000)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(cellBg)
                                .clickable { engine.onTileClicked(pos) },
                            contentAlignment = Alignment.Center
                        ) {
                            CandyView(
                                candy = candy,
                                isSelected = isSelected,
                                isHinted = isHinted
                            )
                        }
                    }
                }
            }
        }

        // Popups flotantes de Puntuación (+100, +300)
        engine.popups.forEach { popup ->
            FloatingScorePopup(popup = popup, rows = rows, cols = cols)
        }
    }
}

/**
 * Animación de puntos flotantes que suben y desaparecen.
 */
@Composable
private fun FloatingScorePopup(popup: ScorePopup, rows: Int, cols: Int) {
    var active by remember { mutableStateOf(true) }

    LaunchedEffect(popup.id) {
        delay(800)
        active = false
    }

    if (active) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = popup.text,
                color = popup.color,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.shadow(4.dp, CircleShape)
            )
        }
    }
}
