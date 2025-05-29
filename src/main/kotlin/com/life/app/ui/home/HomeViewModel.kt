package com.life.app.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.life.app.data.model.Journal
import com.life.app.data.model.Meal
import com.life.app.data.model.Run
import com.life.app.data.model.Workout
import com.life.app.data.repository.*
import com.life.app.data.remote.DeepseekService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Data class representing an activity displayed on the home screen.
 */
data class ActivityItem(
    val title: String,
    val description: String,
    val time: String,
    val icon: ImageVector
)

/**
 * Data class representing the UI state of the home screen.
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val streakCount: Int = 0,
    val motivationalText: String = "Loading your daily motivation...",
    val todayActivities: List<ActivityItem> = emptyList()
)

/**
 * ViewModel for the home screen.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val streakRepository: StreakRepository,
    private val workoutRepository: WorkoutRepository,
    private val runRepository: RunRepository,
    private val mealRepository: MealRepository,
    private val journalRepository: JournalRepository,
    private val motivationRepository: MotivationRepository,
    private val deepseekService: DeepseekService
) : ViewModel() {

    // Assume we have the user ID from authentication
    private val userId = "current_user_id"
    
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    
    init {
        loadData()
    }
    
    /**
     * Load all data for the home screen.
     */
    private fun loadData() {
        viewModelScope.launch {
            // Check if streak needs to be reset
            streakRepository.checkAndResetStreakIfNeeded(userId)
            
            // Combine all data sources
            combine(
                streakRepository.getUserStreak(userId),
                motivationRepository.getLatestMotivation(userId),
                getTodayActivities()
            ) { streak, motivation, activities ->
                HomeUiState(
                    isLoading = false,
                    streakCount = streak?.count ?: 0,
                    motivationalText = motivation?.content ?: "Welcome to life. Start your journey today!",
                    todayActivities = activities
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    /**
     * Get all activities logged today.
     */
    private fun getTodayActivities(): Flow<List<ActivityItem>> {
        val startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)
        val endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59)
        
        return combine(
            workoutRepository.getWorkoutsForDateRange(userId, startOfDay, endOfDay),
            runRepository.getRunsForDateRange(userId, startOfDay, endOfDay),
            mealRepository.getMealsForDateRange(userId, startOfDay, endOfDay),
            journalRepository.getJournalsForDateRange(userId, startOfDay, endOfDay)
        ) { workouts, runs, meals, journals ->
            val activities = mutableListOf<ActivityItem>()
            
            // Add workouts
            workouts.forEach { workout ->
                activities.add(
                    ActivityItem(
                        title = workout.name,
                        description = "${workout.duration} min ${workout.type.name.lowercase()} workout",
                        time = workout.createdAt.format(timeFormatter),
                        icon = Icons.Filled.FitnessCenter
                    )
                )
            }
            
            // Add runs
            runs.forEach { run ->
                activities.add(
                    ActivityItem(
                        title = "${run.distance} km Run",
                        description = "${run.duration} min, ${run.pace} min/km",
                        time = run.createdAt.format(timeFormatter),
                        icon = Icons.Filled.DirectionsRun
                    )
                )
            }
            
            // Add meals
            meals.forEach { meal ->
                activities.add(
                    ActivityItem(
                        title = meal.name,
                        description = "${meal.type.name.lowercase()}${meal.calories?.let { " · $it cal" } ?: ""}",
                        time = meal.createdAt.format(timeFormatter),
                        icon = Icons.Filled.Restaurant
                    )
                )
            }
            
            // Add journals
            journals.forEach { journal ->
                activities.add(
                    ActivityItem(
                        title = "Journal Entry",
                        description = journal.mood?.let { "Mood: $it" } ?: "Journal entry",
                        time = journal.createdAt.format(timeFormatter),
                        icon = Icons.Filled.Book
                    )
                )
            }
            
            // Sort by time, most recent first
            activities.sortedByDescending { it.time }
        }
    }
    
    /**
     * Refresh the motivational text.
     */
    fun refreshMotivation() {
        viewModelScope.launch {
            val streak = streakRepository.getUserStreak(userId).first()
            val hasRecentActivity = _uiState.value.todayActivities.isNotEmpty()
            
            try {
                val motivationalText = deepseekService.generateMotivationalMessage(
                    streak = streak,
                    recentActivity = hasRecentActivity,
                    mood = null
                )
                
                motivationRepository.createMotivation(userId, motivationalText)
                
                _uiState.update { currentState ->
                    currentState.copy(motivationalText = motivationalText)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    // Navigation functions
    private val _navigationEvent = MutableStateFlow<NavigationEvent>(NavigationEvent.NONE)
    val navigationEvent: StateFlow<NavigationEvent> = _navigationEvent.asStateFlow()
    
    fun navigateToWorkout() {
        _navigationEvent.value = NavigationEvent.TO_WORKOUT
    }
    
    fun navigateToRun() {
        _navigationEvent.value = NavigationEvent.TO_RUN
    }
    
    fun navigateToNutrition() {
        _navigationEvent.value = NavigationEvent.TO_NUTRITION
    }
    
    fun navigateToJournal() {
        _navigationEvent.value = NavigationEvent.TO_JOURNAL
    }
    
    fun navigateToFaith() {
        _navigationEvent.value = NavigationEvent.TO_FAITH
    }
    
    fun clearNavigationEvent() {
        _navigationEvent.value = NavigationEvent.NONE
    }
    
    /**
     * Navigation events for the Home screen.
     */
    sealed class NavigationEvent {
        object TO_WORKOUT : NavigationEvent()
        object TO_RUN : NavigationEvent()
        object TO_NUTRITION : NavigationEvent()
        object TO_JOURNAL : NavigationEvent()
        object TO_FAITH : NavigationEvent()
        object NONE : NavigationEvent()
    }
}