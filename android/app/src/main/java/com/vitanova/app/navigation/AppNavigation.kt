package com.vitanova.app.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitanova.app.ui.brain.BrainScreen
import com.vitanova.app.ui.brain.CognitiveTestScreen
import com.vitanova.app.ui.energy.EnergyScreen
import com.vitanova.app.ui.energy.EnergyViewModel
import com.vitanova.app.ui.energy.HrvMeasureScreen
import com.vitanova.app.ui.fitness.ActiveTrackingScreen
import com.vitanova.app.ui.fitness.FitnessScreen
import com.vitanova.app.ui.fitness.FitnessViewModel
import com.vitanova.app.ui.fitness.StretchingScreen
import com.vitanova.app.ui.fitness.WorkoutScreen
import com.vitanova.app.ui.focus.DetoxScreen
import com.vitanova.app.ui.focus.FocusScreen
import com.vitanova.app.ui.focus.FocusTimerScreen
import com.vitanova.app.ui.habits.HabitsScreen
import com.vitanova.app.ui.home.HomeScreen
import com.vitanova.app.ui.nutrition.AddMealScreen
import com.vitanova.app.ui.nutrition.NutritionScreen
import com.vitanova.app.ui.onboarding.FirstDayScreen
import com.vitanova.app.ui.onboarding.ProfileSetupScreen
import com.vitanova.app.ui.onboarding.WelcomeScreen
import com.vitanova.app.ui.profile.PrivacyControlScreen
import com.vitanova.app.ui.profile.ProfileScreen
import com.vitanova.app.ui.sleep.SleepScreen
import com.vitanova.app.ui.sleep.SleepTrackingScreen
import com.vitanova.app.ui.sleep.SleepViewModel
import com.vitanova.app.ui.sleep.SmartAlarmScreen
import com.vitanova.app.ui.theme.VitaBackground
import com.vitanova.app.ui.theme.VitaGreen
import com.vitanova.app.ui.theme.VitaSurface
import com.vitanova.app.ui.theme.VitaTextTertiary

// ── Route definitions ────────────────────────────────────────────────────────

sealed class Route(val path: String) {

    // Main tabs
    data object Home : Route("home")
    data object Sleep : Route("sleep")
    data object Energy : Route("energy")
    data object Fitness : Route("fitness")
    data object Profile : Route("profile")

    // Sleep sub-screens
    data object SleepTracking : Route("sleep/tracking")
    data object SmartAlarm : Route("sleep/smart_alarm")

    // Energy sub-screens
    data object HrvMeasure : Route("energy/hrv_measure")

    // Focus
    data object Focus : Route("focus")
    data object FocusTimer : Route("focus/timer")
    data object Detox : Route("focus/detox")

    // Fitness sub-screens
    data object ActiveTracking : Route("fitness/active_tracking")
    data object Workout : Route("fitness/workout")
    data object Stretching : Route("fitness/stretching")

    // Nutrition
    data object Nutrition : Route("nutrition")
    data object AddMeal : Route("nutrition/add_meal")

    // Brain
    data object Brain : Route("brain")
    data object CognitiveTest : Route("brain/cognitive_test")

    // Habits
    data object Habits : Route("habits")

    // Profile sub-screens
    data object PrivacyControl : Route("profile/privacy_control")

    // Onboarding
    data object Welcome : Route("onboarding/welcome")
    data object ProfileSetup : Route("onboarding/profile_setup")
    data object FirstDay : Route("onboarding/first_day")
}

// ── Bottom navigation items ──────────────────────────────────────────────────

data class BottomNavItem(
    val label: String,
    val route: Route,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        label = "Home",
        route = Route.Home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        label = "Sleep",
        route = Route.Sleep,
        selectedIcon = Icons.Filled.Nightlight,
        unselectedIcon = Icons.Outlined.Nightlight
    ),
    BottomNavItem(
        label = "Energy",
        route = Route.Energy,
        selectedIcon = Icons.Filled.ElectricBolt,
        unselectedIcon = Icons.Outlined.ElectricBolt
    ),
    BottomNavItem(
        label = "Fitness",
        route = Route.Fitness,
        selectedIcon = Icons.Filled.DirectionsRun,
        unselectedIcon = Icons.Outlined.DirectionsRun
    ),
    BottomNavItem(
        label = "Profile",
        route = Route.Profile,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
)

