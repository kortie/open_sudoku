package com.juanstudio.sudoku.sudoku.model

import java.io.Serializable

/**
 * Enum representing the difficulty levels for Sudoku puzzles.
 * Each level specifies the range of blank cells in the puzzle.
 */
enum class DifficultyLevel(
    val minBlanks: Int,
    val maxBlanks: Int,
    val displayName: String
) : Serializable {
    BEGINNER(36, 40, "Beginner"),
    AMATEUR(41, 45, "Amateur"),
    PRO(46, 50, "Pro"),
    HACKER(51, 55, "Hacker");

    companion object {
        /**
         * Gets a random number of blanks for this difficulty level.
         */
        fun getRandomBlankCount(difficulty: DifficultyLevel): Int {
            return (difficulty.minBlanks..difficulty.maxBlanks).random()
        }
    }
}
