package com.example.sudoku.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sudoku.R
import com.example.sudoku.sudoku.model.DifficultyLevel
import com.example.sudoku.databinding.FragmentDifficultySelectionBinding

/**
 * Fragment for difficulty selection.
 */
class DifficultySelectionFragment : Fragment() {
    private var _binding: FragmentDifficultySelectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDifficultySelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set up buttons to navigate to the Sudoku game with selected difficulty
        binding.buttonEasy.setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable("difficulty", DifficultyLevel.BEGINNER)
            }
            findNavController().navigate(R.id.navigation_game, bundle)
        }
        
        binding.buttonMedium.setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable("difficulty", DifficultyLevel.AMATEUR)
            }
            findNavController().navigate(R.id.navigation_game, bundle)
        }
        
        binding.buttonHard.setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable("difficulty", DifficultyLevel.PRO)
            }
            findNavController().navigate(R.id.navigation_game, bundle)
        }
        
        binding.buttonHacker.setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable("difficulty", DifficultyLevel.HACKER)
            }
            findNavController().navigate(R.id.navigation_game, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
