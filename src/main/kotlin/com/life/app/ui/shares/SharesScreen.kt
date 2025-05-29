package com.life.app.ui.shares

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.life.app.R
import com.life.app.ui.theme.LifeTheme
import com.life.app.util.DateTimeUtils

@Composable
fun SharesScreen(
    viewModel: SharesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
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
                    text = stringResource(R.string.shares),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Tabs for Trending and Recent
                var selectedTabIndex by remember { mutableIntStateOf(0) }
                val tabs = listOf(
                    stringResource(R.string.trending),
                    stringResource(R.string.recent)
                )
                
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Content based on selected tab
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.shares.isEmpty()) {
                    EmptySharesView(
                        onCreateShare = { viewModel.showAddShareDialog() }
                    )
                } else {
                    val filteredShares = if (selectedTabIndex == 0) {
                        // Trending - sort by likes
                        uiState.shares.sortedByDescending { it.likeCount }
                    } else {
                        // Recent - sort by date
                        uiState.shares.sortedByDescending { it.date }
                    }
                    
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredShares) { share ->
                            ShareItem(
                                share = share,
                                onLikeClick = { viewModel.toggleLike(share.id) },
                                onCommentClick = { viewModel.selectShare(share.id) }
                            )
                        }
                        
                        // Add some space at the bottom
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
            
            // Add Share FAB
            FloatingActionButton(
                onClick = { viewModel.showAddShareDialog() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.share_something)
                )
            }
            
            // Add Share Dialog
            if (uiState.showAddShareDialog) {
                AddShareDialog(
                    onDismiss = { viewModel.hideAddShareDialog() },
                    onAddShare = { content ->
                        viewModel.addShare(content)
                    }
                )
            }
            
            // Share Detail Dialog with Comments
            if (uiState.selectedShare != null) {
                ShareDetailDialog(
                    share = uiState.selectedShare!!,
                    comments = uiState.comments,
                    onDismiss = { viewModel.deselectShare() },
                    onAddComment = { comment ->
                        viewModel.addComment(uiState.selectedShare!!.id, comment)
                    },
                    onLikeClick = { viewModel.toggleLike(uiState.selectedShare!!.id) },
                    isAddingComment = uiState.isAddingComment
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
        }
    }
}

@Composable
fun ShareItem(
    share: ShareUiModel,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Content
            Text(
                text = share.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Metadata and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date
                Text(
                    text = DateTimeUtils.getRelativeTimeSpan(share.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Like button
                IconButton(
                    onClick = onLikeClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (share.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(R.string.like),
                        tint = if (share.isLiked) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = share.likeCount.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Comment button
                IconButton(
                    onClick = onCommentClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = stringResource(R.string.comment),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = share.commentCount.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptySharesView(
    onCreateShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Forum,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.no_shares_yet),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.be_the_first_to_share),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onCreateShare,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.share_something))
        }
    }
}

@Composable
fun AddShareDialog(
    onDismiss: () -> Unit,
    onAddShare: (String) -> Unit
) {
    var shareContent by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.share_something)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.share_anonymously),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = shareContent,
                    onValueChange = { shareContent = it },
                    placeholder = { Text(stringResource(R.string.whats_on_your_mind)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (shareContent.isNotBlank()) {
                        onAddShare(shareContent)
                        onDismiss()
                    }
                },
                enabled = shareContent.isNotBlank()
            ) {
                Text(stringResource(R.string.share))
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
fun ShareDetailDialog(
    share: ShareUiModel,
    comments: List<CommentUiModel>,
    onDismiss: () -> Unit,
    onAddComment: (String) -> Unit,
    onLikeClick: () -> Unit,
    isAddingComment: Boolean
) {
    var commentText by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Share content
                Text(
                    text = share.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Metadata and actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date
                    Text(
                        text = DateTimeUtils.getRelativeTimeSpan(share.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Like button
                    IconButton(
                        onClick = onLikeClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (share.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.like),
                            tint = if (share.isLiked) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Text(
                        text = share.likeCount.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                
                // Comments section
                Text(
                    text = stringResource(R.string.comments),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Comments list
                if (comments.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_comments_yet),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        comments.forEach { comment ->
                            CommentItem(comment = comment)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Add comment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text(stringResource(R.string.add_a_comment)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                onAddComment(commentText)
                                commentText = ""
                            }
                        },
                        enabled = commentText.isNotBlank() && !isAddingComment
                    ) {
                        if (isAddingComment) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = stringResource(R.string.send),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
        dismissButton = null
    )
}

@Composable
fun CommentItem(comment: CommentUiModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Comment content
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Comment date
            Text(
                text = DateTimeUtils.getRelativeTimeSpan(comment.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.End)
            )
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

// Data classes for UI
data class ShareUiModel(
    val id: Long,
    val content: String,
    val date: LocalDateTime,
    val likeCount: Int,
    val commentCount: Int,
    val isLiked: Boolean
)

data class CommentUiModel(
    val id: Long,
    val shareId: Long,
    val content: String,
    val date: LocalDateTime
)