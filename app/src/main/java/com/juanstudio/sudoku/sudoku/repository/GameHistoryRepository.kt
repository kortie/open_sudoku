package com.juanstudio.sudoku.sudoku.repository

import android.content.Context
import android.content.SharedPreferences
import com.juanstudio.sudoku.sudoku.model.GameHistory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Repository for managing game history data.
 */
class GameHistoryRepository private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("game_history", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        @Volatile
        private var INSTANCE: GameHistoryRepository? = null
        
        fun getInstance(context: Context): GameHistoryRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GameHistoryRepository(context).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Saves a game history record.
     */
    fun saveGameHistory(history: GameHistory) {
        val historyList = getGameHistory().toMutableList()
        historyList.add(history)
        
        // Sort by score descending (high score first)
        historyList.sortByDescending { it.calculateScore() }
        
        // Keep only top 10 records
        if (historyList.size > 10) {
            historyList.removeAt(historyList.size - 1)
        }
        
        val json = gson.toJson(historyList)
        sharedPreferences.edit().putString("history_list", json).apply()
    }
    
    /**
     * Retrieves all game history records, sorted by score (highest first).
     */
    fun getGameHistory(): List<GameHistory> {
        val json = sharedPreferences.getString("history_list", null)
        return if (json != null) {
            val type = object : TypeToken<List<GameHistory>>() {}.type
            val historyList: List<GameHistory> = gson.fromJson(json, type)
            historyList.sortedByDescending { it.calculateScore() }
        } else {
            emptyList()
        }
    }
    
    /**
     * Clears all game history.
     */
    fun clearHistory() {
        sharedPreferences.edit().remove("history_list").apply()
    }
}
