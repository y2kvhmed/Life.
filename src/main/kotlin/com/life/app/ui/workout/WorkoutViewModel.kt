package com.life.app.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.life.app.data.model.Workout
import com.life.app.data.model.WorkoutType
import com.life.app.data.remote.DeepseekService
import com.life.app.data.repository.WorkoutRepository
import com.life.app.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel for the Workout screen.
 */
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val deepseekService: DeepseekService,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    init {
        loadWorkouts()
    }

    /**
     * Load user's workouts from the repository.
     */
    private fun loadWorkouts() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val workouts = workoutRepository.getWorkouts()
                val workoutsWithExercises = workouts.map { workout ->
                    WorkoutWithExercises(
                        workout = workout,
                        exercises = workout.exercises
                    )
                }
                _uiState.update { it.copy(
                    workouts = workoutsWithExercises,
                    isLoading = false,
                    errorMessage = ""
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load workouts"
                ) }
            }
        }
    }

    /**
     * Generate workout suggestions using the Deepseek AI service.
     */
    fun generateWorkoutSuggestions() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoadingSuggestions = true) }
                
                if (!networkUtils.isNetworkAvailable()) {
                    throw Exception("No internet connection. Please check your network and try again.")
                }
                
                val suggestionsText = deepseekService.generateWorkoutSuggestions()
                val parsedSuggestions = parseWorkoutSuggestions(suggestionsText)
                
                _uiState.update { it.copy(
                    suggestedWorkouts = parsedSuggestions,
                    isLoadingSuggestions = false,
                    errorMessage = ""
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoadingSuggestions = false,
                    errorMessage = e.message ?: "Failed to generate workout suggestions"
                ) }
            }
        }
    }

    /**
     * Parse workout suggestions from the AI response text.
     */
    private fun parseWorkoutSuggestions(suggestionsText: String): List<WorkoutSuggestion> {
        // This is a simplified parsing logic. In a real app, you would need more robust parsing.
        val suggestions = mutableListOf<WorkoutSuggestion>()
        
        // Split the text by workout sections (assuming each workout starts with a title)
        val workoutSections = suggestionsText.split("\n\n").filter { it.isNotBlank() }
        
        for (section in workoutSections) {
            val lines = section.split("\n")
            if (lines.isEmpty()) continue
            
            val name = lines[0].trim()
            val description = if (lines.size > 1) lines[1].trim() else ""
            
            // Determine workout type based on keywords in the name and description
            val type = when {
                name.contains("strength", ignoreCase = true) || 
                description.contains("strength", ignoreCase = true) -> WorkoutType.STRENGTH
                
                name.contains("cardio", ignoreCase = true) || 
                description.contains("cardio", ignoreCase = true) || 
                name.contains("run", ignoreCase = true) -> WorkoutType.CARDIO
                
                name.contains("flexibility", ignoreCase = true) || 
                description.contains("flexibility", ignoreCase = true) || 
                name.contains("stretch", ignoreCase = true) -> WorkoutType.FLEXIBILITY
                
                name.contains("hiit", ignoreCase = true) || 
                description.contains("hiit", ignoreCase = true) || 
                name.contains("interval", ignoreCase = true) -> WorkoutType.HIIT
                
                else -> WorkoutType.OTHER
            }
            
            // Extract exercises (assuming they start with a number or bullet point)
            val exercises = lines.drop(2).filter { line ->
                line.trim().matches(Regex("^[•\\-\\d\\.]+.*"))
            }.map { it.replace(Regex("^[•\\-\\d\\.]+\\s*"), "").trim() }
            
            if (name.isNotBlank() && exercises.isNotEmpty()) {
                suggestions.add(WorkoutSuggestion(
                    name = name,
                    description = description,
                    type = type,
                    exercises = exercises
                ))
            }
        }
        
        return suggestions
    }

    /**
     * Add a new workout.
     */
    fun addWorkout(name: String, type: WorkoutType, exercises: List<String>) {
        viewModelScope.launch {
            try {
                val workout = Workout(
                    id = 0, // Will be auto-generated by Room
                    userId = "", // Will be set by the repository
                    name = name,
                    type = type,
                    exercises = exercises,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                
                workoutRepository.createWorkout(workout)
                loadWorkouts() // Reload workouts after adding
                _uiState.update { it.copy(showAddWorkoutDialog = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = e.message ?: "Failed to add workout"
                ) }
            }
        }
    }

    /**
     * Save a workout from a suggestion.
     */
    fun saveWorkoutFromSuggestion(suggestion: WorkoutSuggestion) {
        addWorkout(
            name = suggestion.name,
            type = suggestion.type,
            exercises = suggestion.exercises
        )
    }

    /**
     * Complete a workout and update the streak.
     */
    fun completeWorkout(workout: WorkoutWithExercises) {
        viewModelScope.launch {
            try {
                workoutRepository.completeWorkout(workout.workout.id)
                _uiState.update { it.copy(selectedWorkout = null) }
                loadWorkouts() // Reload workouts after completion
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = e.message ?: "Failed to complete workout"
                ) }
            }
        }
    }

    /**
     * Delete a workout.
     */
    fun deleteWorkout(workout: WorkoutWithExercises) {
        viewModelScope.launch {
            try {
                workoutRepository.deleteWorkout(workout.workout.id)
                loadWorkouts() // Reload workouts after deletion
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = e.message ?: "Failed to delete workout"
                ) }
            }
        }
    }

    /**
     * Show the add workout dialog.
     */
    fun showAddWorkoutDialog() {
        _uiState.update { it.copy(showAddWorkoutDialog = true) }
    }

    /**
     * Hide the add workout dialog.
     */
    fun hideAddWorkoutDialog() {
        _uiState.update { it.copy(showAddWorkoutDialog = false) }
    }

    /**
     * Select a workout to view its details.
     */
    fun selectWorkout(workout: WorkoutWithExercises) {
        _uiState.update { it.copy(selectedWorkout = workout) }
    }

    /**
     * Deselect the currently selected workout.
     */
    fun deselectWorkout() {
        _uiState.update { it.copy(selectedWorkout = null) }
    }

    /**
     * Clear the error message.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = "") }
    }
}

/**
 * UI state for the Workout screen.
 */
data class WorkoutUiState(
    val workouts: List<WorkoutWithExercises> = emptyList(),
    val suggestedWorkouts: List<WorkoutSuggestion> = emptyList(),
    val selectedWorkout: WorkoutWithExercises? = null,
    val isLoading: Boolean = false,
    val isLoadingSuggestions: Boolean = false,
    val showAddWorkoutDialog: Boolean = false,
    val errorMessage: String = ""
)