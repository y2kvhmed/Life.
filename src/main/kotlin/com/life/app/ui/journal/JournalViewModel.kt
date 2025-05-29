package com.life.app.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.life.app.data.model.Journal
import com.life.app.data.repository.JournalRepository
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
 * ViewModel for the Journal screen.
 */
@HiltViewModel
class JournalViewModel @Inject constructor(
    private val journalRepository: JournalRepository,
    private val deepseekService: DeepseekService
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    init {
        loadJournals()
    }

    /**
     * Load user's journal entries from the repository.
     */
    private fun loadJournals() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val journals = journalRepository.getJournals()
                val journalUiModels = journals.map { journal ->
                    JournalUiModel(
                        id = journal.id,
                        title = journal.title ?: "",
                        content = journal.content,
                        date = journal.date,
                        mood = journal.mood,
                        tags = journal.tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),
                        isEncrypted = journal.isEncrypted ?: false
                    )
                }
                _uiState.update { it.copy(
                    journals = journalUiModels,
                    isLoading = false,
                    errorMessage = ""
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load journals"
                ) }
            }
        }
    }

    /**
     * Generate a journal prompt using Deepseek AI.
     */
    fun generateJournalPrompt() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isGeneratingPrompt = true) }
                
                val prompt = deepseekService.generateJournalPrompt()
                
                _uiState.update { it.copy(
                    journalPrompt = prompt,
                    isGeneratingPrompt = false,
                    errorMessage = ""
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isGeneratingPrompt = false,
                    errorMessage = e.message ?: "Failed to generate journal prompt"
                ) }
            }
        }
    }

    /**
     * Use the current prompt for a new journal entry.
     * This will be handled by the JournalEntryViewModel when navigating to the entry screen.
     */
    fun usePromptForNewEntry() {
        // This function sets a flag that will be observed by the JournalEntryViewModel
        // to pre-fill the journal entry with the prompt
        _uiState.update { it.copy(usePromptForNewEntry = true) }
    }

    /**
     * Reset the flag for using prompt for new entry.
     * This should be called after navigating to the entry screen.
     */
    fun resetUsePromptFlag() {
        _uiState.update { it.copy(usePromptForNewEntry = false) }
    }

    /**
     * Get the current journal prompt.
     * This will be used by the JournalEntryViewModel when creating a new entry.
     */
    fun getCurrentPrompt(): String {
        return uiState.value.journalPrompt
    }

    /**
     * Clear the error message.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = "") }
    }
}

/**
 * UI state for the Journal screen.
 */
data class JournalUiState(
    val journals: List<JournalUiModel> = emptyList(),
    val journalPrompt: String = "",
    val isLoading: Boolean = false,
    val isGeneratingPrompt: Boolean = false,
    val usePromptForNewEntry: Boolean = false,
    val errorMessage: String = ""
)