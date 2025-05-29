package com.life.app.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.life.app.ui.faith.FaithScreen

/**
 * Faith navigation routes.
 */
object FaithDestinations {
    const val FAITH_ROUTE = "faith"
}

/**
 * Add faith navigation graph to the NavGraphBuilder.
 */
fun NavGraphBuilder.faithNavigation(navController: NavHostController) {
    navigation(
        startDestination = FaithDestinations.FAITH_ROUTE,
        route = LifeDestination.Faith.route
    ) {
        composable(FaithDestinations.FAITH_ROUTE) {
            FaithScreen()
        }
    }
}