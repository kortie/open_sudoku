package com.example.sudoku.sudoku.utils

import com.example.sudoku.sudoku.model.SudokuBoard
import com.example.sudoku.sudoku.model.DifficultyLevel

/**
 * Utility class for generating Sudoku puzzles.
 */
class SudokuGenerator {
    private val solver = SudokuSolver()
    
    /**
     * Generates a new Sudoku puzzle with the specified difficulty level.
     * @param difficulty The difficulty level for the puzzle
     * @return A SudokuBoard representing the generated puzzle
     */
    fun generatePuzzle(difficulty: DifficultyLevel): SudokuBoard {
        // Create a fully solved board
        val solvedBoard = createSolvedBoard()
        
        // Create a copy for the puzzle
        val puzzleBoard = solvedBoard.copy()
        
        // Determine how many cells to remove
        val cellsToRemove = DifficultyLevel.getRandomBlankCount(difficulty)
        
        // Remove cells to create the puzzle
        removeCells(puzzleBoard, cellsToRemove)
        
        // Save the original puzzle
        for (i in 0..8) {
            for (j in 0..8) {
                puzzleBoard.originalPuzzle[i][j] = puzzleBoard.getValue(i, j)
                puzzleBoard.setFixedCell(i, j, puzzleBoard.getValue(i, j) != 0)
            }
        }
        
        return puzzleBoard
    }
    
    /**
     * Creates a fully solved Sudoku board.
     * @return A SudokuBoard representing a fully solved puzzle
     */
    private fun createSolvedBoard(): SudokuBoard {
        val board = SudokuBoard()
        
        // Fill the diagonal 3x3 boxes first (they are independent)
        fillDiagonalBoxes(board)
        
        // Solve the rest of the board
        solver.solve(board)
        
        return board
    }
    
    /**
     * Fills the diagonal 3x3 boxes of the board.
     * These boxes are independent of each other, so we can fill them randomly.
     * @param board The board to fill
     */
    private fun fillDiagonalBoxes(board: SudokuBoard) {
        for (i in 0..2) {
            fillBox(board, i * 3, i * 3)
        }
    }
    
    /**
     * Fills a 3x3 box with random numbers 1-9.
     * @param board The board to fill
     * @param rowStart The starting row of the box
     * @param colStart The starting column of the box
     */
    private fun fillBox(board: SudokuBoard, rowStart: Int, colStart: Int) {
        val numbers = (1..9).toMutableList()
        numbers.shuffle()
        
        var index = 0
        for (i in 0..2) {
            for (j in 0..2) {
                board.setValue(rowStart + i, colStart + j, numbers[index])
                index++
            }
        }
    }
    
    /**
     * Removes cells from the board to create a puzzle.
     * @param board The board to modify
     * @param cellsToRemove The number of cells to remove
     */
    private fun removeCells(board: SudokuBoard, cellsToRemove: Int) {
        var removed = 0
        val positions = mutableListOf<Pair<Int, Int>>()
        
        // Create a list of all positions
        for (i in 0..8) {
            for (j in 0..8) {
                positions.add(Pair(i, j))
            }
        }
        
        // Shuffle the positions
        positions.shuffle()
        
        // Remove cells one by one
        for ((row, col) in positions) {
            if (removed >= cellsToRemove) {
                break
            }
            
            // Skip if cell is already empty
            if (board.getValue(row, col) == 0) {
                continue
            }
            
            // Store the value before removing
            val backup = board.getValue(row, col)
            board.setValue(row, col, 0)
            
            // Check if the puzzle still has a unique solution
            val tempBoard = board.copy()
            if (solver.countSolutions(tempBoard) == 1) {
                // Unique solution, keep the cell empty
                removed++
            } else {
                // Multiple solutions, restore the cell
                board.setValue(row, col, backup)
            }
        }
    }
}
