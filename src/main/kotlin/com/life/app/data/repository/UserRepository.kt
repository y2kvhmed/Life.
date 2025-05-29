package com.life.app.data.repository

import com.life.app.data.local.UserDao
import com.life.app.data.model.ReligionType
import com.life.app.data.model.ThemeType
import com.life.app.data.model.User
import com.life.app.data.remote.SupabaseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for user-related data operations.
 */
@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val supabaseService: SupabaseService
) {

    /**
     * Get the current user from the local database.
     */
    fun getCurrentUser(userId: String): Flow<User?> {
        return userDao.getUserById(userId)
    }

    /**
     * Create a new user in both local and remote databases.
     */
    suspend fun createUser(email: String, userId: String) {
        val user = User(
            id = userId,
            email = email
        )
        userDao.insertUser(user)
        supabaseService.createUser(user)
    }

    /**
     * Update the user's religion preference.
     */
    suspend fun updateReligion(userId: String, religion: ReligionType?) {
        val user = userDao.getUserById(userId).first()
        if (user != null) {
            val updatedUser = user.copy(religion = religion)
            userDao.updateUser(updatedUser)
            supabaseService.updateUser(updatedUser)
        }
    }

    /**
     * Update the user's theme preference.
     */
    suspend fun updateTheme(userId: String, theme: ThemeType) {
        val user = userDao.getUserById(userId).first()
        if (user != null) {
            val updatedUser = user.copy(theme = theme)
            userDao.updateUser(updatedUser)
            supabaseService.updateUser(updatedUser)
        }
    }

    /**
     * Sync the user data from the remote database to the local database.
     */
    suspend fun syncUser(userId: String) {
        val remoteUser = supabaseService.getUserById(userId)
        if (remoteUser != null) {
            userDao.insertUser(remoteUser)
        }
    }
}