// Screens where the bottom bar should be hidden
private val hideBottomBarRoutes = setOf(
    Route.SleepTracking.path,
    Route.SmartAlarm.path,
    Route.HrvMeasure.path,
    Route.FocusTimer.path,
    Route.Detox.path,
    Route.ActiveTracking.path,
    Route.Workout.path,
    Route.Stretching.path,
    Route.CognitiveTest.path,
    Route.Welcome.path,
    Route.ProfileSetup.path,
    Route.FirstDay.path
)

// ── App scaffold with navigation ─────────────────────────────────────────────

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Route.Home.path
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val shouldShowBottomBar = remember(currentRoute) {
        currentRoute != null && currentRoute !in hideBottomBarRoutes
    }

    Scaffold(
        containerColor = VitaBackground,
        bottomBar = {
            AnimatedVisibility(
                visible = shouldShowBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                VitaNovaBottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ── Main tabs ────────────────────────────────────────────────
            composable(Route.Home.path) {
                HomeScreen(
                    onNavigateToHrvMeasure = {
                        navController.navigate(Route.HrvMeasure.path)
                    },
                    onNavigateToFocusTimer = {
                        navController.navigate(Route.FocusTimer.path)
                    },
                    onNavigateToWorkout = {
                        navController.navigate(Route.Workout.path)
                    },
                    onNavigateToHabits = {
                        navController.navigate(Route.Habits.path)
                    },
                    onNavigateToSleep = {
                        navController.navigate(Route.Sleep.path)
                    },
                    onNavigateToEnergy = {
                        navController.navigate(Route.Energy.path)
                    },
                    onNavigateToFocus = {
                        navController.navigate(Route.Focus.path)
                    },
                    onNavigateToFitness = {
                        navController.navigate(Route.Fitness.path)
                    }
                )
            }

            composable(Route.Sleep.path) {
                val sleepViewModel: SleepViewModel = viewModel()
                SleepScreen(
                    viewModel = sleepViewModel,
                    onNavigateToTracking = {
                        navController.navigate(Route.SleepTracking.path)
                    },
                    onNavigateToSmartAlarm = {
                        navController.navigate(Route.SmartAlarm.path)
                    }
                )
            }

            composable(Route.Energy.path) {
                val energyViewModel: EnergyViewModel = viewModel()
                EnergyScreen(
                    onNavigateToMeasure = {
                        energyViewModel.startHrvMeasurement()
                        navController.navigate(Route.HrvMeasure.path)
                    },
                    viewModel = energyViewModel
                )
            }

            composable(Route.Fitness.path) {
                val fitnessViewModel: FitnessViewModel = viewModel()
                FitnessScreen(
                    viewModel = fitnessViewModel,
                    onNavigateToActiveTracking = { _ ->
                        navController.navigate(Route.ActiveTracking.path)
                    },
                    onNavigateToWorkout = {
                        navController.navigate(Route.Workout.path)
                    },
                    onNavigateToStretching = {
                        navController.navigate(Route.Stretching.path)
                    }
                )
            }

            composable(Route.Profile.path) {
                ProfileScreen(
                    onNavigateToPrivacyControl = {
                        navController.navigate(Route.PrivacyControl.path)
                    },
                    onNavigateToWelcome = {
                        navController.navigate(Route.Welcome.path) {
                            popUpTo(Route.Home.path) { inclusive = true }
                        }
                    }
                )
            }

            // ── Sleep sub ──────────────────────────────────────────────────
            composable(Route.SleepTracking.path) {
                val sleepViewModel: SleepViewModel = viewModel()
                SleepTrackingScreen(
                    viewModel = sleepViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSmartAlarm = {
                        navController.navigate(Route.SmartAlarm.path)
                    }
                )
            }

            composable(Route.SmartAlarm.path) {
                val sleepViewModel: SleepViewModel = viewModel()
                SmartAlarmScreen(
                    viewModel = sleepViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ── Energy ───────────────────────────────────────────────────
            composable(Route.HrvMeasure.path) {
                val parentEntry = remember(it) {
                    navController.getBackStackEntry(Route.Energy.path)
                }
                val energyViewModel: EnergyViewModel = viewModel(parentEntry)
                HrvMeasureScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaveMeasurement = { bpm, rmssd ->
                        energyViewModel.saveMeasurement(bpm, rmssd)
                    }
                )
            }

            // ── Focus ────────────────────────────────────────────────────
            composable(Route.Focus.path) {
                FocusScreen(
                    onNavigateToTimer = {
                        navController.navigate(Route.FocusTimer.path)
                    },
                    onNavigateToDetox = {
                        navController.navigate(Route.Detox.path)
                    }
                )
            }

            composable(Route.FocusTimer.path) {
                FocusTimerScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Route.Detox.path) {
                DetoxScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ── Fitness sub ────────────────────────────────────────────────
            composable(Route.ActiveTracking.path) {
                val fitnessViewModel: FitnessViewModel = viewModel()
                ActiveTrackingScreen(
                    viewModel = fitnessViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Route.Workout.path) {
                WorkoutScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Route.Stretching.path) {
                StretchingScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ── Nutrition ────────────────────────────────────────────────
            composable(Route.Nutrition.path) {
                NutritionScreen(
                    onNavigateToAddMeal = {
                        navController.navigate(Route.AddMeal.path)
                    }
                )
            }

            composable(Route.AddMeal.path) {
                AddMealScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaveMeal = { }
                )
            }

            // ── Brain ────────────────────────────────────────────────────
            composable(Route.Brain.path) {
                BrainScreen(
                    onNavigateToTest = {
                        navController.navigate(Route.CognitiveTest.path)
                    },
                    onStartQuickTest = {
                        navController.navigate(Route.CognitiveTest.path)
                    }
                )
            }

            composable(Route.CognitiveTest.path) {
                CognitiveTestScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ── Habits ───────────────────────────────────────────────────
            composable(Route.Habits.path) {
                HabitsScreen()
            }

            // ── Profile sub ──────────────────────────────────────────────
            composable(Route.PrivacyControl.path) {
                PrivacyControlScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // ── Onboarding ───────────────────────────────────────────────
            composable(Route.Welcome.path) {
                WelcomeScreen(
                    onNavigateToProfileSetup = {
                        navController.navigate(Route.ProfileSetup.path)
                    }
                )
            }

            composable(Route.ProfileSetup.path) {
                ProfileSetupScreen(
                    onNavigateToFirstDay = {
                        navController.navigate(Route.FirstDay.path)
                    }
                )
            }

            composable(Route.FirstDay.path) {
                FirstDayScreen(
                    onNavigateToHome = {
                        navController.navigate(Route.Home.path) {
                            popUpTo(Route.Welcome.path) { inclusive = true }
                        }
                    },
                    onNavigateToHrvMeasure = {
                        navController.navigate(Route.Home.path) {
                            popUpTo(Route.Welcome.path) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

// ── Bottom bar ───────────────────────────────────────────────────────────────

@Composable
private fun VitaNovaBottomBar(navController: NavHostController) {
    NavigationBar(
        containerColor = VitaSurface,
        contentColor = VitaGreen
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any {
                it.route == item.route.path
            } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route.path) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VitaGreen,
                    selectedTextColor = VitaGreen,
                    unselectedIconColor = VitaTextTertiary,
                    unselectedTextColor = VitaTextTertiary,
                    indicatorColor = VitaGreen.copy(alpha = 0.12f)
                )
            )
        }
    }
}
