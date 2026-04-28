package com.islandmatch.yardvibes

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.islandmatch.yardvibes.game.GameEngine
import com.islandmatch.yardvibes.game.GridPos
import com.islandmatch.yardvibes.game.LevelRepository
import com.islandmatch.yardvibes.ui.BoardView

class MainActivity : Activity() {
    private lateinit var boardView: BoardView
    private lateinit var levelText: TextView
    private lateinit var objectiveText: TextView
    private lateinit var scoreText: TextView
    private lateinit var movesText: TextView
    private lateinit var targetText: TextView
    private lateinit var statusText: TextView
    private lateinit var restartButton: Button
    private lateinit var shuffleButton: Button
    private lateinit var nextButton: Button

    private lateinit var levelRepository: LevelRepository
    private lateinit var levelIds: List<String>
    private lateinit var engine: GameEngine

    private var currentLevelIndex = 0
    private var selectedTile: GridPos? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()

        levelRepository = LevelRepository(assets)
        levelIds = levelRepository.listLevelIds()

        boardView.onTileTapped = { pos ->
            if (::engine.isInitialized) {
                handleTileTap(pos)
            }
        }

        restartButton.setOnClickListener {
            if (levelIds.isNotEmpty()) {
                loadLevel(currentLevelIndex, getString(R.string.level_restarted))
            }
        }

        shuffleButton.setOnClickListener {
            if (::engine.isInitialized) {
                engine.shuffleBoard()
                selectedTile = null
                refreshUi(getString(R.string.board_shuffled))
            }
        }

        nextButton.setOnClickListener {
            if (levelIds.isNotEmpty()) {
                val nextIndex = (currentLevelIndex + 1) % levelIds.size
                loadLevel(nextIndex, getString(R.string.level_loaded))
            }
        }

        if (levelIds.isEmpty()) {
            statusText.text = getString(R.string.no_levels_found)
            boardView.render(emptyArray(), null)
            disableControls()
            return
        }

        val preferredStart = levelIds.indexOf("level_004").takeIf { it >= 0 } ?: 0
        loadLevel(preferredStart, getString(R.string.tap_prompt))
    }

    private fun bindViews() {
        boardView = findViewById(R.id.boardView)
        levelText = findViewById(R.id.levelText)
        objectiveText = findViewById(R.id.objectiveText)
        scoreText = findViewById(R.id.scoreText)
        movesText = findViewById(R.id.movesText)
        targetText = findViewById(R.id.targetText)
        statusText = findViewById(R.id.statusText)
        restartButton = findViewById(R.id.restartButton)
        shuffleButton = findViewById(R.id.shuffleButton)
        nextButton = findViewById(R.id.nextButton)
    }

    private fun disableControls() {
        restartButton.isEnabled = false
        shuffleButton.isEnabled = false
        nextButton.isEnabled = false
    }

    private fun loadLevel(levelIndex: Int, statusMessage: String) {
        currentLevelIndex = levelIndex
        val level = levelRepository.loadLevel(levelIds[levelIndex])
        engine = GameEngine(level)
        selectedTile = null
        refreshUi(statusMessage)
    }

    private fun handleTileTap(pos: GridPos) {
        if (engine.movesRemaining == 0 && !engine.hasMetTarget()) {
            refreshUi(getString(R.string.no_moves_left))
            return
        }

        if (engine.tileAt(pos) == 0) {
            return
        }

        val currentSelection = selectedTile
        when {
            currentSelection == null -> {
                selectedTile = pos
                refreshUi(getString(R.string.select_neighbor_prompt))
            }

            currentSelection == pos -> {
                selectedTile = null
                refreshUi(getString(R.string.selection_cleared))
            }

            !engine.areAdjacent(currentSelection, pos) -> {
                selectedTile = pos
                refreshUi(getString(R.string.select_neighbor_prompt))
            }

            else -> {
                val result = engine.trySwap(currentSelection, pos)
                selectedTile = null
                refreshUi(result.message)
            }
        }
    }

    private fun refreshUi(statusMessage: String) {
        if (!::engine.isInitialized) {
            return
        }

        val targetLine = getString(
            R.string.target_value,
            engine.targetScore,
            if (engine.hasMetTarget()) getString(R.string.target_cleared) else getString(R.string.target_live),
        )

        levelText.text = getString(
            R.string.level_value,
            currentLevelIndex + 1,
            levelIds.size,
            engine.levelId,
        )
        objectiveText.text = engine.objectiveSummary
        scoreText.text = getString(R.string.score_value, engine.score)
        movesText.text = getString(R.string.moves_value, engine.movesRemaining)
        targetText.text = targetLine
        statusText.text = statusMessage
        boardView.render(engine.copyBoard(), selectedTile)
    }
}
