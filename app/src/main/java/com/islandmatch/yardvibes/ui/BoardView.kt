package com.islandmatch.yardvibes.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.islandmatch.yardvibes.R
import com.islandmatch.yardvibes.game.GridPos
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

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
        strokeWidth = 8f
    }

    private val shinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val tileShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(55, 0, 53, 84)
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val symbolPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(235, 255, 255, 255)
        style = Paint.Style.FILL
    }

    private val symbolStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(150, 255, 255, 255)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var boardLeft = 0f
    private var boardTop = 0f
    private var cellSize = 0f
    private var rows = 0
    private var cols = 0
    private var pulsePhase = 0f

    private val pulseAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 2200L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            pulsePhase = it.animatedValue as Float
            postInvalidateOnAnimation()
        }
    }

    fun render(board: Array<IntArray>, selectedTile: GridPos?) {
        this.board = board
        this.selectedTile = selectedTile
        rows = board.size
        cols = if (board.isEmpty()) 0 else board[0].size
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!pulseAnimator.isStarted) {
            pulseAnimator.start()
        } else if (pulseAnimator.isPaused) {
            pulseAnimator.resume()
        }
    }

    override fun onDetachedFromWindow() {
        pulseAnimator.cancel()
        super.onDetachedFromWindow()
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
                    drawFancyTile(
                        canvas = canvas,
                        tile = tile,
                        rect = tileRect,
                        row = row,
                        col = col,
                        isSelected = selectedTile?.row == row && selectedTile?.col == col,
                    )
                }

                if (selectedTile?.row == row && selectedTile?.col == col) {
                    val selectionInset = cellSize * 0.06f
                    val selectionRect = RectF(
                        tileRect.left + selectionInset,
                        tileRect.top + selectionInset,
                        tileRect.right - selectionInset,
                        tileRect.bottom - selectionInset,
                    )
                    outlinePaint.strokeWidth = cellSize * (0.06f + 0.01f * sin((pulsePhase * PI * 2.0) + row + col).toFloat())
                    canvas.drawRoundRect(selectionRect, 22f, 22f, outlinePaint)
                }
            }
        }
    }

    private fun drawFancyTile(
        canvas: Canvas,
        tile: Int,
        rect: RectF,
        row: Int,
        col: Int,
        isSelected: Boolean,
    ) {
        val phase = (pulsePhase * PI * 2.0) + row * 0.6 + col * 0.35
        val scale = 1f + if (isSelected) {
            0.04f * sin(phase).toFloat()
        } else {
            0.015f * sin(phase).toFloat()
        }

        val inset = cellSize * 0.12f
        val gemRect = RectF(
            rect.left + inset,
            rect.top + inset,
            rect.right - inset,
            rect.bottom - inset,
        )

        canvas.save()
        canvas.scale(scale, scale, rect.centerX(), rect.centerY())

        val shadowRect = RectF(gemRect)
        shadowRect.offset(0f, cellSize * 0.06f)
        canvas.drawRoundRect(shadowRect, 24f, 24f, tileShadowPaint)

        val baseColor = colorForTile(tile)
        val lighterColor = lightenColor(baseColor)
        val darkerColor = darkenColor(baseColor)

        tilePaint.shader = LinearGradient(
            gemRect.left,
            gemRect.top,
            gemRect.right,
            gemRect.bottom,
            intArrayOf(lighterColor, baseColor, darkerColor),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRoundRect(gemRect, 24f, 24f, tilePaint)
        tilePaint.shader = null

        borderPaint.color = Color.argb(100, 255, 255, 255)
        canvas.drawRoundRect(gemRect, 24f, 24f, borderPaint)

        val shineRect = RectF(
            gemRect.left + inset * 0.4f,
            gemRect.top + inset * 0.45f,
            gemRect.right - inset * 0.4f,
            gemRect.top + gemRect.height() * 0.42f,
        )
        val shineAlpha = if (isSelected) 140 else 85 + (20 * sin(phase + 1.1)).toInt()
        shinePaint.color = Color.argb(shineAlpha.coerceIn(70, 145), 255, 255, 255)
        canvas.drawRoundRect(shineRect, 18f, 18f, shinePaint)

        drawTileSymbol(canvas, tile, gemRect)
        canvas.restore()
    }

    private fun drawTileSymbol(canvas: Canvas, tile: Int, rect: RectF) {
        symbolStrokePaint.strokeWidth = cellSize * 0.05f
        when (tile) {
            1 -> drawFlower(canvas, rect)
            2 -> drawSeaGlass(canvas, rect)
            3 -> drawLeaf(canvas, rect)
            4 -> drawMango(canvas, rect)
            5 -> drawFan(canvas, rect)
        }
    }

    private fun drawFlower(canvas: Canvas, rect: RectF) {
        val centerX = rect.centerX()
        val centerY = rect.centerY()
        val petalDistance = rect.width() * 0.18f
        val petalRadius = rect.width() * 0.15f

        for (index in 0 until 5) {
            val angle = ((index * 72.0) - 90.0) * PI / 180.0
            canvas.drawCircle(
                (centerX + kotlin.math.cos(angle).toFloat() * petalDistance),
                (centerY + kotlin.math.sin(angle).toFloat() * petalDistance),
                petalRadius,
                symbolPaint,
            )
        }

        shinePaint.color = Color.argb(200, 255, 232, 190)
        canvas.drawCircle(centerX, centerY, rect.width() * 0.11f, shinePaint)
    }

    private fun drawSeaGlass(canvas: Canvas, rect: RectF) {
        val diamond = Path().apply {
            moveTo(rect.centerX(), rect.top + rect.height() * 0.12f)
            lineTo(rect.right - rect.width() * 0.18f, rect.centerY())
            lineTo(rect.centerX(), rect.bottom - rect.height() * 0.12f)
            lineTo(rect.left + rect.width() * 0.18f, rect.centerY())
            close()
        }
        canvas.drawPath(diamond, symbolPaint)
        canvas.drawPath(diamond, symbolStrokePaint)
    }

    private fun drawLeaf(canvas: Canvas, rect: RectF) {
        val leaf = Path().apply {
            moveTo(rect.centerX(), rect.top + rect.height() * 0.14f)
            cubicTo(
                rect.right - rect.width() * 0.1f,
                rect.top + rect.height() * 0.28f,
                rect.right - rect.width() * 0.14f,
                rect.bottom - rect.height() * 0.2f,
                rect.centerX(),
                rect.bottom - rect.height() * 0.12f,
            )
            cubicTo(
                rect.left + rect.width() * 0.16f,
                rect.bottom - rect.height() * 0.22f,
                rect.left + rect.width() * 0.08f,
                rect.top + rect.height() * 0.36f,
                rect.centerX(),
                rect.top + rect.height() * 0.14f,
            )
            close()
        }
        canvas.drawPath(leaf, symbolPaint)
        canvas.drawPath(leaf, symbolStrokePaint)
        canvas.drawLine(
            rect.centerX(),
            rect.top + rect.height() * 0.22f,
            rect.centerX(),
            rect.bottom - rect.height() * 0.18f,
            symbolStrokePaint,
        )
    }

    private fun drawMango(canvas: Canvas, rect: RectF) {
        val fruitRect = RectF(
            rect.left + rect.width() * 0.17f,
            rect.top + rect.height() * 0.2f,
            rect.right - rect.width() * 0.17f,
            rect.bottom - rect.height() * 0.14f,
        )
        canvas.drawArc(fruitRect, 210f, 300f, true, symbolPaint)
        canvas.drawArc(fruitRect, 210f, 300f, true, symbolStrokePaint)

        val leaf = Path().apply {
            moveTo(rect.centerX() + rect.width() * 0.08f, rect.top + rect.height() * 0.16f)
            quadTo(
                rect.centerX() + rect.width() * 0.22f,
                rect.top + rect.height() * 0.04f,
                rect.centerX() + rect.width() * 0.28f,
                rect.top + rect.height() * 0.2f,
            )
            quadTo(
                rect.centerX() + rect.width() * 0.16f,
                rect.top + rect.height() * 0.22f,
                rect.centerX() + rect.width() * 0.08f,
                rect.top + rect.height() * 0.16f,
            )
            close()
        }
        canvas.drawPath(leaf, symbolPaint)
    }

    private fun drawFan(canvas: Canvas, rect: RectF) {
        val fanRect = RectF(
            rect.left + rect.width() * 0.18f,
            rect.top + rect.height() * 0.24f,
            rect.right - rect.width() * 0.18f,
            rect.bottom - rect.height() * 0.1f,
        )
        canvas.drawArc(fanRect, 200f, 140f, true, symbolPaint)
        canvas.drawArc(fanRect, 200f, 140f, true, symbolStrokePaint)

        val baseX = rect.centerX()
        val baseY = rect.bottom - rect.height() * 0.18f
        for (index in -2..2) {
            val tipX = baseX + index * rect.width() * 0.11f
            val tipY = rect.top + rect.height() * 0.33f
            canvas.drawLine(baseX, baseY, tipX, tipY, symbolStrokePaint)
        }
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
