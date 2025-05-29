package com.life.app.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.life.app.ui.home.HomeScreen

/**
 * Home navigation routes.
 */
object HomeDestinations {
    const val HOME_ROUTE = "home"
}

/**
 * Add home navigation graph to the NavGraphBuilder.
 */
fun NavGraphBuilder.homeNavigation(navController: NavHostController) {
    navigation(
        startDestination = HomeDestinations.HOME_ROUTE,
        route = LifeDestination.Home.route
    ) {
        composable(HomeDestinations.HOME_ROUTE) {
            HomeScreen(navController = navController)
        }
    }
}