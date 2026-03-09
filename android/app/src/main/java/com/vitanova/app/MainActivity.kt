package com.vitanova.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.vitanova.app.navigation.AppNavigation
import com.vitanova.app.navigation.Route
import com.vitanova.app.ui.theme.VitaNovaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val hasCompletedOnboarding = getSharedPreferences("vitanova_prefs", MODE_PRIVATE)
            .getBoolean("onboarding_complete", false)

        setContent {
            VitaNovaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val startDestination = if (hasCompletedOnboarding) {
                        Route.Home.path
                    } else {
                        Route.Welcome.path
                    }

                    AppNavigation(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
