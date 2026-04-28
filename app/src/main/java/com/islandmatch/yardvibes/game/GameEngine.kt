package com.islandmatch.yardvibes.game

import kotlin.math.abs
import kotlin.random.Random

data class GridPos(val row: Int, val col: Int)

data class LevelData(
    val id: String,
    val version: Int,
    val width: Int,
    val height: Int,
    val moves: Int,
    val targetScore: Int,
    val layout: IntArray,
    val objectives: List<String>,
)

data class SwapResult(
    val success: Boolean,
    val message: String,
)

class GameEngine(
    private val level: LevelData,
    private val random: Random = Random.Default,
) {
    private val board = Array(level.height) { IntArray(level.width) }

    var score: Int = 0
        private set

    var movesRemaining: Int = level.moves
        private set

    val levelId: String
        get() = level.id

    val targetScore: Int
        get() = level.targetScore

    val objectiveSummary: String
        get() = level.objectives.firstOrNull()?.ifBlank { "Reach ${level.targetScore} points." }
            ?: "Reach ${level.targetScore} points."

    init {
        reset()
    }

    fun reset() {
        loadFromLevel()
        sanitizeBoardForPlay()
        score = 0
        movesRemaining = level.moves
    }

    fun copyBoard(): Array<IntArray> = Array(level.height) { row -> board[row].clone() }

    fun tileAt(pos: GridPos): Int = if (isValid(pos)) board[pos.row][pos.col] else 0

    fun areAdjacent(a: GridPos, b: GridPos): Boolean {
        val rowDistance = abs(a.row - b.row)
        val colDistance = abs(a.col - b.col)
        return (rowDistance == 1 && colDistance == 0) || (rowDistance == 0 && colDistance == 1)
    }

    fun hasMetTarget(): Boolean = score >= targetScore

    fun trySwap(a: GridPos, b: GridPos): SwapResult {
        if (!isValid(a) || !isValid(b)) {
            return SwapResult(false, "That tile is outside the board.")
        }

        if (!areAdjacent(a, b)) {
            return SwapResult(false, "Pick neighboring tiles.")
        }

        if (tileAt(a) == 0 || tileAt(b) == 0) {
            return SwapResult(false, "Those tiles cannot move.")
        }

        if (tileAt(a) == tileAt(b)) {
            return SwapResult(false, "Swap into a different color.")
        }

        swapInPlace(a, b)
        if (findMatches().isEmpty()) {
            swapInPlace(a, b)
            return SwapResult(false, "No match there. Try a different pair.")
        }

        movesRemaining = (movesRemaining - 1).coerceAtLeast(0)
        val scoreBefore = score
        resolveUntilStable(countScore = true)

        var shuffled = false
        if (!hasValidMoves()) {
            shuffleBoard()
            shuffled = true
        }

        val pointsEarned = score - scoreBefore
        val message = when {
            hasMetTarget() -> "Target cleared. You can keep farming points or jump to the next board."
            movesRemaining == 0 -> "No moves left. Restart or load the next board."
            shuffled -> "Match cleared for $pointsEarned points. Board shuffled for a fresh move."
            else -> "Match cleared for $pointsEarned points."
        }

        return SwapResult(true, message)
    }

    fun shuffleBoard() {
        var attempts = 0
        while (attempts < 60) {
            shufflePreservingTiles()
            resolveUntilStable(countScore = false)
            if (findMatches().isEmpty() && hasValidMoves()) {
                return
            }
            attempts++
        }

        reseedBoard()
    }

    private fun sanitizeBoardForPlay() {
        resolveUntilStable(countScore = false)

        var attempts = 0
        while (!hasValidMoves() && attempts < 40) {
            shufflePreservingTiles()
            resolveUntilStable(countScore = false)
            attempts++
        }

        if (!hasValidMoves()) {
            reseedBoard()
        }
    }

    private fun loadFromLevel() {
        require(level.layout.size == level.width * level.height) {
            "Layout size does not match level dimensions."
        }

        for (row in 0 until level.height) {
            for (col in 0 until level.width) {
                val flatIndex = row * level.width + col
                board[row][col] = level.layout[flatIndex].coerceIn(0, 5)
            }
        }
    }

    private fun resolveUntilStable(countScore: Boolean) {
        var guard = 0
        while (guard < 100) {
            val matches = findMatches()
            if (matches.isEmpty()) {
                break
            }

            if (countScore) {
                score += matches.size * 10
            }

            for (pos in matches) {
                board[pos.row][pos.col] = 0
            }

            applyGravity()
            refillBoard()
            guard++
        }
    }

    private fun applyGravity() {
        for (col in 0 until level.width) {
            var destinationRow = 0
            for (row in 0 until level.height) {
                val tile = board[row][col]
                if (tile != 0) {
                    if (destinationRow != row) {
                        board[destinationRow][col] = tile
                        board[row][col] = 0
                    }
                    destinationRow++
                }
            }
        }
    }

    private fun refillBoard() {
        for (row in 0 until level.height) {
            for (col in 0 until level.width) {
                if (board[row][col] == 0) {
                    board[row][col] = nextTile()
                }
            }
        }
    }

    private fun findMatches(): Set<GridPos> {
        val matches = linkedSetOf<GridPos>()

        for (row in 0 until level.height) {
            for (col in 0 until level.width - 2) {
                val tile = board[row][col]
                if (tile != 0 && tile == board[row][col + 1] && tile == board[row][col + 2]) {
                    matches += GridPos(row, col)
                    matches += GridPos(row, col + 1)
                    matches += GridPos(row, col + 2)

                    var runCol = col + 3
                    while (runCol < level.width && board[row][runCol] == tile) {
                        matches += GridPos(row, runCol)
                        runCol++
                    }
                }
            }
        }

        for (col in 0 until level.width) {
            for (row in 0 until level.height - 2) {
                val tile = board[row][col]
                if (tile != 0 && tile == board[row + 1][col] && tile == board[row + 2][col]) {
                    matches += GridPos(row, col)
                    matches += GridPos(row + 1, col)
                    matches += GridPos(row + 2, col)

                    var runRow = row + 3
                    while (runRow < level.height && board[runRow][col] == tile) {
                        matches += GridPos(runRow, col)
                        runRow++
                    }
                }
            }
        }

        return matches
    }

    private fun hasValidMoves(): Boolean {
        for (row in 0 until level.height) {
            for (col in 0 until level.width) {
                val current = GridPos(row, col)
                if (col + 1 < level.width && createsMatch(current, GridPos(row, col + 1))) {
                    return true
                }
                if (row + 1 < level.height && createsMatch(current, GridPos(row + 1, col))) {
                    return true
                }
            }
        }

        return false
    }

    private fun createsMatch(a: GridPos, b: GridPos): Boolean {
        val first = tileAt(a)
        val second = tileAt(b)
        if (first == 0 || second == 0 || first == second) {
            return false
        }

        swapInPlace(a, b)
        val hasMatch = findMatches().isNotEmpty()
        swapInPlace(a, b)
        return hasMatch
    }

    private fun shufflePreservingTiles() {
        val tiles = mutableListOf<Int>()
        for (row in 0 until level.height) {
            for (col in 0 until level.width) {
                val tile = board[row][col]
                if (tile != 0) {
                    tiles += tile
                }
            }
        }

        tiles.shuffle(random)
        var index = 0
        for (row in 0 until level.height) {
            for (col in 0 until level.width) {
                board[row][col] = tiles[index]
                index++
            }
        }
    }

    private fun reseedBoard() {
        repeat(200) {
            for (row in 0 until level.height) {
                for (col in 0 until level.width) {
                    board[row][col] = nextTileAvoidingImmediateMatch(row, col)
                }
            }

            if (findMatches().isEmpty() && hasValidMoves()) {
                return
            }
        }

        for (row in 0 until level.height) {
            for (col in 0 until level.width) {
                board[row][col] = nextTile()
            }
        }

        resolveUntilStable(countScore = false)
    }

    private fun nextTileAvoidingImmediateMatch(row: Int, col: Int): Int {
        repeat(30) {
            val candidate = nextTile()
            if (!createsHorizontalRun(row, col, candidate) && !createsVerticalRun(row, col, candidate)) {
                return candidate
            }
        }

        return nextTile()
    }

    private fun createsHorizontalRun(row: Int, col: Int, candidate: Int): Boolean {
        if (col < 2) {
            return false
        }

        return board[row][col - 1] == candidate && board[row][col - 2] == candidate
    }

    private fun createsVerticalRun(row: Int, col: Int, candidate: Int): Boolean {
        if (row < 2) {
            return false
        }

        return board[row - 1][col] == candidate && board[row - 2][col] == candidate
    }

    private fun nextTile(): Int = random.nextInt(from = 1, until = 6)

    private fun swapInPlace(a: GridPos, b: GridPos) {
        val temp = board[a.row][a.col]
        board[a.row][a.col] = board[b.row][b.col]
        board[b.row][b.col] = temp
    }

    private fun isValid(pos: GridPos): Boolean {
        return pos.row in 0 until level.height && pos.col in 0 until level.width
    }
}
