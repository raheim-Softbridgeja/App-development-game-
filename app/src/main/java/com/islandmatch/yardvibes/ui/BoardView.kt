package com.islandmatch.yardvibes.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.islandmatch.yardvibes.R
import com.islandmatch.yardvibes.game.GridPos
import kotlin.math.min

class BoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    var onTileTapped: ((GridPos) -> Unit)? = null

    private var board: Array<IntArray> = emptyArray()
    private var selectedTile: GridPos? = null

    private val boardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.board_surface)
    }

    private val slotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.board_slot)
    }

    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }

    private val shinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var boardLeft = 0f
    private var boardTop = 0f
    private var cellSize = 0f
    private var rows = 0
    private var cols = 0
    private val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun render(board: Array<IntArray>, selectedTile: GridPos?) {
        this.board = board
        this.selectedTile = selectedTile
        rows = board.size
        cols = if (board.isEmpty()) 0 else board[0].size
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (board.isEmpty()) {
            return
        }

        val horizontalSpace = width - paddingLeft - paddingRight
        val verticalSpace = height - paddingTop - paddingBottom
        cellSize = min(horizontalSpace / cols.toFloat(), verticalSpace / rows.toFloat())

        val boardWidth = cellSize * cols
        val boardHeight = cellSize * rows
        boardLeft = paddingLeft + (horizontalSpace - boardWidth) / 2f
        boardTop = paddingTop + (verticalSpace - boardHeight) / 2f

        val boardRect = RectF(boardLeft, boardTop, boardLeft + boardWidth, boardTop + boardHeight)
        canvas.drawRoundRect(boardRect, 32f, 32f, boardPaint)

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val tileRect = tileRectFor(row, col)
                canvas.drawRoundRect(tileRect, 24f, 24f, slotPaint)

                val tile = board[row][col]
                if (tile != 0) {
                    drawFancyTile(canvas, tile, tileRect)
                }

                if (selectedTile?.row == row && selectedTile?.col == col) {
                    val selectionInset = cellSize * 0.05f
                    val selectionRect = RectF(
                        tileRect.left + selectionInset,
                        tileRect.top + selectionInset,
                        tileRect.right - selectionInset,
                        tileRect.bottom - selectionInset,
                    )
                    canvas.drawRoundRect(selectionRect, 22f, 22f, outlinePaint)
                }
            }
        }
    }

    private fun drawFancyTile(canvas: Canvas, tile: Int, rect: RectF) {
        val inset = cellSize * 0.12f
        val gemRect = RectF(
            rect.left + inset,
            rect.top + inset,
            rect.right - inset,
            rect.bottom - inset,
        )

        val baseColor = colorForTile(tile)
        val lighterColor = lightenColor(baseColor)
        val darkerColor = darkenColor(baseColor)

        // Main Gradient Body
        tilePaint.shader = LinearGradient(
            gemRect.left, gemRect.top, gemRect.right, gemRect.bottom,
            intArrayOf(lighterColor, baseColor, darkerColor),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(gemRect, 24f, 24f, tilePaint)
        tilePaint.shader = null

        // Top Shine
        val shineInset = inset * 0.5f
        val shineRect = RectF(
            gemRect.left + shineInset,
            gemRect.top + shineInset,
            gemRect.right - shineInset,
            gemRect.top + gemRect.height() * 0.4f,
        )
        shinePaint.color = Color.argb(100, 255, 255, 255)
        canvas.drawRoundRect(shineRect, 18f, 18f, shinePaint)
    }

    private fun lightenColor(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] = (hsv[2] * 1.3f).coerceAtMost(1.0f)
        hsv[1] = (hsv[1] * 0.7f).coerceAtLeast(0.0f)
        return Color.HSVToColor(hsv)
    }

    private fun darkenColor(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] = (hsv[2] * 0.7f).coerceAtLeast(0.0f)
        return Color.HSVToColor(hsv)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP || board.isEmpty()) {
            return true
        }

        val boardWidth = cellSize * cols
        val boardHeight = cellSize * rows
        if (event.x < boardLeft || event.x > boardLeft + boardWidth) {
            return true
        }
        if (event.y < boardTop || event.y > boardTop + boardHeight) {
            return true
        }

        val col = ((event.x - boardLeft) / cellSize).toInt()
        val drawRow = ((event.y - boardTop) / cellSize).toInt()
        val row = rows - 1 - drawRow

        if (row in 0 until rows && col in 0 until cols) {
            performClick()
            onTileTapped?.invoke(GridPos(row, col))
        }

        return true
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private fun tileRectFor(row: Int, col: Int): RectF {
        val drawRow = rows - 1 - row
        val left = boardLeft + col * cellSize
        val top = boardTop + drawRow * cellSize
        return RectF(left, top, left + cellSize, top + cellSize)
    }

    private fun colorForTile(tile: Int): Int {
        return when (tile) {
            1 -> ContextCompat.getColor(context, R.color.tile_red)
            2 -> ContextCompat.getColor(context, R.color.tile_blue)
            3 -> ContextCompat.getColor(context, R.color.tile_green)
            4 -> ContextCompat.getColor(context, R.color.tile_yellow)
            5 -> ContextCompat.getColor(context, R.color.tile_purple)
            else -> ContextCompat.getColor(context, R.color.tile_empty)
        }
    }
}
