package com.islandmatch.yardvibes

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.islandmatch.yardvibes.game.*
import com.islandmatch.yardvibes.ui.BoardView
import com.islandmatch.yardvibes.ui.MusicManager
import com.islandmatch.yardvibes.ui.SoundManager

class MainActivity : Activity() {
    private enum class ToolMode {
        NONE,
        HAMMER,
        SUNBURST,
    }

    private lateinit var boardView: BoardView
    private lateinit var openMenuButton: Button
    private lateinit var hammerButton: Button
    private lateinit var sunburstButton: Button
    private lateinit var extraMovesButton: Button
    private lateinit var levelText: TextView
    private lateinit var objectiveText: TextView
    private lateinit var scoreText: TextView
    private lateinit var movesText: TextView
    private lateinit var targetText: TextView
    private lateinit var statusText: TextView
    private lateinit var menuOverlay: View
    private lateinit var menuCard: View
    private lateinit var menuPlayButton: Button
    private lateinit var menuMusicButton: Button
    private lateinit var menuProgressText: TextView
    private lateinit var yardProgressText: TextView
    private lateinit var yardTaskList: LinearLayout
    private lateinit var restartButton: Button
    private lateinit var shuffleButton: Button
    private lateinit var nextButton: Button
    private lateinit var starText: TextView

    private lateinit var levelRepository: LevelRepository
    private lateinit var levelIds: List<String>
    private lateinit var engine: GameEngine
    private lateinit var soundManager: SoundManager
    private lateinit var musicManager: MusicManager
    private lateinit var currencyManager: CurrencyManager
    private lateinit var yardTaskManager: YardTaskManager

    private var currentLevelIndex = 0
    private var selectedTile: GridPos? = null
    private var activeTool = ToolMode.NONE
    private var hasEnteredBoard = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()

        soundManager = SoundManager(this)
        musicManager = MusicManager(this)
        currencyManager = CurrencyManager(this)
        yardTaskManager = YardTaskManager(this)
        levelRepository = LevelRepository(assets)
        levelIds = levelRepository.listLevelIds()

        boardView.onTileTapped = { pos ->
            if (::engine.isInitialized && menuOverlay.visibility != View.VISIBLE) {
                handleTileTap(pos)
            }
        }

        openMenuButton.setOnClickListener {
            soundManager.playClick()
            showMenuOverlay()
        }

        hammerButton.setOnClickListener {
            soundManager.playClick()
            if (!::engine.isInitialized) {
                return@setOnClickListener
            }

            if (engine.hammerCharges <= 0) {
                soundManager.playError()
                refreshUi(getString(R.string.hammer_empty))
                return@setOnClickListener
            }

            setToolMode(
                if (activeTool == ToolMode.HAMMER) ToolMode.NONE else ToolMode.HAMMER,
                getString(R.string.hammer_armed),
            )
        }

        sunburstButton.setOnClickListener {
            soundManager.playClick()
            if (!::engine.isInitialized) {
                return@setOnClickListener
            }

            if (engine.sunburstCharges <= 0) {
                soundManager.playError()
                refreshUi(getString(R.string.sunburst_empty))
                return@setOnClickListener
            }

            setToolMode(
                if (activeTool == ToolMode.SUNBURST) ToolMode.NONE else ToolMode.SUNBURST,
                getString(R.string.sunburst_armed),
            )
        }

        extraMovesButton.setOnClickListener {
            soundManager.playClick()
            if (!::engine.isInitialized) {
                return@setOnClickListener
            }

            val result = engine.useExtraMoves()
            if (result.success) {
                soundManager.playMatch()
                activeTool = ToolMode.NONE
            } else {
                soundManager.playError()
            }
            refreshUi(result.message)
        }

        restartButton.setOnClickListener {
            soundManager.playClick()
            if (levelIds.isNotEmpty()) {
                loadLevel(currentLevelIndex, getString(R.string.level_restarted))
            }
        }

        shuffleButton.setOnClickListener {
            soundManager.playClick()
            if (::engine.isInitialized) {
                engine.shuffleBoard()
                selectedTile = null
                activeTool = ToolMode.NONE
                refreshUi(getString(R.string.board_shuffled))
            }
        }

        nextButton.setOnClickListener {
            soundManager.playClick()
            if (levelIds.isNotEmpty()) {
                val nextIndex = (currentLevelIndex + 1) % levelIds.size
                loadLevel(nextIndex, getString(R.string.level_loaded))
            }
        }

        menuPlayButton.setOnClickListener {
            soundManager.playClick()
            hasEnteredBoard = true
            hideMenuOverlay()
        }

        menuMusicButton.setOnClickListener {
            soundManager.playClick()
            musicManager.toggleMuted()
            updateMusicButtonLabel()
        }

        if (levelIds.isEmpty()) {
            statusText.text = getString(R.string.no_levels_found)
            boardView.render(emptyArray(), null)
            disableControls()
            return
        }

