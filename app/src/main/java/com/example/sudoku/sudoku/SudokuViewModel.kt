package com.example.sudoku.sudoku

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudoku.sudoku.model.SudokuBoard
import com.example.sudoku.sudoku.model.DifficultyLevel
import com.example.sudoku.sudoku.utils.SudokuGenerator
import com.example.sudoku.sudoku.utils.SudokuSolver
import com.example.sudoku.sudoku.model.GameHistory
import com.example.sudoku.sudoku.repository.GameHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Sudoku game.
 */
class SudokuViewModel : ViewModel() {
    private val generator = SudokuGenerator()
    private val solver = SudokuSolver()
    
    // Game state
    private val _currentBoard = MutableLiveData<SudokuBoard>()
    val currentBoard: LiveData<SudokuBoard> = _currentBoard
    
    private val _selectedCell = MutableLiveData<Pair<Int, Int>?>(null)
    val selectedCell: LiveData<Pair<Int, Int>?> = _selectedCell
    
    private val _gameStatus = MutableLiveData<GameStatus>(GameStatus.NOT_STARTED)
    val gameStatus: LiveData<GameStatus> = _gameStatus
    
    private val _difficulty = MutableLiveData<DifficultyLevel>()
    val difficulty: LiveData<DifficultyLevel> = _difficulty
    
    private val _timerSeconds = MutableLiveData<Int>(0)
    val timerSeconds: LiveData<Int> = _timerSeconds
    
    private val _invalidCells = MutableLiveData<Set<Pair<Int, Int>>>(emptySet())
    val invalidCells: LiveData<Set<Pair<Int, Int>>> = _invalidCells
    
    private val _completedNumbers = MutableLiveData<Set<Int>>(emptySet())
    val completedNumbers: LiveData<Set<Int>> = _completedNumbers
    
    private val _isNoteMode = MutableLiveData<Boolean>(false)
    val isNoteMode: LiveData<Boolean> = _isNoteMode
    
    private var startTime: Long = 0
    private var isTimerRunning = false
    private var wrongTryCount = 0
    
    init {
        newGame(DifficultyLevel.BEGINNER)
    }
    
    /**
     * Starts a new game with the specified difficulty level.
     * Only starts a new game if there isn't already one in progress,
     * or if a different difficulty is requested.
     */
    fun newGame(difficulty: DifficultyLevel) {
        // If there's already a game in progress with the same difficulty, don't start a new one
        if (_gameStatus.value == GameStatus.PLAYING || _gameStatus.value == GameStatus.PAUSED) {
            if (_difficulty.value == difficulty) {
                // Resume the existing game
                if (_gameStatus.value == GameStatus.PAUSED) {
                    resumeGame()
                }
                return
            } else {
                // Stop the current game before starting a new one with different difficulty
                stopTimer()
            }
        }
        
        viewModelScope.launch {
            _gameStatus.value = GameStatus.GENERATING
            _difficulty.value = difficulty
            
            // Reset game statistics for new game
            wrongTryCount = 0
            _timerSeconds.value = 0
            
            // Generate puzzle on background thread
            val puzzle = withContext(Dispatchers.IO) {
                try {
                    generator.generatePuzzle(difficulty)
                } catch (e: Exception) {
                    Log.e("SudokuViewModel", "Error generating puzzle", e)
                    // Fallback to a simple puzzle
                    createFallbackPuzzle()
                }
            }
            
            _currentBoard.value = puzzle
            _selectedCell.value = null
            _invalidCells.value = emptySet()
            _completedNumbers.value = emptySet()
            _gameStatus.value = GameStatus.PLAYING
            startTimer()
        }
    }
    
    /**
     * Gets the current wrong try count.
     */
    fun getWrongTryCount(): Int {
        return wrongTryCount
    }
    
    /**
     * Saves the game history when the game is completed.
     * Note: This method should be called from the Fragment with a valid context.
     */
    fun saveGameHistory(context: android.content.Context) {
        val repository = GameHistoryRepository.getInstance(context)
        val history = GameHistory(
            finishTime = _timerSeconds.value?.toLong() ?: 0,
            wrongTryCount = wrongTryCount,
            difficulty = _difficulty.value ?: DifficultyLevel.BEGINNER
        )
        repository.saveGameHistory(history)
    }
    
