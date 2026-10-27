package com.juanstudio.sudoku

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.juanstudio.sudoku.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Home and About are top level; Difficulty/Game screens show the Up arrow.
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home, R.id.navigation_about)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Tabs jump within the single back stack (Home -> Difficulty -> Game):
        // pop back to the tab's screen if it's already in the stack, otherwise open it.
        // Replaces NavigationUI's save/restore-state tab switching, which left the
        // Home tab unresponsive from the game screen.
        navView.setOnItemSelectedListener { item ->
            if (!navController.popBackStack(item.itemId, false)) {
                navController.navigate(item.itemId)
            }
            true
        }
        // Tapping the already-selected tab also returns to that tab's root screen
        // (e.g. Play Sudoku while in a game goes back to difficulty selection).
        navView.setOnItemReselectedListener { item ->
            navController.popBackStack(item.itemId, false)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment_activity_main).navigateUp() ||
            super.onSupportNavigateUp()
    }
}