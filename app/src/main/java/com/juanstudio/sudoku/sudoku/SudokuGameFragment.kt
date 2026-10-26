package com.juanstudio.sudoku.sudoku

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.juanstudio.sudoku.sudoku.model.DifficultyLevel
import com.juanstudio.sudoku.databinding.FragmentSudokuGameBinding
import com.juanstudio.sudoku.R

/**
 * Fragment for the Sudoku game screen.
 */
class SudokuGameFragment : Fragment() {
    private var _binding: FragmentSudokuGameBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: SudokuViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSudokuGameBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel with activity scope to maintain game state across navigation
        viewModel = ViewModelProvider(requireActivity()).get(SudokuViewModel::class.java)
        
        // Get difficulty from arguments or use default
        val difficulty = arguments?.getSerializable("difficulty") as DifficultyLevel? ?: DifficultyLevel.BEGINNER
        
        // Start a new game with the selected difficulty
        viewModel.newGame(difficulty)
        
        // Set up the Sudoku grid view
        binding.sudokuGrid.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                val col = (event.x / (binding.sudokuGrid.width / 9f)).toInt()
                val row = (event.y / (binding.sudokuGrid.height / 9f)).toInt()
                
                if (row in 0..8 && col in 0..8) {
                    viewModel.selectCell(row, col)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
        
        // Set up number buttons
        binding.button1.setOnClickListener { viewModel.placeNumber(1) }
        binding.button2.setOnClickListener { viewModel.placeNumber(2) }
        binding.button3.setOnClickListener { viewModel.placeNumber(3) }
        binding.button4.setOnClickListener { viewModel.placeNumber(4) }
        binding.button5.setOnClickListener { viewModel.placeNumber(5) }
        binding.button6.setOnClickListener { viewModel.placeNumber(6) }
        binding.button7.setOnClickListener { viewModel.placeNumber(7) }
        binding.button8.setOnClickListener { viewModel.placeNumber(8) }
        binding.button9.setOnClickListener { viewModel.placeNumber(9) }
        
        // Set up action buttons
        binding.buttonClear.setOnClickListener { viewModel.clearCell() }
        binding.buttonNote.setOnClickListener { viewModel.toggleNoteMode() }
        binding.buttonGiveUp.setOnClickListener { viewModel.giveUp() }
        
        // Observe ViewModel
        observeViewModel()
    }
    
    private fun observeViewModel() {
        viewModel.currentBoard.observe(viewLifecycleOwner) { board ->
            binding.sudokuGrid.setBoard(board)
            
            // Highlight same value cells if a cell is selected
            val selected = viewModel.selectedCell.value
            if (selected != null && board != null) {
                val (row, col) = selected
                val value = board.getValue(row, col)
                if (value != 0) {
                    val sameValueCells = mutableSetOf<Pair<Int, Int>>()
                    for (i in 0..8) {
                        for (j in 0..8) {
                            if (board.getValue(i, j) == value) {
                                sameValueCells.add(Pair(i, j))
                            }
                        }
                    }
                    binding.sudokuGrid.setSameValueCells(sameValueCells)
                } else {
                    binding.sudokuGrid.setSameValueCells(emptySet())
                }
            } else {
                binding.sudokuGrid.setSameValueCells(emptySet())
            }
        }
        
        viewModel.selectedCell.observe(viewLifecycleOwner) { cell ->
            binding.sudokuGrid.setSelectedCell(cell)
            
            // Update same value highlighting when selection changes
            val board = viewModel.currentBoard.value
            if (cell != null && board != null) {
                val (row, col) = cell
                val value = board.getValue(row, col)
                if (value != 0) {
                    val sameValueCells = mutableSetOf<Pair<Int, Int>>()
                    for (i in 0..8) {
                        for (j in 0..8) {
                            if (board.getValue(i, j) == value) {
                                sameValueCells.add(Pair(i, j))
                            }
                        }
                    }
                    binding.sudokuGrid.setSameValueCells(sameValueCells)
                } else {
                    binding.sudokuGrid.setSameValueCells(emptySet())
                }
            } else {
                binding.sudokuGrid.setSameValueCells(emptySet())
            }
        }
        
        // Game status is no longer displayed in the UI
        viewModel.gameStatus.observe(viewLifecycleOwner) { status ->
            // Save game history only when game is completed, not when given up
            when (status) {
                GameStatus.COMPLETED -> {
                    showCongratulationsDialog()
                    viewModel.saveGameHistory(requireContext())
                }
                GameStatus.GAVE_UP -> {
                    // Show a different dialog for giving up, but don't save to history
                    showGaveUpDialog()
                }
                else -> {
                    // Do nothing for other statuses
                }
            }
        }
        
        viewModel.difficulty.observe(viewLifecycleOwner) { difficulty ->
            binding.textDifficulty.text = "Difficulty: ${difficulty.displayName}"
        }
        
        viewModel.timerSeconds.observe(viewLifecycleOwner) { seconds ->
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            binding.textTimer.text = String.format("%02d:%02d", minutes, remainingSeconds)
        }
        
        viewModel.invalidCells.observe(viewLifecycleOwner) { invalidCells ->
            binding.sudokuGrid.setInvalidCells(invalidCells)
        }
        
        viewModel.completedNumbers.observe(viewLifecycleOwner) { completedNumbers ->
            // Highlight completed numbers with a simple animation effect
            highlightCompletedNumbers(completedNumbers)
        }
        
        viewModel.isNoteMode.observe(viewLifecycleOwner) { isNoteMode ->
            // Note button's background selector shows the active state
            binding.buttonNote.isSelected = isNoteMode
        }
    }
    