    /**
     * Selects a cell on the board.
     */
    fun selectCell(row: Int, col: Int) {
        if (row in 0..8 && col in 0..8) {
            _selectedCell.value = Pair(row, col)
        }
    }
    
    /**
     * Places a number in the selected cell.
     */
    fun placeNumber(number: Int) {
        val selected = _selectedCell.value
        if (selected != null && _currentBoard.value != null) {
            val (row, col) = selected
            val board = _currentBoard.value!!
            
            // Only allow placing numbers in non-fixed cells
            if (!board.isFixedCell(row, col)) {
                // If in note mode, add/remove note instead of placing number
                if (_isNoteMode.value == true) {
                    // Toggle note
                    if (board.hasNote(row, col, number)) {
                        board.removeNote(row, col, number)
                    } else {
                        board.setNote(row, col, number)
                    }
                    _currentBoard.value = board
                } else {
                    // Clear any notes when placing a number
                    board.clearNotes(row, col)
                    
                    // Check if the number is correct by comparing with the solution
                    val solutionBoard = board.copy()
                    if (solver.solve(solutionBoard)) {
                        val correctValue = solutionBoard.getValue(row, col)
                        if (number != correctValue && number != 0) {
                            // Wrong try - increment counter
                            wrongTryCount++
                        }
                    }
                    
                    board.setValue(row, col, number)
                    _currentBoard.value = board
                    
                    // Validate the board and highlight invalid cells
                    validateBoard()
                    
                    // Check for completed numbers
                    checkCompletedNumbers()
                    
                    // Check if the game is solved
                    if (board.isSolved()) {
                        stopTimer()
                        _gameStatus.value = GameStatus.COMPLETED
                    }
                }
            }
        }
    }
    
    /**
     * Toggles note mode.
     */
    fun toggleNoteMode() {
        _isNoteMode.value = !(_isNoteMode.value ?: false)
    }
    
    /**
     * Checks for completed numbers (numbers that appear 9 times on the board).
     */
    private fun checkCompletedNumbers() {
        val board = _currentBoard.value ?: return
        val count = IntArray(10) // Index 0-9
        
        // Count occurrences of each number
        for (i in 0..8) {
            for (j in 0..8) {
                val value = board.getValue(i, j)
                if (value != 0) {
                    count[value]++
                }
            }
        }
        
        // Find completed numbers (9 occurrences)
        val completed = mutableSetOf<Int>()
        for (i in 1..9) {
            if (count[i] == 9) {
                completed.add(i)
            }
        }
        
        _completedNumbers.value = completed
    }
    
    /**
     * Validates the current board and updates invalid cells.
     */
    private fun validateBoard() {
        val board = _currentBoard.value ?: return
        val invalid = mutableSetOf<Pair<Int, Int>>()
        
        // Check rows
        for (row in 0..8) {
            val seen = BooleanArray(10) // Index 0-9
            for (col in 0..8) {
                val value = board.getValue(row, col)
                if (value != 0) {
                    if (seen[value]) {
                        // Find all cells with this value in the row
                        for (c in 0..8) {
                            if (board.getValue(row, c) == value) {
                                invalid.add(Pair(row, c))
                            }
                        }
                    }
                    seen[value] = true
                }
            }
        }
        
        // Check columns
        for (col in 0..8) {
            val seen = BooleanArray(10) // Index 0-9
            for (row in 0..8) {
                val value = board.getValue(row, col)
                if (value != 0) {
                    if (seen[value]) {
                        // Find all cells with this value in the column
                        for (r in 0..8) {
                            if (board.getValue(r, col) == value) {
                                invalid.add(Pair(r, col))
                            }
                        }
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
                        val value = board.getValue(row, col)
                        if (value != 0) {
                            if (seen[value]) {
                                // Find all cells with this value in the box
                                for (r in 0..2) {
                                    for (c in 0..2) {
                                        val boxRowCheck = boxRow * 3 + r
                                        val boxColCheck = boxCol * 3 + c
                                        if (board.getValue(boxRowCheck, boxColCheck) == value) {
                                            invalid.add(Pair(boxRowCheck, boxColCheck))
                                        }
                                    }
                                }
                            }
                            seen[value] = true
                        }
                    }
                }
            }
        }
        
        _invalidCells.value = invalid
    }
    
