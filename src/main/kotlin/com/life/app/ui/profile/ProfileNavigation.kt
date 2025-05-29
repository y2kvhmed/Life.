package com.life.app.ui.profile

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/**
 * Navigation route for the Profile screen.
 */
const val PROFILE_ROUTE = "profile"

/**
 * Extension function to add Profile screen to the navigation graph.
 */
fun NavGraphBuilder.profileScreen(navController: NavController) {
    composable(PROFILE_ROUTE) {
        ProfileScreen(navController = navController)
    }
}