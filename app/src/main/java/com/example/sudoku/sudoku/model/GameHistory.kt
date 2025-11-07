package com.example.sudoku.sudoku.model

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class representing a game history record.
 */
data class GameHistory(
    val id: Long = System.currentTimeMillis(),
    val finishTime: Long, // in seconds
    val wrongTryCount: Int,
    val difficulty: DifficultyLevel,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable {
    
    /**
     * Calculates the score based on difficulty, wrong tries, and finish time.
     * Formula: 
     * - Base score: difficulty level (Easy=1000, Medium=2000, Hard=3000, Hacker=4000)
     * - Time score: 5000 minus playing time in seconds
     * - Penalty: -50 per wrong try
     */
    fun calculateScore(): Int {
        val baseScore = when (difficulty) {
            DifficultyLevel.BEGINNER -> 1000
            DifficultyLevel.AMATEUR -> 2000
            DifficultyLevel.PRO -> 3000
            DifficultyLevel.HACKER -> 4000
        }
        
        val timeScore = 5000 - finishTime.toInt()
        val penalty = wrongTryCount * 50
        
        return baseScore + timeScore - penalty
    }
    
    /**
     * Formats the timestamp for display.
     */
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    /**
     * Formats the finish time for display (MM:SS).
     */
    fun getFormattedFinishTime(): String {
        val minutes = finishTime / 60
        val seconds = finishTime % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
