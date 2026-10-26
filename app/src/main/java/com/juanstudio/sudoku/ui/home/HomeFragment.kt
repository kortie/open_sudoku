package com.juanstudio.sudoku.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.juanstudio.sudoku.R
import com.juanstudio.sudoku.databinding.FragmentHomeBinding
import com.juanstudio.sudoku.sudoku.repository.GameHistoryRepository

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set up button to navigate to difficulty selection
        binding.buttonNewGame.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_difficulty_selection)
        }
        
        // Display game history
        displayGameHistory()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh game history when fragment becomes visible again
        displayGameHistory()
    }
    
    private fun displayGameHistory() {
        val repository = GameHistoryRepository.getInstance(requireContext())
        val historyList = repository.getGameHistory()
        
        // Clear existing views
        binding.historyContainer.removeAllViews()
        
        if (historyList.isEmpty()) {
            val emptyView = TextView(requireContext()).apply {
                text = "No games yet 🐾\nFinish a puzzle to see your scores here!"
                textSize = 15f
                setTextColor(requireContext().getColor(R.color.text_secondary))
                gravity = android.view.Gravity.CENTER
                setPadding(16, 32, 16, 32)
            }
            binding.historyContainer.addView(emptyView)
        } else {
            // Create table header
            val headerRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            
            // Add header cells
            addHeaderCell(headerRow, "Score", 1f)
            addHeaderCell(headerRow, "Difficulty", 1f)
            addHeaderCell(headerRow, "Time", 1f)
            addHeaderCell(headerRow, "Tries", 1f)
            addHeaderCell(headerRow, "Date", 1f)
            
            binding.historyContainer.addView(headerRow)
            
            // Add history items
            for (history in historyList) {
                val row = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                
                // Add data cells
                addDataCell(row, history.calculateScore().toString(), 1f)
                addDataCell(row, history.difficulty.displayName, 1f)
                addDataCell(row, history.getFormattedFinishTime(), 1f)
                addDataCell(row, history.wrongTryCount.toString(), 1f)
                addDataCell(row, history.getFormattedDate(), 1f)
                
                binding.historyContainer.addView(row)
            }
        }
    }
    
    private fun addHeaderCell(row: LinearLayout, text: String, weight: Float) {
        val cell = TextView(requireContext()).apply {
            this.text = text
            textSize = 12f
            setPadding(4, 12, 4, 12)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(requireContext().getColor(R.color.primary_dark))
            setBackgroundResource(R.color.bg_tertiary)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight)
            gravity = android.view.Gravity.CENTER
        }
        row.addView(cell)
    }
    
    private fun addDataCell(row: LinearLayout, text: String, weight: Float) {
        val cell = TextView(requireContext()).apply {
            this.text = text
            textSize = 14f
            setPadding(4, 12, 4, 12)
            setTextColor(requireContext().getColor(R.color.text_primary))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight)
            gravity = android.view.Gravity.CENTER
        }
        row.addView(cell)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
