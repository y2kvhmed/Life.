package com.life.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.life.app.data.repository.RunRepository
import com.life.app.data.repository.StreakRepository
import com.life.app.data.repository.UserRepository
import com.life.app.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Profile screen.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val runRepository: RunRepository,
    private val streakRepository: StreakRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    /**
     * Load user profile data.
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = "") }
                
                // Get current user ID
                val userId = userRepository.getCurrentUserId()
                if (userId.isEmpty()) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "User not logged in"
                    ) }
                    return@launch
                }
                
                // Get user data
                val user = userRepository.getUser()
                if (user == null) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "User data not found"
                    ) }
                    return@launch
                }
                
                // Get workout count
                val workoutCount = workoutRepository.getWorkoutCount(userId)
                
                // Get run count
                val runCount = runRepository.getRunCount(userId)
                
                // Get streak days
                val streak = streakRepository.getCurrentStreak(userId)
                val streakDays = streak?.days ?: 0
                
                // Update UI state
                _uiState.update { it.copy(
                    isLoading = false,
                    name = user.displayName,
                    email = user.email,
                    phone = user.phone ?: "",
                    workoutCount = workoutCount,
                    runCount = runCount,
                    streakDays = streakDays
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load profile"
                ) }
            }
        }
    }

    /**
     * Clear the error message.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = "") }
    }
    
    /**
     * Alias for clearError() to maintain compatibility with other screens.
     */
    fun clearErrorMessage() {
        clearError()
    }
}

/**
 * UI state for the Profile screen.
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val workoutCount: Int = 0,
    val runCount: Int = 0,
    val streakDays: Int = 0,
    val errorMessage: String = ""
)