package com.life.app.data.repository

import com.life.app.data.local.NoteDao
import com.life.app.data.model.Note
import com.life.app.data.remote.SupabaseService
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing notes.
 */
@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val supabaseService: SupabaseService
) {

    /**
     * Get all notes for a user.
     */
    fun getAllNotes(userId: String): Flow<List<Note>> {
        return noteDao.getAllNotesForUser(userId)
    }

    /**
     * Get notes with specific tags.
     */
    fun getNotesByTags(userId: String, tags: List<String>): Flow<List<Note>> {
        return noteDao.getNotesByTags(userId, tags)
    }

    /**
     * Get a specific note by ID.
     */
    suspend fun getNoteById(noteId: String): Note? {
        return noteDao.getNoteById(noteId)
    }

    /**
     * Create a new note.
     */
    suspend fun createNote(
        userId: String,
        title: String,
        content: String,
        tags: List<String>? = null,
        isEncrypted: Boolean = false
    ): Note {
        val note = Note(
            id = generateNoteId(),
            userId = userId,
            title = title,
            content = content,
            tags = tags,
            isEncrypted = isEncrypted,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // Save locally
        noteDao.insertNote(note)

        // Save to Supabase
        try {
            supabaseService.createNote(note)
        } catch (e: Exception) {
            // Handle error, but don't block the local save
        }

        return note
    }

    /**
     * Update an existing note.
     */
    suspend fun updateNote(note: Note): Note {
        val updatedNote = note.copy(updatedAt = LocalDateTime.now())

        // Update locally
        noteDao.updateNote(updatedNote)

        // Update in Supabase
        try {
            supabaseService.updateNote(updatedNote)
        } catch (e: Exception) {
            // Handle error, but don't block the local update
        }

        return updatedNote
    }

    /**
     * Delete a note.
     */
    suspend fun deleteNote(note: Note) {
        // Delete locally
        noteDao.deleteNote(note)

        // Delete from Supabase
        try {
            supabaseService.deleteNote(note.id)
        } catch (e: Exception) {
            // Handle error, but don't block the local delete
        }
    }

    /**
     * Search notes by content.
     */
    fun searchNotes(userId: String, query: String): Flow<List<Note>> {
        return noteDao.searchNotes(userId, query)
    }

    /**
     * Sync notes with Supabase.
     */
    suspend fun syncWithRemote(userId: String) {
        try {
            // Get notes from Supabase
            val remoteNotes = supabaseService.getNotesForUser(userId)
            
            // Get local notes
            val localNotes = noteDao.getAllNotesForUserAsList(userId)
            
            // Find notes that are in remote but not in local
            val notesToAdd = remoteNotes.filter { remoteNote ->
                localNotes.none { it.id == remoteNote.id }
            }
            
            // Find notes that are in local but not in remote
            val notesToUpload = localNotes.filter { localNote ->
                remoteNotes.none { it.id == localNote.id }
            }
            
            // Find notes that are in both but might have different data
            val notesToUpdate = localNotes.filter { localNote ->
                remoteNotes.any { it.id == localNote.id && it.updatedAt != localNote.updatedAt }
            }
            
            // Add remote notes to local
            if (notesToAdd.isNotEmpty()) {
                noteDao.insertNotes(notesToAdd)
            }
            
            // Upload local notes to remote
            notesToUpload.forEach { note ->
                supabaseService.createNote(note)
            }
            
            // Update notes that are different
            notesToUpdate.forEach { localNote ->
                val remoteNote = remoteNotes.first { it.id == localNote.id }
                
                // Use the most recent version
                if (localNote.updatedAt.isAfter(remoteNote.updatedAt)) {
                    supabaseService.updateNote(localNote)
                } else {
                    noteDao.updateNote(remoteNote)
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    /**
     * Generate a unique ID for a new note.
     */
    private fun generateNoteId(): String {
        return "note_${System.currentTimeMillis()}_${(0..1000).random()}"
    }
}