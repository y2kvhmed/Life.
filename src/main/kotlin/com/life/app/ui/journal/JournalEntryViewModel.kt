package com.life.app.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.life.app.data.model.Journal
import com.life.app.data.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel for the Journal Entry screen.
 */
@HiltViewModel
class JournalEntryViewModel @Inject constructor(
    private val journalRepository: JournalRepository,
    private val journalViewModel: JournalViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalEntryUiState())
    val uiState: StateFlow<JournalEntryUiState> = _uiState.asStateFlow()
    
    private var originalJournal: Journal? = null
    private var journalId: Long = 0

    init {
        // Check if we should use a prompt from the JournalViewModel
        if (journalViewModel.uiState.value.usePromptForNewEntry) {
            val prompt = journalViewModel.getCurrentPrompt()
            if (prompt.isNotEmpty()) {
                _uiState.update { it.copy(
                    content = "Prompt: \"$prompt\"\n\n",
                    hasChanges = true
                ) }
            }
            journalViewModel.resetUsePromptFlag()
        }
    }

    /**
     * Load a journal for editing.
     */
    fun loadJournal(id: Long) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                val journal = journalRepository.getJournal(id)
                originalJournal = journal
                journalId = journal.id
                
                _uiState.update { it.copy(
                    title = journal.title ?: "",
                    content = journal.content,
                    mood = journal.mood,
                    tags = journal.tags ?: "",
                    isEncrypted = journal.isEncrypted ?: false,
                    isLoading = false,
                    hasChanges = false,
                    errorMessage = ""
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load journal"
                ) }
            }
        }
    }

    /**
     * Save the current journal entry.
     */
    fun saveJournal(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true) }
                
                val journal = Journal(
                    id = journalId,
                    userId = "", // Will be set by the repository
                    title = uiState.value.title.ifBlank { null },
                    content = uiState.value.content,
                    date = LocalDateTime.now(),
                    mood = uiState.value.mood,
                    tags = uiState.value.tags.ifBlank { null },
                    isEncrypted = uiState.value.isEncrypted
                )
                
                if (journalId > 0) {
                    // Update existing journal
                    journalRepository.updateJournal(journal)
                } else {
                    // Create new journal
                    journalRepository.createJournal(journal)
                }
                
                _uiState.update { it.copy(
                    isSaving = false,
                    hasChanges = false,
                    errorMessage = ""
                ) }
                
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Failed to save journal"
                ) }
            }
        }
    }

    /**
     * Delete a journal entry.
     */
    fun deleteJournal(id: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                journalRepository.deleteJournal(id)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = e.message ?: "Failed to delete journal"
                ) }
            }
        }
    }

    /**
     * Update the journal title.
     */
    fun updateTitle(title: String) {
        _uiState.update { it.copy(
            title = title,
            hasChanges = hasChangesFromOriginal(title = title)
        ) }
    }

    /**
     * Update the journal content.
     */
    fun updateContent(content: String) {
        _uiState.update { it.copy(
            content = content,
            hasChanges = hasChangesFromOriginal(content = content)
        ) }
    }

    /**
     * Append text to the journal content.
     * Used for voice input.
     */
    fun appendContent(text: String) {
        val updatedContent = uiState.value.content + "\n" + text
        updateContent(updatedContent)
    }

    /**
     * Update the journal mood.
     */
    fun updateMood(mood: Int) {
        _uiState.update { it.copy(
            mood = mood,
            hasChanges = hasChangesFromOriginal(mood = mood)
        ) }
    }

    /**
     * Update the journal tags.
     */
    fun updateTags(tags: String) {
        _uiState.update { it.copy(
            tags = tags,
            hasChanges = hasChangesFromOriginal(tags = tags)
        ) }
    }

    /**
     * Update the journal encryption status.
     */
    fun updateEncrypted(isEncrypted: Boolean) {
        _uiState.update { it.copy(
            isEncrypted = isEncrypted,
            hasChanges = hasChangesFromOriginal(isEncrypted = isEncrypted)
        ) }
    }

    /**
     * Show the voice input dialog.
     */
    fun showVoiceInputDialog() {
        _uiState.update { it.copy(showVoiceInputDialog = true) }
    }

    /**
     * Hide the voice input dialog.
     */
    fun hideVoiceInputDialog() {
        _uiState.update { it.copy(showVoiceInputDialog = false) }
    }

    /**
     * Show the discard changes dialog.
     */
    fun showDiscardDialog() {
        _uiState.update { it.copy(showDiscardDialog = true) }
    }

    /**
     * Hide the discard changes dialog.
     */
    fun hideDiscardDialog() {
        _uiState.update { it.copy(showDiscardDialog = false) }
    }

    /**
     * Clear the error message.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = "") }
    }

    /**
     * Check if the current state has changes from the original journal.
     */
    private fun hasChangesFromOriginal(
        title: String = uiState.value.title,
        content: String = uiState.value.content,
        mood: Int? = uiState.value.mood,
        tags: String = uiState.value.tags,
        isEncrypted: Boolean = uiState.value.isEncrypted
    ): Boolean {
        // If it's a new journal, check if any fields have content
        if (originalJournal == null) {
            return title.isNotBlank() || content.isNotBlank() || tags.isNotBlank() || mood != 3
        }
        
        // If it's an existing journal, check if any fields have changed
        return title != (originalJournal?.title ?: "") ||
                content != originalJournal?.content ||
                mood != originalJournal?.mood ||
                tags != (originalJournal?.tags ?: "") ||
                isEncrypted != (originalJournal?.isEncrypted ?: false)
    }
}

/**
 * UI state for the Journal Entry screen.
 */
data class JournalEntryUiState(
    val title: String = "",
    val content: String = "",
    val mood: Int? = 3, // Default to neutral mood
    val tags: String = "",
    val isEncrypted: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val hasChanges: Boolean = false,
    val showVoiceInputDialog: Boolean = false,
    val showDiscardDialog: Boolean = false,
    val errorMessage: String = ""
)