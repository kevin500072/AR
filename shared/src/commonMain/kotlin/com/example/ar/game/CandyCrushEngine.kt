package com.example.ar.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

enum class GameStatus {
    PLAYING,
    PAUSED,
    LEVEL_COMPLETE,
    GAME_OVER
}

class CandyCrushEngine(
    val rows: Int = 8,
    val cols: Int = 8,
    initialTargetScore: Int = 3000,
    initialMoves: Int = 20
) {
    var level by mutableStateOf(1)
        private set

    var score by mutableStateOf(0)
        private set

    var targetScore by mutableStateOf(initialTargetScore)
        private set

    var movesLeft by mutableStateOf(initialMoves)
        private set

    var gameStatus by mutableStateOf(GameStatus.PLAYING)
        private set

    var selectedPosition by mutableStateOf<Position?>(null)
        private set

    var activeBooster by mutableStateOf<BoosterType?>(null)
        private set

    var hintPair by mutableStateOf<Pair<Position, Position>?>(null)
        private set

    var comboMessage by mutableStateOf<String?>(null)
        private set

    // Boosters disponible
    val boosterCounts = mutableStateMapOf<BoosterType, Int>(
        BoosterType.HAMMER to 3,
        BoosterType.FREE_SWAP to 2,
        BoosterType.COLOR_BOMB to 1,
        BoosterType.SHUFFLE to 2
    )

    // Matriz del tablero
    var board by mutableStateOf(Array(rows) { r -> Array(cols) { c -> createRandomCandy(r, c) } })
        private set

    // Popups de texto flotante
    val popups = mutableStateListOf<ScorePopup>()

    private var nextCandyId = 1L

    init {
        startNewLevel(1)
    }

    private fun createRandomCandy(r: Int, c: Int, type: CandyType = CandyType.random()): Candy {
        return Candy(
            id = "candy_${nextCandyId++}_${r}_${c}",
            type = type
        )
    }

    fun startNewLevel(newLevel: Int = level) {
        level = newLevel
        score = 0
        targetScore = 2500 + (newLevel * 1000)
        movesLeft = 20 + (newLevel / 2)
        gameStatus = GameStatus.PLAYING
        selectedPosition = null
        activeBooster = null
        hintPair = null
        comboMessage = null
        popups.clear()

        generateInitialBoard()
    }

    fun restartCurrentLevel() {
        startNewLevel(level)
    }

    private fun generateInitialBoard() {
        val newBoard = Array(rows) { r -> Array(cols) { c -> createRandomCandy(r, c) } }

        // Asegurar que no haya coincidencias de 3 o más al inicio
        var hasInitialMatches = true
        var attempts = 0
        while (hasInitialMatches && attempts < 100) {
            hasInitialMatches = false
            attempts++
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if ((c >= 2 && newBoard[r][c].type == newBoard[r][c - 1].type && newBoard[r][c].type == newBoard[r][c - 2].type) ||
                        (r >= 2 && newBoard[r][c].type == newBoard[r - 1][c].type && newBoard[r][c].type == newBoard[r - 2][c].type)
                    ) {
                        newBoard[r][c] = createRandomCandy(r, c)
                        hasInitialMatches = true
                    }
                }
            }
        }
        board = newBoard
    }

    fun togglePause() {
        if (gameStatus == GameStatus.PLAYING) {
            gameStatus = GameStatus.PAUSED
        } else if (gameStatus == GameStatus.PAUSED) {
            gameStatus = GameStatus.PLAYING
        }
    }

    /**
     * Manejo del toque en una casilla del tablero.
     */
    fun onTileClicked(pos: Position) {
        if (gameStatus != GameStatus.PLAYING) return
        hintPair = null

        // Manejo de Booster activo
        val booster = activeBooster
        if (booster != null) {
            applyBooster(booster, pos)
            activeBooster = null
            return
        }

        val currentSelected = selectedPosition
        if (currentSelected == null) {
            selectedPosition = pos
        } else if (currentSelected == pos) {
            // Deseleccionar si toca el mismo
            selectedPosition = null
        } else if (currentSelected.isAdjacentTo(pos)) {
            // Intentar intercambio
            attemptSwap(currentSelected, pos)
            selectedPosition = null
        } else {
            // Seleccionar la nueva casilla
            selectedPosition = pos
        }
    }

    /**
     * Intercambia dos posiciones y procesa las coincidencias.
     */
    private fun attemptSwap(p1: Position, p2: Position) {
        if (movesLeft <= 0) return

        // Intercambiar temporalmente
        swapTilesOnBoard(p1, p2)

        val matches = findMatches()
        if (matches.isNotEmpty()) {
            movesLeft--
            processMatchesCascade(matches, initialCombo = 1)
            checkGameEnd()
        } else {
            // Si no hay coincidencias, deshacer intercambio
            swapTilesOnBoard(p1, p2)
            addPopup("¡Sin combinación!", Color.Red, p2.row, p2.col)
        }
    }

    private fun swapTilesOnBoard(p1: Position, p2: Position) {
        val newBoard = board.map { it.clone() }.toTypedArray()
        val temp = newBoard[p1.row][p1.col]
        newBoard[p1.row][p1.col] = newBoard[p2.row][p2.col]
        newBoard[p2.row][p2.col] = temp
        board = newBoard
    }

    /**
     * Procesa la eliminación de coincidencias, otorgar puntos y la caída en cascada.
     */
    private fun processMatchesCascade(initialMatches: Set<Position>, initialCombo: Int) {
        var currentMatches = initialMatches
        var comboMultiplier = initialCombo

        val newBoard = board.map { it.clone() }.toTypedArray()

        while (currentMatches.isNotEmpty()) {
            // Mostrar texto de Combo según el multiplicador
            val comboText = when (comboMultiplier) {
                2 -> "¡DULCE! x2"
                3 -> "¡SABOROSO! x3"
                4 -> "¡DELICIOSO! x4"
                5 -> "¡SUGAR RUSH! x5"
                else -> if (comboMultiplier > 5) "¡INCREÍBLE x$comboMultiplier!" else null
            }
            if (comboText != null) {
                comboMessage = comboText
            }

            // Calcular puntos
            val pointsPerTile = 100 * comboMultiplier
            val earnedPoints = currentMatches.size * pointsPerTile
            score += earnedPoints

            val firstPos = currentMatches.first()
            addPopup("+$earnedPoints", Color(0xFFFFD700), firstPos.row, firstPos.col)

            // Vaciar las casillas emparejadas
            for (pos in currentMatches) {
                newBoard[pos.row][pos.col] = Candy(
                    id = "empty_${pos.row}_${pos.col}",
                    type = CandyType.RED,
                    isMatched = true
                )
            }

            // Hacer caer los caramelos superiores
            for (c in 0 until cols) {
                var emptyRow = rows - 1
                for (r in rows - 1 downTo 0) {
                    if (!newBoard[r][c].isMatched) {
                        if (r != emptyRow) {
                            newBoard[emptyRow][c] = newBoard[r][c]
                        }
                        emptyRow--
                    }
                }
                // Rellenar las casillas superiores vacías con caramelos nuevos
                for (r in emptyRow downTo 0) {
                    newBoard[r][c] = createRandomCandy(r, c)
                }
            }

            board = newBoard

            // Buscar nuevas coincidencias tras la caída (Cascada)
            comboMultiplier++
            currentMatches = findMatches()
        }
    }

    /**
     * Encuentra todas las coincidencias horizontales y verticales >= 3.
     */
    private fun findMatches(): Set<Position> {
        val matchedPositions = mutableSetOf<Position>()

        // Coincidencias Horizontales
        for (r in 0 until rows) {
            var matchLength = 1
            for (c in 0 until cols) {
                val isSameAsNext = c < cols - 1 && board[r][c].type == board[r][c + 1].type
                if (isSameAsNext) {
                    matchLength++
                } else {
                    if (matchLength >= 3) {
                        for (i in 0 until matchLength) {
                            matchedPositions.add(Position(r, c - i))
                        }
                    }
                    matchLength = 1
                }
            }
        }

        // Coincidencias Verticales
        for (c in 0 until cols) {
            var matchLength = 1
            for (r in 0 until rows) {
                val isSameAsNext = r < rows - 1 && board[r][c].type == board[r + 1][c].type
                if (isSameAsNext) {
                    matchLength++
                } else {
                    if (matchLength >= 3) {
                        for (i in 0 until matchLength) {
                            matchedPositions.add(Position(r - i, c))
                        }
                    }
                    matchLength = 1
                }
            }
        }

        return matchedPositions
    }

    /**
     * Activa o ejecuta un Booster.
     */
    fun selectBooster(booster: BoosterType) {
        val count = boosterCounts[booster] ?: 0
        if (count <= 0) return

        if (booster == BoosterType.SHUFFLE) {
            useBooster(booster)
            shuffleBoard()
        } else if (booster == BoosterType.COLOR_BOMB) {
            useBooster(booster)
            clearRandomType()
        } else {
            // Seleccionar para la próxima casilla tocada (HAMMER, FREE_SWAP)
            activeBooster = if (activeBooster == booster) null else booster
        }
    }

    private fun useBooster(booster: BoosterType) {
        val currentCount = boosterCounts[booster] ?: 0
        if (currentCount > 0) {
            boosterCounts[booster] = currentCount - 1
        }
    }

    private fun applyBooster(booster: BoosterType, pos: Position) {
        useBooster(booster)
        when (booster) {
            BoosterType.HAMMER -> {
                // Romper un solo caramelo
                val newBoard = board.map { it.clone() }.toTypedArray()
                newBoard[pos.row][pos.col] = createRandomCandy(pos.row, pos.col)
                board = newBoard
                score += 200
                addPopup("🔨 +200", Color(0xFFFF4081), pos.row, pos.col)
                processMatchesCascade(findMatches(), initialCombo = 1)
            }
            BoosterType.FREE_SWAP -> {
                // Intercambiar con la casilla seleccionada anteriormente
                val selected = selectedPosition
                if (selected != null) {
                    swapTilesOnBoard(selected, pos)
                    selectedPosition = null
                    processMatchesCascade(findMatches(), initialCombo = 1)
                } else {
                    selectedPosition = pos
                }
            }
            else -> {}
        }
    }

    /**
     * Mezcla el tablero completo.
     */
    private fun shuffleBoard() {
        val newBoard = board.map { it.clone() }.toTypedArray()
        val allTypes = mutableListOf<CandyType>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                allTypes.add(newBoard[r][c].type)
            }
        }
        allTypes.shuffle()
        var index = 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                newBoard[r][c] = createRandomCandy(r, c, allTypes[index++])
            }
        }
        board = newBoard
        addPopup("🔀 ¡Mezclado!", Color(0xFF00E676), rows / 2, cols / 2)
        processMatchesCascade(findMatches(), initialCombo = 1)
    }

    /**
     * Elimina todos los caramelos de un tipo aleatorio (Bomba de Color).
     */
    private fun clearRandomType() {
        val randomType = CandyType.random()
        val matches = mutableSetOf<Position>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (board[r][c].type == randomType) {
                    matches.add(Position(r, c))
                }
            }
        }
        addPopup("🌈 ¡Bomba ${randomType.nameEs}!", randomType.primaryColor, rows / 2, cols / 2)
        processMatchesCascade(matches, initialCombo = 1)
    }

    /**
     * Sugiere un movimiento al jugador (Pista).
     */
    fun findHint() {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val currentPos = Position(r, c)
                // Probar a la derecha
                if (c < cols - 1) {
                    val rightPos = Position(r, c + 1)
                    swapTilesOnBoard(currentPos, rightPos)
                    val matches = findMatches()
                    swapTilesOnBoard(currentPos, rightPos)
                    if (matches.isNotEmpty()) {
                        hintPair = Pair(currentPos, rightPos)
                        return
                    }
                }
                // Probar abajo
                if (r < rows - 1) {
                    val downPos = Position(r + 1, c)
                    swapTilesOnBoard(currentPos, downPos)
                    val matches = findMatches()
                    swapTilesOnBoard(currentPos, downPos)
                    if (matches.isNotEmpty()) {
                        hintPair = Pair(currentPos, downPos)
                        return
                    }
                }
            }
        }
        // Si no hay pistas posibles, mezclar automáticamente
        shuffleBoard()
    }

    private fun addPopup(text: String, color: Color, row: Int, col: Int) {
        popups.add(ScorePopup(System.currentTimeMillis(), text, color, row, col))
        if (popups.size > 5) {
            popups.removeAt(0)
        }
    }

    private fun checkGameEnd() {
        if (score >= targetScore) {
            gameStatus = GameStatus.LEVEL_COMPLETE
        } else if (movesLeft <= 0) {
            gameStatus = GameStatus.GAME_OVER
        }
    }
}
