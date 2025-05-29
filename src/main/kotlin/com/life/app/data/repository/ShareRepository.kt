package com.life.app.data.repository

import com.life.app.data.local.ShareDao
import com.life.app.data.local.CommentDao
import com.life.app.data.model.Share
import com.life.app.data.model.Comment
import com.life.app.data.remote.SupabaseService
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing social shares and comments.
 */
@Singleton
class ShareRepository @Inject constructor(
    private val shareDao: ShareDao,
    private val commentDao: CommentDao,
    private val supabaseService: SupabaseService
) {

    /**
     * Get all shares (community feed).
     */
    fun getAllShares(): Flow<List<Share>> {
        return shareDao.getAllShares()
    }

    /**
     * Get shares by a specific user.
     */
    fun getSharesByUser(userId: String): Flow<List<Share>> {
        return shareDao.getSharesByUser(userId)
    }

    /**
     * Get a specific share by ID.
     */
    suspend fun getShareById(shareId: String): Share? {
        return shareDao.getShareById(shareId)
    }

    /**
     * Create a new share.
     */
    suspend fun createShare(
        userId: String,
        content: String,
        tags: List<String>? = null
    ): Share {
        val share = Share(
            id = generateShareId(),
            userId = userId,
            content = content,
            tags = tags,
            likes = 0,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // Save locally
        shareDao.insertShare(share)

        // Save to Supabase
        try {
            supabaseService.createShare(share)
        } catch (e: Exception) {
            // Handle error, but don't block the local save
        }

        return share
    }

    /**
     * Update an existing share.
     */
    suspend fun updateShare(share: Share): Share {
        val updatedShare = share.copy(updatedAt = LocalDateTime.now())

        // Update locally
        shareDao.updateShare(updatedShare)

        // Update in Supabase
        try {
            supabaseService.updateShare(updatedShare)
        } catch (e: Exception) {
            // Handle error, but don't block the local update
        }

        return updatedShare
    }

    /**
     * Delete a share.
     */
    suspend fun deleteShare(share: Share) {
        // Delete locally
        shareDao.deleteShare(share)

        // Delete from Supabase
        try {
            supabaseService.deleteShare(share.id)
        } catch (e: Exception) {
            // Handle error, but don't block the local delete
        }
    }

    /**
     * Like a share.
     */
    suspend fun likeShare(shareId: String) {
        val share = getShareById(shareId) ?: return
        val updatedShare = share.copy(likes = share.likes + 1)
        updateShare(updatedShare)
    }

    /**
     * Get all comments for a share.
     */
    fun getCommentsForShare(shareId: String): Flow<List<Comment>> {
        return commentDao.getCommentsForShare(shareId)
    }

    /**
     * Create a new comment on a share.
     */
    suspend fun createComment(
        userId: String,
        shareId: String,
        content: String
    ): Comment {
        val comment = Comment(
            id = generateCommentId(),
            userId = userId,
            shareId = shareId,
            content = content,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // Save locally
        commentDao.insertComment(comment)

        // Save to Supabase
        try {
            supabaseService.createComment(comment)
        } catch (e: Exception) {
            // Handle error, but don't block the local save
        }

        return comment
    }

    /**
     * Update an existing comment.
     */
    suspend fun updateComment(comment: Comment): Comment {
        val updatedComment = comment.copy(updatedAt = LocalDateTime.now())

        // Update locally
        commentDao.updateComment(updatedComment)

        // Update in Supabase
        try {
            supabaseService.updateComment(updatedComment)
        } catch (e: Exception) {
            // Handle error, but don't block the local update
        }

        return updatedComment
    }

    /**
     * Delete a comment.
     */
    suspend fun deleteComment(comment: Comment) {
        // Delete locally
        commentDao.deleteComment(comment)

        // Delete from Supabase
        try {
            supabaseService.deleteComment(comment.id)
        } catch (e: Exception) {
            // Handle error, but don't block the local delete
        }
    }

    /**
     * Sync shares with Supabase.
     */
    suspend fun syncSharesWithRemote() {
        try {
            // Get shares from Supabase
            val remoteShares = supabaseService.getAllShares()
            
            // Get local shares
            val localShares = shareDao.getAllSharesAsList()
            
            // Find shares that are in remote but not in local
            val sharesToAdd = remoteShares.filter { remoteShare ->
                localShares.none { it.id == remoteShare.id }
            }
            
            // Find shares that are in local but not in remote
            val sharesToUpload = localShares.filter { localShare ->
                remoteShares.none { it.id == localShare.id }
            }
            
            // Find shares that are in both but might have different data
            val sharesToUpdate = localShares.filter { localShare ->
                remoteShares.any { it.id == localShare.id && it.updatedAt != localShare.updatedAt }
            }
            
            // Add remote shares to local
            if (sharesToAdd.isNotEmpty()) {
                shareDao.insertShares(sharesToAdd)
            }
            
            // Upload local shares to remote
            sharesToUpload.forEach { share ->
                supabaseService.createShare(share)
            }
            
            // Update shares that are different
            sharesToUpdate.forEach { localShare ->
                val remoteShare = remoteShares.first { it.id == localShare.id }
                
                // Use the most recent version
                if (localShare.updatedAt.isAfter(remoteShare.updatedAt)) {
                    supabaseService.updateShare(localShare)
                } else {
                    shareDao.updateShare(remoteShare)
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    /**
     * Sync comments with Supabase.
     */
    suspend fun syncCommentsWithRemote() {
        try {
            // Get comments from Supabase
            val remoteComments = supabaseService.getAllComments()
            
            // Get local comments
            val localComments = commentDao.getAllCommentsAsList()
            
            // Find comments that are in remote but not in local
            val commentsToAdd = remoteComments.filter { remoteComment ->
                localComments.none { it.id == remoteComment.id }
            }
            
            // Find comments that are in local but not in remote
            val commentsToUpload = localComments.filter { localComment ->
                remoteComments.none { it.id == localComment.id }
            }
            
            // Find comments that are in both but might have different data
            val commentsToUpdate = localComments.filter { localComment ->
                remoteComments.any { it.id == localComment.id && it.updatedAt != localComment.updatedAt }
            }
            
            // Add remote comments to local
            if (commentsToAdd.isNotEmpty()) {
                commentDao.insertComments(commentsToAdd)
            }
            
            // Upload local comments to remote
            commentsToUpload.forEach { comment ->
                supabaseService.createComment(comment)
            }
            
            // Update comments that are different
            commentsToUpdate.forEach { localComment ->
                val remoteComment = remoteComments.first { it.id == localComment.id }
                
                // Use the most recent version
                if (localComment.updatedAt.isAfter(remoteComment.updatedAt)) {
                    supabaseService.updateComment(localComment)
                } else {
                    commentDao.updateComment(remoteComment)
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    /**
     * Generate a unique ID for a new share.
     */
    private fun generateShareId(): String {
        return "share_${System.currentTimeMillis()}_${(0..1000).random()}"
    }

    /**
     * Generate a unique ID for a new comment.
     */
    private fun generateCommentId(): String {
        return "comment_${System.currentTimeMillis()}_${(0..1000).random()}"
    }
}