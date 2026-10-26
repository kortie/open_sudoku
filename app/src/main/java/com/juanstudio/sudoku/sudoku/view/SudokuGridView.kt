package com.juanstudio.sudoku.sudoku.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.juanstudio.sudoku.R
import com.juanstudio.sudoku.sudoku.model.SudokuBoard
import kotlin.math.min

/**
 * Custom view for displaying and interacting with a Sudoku board.
 */
class SudokuGridView : View {
    // Paint objects for drawing
    private val gridPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
        color = context.getColor(R.color.board_line_thin)
    }
    
    private val thickGridPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        color = context.getColor(R.color.board_line_thick)
    }
    
    private val textPaint = Paint().apply {
        textSize = 48f
        isAntiAlias = true
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    
    private val noteTextPaint = Paint().apply {
        textSize = 24f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        color = context.getColor(R.color.digit_note)
    }

    private val boardPaint = Paint().apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.board_light)
    }
    
    private val fixedCellPaint = Paint().apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.cell_fixed)
    }
    
    private val selectedCellPaint = Paint().apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.cell_selected)
    }

    private val sameValueCellPaint = Paint().apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.cell_highlight)
    }
    
    private val conflictingCellPaint = Paint().apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.cell_conflict)
    }
    
    // Game data
    private var board: SudokuBoard? = null
    private var selectedCell: Pair<Int, Int>? = null
    private var conflictingCells = mutableSetOf<Pair<Int, Int>>()
    private var sameValueCells = mutableSetOf<Pair<Int, Int>>()
    private var invalidCells = mutableSetOf<Pair<Int, Int>>()
    
    // Cell dimensions
    private var cellSize = 0f
    private val textBounds = Rect()

    // Rounded board outline
    private val boardRect = RectF()
    private val boardClip = Path()
    private val cornerRadius = 18f * resources.displayMetrics.density
    
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    
    /**
     * Sets the Sudoku board to display.
     */
    fun setBoard(board: SudokuBoard?) {
        this.board = board
        invalidate()
    }
    
    /**
     * Sets the selected cell.
     */
    fun setSelectedCell(cell: Pair<Int, Int>?) {
        this.selectedCell = cell
        invalidate()
    }
    
    /**
     * Sets the conflicting cells to highlight.
     */
    fun setConflictingCells(cells: Set<Pair<Int, Int>>) {
        this.conflictingCells = cells.toMutableSet()
        invalidate()
    }
    
    /**
     * Sets the cells with the same value to highlight.
     */
    fun setSameValueCells(cells: Set<Pair<Int, Int>>) {
        this.sameValueCells = cells.toMutableSet()
        invalidate()
    }
    
    /**
     * Sets the invalid cells to highlight.
     */
    fun setInvalidCells(cells: Set<Pair<Int, Int>>) {
        this.invalidCells = cells.toMutableSet()
        invalidate()
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Use the smaller of width or height to ensure square cells
        val size = min(w, h)
        cellSize = size / 9f
        textPaint.textSize = cellSize * 0.6f
        noteTextPaint.textSize = cellSize * 0.28f

        val inset = thickGridPaint.strokeWidth / 2
        boardRect.set(inset, inset, size - inset, size - inset)
        boardClip.reset()
        boardClip.addRoundRect(boardRect, cornerRadius, cornerRadius, Path.Direction.CW)
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        
        // Calculate the size for a square grid (9 cells)
        val desiredSize = 360 // 9 cells * 40px per cell
        
        // Determine the size that will make the view square
        val size = when {
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY -> {
                // Both dimensions are fixed, use the smaller one
                min(width, height)
            }
            widthMode == MeasureSpec.EXACTLY -> {
                // Only width is fixed, use it but cap it
                min(width, desiredSize)
            }
            heightMode == MeasureSpec.EXACTLY -> {
                // Only height is fixed, use it but cap it
                min(height, desiredSize)
            }
            else -> {
                // Neither is fixed, use our desired size
                desiredSize
            }
        }
        
        // Ensure we don't exceed the available space
        val finalSize = min(size, min(width, height))
        
        setMeasuredDimension(finalSize, finalSize)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (board == null) return
        
        canvas.save()
        canvas.clipPath(boardClip)
        canvas.drawRect(boardRect, boardPaint)
        drawCells(canvas)
        drawGrid(canvas)
        canvas.restore()

        // Rounded outer border on top
        canvas.drawRoundRect(boardRect, cornerRadius, cornerRadius, thickGridPaint)
    }
    
    /**
     * Draws the Sudoku grid lines.
     */
    private fun drawGrid(canvas: Canvas) {
        val size = cellSize * 9

        // Draw thin grid lines (inner cell borders only; outer border is rounded)
        for (i in 1..8) {
            if (i % 3 == 0) continue
            val start = i * cellSize
            canvas.drawLine(0f, start, size, start, gridPaint)
            canvas.drawLine(start, 0f, start, size, gridPaint)
        }
        
        // Draw thick grid lines for 3x3 boxes
        for (i in 1..2) {
            val start = i * 3 * cellSize
            canvas.drawLine(0f, start, size, start, thickGridPaint)
            canvas.drawLine(start, 0f, start, size, thickGridPaint)
        }
    }
    
    /**
     * Draws the cell values and backgrounds.
     */
    private fun drawCells(canvas: Canvas) {
        val board = this.board ?: return
        
        for (i in 0..8) {
            for (j in 0..8) {
                val left = j * cellSize
                val top = i * cellSize
                val right = left + cellSize
                val bottom = top + cellSize
                
                // Draw cell background
                drawCellBackground(canvas, i, j, left, top, right, bottom)
                
                // Draw cell value or notes
                val value = board.getValue(i, j)
                if (value != 0) {
                    drawCellValue(canvas, i, j, value, left, top, right, bottom)
                } else {
                    // Draw notes if any
                    drawCellNotes(canvas, i, j, left, top, right, bottom)
                }
            }
        }
    }
    
    /**
     * Draws the notes for a cell.
     */
    private fun drawCellNotes(
        canvas: Canvas,
        row: Int,
        col: Int,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) {
        val board = this.board ?: return
        
        // Get notes for this cell
        val notes = board.getNotes(row, col)
        if (notes.isEmpty()) return
        
        // Calculate positions for 3x3 grid of notes within the cell
        val noteCellWidth = cellSize / 3
        val noteCellHeight = cellSize / 3
        
        for (note in notes) {
            // Calculate position for this note (1-9 grid)
            val noteRow = (note - 1) / 3  // 0, 1, 2
            val noteCol = (note - 1) % 3  // 0, 1, 2
            
            // Calculate center position for this note
            val noteCenterX = left + (noteCol + 0.5f) * noteCellWidth
            val noteCenterY = top + (noteRow + 0.5f) * noteCellHeight + noteTextPaint.textSize / 3
            
            // Draw the note
            canvas.drawText(
                note.toString(),
                noteCenterX,
                noteCenterY,
                noteTextPaint
            )
        }
    }
    
    /**
     * Draws the background for a cell.
     */
    private fun drawCellBackground(
        canvas: Canvas,
        row: Int,
        col: Int,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) {
        val board = this.board ?: return
        
        // Draw invalid cell background (highest priority)
        if (invalidCells.contains(Pair(row, col))) {
            canvas.drawRect(left, top, right, bottom, conflictingCellPaint)
            return
        }
        
        // Draw selected cell background
        if (selectedCell?.first == row && selectedCell?.second == col) {
            canvas.drawRect(left, top, right, bottom, selectedCellPaint)
            return
        }
        
        // Draw same value cell background
        if (sameValueCells.contains(Pair(row, col))) {
            canvas.drawRect(left, top, right, bottom, sameValueCellPaint)
            return
        }
        
        // Draw conflicting cell background
        if (conflictingCells.contains(Pair(row, col))) {
            canvas.drawRect(left, top, right, bottom, conflictingCellPaint)
            return
        }
        
        // Draw fixed cell background
        if (board.isFixedCell(row, col)) {
            canvas.drawRect(left, top, right, bottom, fixedCellPaint)
        }
    }
    
    /**
     * Draws the value for a cell.
     */
    private fun drawCellValue(
        canvas: Canvas,
        row: Int,
        col: Int,
        value: Int,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) {
        val board = this.board ?: return
        val centerX = (left + right) / 2
        val centerY = (top + bottom) / 2
        
        // Set text color based on whether it's a fixed cell
        textPaint.color = if (board.isFixedCell(row, col)) {
            context.getColor(R.color.digit_fixed)
        } else {
            context.getColor(R.color.digit_user)
        }
        
        // Get text bounds to center it properly
        val text = value.toString()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        
        // Draw the text centered in the cell
        canvas.drawText(
            text,
            centerX,
            centerY + textBounds.height() / 2f,
            textPaint
        )
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val col = (event.x / cellSize).toInt()
            val row = (event.y / cellSize).toInt()
            
            if (row in 0..8 && col in 0..8) {
                performClick()
                onCellClicked(row, col)
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    
    /**
     * Called when a cell is clicked.
     * Override this method to handle cell clicks.
     */
    fun onCellClicked(row: Int, col: Int) {
        // Default implementation does nothing
        // Override in subclasses or set a listener
    }
    
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
