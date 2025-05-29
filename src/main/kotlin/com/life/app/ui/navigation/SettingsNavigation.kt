package com.life.app.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.life.app.ui.profile.PROFILE_ROUTE
import com.life.app.ui.profile.ProfileScreen
import com.life.app.ui.settings.SettingsScreen

/**
 * Settings navigation routes.
 */
object SettingsDestinations {
    const val SETTINGS_ROUTE = "settings"
    const val PROFILE_ROUTE = PROFILE_ROUTE
}

/**
 * Add settings navigation graph to the NavGraphBuilder.
 */
fun NavGraphBuilder.settingsNavigation(navController: NavHostController) {
    navigation(
        startDestination = SettingsDestinations.SETTINGS_ROUTE,
        route = LifeDestination.Settings.route
    ) {
        composable(SettingsDestinations.SETTINGS_ROUTE) {
            SettingsScreen(navController = navController)
        }
        
        composable(SettingsDestinations.PROFILE_ROUTE) {
            ProfileScreen(navController = navController)
        }
    }
}