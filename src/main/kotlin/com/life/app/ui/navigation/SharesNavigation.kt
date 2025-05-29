package com.life.app.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.life.app.ui.shares.SharesScreen

/**
 * Shares navigation routes.
 */
object SharesDestinations {
    const val SHARES_ROUTE = "shares"
}

/**
 * Add shares navigation graph to the NavGraphBuilder.
 */
fun NavGraphBuilder.sharesNavigation(navController: NavHostController) {
    navigation(
        startDestination = SharesDestinations.SHARES_ROUTE,
        route = LifeDestination.Shares.route
    ) {
        composable(SharesDestinations.SHARES_ROUTE) {
            SharesScreen()
        }
    }
}