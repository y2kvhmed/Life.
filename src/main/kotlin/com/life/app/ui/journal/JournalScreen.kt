package com.life.app.ui.journal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.life.app.R
import com.life.app.ui.theme.LifeTheme
import com.life.app.util.DateTimeUtils
import com.life.app.util.PermissionUtils

@Composable
fun JournalScreen(
    viewModel: JournalViewModel = hiltViewModel(),
    onNavigateToJournalEntry: (Long?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
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
                    text = stringResource(R.string.journal),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Journal Prompt Card
                if (uiState.journalPrompt.isNotEmpty()) {
                    JournalPromptCard(
                        prompt = uiState.journalPrompt,
                        isGenerating = uiState.isGeneratingPrompt,
                        onGeneratePrompt = { viewModel.generateJournalPrompt() },
                        onUsePrompt = { viewModel.usePromptForNewEntry() }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Journal Entries
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.journals.isEmpty()) {
                    EmptyJournalView(
                        onCreateJournal = { onNavigateToJournalEntry(null) },
                        onGeneratePrompt = { viewModel.generateJournalPrompt() }
                    )
                } else {
                    Text(
                        text = stringResource(R.string.your_entries),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.journals) { journal ->
                            JournalItem(
                                journal = journal,
                                onClick = { onNavigateToJournalEntry(journal.id) }
                            )
                        }
                    }
                }
            }
            
            // Error message
            if (uiState.errorMessage.isNotEmpty()) {
                ErrorSnackbar(
                    message = uiState.errorMessage,
                    onDismiss = { viewModel.clearError() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
            
            // Add Journal FAB
            FloatingActionButton(
                onClick = { onNavigateToJournalEntry(null) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.new_journal_entry)
                )
            }
        }
    }
}

@Composable
fun JournalPromptCard(
    prompt: String,
    isGenerating: Boolean,
    onGeneratePrompt: () -> Unit,
    onUsePrompt: () -> Unit
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
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = stringResource(R.string.journal_prompt),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(
                    onClick = onGeneratePrompt,
                    enabled = !isGenerating
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.generate_new_prompt),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isGenerating) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Text(
                    text = "\"$prompt\"",
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onUsePrompt,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(R.string.use_this_prompt))
                }
            }
        }
    }
}

@Composable
fun EmptyJournalView(
    onCreateJournal: () -> Unit,
    onGeneratePrompt: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Book,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.no_journal_entries),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.start_journaling),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onCreateJournal,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Create,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.write_first_entry))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(
            onClick = onGeneratePrompt
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.get_writing_prompt))
        }
    }
}

@Composable
fun JournalItem(
    journal: JournalUiModel,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Date and mood
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateTimeUtils.formatDate(journal.date),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Mood indicator if available
                journal.mood?.let { mood ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(getMoodColor(mood)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getMoodEmoji(mood),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Title if available
            if (journal.title.isNotBlank()) {
                Text(
                    text = journal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            // Content preview
            Text(
                text = journal.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // Tags if available
            if (journal.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    journal.tags.take(3).forEach { tag ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(tag) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                    
                    // Show +X more if there are more than 3 tags
                    if (journal.tags.size > 3) {
                        Text(
                            text = "+${journal.tags.size - 3}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        }
    }
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

// Data class for UI
data class JournalUiModel(
    val id: Long,
    val title: String,
    val content: String,
    val date: LocalDateTime,
    val mood: Int? = null,
    val tags: List<String> = emptyList(),
    val isEncrypted: Boolean = false
)