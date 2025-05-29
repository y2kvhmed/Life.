package com.life.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.life.app.R
import com.life.app.ui.coach.CoachScreen
import com.life.app.ui.faith.FaithScreen
import com.life.app.ui.home.HomeScreen
import com.life.app.ui.journal.JournalScreen
import com.life.app.ui.navigation.LifeDestination
import com.life.app.ui.navigation.coachNavigation
import com.life.app.ui.nutrition.NutritionScreen
import com.life.app.ui.running.RunningScreen
import com.life.app.ui.settings.SettingsScreen
import com.life.app.ui.shares.SharesScreen
import com.life.app.ui.theme.LifeTheme
import com.life.app.ui.workout.WorkoutScreen

/**
 * Main composable for the life. app.
 * Sets up the navigation and bottom navigation bar.
 */
@Composable
fun LifeApp() {
    LifeTheme {
        val navController = rememberNavController()
        val items = listOf(
            LifeDestination.Home,
            LifeDestination.Coach,
            LifeDestination.Workout,
            LifeDestination.Running,
            LifeDestination.Nutrition,
            LifeDestination.Journal,
            LifeDestination.Faith,
            LifeDestination.Shares,
            LifeDestination.Settings
        )
        
        Scaffold(
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(stringResource(screen.resourceId)) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = LifeDestination.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                homeNavigation(navController)
                coachNavigation(navController)
                workoutNavigation(navController)
                runningNavigation(navController)
                nutritionNavigation(navController)
                journalNavigation(navController)
                faithNavigation(navController)
                sharesNavigation(navController)
                settingsNavigation(navController)
                authNavigation(navController)
            }
        }
    }
}