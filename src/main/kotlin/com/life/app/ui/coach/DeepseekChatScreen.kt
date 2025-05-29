package com.life.app.ui.coach

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.life.app.R
import com.life.app.ui.theme.LifeTheme
import kotlinx.coroutines.launch

@Composable
fun DeepseekChatScreen(
    viewModel: CoachViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    
    var userInput by remember { mutableStateOf("") }
    
    // Scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }
    
    LifeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Chat messages
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    state = listState,
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    if (uiState.messages.isEmpty()) {
                        item {
                            EmptyChatPlaceholder()
                        }
                    } else {
                        items(uiState.messages) { message ->
                            ChatMessageItem(
                                message = message,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                    
                    // Loading indicator for when a message is being processed
                    item {
                        AnimatedVisibility(
                            visible = uiState.isLoading,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
                
                // Input field
                ChatInputField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    onSend = {
                        if (userInput.isNotBlank() && !uiState.isLoading) {
                            viewModel.sendMessage(userInput)
                            userInput = ""
                            focusManager.clearFocus()
                            coroutineScope.launch {
                                // Scroll to bottom
                                listState.animateScrollToItem(uiState.messages.size)
                            }
                        }
                    },
                    isLoading = uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyChatPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.ai_coach),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.coach_welcome_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        SuggestionChips()
    }
}

@Composable
fun SuggestionChips() {
    val suggestions = listOf(
        "Create a workout plan",
        "Suggest a healthy meal",
        "Help me with motivation",
        "Give me a journal prompt"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        suggestions.forEach { suggestion ->
            SuggestionChip(
                onClick = { /* Handle suggestion click */ },
                label = { Text(suggestion) },
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isUserMessage = message.isFromUser
    val backgroundColor = if (isUserMessage) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val textColor = if (isUserMessage) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start
    ) {
        if (!isUserMessage) {
            // AI avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.Top),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AI",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Surface(
            shape = RoundedCornerShape(
                topStart = if (isUserMessage) 16.dp else 0.dp,
                topEnd = if (isUserMessage) 0.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            color = backgroundColor,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.content,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(12.dp)
            )
        }
        
        if (isUserMessage) {
            Spacer(modifier = Modifier.width(8.dp))
            
            // User avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
                    .align(Alignment.Top),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You",
                    color = MaterialTheme.colorScheme.onTertiary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun ChatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(stringResource(R.string.ask_coach)) },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                maxLines = 4
            )
            
            IconButton(
                onClick = onSend,
                enabled = !isLoading && value.isNotBlank(),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .size(48.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = stringResource(R.string.send),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

/**
 * Data class representing a chat message.
 */
data class ChatMessage(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)