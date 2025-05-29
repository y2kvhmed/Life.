package com.life.app.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.life.app.R
import com.life.app.ui.theme.LifeTheme
import com.life.app.util.PermissionUtils

@Composable
fun JournalEntryScreen(
    journalId: Long?,
    viewModel: JournalEntryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Load journal if editing an existing one
    LaunchedEffect(journalId) {
        if (journalId != null && journalId > 0) {
            viewModel.loadJournal(journalId)
        }
    }
    
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
                // Top Bar with back button and save/delete actions
                JournalEntryTopBar(
                    isNewEntry = journalId == null,
                    isSaving = uiState.isSaving,
                    onBackClick = onNavigateBack,
                    onSaveClick = { viewModel.saveJournal(onSuccess = onNavigateBack) },
                    onDeleteClick = {
                        if (journalId != null) {
                            viewModel.deleteJournal(journalId, onSuccess = onNavigateBack)
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Journal Entry Form
                JournalEntryForm(
                    title = uiState.title,
                    onTitleChange = { viewModel.updateTitle(it) },
                    content = uiState.content,
                    onContentChange = { viewModel.updateContent(it) },
                    mood = uiState.mood,
                    onMoodChange = { viewModel.updateMood(it) },
                    tags = uiState.tags,
                    onTagsChange = { viewModel.updateTags(it) },
                    isEncrypted = uiState.isEncrypted,
                    onEncryptedChange = { viewModel.updateEncrypted(it) },
                    isLoading = uiState.isLoading
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
            
            // Voice input dialog
            if (uiState.showVoiceInputDialog) {
                VoiceInputDialog(
                    onDismiss = { viewModel.hideVoiceInputDialog() },
                    onTextRecognized = { viewModel.appendContent(it) }
                )
            }
            
            // Discard changes dialog
            if (uiState.showDiscardDialog) {
                DiscardChangesDialog(
                    onConfirm = {
                        viewModel.hideDiscardDialog()
                        onNavigateBack()
                    },
                    onDismiss = { viewModel.hideDiscardDialog() }
                )
            }
        }
    }
    
    // Handle back press
    BackHandler(enabled = uiState.hasChanges) {
        viewModel.showDiscardDialog()
    }
}

@Composable
fun JournalEntryTopBar(
    isNewEntry: Boolean,
    isSaving: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(R.string.back)
            )
        }
        
        Text(
            text = stringResource(
                if (isNewEntry) R.string.new_journal_entry else R.string.edit_journal_entry
            ),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Save button
        Button(
            onClick = onSaveClick,
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.save))
            }
        }
        
        // Delete button (only for existing entries)
        if (!isNewEntry) {
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun JournalEntryForm(
    title: String,
    onTitleChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit,
    mood: Int?,
    onMoodChange: (Int) -> Unit,
    tags: String,
    onTagsChange: (String) -> Unit,
    isEncrypted: Boolean,
    onEncryptedChange: (Boolean) -> Unit,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text(stringResource(R.string.title_optional)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mood selector
            MoodSelector(
                selectedMood = mood ?: 3,
                onMoodSelected = onMoodChange
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Content
            OutlinedTextField(
                value = content,
                onValueChange = onContentChange,
                label = { Text(stringResource(R.string.your_thoughts)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text
                ),
                trailingIcon = {
                    Row {
                        // Voice input button
                        IconButton(onClick = { /* Show voice input dialog */ }) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = stringResource(R.string.voice_input)
                            )
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tags
            OutlinedTextField(
                value = tags,
                onValueChange = onTagsChange,
                label = { Text(stringResource(R.string.tags_comma_separated)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    imeAction = ImeAction.Done
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Encryption toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.encrypt_entry),
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Switch(
                    checked = isEncrypted,
                    onCheckedChange = onEncryptedChange
                )
            }
            
            if (isEncrypted) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.encryption_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            
            // Add some space at the bottom for better scrolling experience
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun MoodSelector(
    selectedMood: Int,
    onMoodSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.how_are_you_feeling),
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (mood in 1..5) {
                MoodButton(
                    mood = mood,
                    isSelected = mood == selectedMood,
                    onSelect = { onMoodSelected(mood) }
                )
            }
        }
    }
}

@Composable
fun MoodButton(
    mood: Int,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val emoji = getMoodEmoji(mood)
    val color = getMoodColor(mood)
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) color else color.copy(alpha = 0.3f))
            .padding(4.dp)
            .clickable(onClick = onSelect),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun VoiceInputDialog(
    onDismiss: () -> Unit,
    onTextRecognized: (String) -> Unit
) {
    // In a real app, this would use the Speech Recognition API
    // For this implementation, we'll just show a placeholder
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.voice_input)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.listening),
                    style = MaterialTheme.typography.bodyLarge
                )
                
                // In a real app, this would show the recognized text in real-time
                // and have proper recording controls
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Simulate recognized text
                    onTextRecognized("This is a simulated voice input. In a real app, this would be the text recognized from your speech.")
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.done))
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
fun DiscardChangesDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.discard_changes)) },
        text = { Text(stringResource(R.string.discard_changes_message)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.discard))
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

// Helper functions
fun getMoodEmoji(mood: Int): String {
    return when (mood) {
        1 -> "😢"
        2 -> "😕"
        3 -> "😐"
        4 -> "🙂"
        5 -> "😄"
        else -> "😐"
    }
}

fun getMoodColor(mood: Int): Color {
    return when (mood) {
        1 -> Color(0xFFE57373) // Red-ish
        2 -> Color(0xFFFFB74D) // Orange-ish
        3 -> Color(0xFFFFEE58) // Yellow-ish
        4 -> Color(0xFF81C784) // Light green
        5 -> Color(0xFF4CAF50) // Green
        else -> Color(0xFFFFEE58) // Default yellow
    }
}

// BackHandler composable for handling back button presses
@Composable
fun BackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    // In a real app, this would use the actual BackHandler from androidx.activity.compose
    // For this implementation, we'll just define the interface
    
    // The actual implementation would look like:
    // androidx.activity.compose.BackHandler(enabled) {
    //     onBack()
    // }
    
    // Since we can't import the actual BackHandler, this is just a placeholder
    DisposableEffect(enabled) {
        onDispose {}
    }
}