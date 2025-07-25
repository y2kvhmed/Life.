package com.life.app.ui.shares

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.life.app.data.model.Comment
import com.life.app.data.model.Share
import com.life.app.data.repository.ShareRepository
import com.life.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel for the Shares screen.
 */
@HiltViewModel
class SharesViewModel @Inject constructor(
    private val shareRepository: ShareRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SharesUiState())
    val uiState: StateFlow<SharesUiState> = _uiState.asStateFlow()

    init {
        loadShares()
    }

    /**
     * Load all shares from the repository.
     */
    private fun loadShares() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                val shares = shareRepository.getAllShares()
                val userId = userRepository.getUserId()
                
                val shareUiModels = shares.map { share ->
                    ShareUiModel(
                        id = share.id,
                        content = share.content,
                        date = share.date,
                        likeCount = share.likeCount,
                        commentCount = share.commentCount,
                        isLiked = share.likedBy.contains(userId)
                    )
                }
                
                _uiState.update { it.copy(
                    shares = shareUiModels,
                    isLoading = false,
                    errorMessage = ""
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load shares"
                ) }
            }
        }
    }

    /**
     * Add a new share.
     */
    fun addShare(content: String) {
        viewModelScope.launch {
            try {
                val share = Share(
                    id = 0, // Will be auto-generated by Room
                    content = content,
                    date = LocalDateTime.now(),
                    likeCount = 0,
                    commentCount = 0,
                    likedBy = emptyList()
                )
                
                shareRepository.addShare(share)
                
                // Reload shares to get the updated list
                loadShares()
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = e.message ?: "Failed to add share"
                ) }
            }
        }
    }

    /**
     * Toggle like for a share.
     */
    fun toggleLike(shareId: Long) {
        viewModelScope.launch {
            try {
                val userId = userRepository.getUserId()
                val share = shareRepository.getShare(shareId)
                
                if (share != null) {
                    val isLiked = share.likedBy.contains(userId)
                    
                    if (isLiked) {
                        // Unlike
                        shareRepository.unlikeShare(shareId, userId)
                    } else {
                        // Like
                        shareRepository.likeShare(shareId, userId)
                    }
                    
                    // Update UI state immediately for better UX
                    _uiState.update { state ->
                        val updatedShares = state.shares.map { shareUiModel ->
                            if (shareUiModel.id == shareId) {
                                shareUiModel.copy(
                                    isLiked = !isLiked,
                                    likeCount = if (isLiked) shareUiModel.likeCount - 1 else shareUiModel.likeCount + 1
                                )
                            } else {
                                shareUiModel
                            }
                        }
                        
                        // Also update selected share if it's the one being liked
                        val updatedSelectedShare = state.selectedShare?.let { selectedShare ->
                            if (selectedShare.id == shareId) {
                                selectedShare.copy(
                                    isLiked = !isLiked,
                                    likeCount = if (isLiked) selectedShare.likeCount - 1 else selectedShare.likeCount + 1
                                )
                            } else {
                                selectedShare
                            }
                        }
                        
                        state.copy(
                            shares = updatedShares,
                            selectedShare = updatedSelectedShare
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = e.message ?: "Failed to update like"
                ) }
            }
        }
    }

    /**
     * Select a share to view its details and comments.
     */
    fun selectShare(shareId: Long) {
        viewModelScope.launch {
            try {
                val share = _uiState.value.shares.find { it.id == shareId }
                
                if (share != null) {
                    // Load comments for this share
                    val comments = shareRepository.getCommentsForShare(shareId)
                    
                    val commentUiModels = comments.map { comment ->
                        CommentUiModel(
                            id = comment.id,
                            shareId = comment.shareId,
                            content = comment.content,
                            date = comment.date
                        )
                    }
                    
                    _uiState.update { it.copy(
                        selectedShare = share,
                        comments = commentUiModels,
                        errorMessage = ""
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = e.message ?: "Failed to load share details"
                ) }
            }
        }
    }

    /**
     * Deselect the currently selected share.
     */
    fun deselectShare() {
        _uiState.update { it.copy(
            selectedShare = null,
            comments = emptyList()
        ) }
    }

    /**
     * Add a comment to a share.
     */
    fun addComment(shareId: Long, content: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isAddingComment = true) }
                
                val comment = Comment(
                    id = 0, // Will be auto-generated by Room
                    shareId = shareId,
                    content = content,
                    date = LocalDateTime.now()
                )
                
                shareRepository.addComment(comment)
                
                // Reload comments for this share
                val comments = shareRepository.getCommentsForShare(shareId)
                
                val commentUiModels = comments.map { updatedComment ->
                    CommentUiModel(
                        id = updatedComment.id,
                        shareId = updatedComment.shareId,
                        content = updatedComment.content,
                        date = updatedComment.date
                    )
                }
                
                // Update UI state
                _uiState.update { state ->
                    // Update comment count in shares list
                    val updatedShares = state.shares.map { shareUiModel ->
                        if (shareUiModel.id == shareId) {
                            shareUiModel.copy(commentCount = shareUiModel.commentCount + 1)
                        } else {
                            shareUiModel
                        }
                    }
                    
                    // Update selected share if it's the one being commented on
                    val updatedSelectedShare = state.selectedShare?.let { selectedShare ->
                        if (selectedShare.id == shareId) {
                            selectedShare.copy(commentCount = selectedShare.commentCount + 1)
                        } else {
                            selectedShare
                        }
                    }
                    
                    state.copy(
                        shares = updatedShares,
                        selectedShare = updatedSelectedShare,
                        comments = commentUiModels,
                        isAddingComment = false,
                        errorMessage = ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isAddingComment = false,
                    errorMessage = e.message ?: "Failed to add comment"
                ) }
            }
        }
    }

    /**
     * Show the add share dialog.
     */
    fun showAddShareDialog() {
        _uiState.update { it.copy(showAddShareDialog = true) }
    }

    /**
     * Hide the add share dialog.
     */
    fun hideAddShareDialog() {
        _uiState.update { it.copy(showAddShareDialog = false) }
    }

    /**
     * Clear the error message.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = "") }
    }
}

/**
 * UI state for the Shares screen.
 */
data class SharesUiState(
    val shares: List<ShareUiModel> = emptyList(),
    val selectedShare: ShareUiModel? = null,
    val comments: List<CommentUiModel> = emptyList(),
    val showAddShareDialog: Boolean = false,
    val isLoading: Boolean = false,
    val isAddingComment: Boolean = false,
    val errorMessage: String = ""
)