package com.life.app.ui.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.life.app.R
import com.life.app.ui.theme.LifeTheme

@Composable
fun CoachScreen(
    navController: NavController,
    viewModel: CoachViewModel = hiltViewModel(),
    onNavigateToChat: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    LifeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = stringResource(R.string.ai_coach),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Chat with Coach Button
                ChatWithCoachButton(onClick = onNavigateToChat)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Daily Plan Section
                DailyPlanSection(
                    onGeneratePlan = { viewModel.generateDailyPlan() },
                    isLoading = uiState.isLoading
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Quick Suggestions Section
                QuickSuggestionsSection(
                    onGenerateWorkout = { viewModel.generateWorkoutSuggestions() },
                    onGenerateMeal = { viewModel.generateMealSuggestions() },
                    onGenerateMotivation = { viewModel.generateMotivationalMessage() },
                    isLoading = uiState.isLoading
                )
                
                // Error message
                if (uiState.errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ErrorMessage(
                        message = uiState.errorMessage,
                        onDismiss = { viewModel.clearError() }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatWithCoachButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.ask_coach),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = stringResource(R.string.coach_welcome_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun DailyPlanSection(
    onGeneratePlan: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Section Title
        Text(
            text = stringResource(R.string.coach_daily_plan_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Daily Plan Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Get a personalized daily plan based on your goals and preferences.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Button(
                    onClick = onGeneratePlan,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.coach_generate_plan))
                    }
                }
            }
        }
    }
}

@Composable
fun QuickSuggestionsSection(
    onGenerateWorkout: () -> Unit,
    onGenerateMeal: () -> Unit,
    onGenerateMotivation: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Section Title
        Text(
            text = stringResource(R.string.suggestions),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Suggestions Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SuggestionCard(
                title = stringResource(R.string.coach_generate_workout),
                icon = Icons.Default.FitnessCenter,
                onClick = onGenerateWorkout,
                isLoading = isLoading,
                modifier = Modifier.weight(1f)
            )
            
            SuggestionCard(
                title = stringResource(R.string.coach_generate_meal),
                icon = Icons.Default.Restaurant,
                onClick = onGenerateMeal,
                isLoading = isLoading,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        SuggestionCard(
            title = stringResource(R.string.coach_generate_motivation),
            icon = Icons.Default.EmojiEmotions,
            onClick = onGenerateMotivation,
            isLoading = isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SuggestionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(100.dp)
            .clickable(enabled = !isLoading, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 1.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.cancel),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}