    /**
     * Highlights completed numbers with a simple animation effect.
     */
    private fun highlightCompletedNumbers(completedNumbers: Set<Int>) {
        // For now, we'll just update the button colors
        // In a more advanced implementation, we could add animations
        updateButtonColors(completedNumbers)
    }
    
    /**
     * Updates button colors based on completed numbers.
     */
    private fun updateButtonColors(completedNumbers: Set<Int>) {
        // Reset all buttons to default background
        val buttons = listOf(
            binding.button1, binding.button2, binding.button3,
            binding.button4, binding.button5, binding.button6,
            binding.button7, binding.button8, binding.button9
        )
        
        // Highlight completed numbers
        for ((index, button) in buttons.withIndex()) {
            val number = index + 1  // 1-9 for buttons 1-9
            if (number in completedNumbers) {
                // Highlight completed numbers with the selected state
                button.isSelected = true
            } else {
                // Reset to default state
                button.isSelected = false
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    /**
     * Shows a congratulation dialog with game statistics when the game is completed.
     */
    private fun showCongratulationsDialog() {
        val difficulty = viewModel.difficulty.value?.displayName ?: "Unknown"
        val playingTime = viewModel.timerSeconds.value ?: 0
        val wrongTryCount = viewModel.getWrongTryCount()
        
        // Create GameHistory to calculate score
        val history = com.juanstudio.sudoku.sudoku.model.GameHistory(
            finishTime = playingTime.toLong(),
            wrongTryCount = wrongTryCount,
            difficulty = viewModel.difficulty.value ?: DifficultyLevel.BEGINNER
        )
        val score = history.calculateScore()
        
        val minutes = playingTime / 60
        val seconds = playingTime % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)
        
        val message = "Congratulations! You've completed the Sudoku puzzle!\n\n" +
                "Difficulty: $difficulty\n" +
                "Playing Time: $timeString\n" +
                "Wrong Tries: $wrongTryCount\n" +
                "Score: $score"
        
        AlertDialog.Builder(requireContext())
            .setTitle("Game Completed!")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    /**
     * Shows a dialog when the user gives up.
     */
    private fun showGaveUpDialog() {
        val difficulty = viewModel.difficulty.value?.displayName ?: "Unknown"
        val playingTime = viewModel.timerSeconds.value ?: 0
        val wrongTryCount = viewModel.getWrongTryCount()
        
        // Create GameHistory to calculate score
        val history = com.juanstudio.sudoku.sudoku.model.GameHistory(
            finishTime = playingTime.toLong(),
            wrongTryCount = wrongTryCount,
            difficulty = viewModel.difficulty.value ?: DifficultyLevel.BEGINNER
        )
        val score = history.calculateScore()
        
        val minutes = playingTime / 60
        val seconds = playingTime % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)
        
        val message = "You've given up on this Sudoku puzzle.\n\n" +
                "Difficulty: $difficulty\n" +
                "Playing Time: $timeString\n" +
                "Wrong Tries: $wrongTryCount\n" +
                "Score: $score"
        
        AlertDialog.Builder(requireContext())
            .setTitle("Game Over")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
