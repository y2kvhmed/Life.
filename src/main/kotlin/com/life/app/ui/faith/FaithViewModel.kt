package com.life.app.ui.faith

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.life.app.data.model.Prayer
import com.life.app.data.model.Religion
import com.life.app.data.repository.PrayerRepository
import com.life.app.data.repository.UserRepository
import com.life.app.data.remote.DeepseekService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel for the Faith screen.
 */
@HiltViewModel
class FaithViewModel @Inject constructor(
    private val prayerRepository: PrayerRepository,
    private val userRepository: UserRepository,
    private val deepseekService: DeepseekService
) : ViewModel() {

    private val _uiState = MutableStateFlow(FaithUiState())
    val uiState: StateFlow<FaithUiState> = _uiState.asStateFlow()

    init {
        loadUserReligion()
    }

    /**
     * Load the user's selected religion from the repository.
     */
    private fun loadUserReligion() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                val user = userRepository.getUser()
                val religion = user?.religion ?: Religion.ISLAM
                
                _uiState.update { it.copy(
                    selectedReligion = religion,
                    isLoading = false,
                    errorMessage = ""
                ) }
                
                // Load religion-specific data
                loadReligionData(religion)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load user religion"
                ) }
            }
        }
    }

    /**
     * Load religion-specific data based on the selected religion.
     */
    private fun loadReligionData(religion: Religion) {
        viewModelScope.launch {
            try {
                // Load prayer times
                val prayerTimes = prayerRepository.getPrayerTimes(religion)
                
                // Load completed prayers for today
                val completedPrayers = prayerRepository.getCompletedPrayers(LocalDateTime.now())
                    .map { it.name }
                    .toSet()
                
                // Load religious message
                val message = prayerRepository.getReligiousMessage(religion)
                
                // Load religion-specific data
                when (religion) {
                    Religion.ISLAM -> {
                        val isFasting = prayerRepository.isFasting()
                        _uiState.update { it.copy(
                            prayerTimes = prayerTimes,
                            completedPrayers = completedPrayers,
                            religiousMessage = message ?: "",
                            isFasting = isFasting,
                            isLoading = false,
                            errorMessage = ""
                        ) }
                    }
                    Religion.CHRISTIANITY -> {
                        _uiState.update { it.copy(
                            prayerTimes = prayerTimes,
                            completedPrayers = completedPrayers,
                            religiousMessage = message ?: "",
                            isLoading = false,
                            errorMessage = ""
                        ) }
                    }
                    Religion.JUDAISM -> {
                        val isShabbat = prayerRepository.isShabbat()
                        val completedMitzvot = prayerRepository.getCompletedMitzvotCount()
                        _uiState.update { it.copy(
                            prayerTimes = prayerTimes,
                            completedPrayers = completedPrayers,
                            religiousMessage = message ?: "",
                            isShabbat = isShabbat,
                            completedMitzvot = completedMitzvot,
                            isLoading = false,
                            errorMessage = ""
                        ) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load religion data"
                ) }
            }
        }
    }

    /**
     * Select a religion.
     */
    fun selectReligion(religion: Religion) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(
                    selectedReligion = religion,
                    isLoading = true
                ) }
                
                // Update user's religion preference
                userRepository.updateReligion(religion)
                
                // Load religion-specific data
                loadReligionData(religion)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to update religion"
                ) }
            }
        }
    }

    /**
     * Mark a prayer as completed.
     */
    fun completePrayer(prayerName: String) {
        viewModelScope.launch {
            try {
                val isCompleted = uiState.value.completedPrayers.contains(prayerName)
                
                if (isCompleted) {
                    // Remove prayer from completed list
                    prayerRepository.uncompletePrayer(prayerName)
                    
                    _uiState.update { it.copy(
                        completedPrayers = it.completedPrayers - prayerName
                    ) }
                } else {
                    // Add prayer to completed list
                    val prayer = Prayer(
                        id = 0, // Will be auto-generated by Room
                        userId = "", // Will be set by the repository
                        name = prayerName,
                        religion = uiState.value.selectedReligion,
                        date = LocalDateTime.now()
                    )
                    
                    prayerRepository.completePrayer(prayer)
                    
                    _uiState.update { it.copy(
                        completedPrayers = it.completedPrayers + prayerName
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = e.message ?: "Failed to update prayer"
                ) }
            }
        }
    }

    /**
     * Generate a religious message using Deepseek AI.
     */
    fun generateReligiousMessage() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isGeneratingMessage = true) }
                
                val message = deepseekService.generateReligiousMessage(uiState.value.selectedReligion)
                
                // Save the message to the repository
                prayerRepository.saveReligiousMessage(uiState.value.selectedReligion, message)
                
                _uiState.update { it.copy(
                    religiousMessage = message,
                    isGeneratingMessage = false,
                    errorMessage = ""
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isGeneratingMessage = false,
                    errorMessage = e.message ?: "Failed to generate religious message"
                ) }
            }
        }
    }

    /**
     * Toggle fasting status (for Islam).
     */
    fun toggleFasting() {
        viewModelScope.launch {
            try {
                val newFastingStatus = !uiState.value.isFasting
                
                prayerRepository.setFasting(newFastingStatus)
                
                _uiState.update { it.copy(
                    isFasting = newFastingStatus,
                    errorMessage = ""
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = e.message ?: "Failed to update fasting status"
                ) }
            }
        }
    }

    /**
     * Toggle Shabbat status (for Judaism).
     */
    fun toggleShabbat() {
        viewModelScope.launch {
            try {
                val newShabbatStatus = !uiState.value.isShabbat
                
                prayerRepository.setShabbat(newShabbatStatus)
                
                _uiState.update { it.copy(
                    isShabbat = newShabbatStatus,
                    errorMessage = ""
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = e.message ?: "Failed to update Shabbat status"
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
}

/**
 * UI state for the Faith screen.
 */
data class FaithUiState(
    val selectedReligion: Religion = Religion.ISLAM,
    val prayerTimes: Map<String, String> = emptyMap(),
    val completedPrayers: Set<String> = emptySet(),
    val religiousMessage: String = "",
    val isFasting: Boolean = false,
    val isShabbat: Boolean = false,
    val completedMitzvot: Int = 0,
    val isLoading: Boolean = false,
    val isGeneratingMessage: Boolean = false,
    val errorMessage: String = ""
)