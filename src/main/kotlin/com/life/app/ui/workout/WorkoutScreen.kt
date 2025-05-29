package com.life.app.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.life.app.R
import com.life.app.data.model.WorkoutType
import com.life.app.ui.theme.LifeTheme

@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
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
            ) {
                // Header
                Text(
                    text = stringResource(R.string.workout),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Tabs for My Workouts and Suggested Workouts
                var selectedTabIndex by remember { mutableIntStateOf(0) }
                val tabs = listOf(
                    stringResource(R.string.my_workouts),
                    stringResource(R.string.suggested_workouts)
                )
                
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Content based on selected tab
                when (selectedTabIndex) {
                    0 -> MyWorkoutsTab(
                        workouts = uiState.workouts,
                        isLoading = uiState.isLoading,
                        onAddWorkout = { viewModel.showAddWorkoutDialog() },
                        onWorkoutClick = { viewModel.selectWorkout(it) },
                        onCompleteWorkout = { viewModel.completeWorkout(it) }
                    )
                    1 -> SuggestedWorkoutsTab(
                        suggestedWorkouts = uiState.suggestedWorkouts,
                        isLoading = uiState.isLoadingSuggestions,
                        onGenerateWorkouts = { viewModel.generateWorkoutSuggestions() },
                        onSaveWorkout = { viewModel.saveWorkoutFromSuggestion(it) }
                    )
                }
            }
            
            // Add Workout Dialog
            if (uiState.showAddWorkoutDialog) {
                AddWorkoutDialog(
                    onDismiss = { viewModel.hideAddWorkoutDialog() },
                    onAddWorkout = { name, type, exercises ->
                        viewModel.addWorkout(name, type, exercises)
                    }
                )
            }
            
            // Workout Detail Dialog
            if (uiState.selectedWorkout != null) {
                WorkoutDetailDialog(
                    workout = uiState.selectedWorkout!!,
                    onDismiss = { viewModel.deselectWorkout() },
                    onComplete = { viewModel.completeWorkout(uiState.selectedWorkout!!) },
                    onDelete = { viewModel.deleteWorkout(uiState.selectedWorkout!!) }
                )
            }
            
            // Error message
            if (uiState.errorMessage.isNotEmpty()) {
                ErrorSnackbar(
                    message = uiState.errorMessage,
                    onDismiss = { viewModel.clearError() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
            
            // Add Workout FAB
            FloatingActionButton(
                onClick = { viewModel.showAddWorkoutDialog() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_workout)
                )
            }
        }
    }
}

@Composable
fun MyWorkoutsTab(
    workouts: List<WorkoutWithExercises>,
    isLoading: Boolean,
    onAddWorkout: () -> Unit,
    onWorkoutClick: (WorkoutWithExercises) -> Unit,
    onCompleteWorkout: (WorkoutWithExercises) -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (workouts.isEmpty()) {
        EmptyWorkoutsView(onAddWorkout = onAddWorkout)
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(workouts) { workout ->
                WorkoutItem(
                    workout = workout,
                    onClick = { onWorkoutClick(workout) },
                    onComplete = { onCompleteWorkout(workout) }
                )
            }
        }
    }
}

@Composable
fun SuggestedWorkoutsTab(
    suggestedWorkouts: List<WorkoutSuggestion>,
    isLoading: Boolean,
    onGenerateWorkouts: () -> Unit,
    onSaveWorkout: (WorkoutSuggestion) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onGenerateWorkouts,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
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
                Text(text = stringResource(R.string.generate_workout_suggestions))
            }
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.generating_workouts),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else if (suggestedWorkouts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = stringResource(R.string.no_suggestions_yet),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(suggestedWorkouts) { suggestion ->
                    SuggestedWorkoutItem(
                        suggestion = suggestion,
                        onSave = { onSaveWorkout(suggestion) }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyWorkoutsView(onAddWorkout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.no_workouts_yet),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.create_your_first_workout),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddWorkout,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.add_workout))
        }
    }
}

