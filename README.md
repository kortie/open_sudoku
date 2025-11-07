# Sudoku Game for Android

An Android implementation of the classic Sudoku puzzle game with four difficulty levels.

## Features
- Four difficulty levels: Beginner, Amateur, Pro, and Hacker
- Puzzle generation with unique solutions
- Game timer and statistics
- Hint system
- Save/restore game state

## TODO List

All tasks have been completed successfully! The Sudoku game is fully implemented with all requested features.

## Build Status
[![Build Status](https://img.shields.io/badge/build-success-brightgreen)](https://github.com/your-username/sudoku-android)

The app has been successfully built and an APK is available at `app/build/outputs/apk/debug/app-debug.apk`.

## Implementation Details

### Core Components

1. **Sudoku Data Models**
   - `SudokuBoard`: Represents the 9x9 game board
   - `DifficultyLevel`: Enum for the 4 difficulty levels

2. **Game Logic**
   - `SudokuGenerator`: Creates valid Sudoku puzzles with different difficulty levels
   - `SudokuSolver`: Validates player moves and can solve puzzles using backtracking

3. **UI Components**
   - `SudokuGridView`: Custom view for displaying the Sudoku board with responsive design
   - `SudokuGameFragment`: Main game screen with scrollable layout for small screens
   - Updated HomeFragment to serve as main menu

### Difficulty Levels
- **Beginner**: 36-40 blank cells
- **Amateur**: 41-45 blank cells
- **Pro**: 46-50 blank cells
- **Hacker**: 51-55 blank cells

### Game Features
- Interactive Sudoku board with touch input
- Number input pad
- Hint system
- Game timer
- Difficulty selection
- Victory detection
- Responsive layout for various screen sizes
- Highlight same numbers when a cell is selected
- Highlight invalid entries in red

## Architecture
The app follows the MVVM (Model-View-ViewModel) architecture pattern:
- **Model**: Sudoku data classes and utility functions
- **View**: Fragments and custom views
- **ViewModel**: SudokuViewModel for game state management

## Algorithms Used
- **Backtracking Algorithm**: For solving and generating Sudoku puzzles
- **Constraint Satisfaction**: For validating board states
- **Randomized Puzzle Generation**: For creating unique puzzles at each difficulty level
