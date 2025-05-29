package com.life.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.life.app.ui.coach.CoachScreen
import com.life.app.ui.coach.CoachViewModel
import com.life.app.ui.coach.DeepseekChatScreen

/**
 * Coach navigation destinations
 */
object CoachDestinations {
    const val COACH_ROUTE = "coach"
    const val COACH_CHAT_ROUTE = "coach/chat"
}

/**
 * Add coach navigation graph to the NavGraphBuilder
 */
fun NavGraphBuilder.coachNavigation(navController: NavController) {
    composable(CoachDestinations.COACH_ROUTE) {
        val viewModel = hiltViewModel<CoachViewModel>()
        CoachScreen(
            navController = navController,
            viewModel = viewModel,
            onNavigateToChat = {
                navController.navigate(CoachDestinations.COACH_CHAT_ROUTE)
            }
        )
    }
    
    composable(CoachDestinations.COACH_CHAT_ROUTE) {
        DeepseekChatScreen()
    }
}