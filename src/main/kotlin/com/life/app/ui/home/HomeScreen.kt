package com.life.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.life.app.R
import com.life.app.ui.components.ActivityCard
import com.life.app.ui.components.MotivationCard
import com.life.app.ui.components.QuickLogSection
import com.life.app.ui.components.StreakDisplay
import com.life.app.ui.home.HomeViewModel.NavigationEvent

/**
 * Home screen composable that displays the main dashboard of the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()
    
    // Handle navigation events
    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            is NavigationEvent.TO_WORKOUT -> {
                navController.navigate("workout")
                viewModel.clearNavigationEvent()
            }
            is NavigationEvent.TO_RUN -> {
                navController.navigate("running")
                viewModel.clearNavigationEvent()
            }
            is NavigationEvent.TO_NUTRITION -> {
                navController.navigate("nutrition")
                viewModel.clearNavigationEvent()
            }
            is NavigationEvent.TO_JOURNAL -> {
                navController.navigate("journal")
                viewModel.clearNavigationEvent()
            }
            is NavigationEvent.TO_FAITH -> {
                navController.navigate("faith")
                viewModel.clearNavigationEvent()
            }
            else -> { /* Do nothing */ }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // App title and tagline
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Streak display
        StreakDisplay(
            count = uiState.streakCount,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Motivation card
        MotivationCard(
            motivationalText = uiState.motivationalText,
            onRefresh = { viewModel.refreshMotivation() },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Today's overview section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.today_overview),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Display recent activities
                if (uiState.todayActivities.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No activities logged today",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    uiState.todayActivities.forEach { activity ->
                        ActivityCard(
                            title = activity.title,
                            description = activity.description,
                            time = activity.time,
                            icon = activity.icon,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Quick log section
        QuickLogSection(
            onWorkoutClick = { viewModel.navigateToWorkout() },
            onRunClick = { viewModel.navigateToRun() },
            onMealClick = { viewModel.navigateToNutrition() },
            onJournalClick = { viewModel.navigateToJournal() },
            onPrayerClick = { viewModel.navigateToFaith() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}