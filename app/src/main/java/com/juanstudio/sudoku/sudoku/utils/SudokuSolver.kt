package com.juanstudio.sudoku.sudoku.utils

import com.juanstudio.sudoku.sudoku.model.SudokuBoard

/**
 * Utility class for solving Sudoku puzzles using backtracking algorithm.
 */
class SudokuSolver {
    
    /**
     * Solves the Sudoku puzzle using backtracking algorithm.
     * @param board The Sudoku board to solve
     * @return true if the puzzle is solved, false otherwise
     */
    fun solve(board: SudokuBoard): Boolean {
        val emptyCell = findEmptyCell(board)
        
        // If no empty cell is found, the puzzle is solved
        if (emptyCell == null) {
            return true
        }
        
        val (row, col) = emptyCell
        
        // Try numbers 1-9 in the empty cell
        for (num in 1..9) {
            if (isSafe(board, row, col, num)) {
                // Place the number
                board.setValue(row, col, num)
                
                // Recursively solve the rest of the puzzle
                if (solve(board)) {
                    return true
                }
                
                // If placing num doesn't lead to a solution, backtrack
                board.setValue(row, col, 0)
            }
        }
        
        // Trigger backtracking
        return false
    }
    
    /**
     * Finds the first empty cell in the board.
     * @param board The Sudoku board to search
     * @return Pair of (row, col) of the empty cell, or null if no empty cell is found
     */
    private fun findEmptyCell(board: SudokuBoard): Pair<Int, Int>? {
        for (i in 0..8) {
            for (j in 0..8) {
                if (board.getValue(i, j) == 0) {
                    return Pair(i, j)
                }
            }
        }
        return null
    }
    
    /**
     * Checks if it's safe to place a number in a given cell.
     * @param board The Sudoku board
     * @param row The row index
     * @param col The column index
     * @param num The number to place
     * @return true if it's safe to place the number, false otherwise
     */
    private fun isSafe(board: SudokuBoard, row: Int, col: Int, num: Int): Boolean {
        // Check if the number is already in the row
        for (j in 0..8) {
            if (board.getValue(row, j) == num) {
                return false
            }
        }
        
        // Check if the number is already in the column
        for (i in 0..8) {
            if (board.getValue(i, col) == num) {
                return false
            }
        }
        
        // Check if the number is already in the 3x3 box
        val boxRowStart = row - row % 3
        val boxColStart = col - col % 3
        
        for (i in 0..2) {
            for (j in 0..2) {
                if (board.getValue(boxRowStart + i, boxColStart + j) == num) {
                    return false
                }
            }
        }
        
        return true
    }
    
    /**
     * Counts the number of solutions for a given Sudoku puzzle.
     * @param board The Sudoku board
     * @return The number of possible solutions
     */
    fun countSolutions(board: SudokuBoard): Int {
        val emptyCell = findEmptyCell(board)
        
        // If no empty cell is found, we found one solution
        if (emptyCell == null) {
            return 1
        }
        
        val (row, col) = emptyCell
        var count = 0
        
        // Try numbers 1-9 in the empty cell
        for (num in 1..9) {
            if (isSafe(board, row, col, num)) {
                // Place the number
                board.setValue(row, col, num)
                
                // Recursively count solutions
                count += countSolutions(board)
                
                // Backtrack
                board.setValue(row, col, 0)
                
                // Early termination if we already found more than one solution
                if (count > 1) {
                    return count
                }
            }
        }
        
        return count
    }
}