        val preferredStart = levelIds.indexOf("level_004").takeIf { it >= 0 } ?: 0
        loadLevel(preferredStart, getString(R.string.tap_prompt))
        updateMusicButtonLabel()
        showMenuOverlay(animate = false)
        musicManager.playMenuLoop()
    }

    private fun bindViews() {
        boardView = findViewById(R.id.boardView)
        openMenuButton = findViewById(R.id.openMenuButton)
        hammerButton = findViewById(R.id.hammerButton)
        sunburstButton = findViewById(R.id.sunburstButton)
        extraMovesButton = findViewById(R.id.extraMovesButton)
        levelText = findViewById(R.id.levelText)
        objectiveText = findViewById(R.id.objectiveText)
        scoreText = findViewById(R.id.scoreText)
        movesText = findViewById(R.id.movesText)
        targetText = findViewById(R.id.targetText)
        statusText = findViewById(R.id.statusText)
        menuOverlay = findViewById(R.id.menuOverlay)
        menuCard = findViewById(R.id.menuCard)
        menuPlayButton = findViewById(R.id.menuPlayButton)
        menuMusicButton = findViewById(R.id.menuMusicButton)
        menuProgressText = findViewById(R.id.menuProgressText)
        yardProgressText = findViewById(R.id.yardProgressText)
        yardTaskList = findViewById(R.id.yardTaskList)
        restartButton = findViewById(R.id.restartButton)
        shuffleButton = findViewById(R.id.shuffleButton)
        nextButton = findViewById(R.id.nextButton)
        starText = findViewById(R.id.starText)
    }

    private fun disableControls() {
        restartButton.isEnabled = false
        shuffleButton.isEnabled = false
        nextButton.isEnabled = false
        hammerButton.isEnabled = false
        sunburstButton.isEnabled = false
        extraMovesButton.isEnabled = false
    }

    private fun loadLevel(levelIndex: Int, statusMessage: String) {
        currentLevelIndex = levelIndex
        val level = levelRepository.loadLevel(levelIds[levelIndex])
        engine = GameEngine(level)
        selectedTile = null
        activeTool = ToolMode.NONE
        refreshUi(statusMessage)
    }

    private fun handleTileTap(pos: GridPos) {
        if (engine.movesRemaining == 0 && !engine.hasMetTarget() && activeTool == ToolMode.NONE) {
            soundManager.playError()
            refreshUi(getString(R.string.no_moves_left))
            return
        }

        if (engine.tileAt(pos) == 0) {
            return
        }

        if (activeTool == ToolMode.HAMMER) {
            val result = engine.useHammer(pos)
            if (result.success) {
                soundManager.playMatch()
            } else {
                soundManager.playError()
            }
            activeTool = ToolMode.NONE
            selectedTile = null
            refreshUi(if (result.success) awardLevelStarIfNeeded(result.message) else result.message)
            return
        }

        if (activeTool == ToolMode.SUNBURST) {
            val result = engine.useSunburst(pos)
            if (result.success) {
                soundManager.playMatch()
            } else {
                soundManager.playError()
            }
            activeTool = ToolMode.NONE
            selectedTile = null
            refreshUi(if (result.success) awardLevelStarIfNeeded(result.message) else result.message)
            return
        }

        val currentSelection = selectedTile
        when {
            currentSelection == null -> {
                soundManager.playClick()
                selectedTile = pos
                refreshUi(getString(R.string.select_neighbor_prompt))
            }

            currentSelection == pos -> {
                soundManager.playClick()
                selectedTile = null
                refreshUi(getString(R.string.selection_cleared))
            }

            !engine.areAdjacent(currentSelection, pos) -> {
                soundManager.playClick()
                selectedTile = pos
                refreshUi(getString(R.string.select_neighbor_prompt))
            }

            else -> {
                val result = engine.trySwap(currentSelection, pos)
                if (result.success) {
                    soundManager.playMatch()
                } else {
                    soundManager.playError()
                }
                selectedTile = null
                refreshUi(if (result.success) awardLevelStarIfNeeded(result.message) else result.message)
            }
        }
    }

    private fun refreshUi(statusMessage: String) {
        if (!::engine.isInitialized) {
            return
        }
        
        starText.text = getString(R.string.stars_value, currencyManager.starBalance)
        renderYardTasks()
        
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
        hammerButton.text = getString(R.string.hammer_value, engine.hammerCharges)
        hammerButton.isEnabled = engine.hammerCharges > 0
        hammerButton.alpha = if (activeTool == ToolMode.HAMMER) 1f else 0.92f
        sunburstButton.text = getString(R.string.sunburst_value, engine.sunburstCharges)
        sunburstButton.isEnabled = engine.sunburstCharges > 0
        sunburstButton.alpha = if (activeTool == ToolMode.SUNBURST) 1f else 0.92f
        extraMovesButton.text = getString(R.string.extra_moves_value, engine.extraMoveCharges)
        extraMovesButton.isEnabled = engine.extraMoveCharges > 0
        extraMovesButton.alpha = if (engine.extraMoveCharges > 0) 0.96f else 0.62f
        menuProgressText.text = getString(
            R.string.menu_progress_value,
            currentLevelIndex + 1,
            levelIds.size,
            engine.targetScore,
        )
        boardView.render(engine.copyBoard(), selectedTile)
    }

    private fun awardLevelStarIfNeeded(statusMessage: String): String {
        if (!engine.hasMetTarget() || engine.isLevelFinished) {
            return statusMessage
        }

        engine.markLevelFinished()
        currencyManager.addStars(1)
        return getString(R.string.level_cleared)
    }

    private fun renderYardTasks() {
        if (!::yardTaskList.isInitialized || !::yardProgressText.isInitialized) {
            return
        }

        val tasks = yardTaskManager.tasks()
        yardProgressText.text = getString(
            R.string.yard_progress_value,
            yardTaskManager.completedCount(),
            yardTaskManager.totalCount(),
        )
        yardTaskList.removeAllViews()

        tasks.forEach { task ->
            val canBuy = !task.isCompleted && currencyManager.canAfford(task.cost)
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setBackgroundResource(R.drawable.panel_task)
                setPadding(12.dp(), 8.dp(), 10.dp(), 8.dp())
            }
            val rowParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                bottomMargin = 8.dp()
            }

            val label = TextView(this).apply {
                text = if (task.isCompleted) {
                    getString(R.string.star_task_completed, task.description)
                } else {
                    task.description
                }
                textSize = 14f
                maxLines = 2
                typeface = if (task.isCompleted) Typeface.DEFAULT else Typeface.DEFAULT_BOLD
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_primary))
            }
            val labelParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f,
            ).apply {
                marginEnd = 8.dp()
            }

            val actionButton = Button(this).apply {
                text = if (task.isCompleted) getString(R.string.star_task_done) else taskCostLabel(task.cost)
                isEnabled = canBuy
                isAllCaps = false
                minHeight = 0
                minimumHeight = 0
                setPadding(8.dp(), 0, 8.dp(), 0)
                setTextColor(
                    ContextCompat.getColor(
                        this@MainActivity,
                        if (canBuy) R.color.text_white else R.color.text_primary,
                    ),
                )
                setBackgroundResource(if (canBuy) R.drawable.button_primary else R.drawable.button_secondary)
                if (!task.isCompleted) {
                    setOnClickListener { spendStarsOnTask(task) }
                }
            }

            row.addView(label, labelParams)
            row.addView(actionButton, LinearLayout.LayoutParams(108.dp(), 42.dp()))
            yardTaskList.addView(row, rowParams)
        }
    }

    private fun spendStarsOnTask(task: YardTask) {
        if (task.isCompleted) {
            return
        }

        if (!currencyManager.spendStars(task.cost)) {
            soundManager.playError()
            refreshUi(getString(R.string.star_task_not_enough))
            return
        }

        val completedTask = yardTaskManager.complete(task.id)
        soundManager.playMatch()
        refreshUi(getString(R.string.star_task_purchased, completedTask?.description ?: task.description))
    }

    private fun taskCostLabel(cost: Int): String {
        return getString(
            if (cost == 1) R.string.star_task_cost else R.string.star_task_cost_plural,
            cost,
        )
    }

    private fun setToolMode(mode: ToolMode, armedMessage: String) {
        activeTool = mode
        selectedTile = null
        refreshUi(if (mode == ToolMode.NONE) getString(R.string.tool_cleared) else armedMessage)
    }

    private fun showMenuOverlay(animate: Boolean = true) {
        renderYardTasks()
        updateMusicButtonLabel()
        menuPlayButton.text = if (hasEnteredBoard) getString(R.string.resume_adventure) else getString(R.string.start_adventure)
        menuOverlay.visibility = View.VISIBLE

        if (!animate) {
            menuOverlay.alpha = 1f
            menuCard.alpha = 1f
            menuCard.translationY = 0f
            musicManager.playMenuLoop()
            return
        }

        menuOverlay.alpha = 0f
        menuCard.alpha = 0f
        menuCard.translationY = 80f
        menuOverlay.animate().alpha(1f).setDuration(180L).start()
        menuCard.animate().alpha(1f).translationY(0f).setDuration(260L).start()
        musicManager.playMenuLoop()
    }

    private fun hideMenuOverlay() {
        menuOverlay.animate()
            .alpha(0f)
            .setDuration(180L)
            .withEndAction {
                menuOverlay.visibility = View.GONE
                menuOverlay.alpha = 1f
                menuCard.alpha = 1f
                menuCard.translationY = 0f
            }
            .start()
        musicManager.playGameplayLoop()
    }

    private fun updateMusicButtonLabel() {
        menuMusicButton.text = getString(
            if (musicManager.isMuted()) {
                R.string.music_off
            } else {
                R.string.music_on
            },
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
        musicManager.release()
    }

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()
}