@Composable
fun WorkoutItem(
    workout: WorkoutWithExercises,
    onClick: () -> Unit,
    onComplete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Workout type icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (workout.workout.type) {
                        WorkoutType.STRENGTH -> Icons.Default.FitnessCenter
                        WorkoutType.CARDIO -> Icons.Default.DirectionsRun
                        WorkoutType.FLEXIBILITY -> Icons.Default.SelfImprovement
                        WorkoutType.HIIT -> Icons.Default.Timer
                        WorkoutType.OTHER -> Icons.Default.Accessibility
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Workout details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = workout.workout.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${workout.exercises.size} ${stringResource(R.string.exercises)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            // Complete button
            IconButton(
                onClick = onComplete
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = stringResource(R.string.complete_workout),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SuggestedWorkoutItem(
    suggestion: WorkoutSuggestion,
    onSave: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = suggestion.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(
                    onClick = onSave
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = stringResource(R.string.save_workout),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = suggestion.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = stringResource(R.string.exercises),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            suggestion.exercises.forEach { exercise ->
                Text(
                    text = "• ${exercise}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun AddWorkoutDialog(
    onDismiss: () -> Unit,
    onAddWorkout: (String, WorkoutType, List<String>) -> Unit
) {
    var workoutName by remember { mutableStateOf("") }
    var workoutType by remember { mutableStateOf(WorkoutType.STRENGTH) }
    var exerciseInput by remember { mutableStateOf("") }
    var exercises by remember { mutableStateOf(listOf<String>()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_workout)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Workout Name
                OutlinedTextField(
                    value = workoutName,
                    onValueChange = { workoutName = it },
                    label = { Text(stringResource(R.string.workout_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Workout Type
                Text(
                    text = stringResource(R.string.workout_type),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WorkoutType.values().forEach { type ->
                        FilterChip(
                            selected = workoutType == type,
                            onClick = { workoutType = type },
                            label = { Text(type.name) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Exercises
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = exerciseInput,
                        onValueChange = { exerciseInput = it },
                        label = { Text(stringResource(R.string.add_exercise)) },
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = {
                            if (exerciseInput.isNotBlank()) {
                                exercises = exercises + exerciseInput.trim()
                                exerciseInput = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Exercise List
                if (exercises.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.exercises),
                        style = MaterialTheme.typography.labelLarge
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    exercises.forEachIndexed { index, exercise ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}. $exercise",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            
                            IconButton(
                                onClick = {
                                    exercises = exercises.toMutableList().apply {
                                        removeAt(index)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.remove),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (workoutName.isNotBlank() && exercises.isNotEmpty()) {
                        onAddWorkout(workoutName, workoutType, exercises)
                        onDismiss()
                    }
                },
                enabled = workoutName.isNotBlank() && exercises.isNotEmpty()
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun WorkoutDetailDialog(
    workout: WorkoutWithExercises,
    onDismiss: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(workout.workout.name) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Workout Type
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (workout.workout.type) {
                            WorkoutType.STRENGTH -> Icons.Default.FitnessCenter
                            WorkoutType.CARDIO -> Icons.Default.DirectionsRun
                            WorkoutType.FLEXIBILITY -> Icons.Default.SelfImprovement
                            WorkoutType.HIIT -> Icons.Default.Timer
                            WorkoutType.OTHER -> Icons.Default.Accessibility
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = workout.workout.type.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Exercises
                Text(
                    text = stringResource(R.string.exercises),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                workout.exercises.forEachIndexed { index, exercise ->
                    Text(
                        text = "${index + 1}. $exercise",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onComplete()
                    onDismiss()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.complete))
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(stringResource(R.string.close))
                }
                
                TextButton(
                    onClick = {
                        onDelete()
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.delete))
                }
            }
        }
    )
}

@Composable
fun ErrorSnackbar(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Snackbar(
        modifier = modifier.padding(16.dp),
        action = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.dismiss))
            }
        },
        dismissAction = {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.dismiss)
                )
            }
        }
    ) {
        Text(message)
    }
}

// Data classes for UI
data class WorkoutWithExercises(
    val workout: com.life.app.data.model.Workout,
    val exercises: List<String>
)

data class WorkoutSuggestion(
    val name: String,
    val description: String,
    val type: WorkoutType,
    val exercises: List<String>
)