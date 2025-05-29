package com.life.app.ui.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.life.app.data.remote.DeepseekService
import com.life.app.data.repository.MotivationRepository
import com.life.app.data.repository.PlanRepository
import com.life.app.util.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for the AI Coach feature.
 */
@HiltViewModel
class CoachViewModel @Inject constructor(
    private val deepseekService: DeepseekService,
    private val motivationRepository: MotivationRepository,
    private val planRepository: PlanRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoachUiState())
    val uiState: StateFlow<CoachUiState> = _uiState.asStateFlow()

    init {
        // Add a welcome message
        addAiMessage("Hello! I'm your AI coach. How can I help you today with your fitness, nutrition, or wellness goals?")
    }

    /**
     * Send a message to the AI coach.
     */
    fun sendMessage(content: String) {
        if (content.isBlank()) return
        
        // Add user message to the chat
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = content,
            isFromUser = true
        )
        
        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                isLoading = true,
                errorMessage = ""
            )
        }
        
        // Process the message with Deepseek AI
        viewModelScope.launch {
            try {
                if (!networkUtils.isNetworkAvailable()) {
                    throw Exception("No internet connection. Please check your network and try again.")
                }
                
                val response = deepseekService.processUserQuery(content)
                addAiMessage(response)
                
                // Save interesting insights or suggestions as motivations
                if (shouldSaveAsMotivation(content, response)) {
                    motivationRepository.createMotivation(response)
                }
                
                // If it's a plan request, save it as a plan
                if (isPlanRequest(content)) {
                    planRepository.generatePlan(content, response)
                }
                
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to get a response. Please try again."
                    )
                }
                
                // Add error message to chat
                addAiMessage("I'm sorry, I encountered an error: ${e.message ?: "Unknown error"}. Please try again.")
            }
        }
    }

    /**
     * Generate a daily plan for the user.
     */
    fun generateDailyPlan() {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                if (!networkUtils.isNetworkAvailable()) {
                    throw Exception("No internet connection. Please check your network and try again.")
                }
                
                val plan = deepseekService.generateDailyPlan()
                addAiMessage("Here's your daily plan:\n\n$plan")
                
                // Save the plan
                planRepository.createPlan(
                    title = "Daily Plan for ${java.time.LocalDate.now()}",
                    description = plan,
                    tags = listOf("daily", "ai-generated")
                )
                
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to generate a daily plan. Please try again."
                    )
                }
                
                // Add error message to chat
                addAiMessage("I'm sorry, I couldn't generate a daily plan: ${e.message ?: "Unknown error"}. Please try again.")
            }
        }
    }

    /**
     * Generate workout suggestions for the user.
     */
    fun generateWorkoutSuggestions() {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                if (!networkUtils.isNetworkAvailable()) {
                    throw Exception("No internet connection. Please check your network and try again.")
                }
                
                val suggestions = deepseekService.generateWorkoutSuggestions()
                addAiMessage("Here are some workout suggestions for you:\n\n$suggestions")
                
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to generate workout suggestions. Please try again."
                    )
                }
                
                // Add error message to chat
                addAiMessage("I'm sorry, I couldn't generate workout suggestions: ${e.message ?: "Unknown error"}. Please try again.")
            }
        }
    }

    /**
     * Generate meal suggestions for the user.
     */
    fun generateMealSuggestions() {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                if (!networkUtils.isNetworkAvailable()) {
                    throw Exception("No internet connection. Please check your network and try again.")
                }
                
                val suggestions = deepseekService.generateMealSuggestions()
                addAiMessage("Here are some meal suggestions for you:\n\n$suggestions")
                
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to generate meal suggestions. Please try again."
                    )
                }
                
                // Add error message to chat
                addAiMessage("I'm sorry, I couldn't generate meal suggestions: ${e.message ?: "Unknown error"}. Please try again.")
            }
        }
    }

    /**
     * Generate a motivational message for the user.
     */
    fun generateMotivationalMessage() {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                if (!networkUtils.isNetworkAvailable()) {
                    throw Exception("No internet connection. Please check your network and try again.")
                }
                
                val message = deepseekService.generateMotivationalMessage()
                addAiMessage(message)
                
                // Save the motivational message
                motivationRepository.createMotivation(message)
                
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to generate a motivational message. Please try again."
                    )
                }
                
                // Add error message to chat
                addAiMessage("I'm sorry, I couldn't generate a motivational message: ${e.message ?: "Unknown error"}. Please try again.")
            }
        }
    }

    /**
     * Add an AI message to the chat.
     */
    private fun addAiMessage(content: String) {
        val aiMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = content,
            isFromUser = false
        )
        
        _uiState.update { state ->
            state.copy(
                messages = state.messages + aiMessage,
                isLoading = false
            )
        }
    }

    /**
     * Determine if a message should be saved as a motivation.
     */
    private fun shouldSaveAsMotivation(userQuery: String, aiResponse: String): Boolean {
        val motivationalKeywords = listOf(
            "motivat", "inspir", "encourage", "uplift", "positive",
            "boost", "spirit", "mood", "happy", "joy", "energy"
        )
        
        return motivationalKeywords.any { keyword ->
            userQuery.contains(keyword, ignoreCase = true) ||
            aiResponse.contains(keyword, ignoreCase = true)
        }
    }

    /**
     * Determine if a message is a plan request.
     */
    private fun isPlanRequest(userQuery: String): Boolean {
        val planKeywords = listOf(
            "plan", "schedule", "routine", "program", "regimen",
            "create a", "make me a", "design a", "develop a"
        )
        
        return planKeywords.any { keyword ->
            userQuery.contains(keyword, ignoreCase = true)
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
 * UI state for the Coach screen.
 */
data class CoachUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)