package com.example.sudoku.sudoku.model

/**
 * Represents a Sudoku board as a 9x9 grid.
 * Each cell can contain a value from 1-9 or 0 for an empty cell.
 */
class SudokuBoard {
    // 9x9 grid representing the Sudoku board
    // 0 represents an empty cell
    val grid = Array(9) { IntArray(9) }
    
    // Original puzzle (for checking player moves against the initial puzzle)
    val originalPuzzle = Array(9) { IntArray(9) }
    
    // Indicates which cells are fixed (part of the original puzzle)
    val fixedCells = Array(9) { BooleanArray(9) }
    
    // Notes for each cell (bitmask for numbers 1-9)
    val notes = Array(9) { IntArray(9) }
    
    /**
     * Creates a copy of this board.
     */
    fun copy(): SudokuBoard {
        val newBoard = SudokuBoard()
        for (i in 0..8) {
            for (j in 0..8) {
                newBoard.grid[i][j] = this.grid[i][j]
                newBoard.originalPuzzle[i][j] = this.originalPuzzle[i][j]
                newBoard.fixedCells[i][j] = this.fixedCells[i][j]
                newBoard.notes[i][j] = this.notes[i][j]
            }
        }
        return newBoard
    }
    
    /**
     * Sets a note at the specified position.
     */
    fun setNote(row: Int, col: Int, number: Int) {
        if (row in 0..8 && col in 0..8 && number in 1..9) {
            notes[row][col] = notes[row][col] or (1 shl (number - 1))
        }
    }
    
    /**
     * Removes a note at the specified position.
     */
    fun removeNote(row: Int, col: Int, number: Int) {
        if (row in 0..8 && col in 0..8 && number in 1..9) {
            notes[row][col] = notes[row][col] and (1 shl (number - 1)).inv()
        }
    }
    
    /**
     * Checks if a note is set at the specified position.
     */
    fun hasNote(row: Int, col: Int, number: Int): Boolean {
        return if (row in 0..8 && col in 0..8 && number in 1..9) {
            (notes[row][col] and (1 shl (number - 1))) != 0
        } else {
            false
        }
    }
    
    /**
     * Clears all notes at the specified position.
     */
    fun clearNotes(row: Int, col: Int) {
        if (row in 0..8 && col in 0..8) {
            notes[row][col] = 0
        }
    }
    
    /**
     * Gets all notes at the specified position.
     */
    fun getNotes(row: Int, col: Int): Set<Int> {
        val result = mutableSetOf<Int>()
        if (row in 0..8 && col in 0..8) {
            for (i in 1..9) {
                if (hasNote(row, col, i)) {
                    result.add(i)
                }
            }
        }
        return result
    }
    
    /**
     * Sets a value at the specified position.
     */
    fun setValue(row: Int, col: Int, value: Int) {
        if (row in 0..8 && col in 0..8 && value in 0..9) {
            grid[row][col] = value
        }
    }
    
    /**
     * Gets the value at the specified position.
     */
    fun getValue(row: Int, col: Int): Int {
        return if (row in 0..8 && col in 0..8) {
            grid[row][col]
        } else {
            0
        }
    }
    
    /**
     * Checks if a cell is fixed (part of the original puzzle).
     */
    fun isFixedCell(row: Int, col: Int): Boolean {
        return if (row in 0..8 && col in 0..8) {
            fixedCells[row][col]
        } else {
            false
        }
    }
    
    /**
     * Sets a cell as fixed.
     */
    fun setFixedCell(row: Int, col: Int, isFixed: Boolean) {
        if (row in 0..8 && col in 0..8) {
            fixedCells[row][col] = isFixed
        }
    }
    
    /**
     * Clears the board.
     */
    fun clear() {
        for (i in 0..8) {
            for (j in 0..8) {
                grid[i][j] = 0
                originalPuzzle[i][j] = 0
                fixedCells[i][j] = false
            }
        }
    }
    
    /**
     * Checks if the board is completely filled.
     */
    fun isComplete(): Boolean {
        for (i in 0..8) {
            for (j in 0..8) {
                if (grid[i][j] == 0) {
                    return false
                }
            }
        }
        return true
    }
    
    /**
     * Checks if the board is valid (no conflicts).
     */
    fun isValid(): Boolean {
        // Check rows
        for (row in 0..8) {
            val seen = BooleanArray(10) // Index 0-9
            for (col in 0..8) {
                val value = grid[row][col]
                if (value != 0) {
                    if (seen[value]) {
                        return false
                    }
                    seen[value] = true
                }
            }
        }
        
        // Check columns
        for (col in 0..8) {
            val seen = BooleanArray(10) // Index 0-9
            for (row in 0..8) {
                val value = grid[row][col]
                if (value != 0) {
                    if (seen[value]) {
                        return false
                    }
                    seen[value] = true
                }
            }
        }
        
        // Check 3x3 boxes
        for (boxRow in 0..2) {
            for (boxCol in 0..2) {
                val seen = BooleanArray(10) // Index 0-9
                for (i in 0..2) {
                    for (j in 0..2) {
                        val row = boxRow * 3 + i
                        val col = boxCol * 3 + j
                        val value = grid[row][col]
                        if (value != 0) {
                            if (seen[value]) {
                                return false
                            }
                            seen[value] = true
                        }
                    }
                }
            }
        }
        
        return true
    }
    
    /**
     * Checks if the board is solved correctly.
     */
    fun isSolved(): Boolean {
        return isComplete() && isValid()
    }
}
