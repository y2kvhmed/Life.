package com.life.app.ui.navigation

import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.life.app.ui.journal.JournalEntryScreen
import com.life.app.ui.journal.JournalScreen

/**
 * Journal navigation routes.
 */
object JournalDestinations {
    const val JOURNAL_ROUTE = "journal"
    const val JOURNAL_ENTRY_ROUTE = "journal_entry"
    const val JOURNAL_ENTRY_WITH_ID_ROUTE = "journal_entry/{journalId}"
}

/**
 * Add journal navigation graph to the NavGraphBuilder.
 */
fun NavGraphBuilder.journalNavigation(navController: NavHostController) {
    navigation(
        startDestination = JournalDestinations.JOURNAL_ROUTE,
        route = LifeDestination.Journal.route
    ) {
        composable(JournalDestinations.JOURNAL_ROUTE) {
            JournalScreen(
                onNavigateToJournalEntry = { journalId ->
                    if (journalId == null) {
                        navController.navigate(JournalDestinations.JOURNAL_ENTRY_ROUTE)
                    } else {
                        navController.navigate("${JournalDestinations.JOURNAL_ENTRY_ROUTE}/$journalId")
                    }
                }
            )
        }
        
        composable(JournalDestinations.JOURNAL_ENTRY_ROUTE) {
            JournalEntryScreen(
                journalId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = JournalDestinations.JOURNAL_ENTRY_WITH_ID_ROUTE,
            arguments = listOf(navArgument("journalId") { type = NavType.LongType })
        ) { backStackEntry ->
            val journalId = backStackEntry.arguments?.getLong("journalId")
            JournalEntryScreen(
                journalId = journalId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}