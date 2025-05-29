package com.life.app.data.remote

import android.content.Context
import com.life.app.data.model.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for interacting with Supabase.
 */
@Singleton
class SupabaseService @Inject constructor(context: Context) {

    private val client: SupabaseClient = createSupabaseClient {
        supabaseUrl = context.getString(com.life.app.R.string.supabase_url)
        supabaseKey = context.getString(com.life.app.R.string.supabase_anon_key)
        install(Auth)
        install(Postgrest)
        install(Realtime)
    }

    // Auth methods
    suspend fun signUp(email: String, password: String) {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signIn(email: String, password: String) {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        client.auth.signOut()
    }

    fun getCurrentUser() = client.auth.currentUser

    // User methods
    suspend fun createUser(user: User) {
        client.postgrest["users"].insert(user)
    }

    suspend fun updateUser(user: User) {
        client.postgrest["users"].update(user) {
            filter {
                eq("id", user.id)
            }
        }
    }

    suspend fun getUserById(id: String): User? {
        return client.postgrest["users"].select {
            filter {
                eq("id", id)
            }
        }.decodeSingle<User>()
    }

    // Workout methods
    suspend fun createWorkout(workout: Workout) {
        client.postgrest["workouts"].insert(workout)
    }

    suspend fun updateWorkout(workout: Workout) {
        client.postgrest["workouts"].update(workout) {
            filter {
                eq("id", workout.id)
            }
        }
    }

    suspend fun deleteWorkout(id: String) {
        client.postgrest["workouts"].delete {
            filter {
                eq("id", id)
            }
        }
    }

    suspend fun getWorkoutsByUserId(userId: String): List<Workout> {
        return client.postgrest["workouts"].select {
            filter {
                eq("userId", userId)
            }
            order("createdAt", Order.DESCENDING)
        }.decodeList<Workout>()
    }

    // Run methods
    suspend fun createRun(run: Run) {
        client.postgrest["runs"].insert(run)
    }

    suspend fun updateRun(run: Run) {
        client.postgrest["runs"].update(run) {
            filter {
                eq("id", run.id)
            }
        }
    }

    suspend fun deleteRun(id: String) {
        client.postgrest["runs"].delete {
            filter {
                eq("id", id)
            }
        }
    }

    suspend fun getRunsByUserId(userId: String): List<Run> {
        return client.postgrest["runs"].select {
            filter {
                eq("userId", userId)
            }
            order("createdAt", Order.DESCENDING)
        }.decodeList<Run>()
    }

    // Meal methods
    suspend fun createMeal(meal: Meal) {
        client.postgrest["meals"].insert(meal)
    }

    suspend fun updateMeal(meal: Meal) {
        client.postgrest["meals"].update(meal) {
            filter {
                eq("id", meal.id)
            }
        }
    }

    suspend fun deleteMeal(id: String) {
        client.postgrest["meals"].delete {
            filter {
                eq("id", id)
            }
        }
    }

    suspend fun getMealsByUserId(userId: String): List<Meal> {
        return client.postgrest["meals"].select {
            filter {
                eq("userId", userId)
            }
            order("createdAt", Order.DESCENDING)
        }.decodeList<Meal>()
    }

    // Water intake methods
    suspend fun createWaterIntake(waterIntake: WaterIntake) {
        client.postgrest["water_intake"].insert(waterIntake)
    }

    suspend fun getWaterIntakeByUserId(userId: String): List<WaterIntake> {
        return client.postgrest["water_intake"].select {
            filter {
                eq("userId", userId)
            }
            order("createdAt", Order.DESCENDING)
        }.decodeList<WaterIntake>()
    }

    // Journal methods
    suspend fun createJournal(journal: Journal) {
        client.postgrest["journals"].insert(journal)
    }

    suspend fun updateJournal(journal: Journal) {
        client.postgrest["journals"].update(journal) {
            filter {
                eq("id", journal.id)
            }
        }
    }

    suspend fun deleteJournal(id: String) {
        client.postgrest["journals"].delete {
            filter {
                eq("id", id)
            }
        }
    }

    suspend fun getJournalsByUserId(userId: String): List<Journal> {
        return client.postgrest["journals"].select {
            filter {
                eq("userId", userId)
            }
            order("createdAt", Order.DESCENDING)
        }.decodeList<Journal>()
    }

    // Motivation methods
    suspend fun createMotivation(motivation: Motivation) {
        client.postgrest["motivations"].insert(motivation)
    }

    suspend fun updateMotivation(motivation: Motivation) {
        client.postgrest["motivations"].update(motivation) {
            filter {
                eq("id", motivation.id)
            }
        }
    }

    suspend fun getLatestMotivationByUserId(userId: String): Motivation? {
        return client.postgrest["motivations"].select {
            filter {
                eq("userId", userId)
            }
            order("createdAt", Order.DESCENDING)
            limit(1)
        }.decodeSingleOrNull<Motivation>()
    }

    // Prayer methods
    suspend fun createPrayer(prayer: Prayer) {
        client.postgrest["prayers"].insert(prayer)
    }

    suspend fun updatePrayer(prayer: Prayer) {
        client.postgrest["prayers"].update(prayer) {
            filter {
                eq("id", prayer.id)
            }
        }
    }

    suspend fun getPrayersByUserId(userId: String): List<Prayer> {
        return client.postgrest["prayers"].select {
            filter {
                eq("userId", userId)
            }
            order("createdAt", Order.DESCENDING)
        }.decodeList<Prayer>()
    }

    // Streak methods
    suspend fun createStreak(streak: Streak) {
        client.postgrest["streaks"].insert(streak)
    }

    suspend fun updateStreak(streak: Streak) {
        client.postgrest["streaks"].update(streak) {
            filter {
                eq("id", streak.id)
            }
        }
    }

    suspend fun getStreakByUserId(userId: String): Streak? {
        return client.postgrest["streaks"].select {
            filter {
                eq("userId", userId)
            }
        }.decodeSingleOrNull<Streak>()
    }

    // Note methods
    suspend fun createNote(note: Note) {
        client.postgrest["notes"].insert(note)
    }

    suspend fun updateNote(note: Note) {
        client.postgrest["notes"].update(note) {
            filter {
                eq("id", note.id)
            }
        }
    }

    suspend fun deleteNote(id: String) {
        client.postgrest["notes"].delete {
            filter {
                eq("id", id)
            }
        }
    }

    suspend fun getNotesByUserId(userId: String): List<Note> {
        return client.postgrest["notes"].select {
            filter {
                eq("userId", userId)
            }
            order("updatedAt", Order.DESCENDING)
        }.decodeList<Note>()
    }

    // Plan methods
    suspend fun createPlan(plan: Plan) {
        client.postgrest["plans"].insert(plan)
    }

    suspend fun updatePlan(plan: Plan) {
        client.postgrest["plans"].update(plan) {
            filter {
                eq("id", plan.id)
            }
        }
    }

    suspend fun deletePlan(id: String) {
        client.postgrest["plans"].delete {
            filter {
                eq("id", id)
            }
        }
    }

    suspend fun getPlansByUserId(userId: String): List<Plan> {
        return client.postgrest["plans"].select {
            filter {
                eq("userId", userId)
            }
            order("dueDate", Order.ASCENDING)
        }.decodeList<Plan>()
    }

    // Share methods
    suspend fun createShare(share: Share) {
        client.postgrest["shares"].insert(share)
    }

    suspend fun updateShare(share: Share) {
        client.postgrest["shares"].update(share) {
            filter {
                eq("id", share.id)
            }
        }
    }

    suspend fun deleteShare(id: String) {
        client.postgrest["shares"].delete {
            filter {
                eq("id", id)
            }
        }
    }

    suspend fun getAllShares(): List<Share> {
        return client.postgrest["shares"].select {
            order("createdAt", Order.DESCENDING)
        }.decodeList<Share>()
    }

    suspend fun getAllSharesByLikes(): List<Share> {
        return client.postgrest["shares"].select {
            order("likesCount", Order.DESCENDING)
        }.decodeList<Share>()
    }

    suspend fun getAllSharesByComments(): List<Share> {
        return client.postgrest["shares"].select {
            order("commentsCount", Order.DESCENDING)
        }.decodeList<Share>()
    }

    // Comment methods
    suspend fun createComment(comment: Comment) {
        client.postgrest["comments"].insert(comment)
    }

    suspend fun updateComment(comment: Comment) {
        client.postgrest["comments"].update(comment) {
            filter {
                eq("id", comment.id)
            }
        }
    }

    suspend fun deleteComment(id: String) {
        client.postgrest["comments"].delete {
            filter {
                eq("id", id)
            }
        }
    }

    suspend fun getCommentsByShareId(shareId: String): List<Comment> {
        return client.postgrest["comments"].select {
            filter {
                eq("shareId", shareId)
            }
            order("createdAt", Order.ASCENDING)
        }.decodeList<Comment>()
    }

    // Helper function to generate a new UUID
    fun generateId(): String = UUID.randomUUID().toString()
}