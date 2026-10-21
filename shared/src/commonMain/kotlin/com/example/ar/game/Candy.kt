package com.example.ar.game

import androidx.compose.ui.graphics.Color

/**
 * Representa el tipo de caramelo con sus colores principales para los gradientes y brillos.
 */
enum class CandyType(
    val id: Int,
    val primaryColor: Color,
    val secondaryColor: Color,
    val glossColor: Color,
    val nameEs: String
) {
    RED(
        id = 1,
        primaryColor = Color(0xFFFF2A55),
        secondaryColor = Color(0xFFB3002D),
        glossColor = Color(0xFFFF99AA),
        nameEs = "Fresa"
    ),
    BLUE(
        id = 2,
        primaryColor = Color(0xFF29B6F6),
        secondaryColor = Color(0xFF0277BD),
        glossColor = Color(0xFFB3E5FC),
        nameEs = "Mora"
    ),
    GREEN(
        id = 3,
        primaryColor = Color(0xFF66BB6A),
        secondaryColor = Color(0xFF2E7D32),
        glossColor = Color(0xFFC8E6C9),
        nameEs = "Manzana"
    ),
    YELLOW(
        id = 4,
        primaryColor = Color(0xFFFFCA28),
        secondaryColor = Color(0xFFF57F17),
        glossColor = Color(0xFFFFF59D),
        nameEs = "Limón"
    ),
    PURPLE(
        id = 5,
        primaryColor = Color(0xFFAB47BC),
        secondaryColor = Color(0xFF6A1B9A),
        glossColor = Color(0xFFE1BEE7),
        nameEs = "Uva"
    ),
    ORANGE(
        id = 6,
        primaryColor = Color(0xFFFF7043),
        secondaryColor = Color(0xFFD84315),
        glossColor = Color(0xFFFFCCBC),
        nameEs = "Naranja"
    );

    companion object {
        fun random(): CandyType = values().random()
    }
}

/**
 * Especialidades de caramelos (Rayado, Empaquetado, Bomba de Color).
 */
enum class SpecialCandy {
    NONE,
    STRIPED_HORIZONTAL,
    STRIPED_VERTICAL,
    WRAPPED,
    COLOR_BOMB
}

/**
 * Representa una casilla/caramelo único en el tablero.
 */
data class Candy(
    val id: String,
    val type: CandyType,
    val special: SpecialCandy = SpecialCandy.NONE,
    val isMatched: Boolean = false
)

/**
 * Posición en la cuadrícula (fila, columna).
 */
data class Position(val row: Int, val col: Int) {
    fun isAdjacentTo(other: Position): Boolean {
        val rowDiff = kotlin.math.abs(row - other.row)
        val colDiff = kotlin.math.abs(col - other.col)
        return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1)
    }
}

/**
 * Tipos de boosters disponibles.
 */
enum class BoosterType(val titleEs: String, val icon: String) {
    HAMMER("Martillo", "🔨"),
    FREE_SWAP("Cambio Libre", "🔄"),
    COLOR_BOMB("Bomba Color", "🌈"),
    SHUFFLE("Mezclar", "🔀")
}

/**
 * Anuncio flotante al hacer combos ("¡DULCE!", "¡DELICIOSO!", "+300").
 */
data class ScorePopup(
    val id: Long,
    val text: String,
    val color: Color,
    val row: Int,
    val col: Int
)