    /**
     * Clears the selected cell.
     */
    fun clearCell() {
        val selected = _selectedCell.value
        if (selected != null && _currentBoard.value != null) {
            val (row, col) = selected
            val board = _currentBoard.value!!
            
            // Only allow clearing non-fixed cells
            if (!board.isFixedCell(row, col)) {
                board.setValue(row, col, 0)
                _currentBoard.value = board
                
                // Re-validate the board to remove any invalid cell highlighting
                validateBoard()
            }
        }
    }
    
    /**
     * Provides a hint for the selected cell.
     */
    fun getHint() {
        val selected = _selectedCell.value
        if (selected != null && _currentBoard.value != null) {
            val (row, col) = selected
            val board = _currentBoard.value!!
            
            // Only provide hints for non-fixed cells
            if (!board.isFixedCell(row, col) && board.getValue(row, col) == 0) {
                // Solve a copy of the board to get the correct value
                val solutionBoard = board.copy()
                if (solver.solve(solutionBoard)) {
                    val correctValue = solutionBoard.getValue(row, col)
                    board.setValue(row, col, correctValue)
                    _currentBoard.value = board
                    
                    // Check if the game is solved
                    if (board.isSolved()) {
                        stopTimer()
                        _gameStatus.value = GameStatus.COMPLETED
                    }
                }
            }
        }
    }
    
    /**
     * Starts the game timer.
     */
    private fun startTimer() {
        startTime = System.currentTimeMillis()
        isTimerRunning = true
        _timerSeconds.value = 0
        
        viewModelScope.launch {
            while (isTimerRunning) {
                kotlinx.coroutines.delay(1000)
                if (isTimerRunning) {
                    _timerSeconds.postValue(((System.currentTimeMillis() - startTime) / 1000).toInt())
                }
            }
        }
    }
    
    /**
     * Stops the game timer.
     */
    private fun stopTimer() {
        isTimerRunning = false
    }
    
    /**
     * Pauses the game.
     */
    fun pauseGame() {
        if (_gameStatus.value == GameStatus.PLAYING) {
            stopTimer()
            _gameStatus.value = GameStatus.PAUSED
        }
    }
    
    /**
     * Resumes the game.
     */
    fun resumeGame() {
        if (_gameStatus.value == GameStatus.PAUSED) {
            startTimer()
            _gameStatus.value = GameStatus.PLAYING
        }
    }
    
    /**
     * Creates a simple fallback puzzle in case generation fails.
     */
    private fun createFallbackPuzzle(): SudokuBoard {
        val board = SudokuBoard()
        // Simple predefined puzzle
        val puzzle = arrayOf(
            intArrayOf(5, 3, 0, 0, 7, 0, 0, 0, 0),
            intArrayOf(6, 0, 0, 1, 9, 5, 0, 0, 0),
            intArrayOf(0, 9, 8, 0, 0, 0, 0, 6, 0),
            intArrayOf(8, 0, 0, 0, 6, 0, 0, 0, 3),
            intArrayOf(4, 0, 0, 8, 0, 3, 0, 0, 1),
            intArrayOf(7, 0, 0, 0, 2, 0, 0, 0, 6),
            intArrayOf(0, 6, 0, 0, 0, 0, 2, 8, 0),
            intArrayOf(0, 0, 0, 4, 1, 9, 0, 0, 5),
            intArrayOf(0, 0, 0, 0, 8, 0, 0, 7, 9)
        )
        
        for (i in 0..8) {
            for (j in 0..8) {
                board.setValue(i, j, puzzle[i][j])
                board.originalPuzzle[i][j] = puzzle[i][j]
                board.setFixedCell(i, j, puzzle[i][j] != 0)
            }
        }
        
        return board
    }
    
    /**
     * Gives up on the current game.
     * Stops the timer and reveals the solution.
     */
    fun giveUp() {
        // Stop the timer
        stopTimer()
        
        // Set game status to gave up
        _gameStatus.value = GameStatus.GAVE_UP
        
        // Reveal the solution by solving the puzzle
        val board = _currentBoard.value
        if (board != null) {
            val solutionBoard = board.copy()
            if (solver.solve(solutionBoard)) {
                _currentBoard.value = solutionBoard
            }
        }
    }
}

/**
 * Enum representing the game status.
 */
enum class GameStatus {
    NOT_STARTED,
    GENERATING,
    PLAYING,
    PAUSED,
    COMPLETED,
    GAVE_UP
}
