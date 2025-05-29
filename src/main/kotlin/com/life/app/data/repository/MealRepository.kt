package com.life.app.data.repository

import com.life.app.data.local.MealDao
import com.life.app.data.model.Meal
import com.life.app.data.model.MealType
import com.life.app.data.remote.SupabaseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing meal data.
 */
@Singleton
class MealRepository @Inject constructor(
    private val mealDao: MealDao,
    private val supabaseService: SupabaseService,
    private val streakRepository: StreakRepository
) {

    /**
     * Get all meals for a user.
     */
    fun getAllMeals(userId: String): Flow<List<Meal>> {
        return mealDao.getAllMealsForUser(userId)
    }

    /**
     * Get meals for a specific date range.
     */
    fun getMealsForDateRange(
        userId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Meal>> {
        return mealDao.getMealsForDateRange(userId, startDate, endDate)
    }

    /**
     * Get meals of a specific type for a date range.
     */
    fun getMealsOfTypeForDateRange(
        userId: String,
        mealType: MealType,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Meal>> {
        return mealDao.getMealsOfTypeForDateRange(userId, mealType, startDate, endDate)
    }

    /**
     * Get a specific meal by ID.
     */
    suspend fun getMealById(mealId: String): Meal? {
        return mealDao.getMealById(mealId)
    }

    /**
     * Create a new meal.
     */
    suspend fun createMeal(
        userId: String,
        name: String,
        type: MealType,
        ingredients: List<String>? = null,
        calories: Int? = null,
        protein: Int? = null,
        carbs: Int? = null,
        fat: Int? = null,
        notes: String? = null
    ): Meal {
        val meal = Meal(
            id = generateMealId(),
            userId = userId,
            name = name,
            type = type,
            ingredients = ingredients,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            notes = notes,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // Save locally
        mealDao.insertMeal(meal)

        // Save to Supabase
        try {
            supabaseService.createMeal(meal)
        } catch (e: Exception) {
            // Handle error, but don't block the local save
        }

        // Update streak
        streakRepository.updateStreakForActivity(userId)

        return meal
    }

    /**
     * Update an existing meal.
     */
    suspend fun updateMeal(meal: Meal): Meal {
        val updatedMeal = meal.copy(updatedAt = LocalDateTime.now())

        // Update locally
        mealDao.updateMeal(updatedMeal)

        // Update in Supabase
        try {
            supabaseService.updateMeal(updatedMeal)
        } catch (e: Exception) {
            // Handle error, but don't block the local update
        }

        return updatedMeal
    }

    /**
     * Delete a meal.
     */
    suspend fun deleteMeal(meal: Meal) {
        // Delete locally
        mealDao.deleteMeal(meal)

        // Delete from Supabase
        try {
            supabaseService.deleteMeal(meal.id)
        } catch (e: Exception) {
            // Handle error, but don't block the local delete
        }

        // Check if we need to update streak
        val userId = meal.userId
        val today = LocalDateTime.now()
        val startOfDay = today.withHour(0).withMinute(0).withSecond(0)
        val endOfDay = today.withHour(23).withMinute(59).withSecond(59)

        // If the deleted meal was from today, check if there are any other activities today
        if (meal.createdAt.isAfter(startOfDay) && meal.createdAt.isBefore(endOfDay)) {
            val hasOtherMealsToday = getMealsForDateRange(userId, startOfDay, endOfDay).first().isNotEmpty()
            
            if (!hasOtherMealsToday) {
                // Check other activity types
                val hasWorkouts = false // Replace with actual check from WorkoutRepository
                val hasRuns = false // Replace with actual check from RunRepository
                val hasJournals = false // Replace with actual check from JournalRepository
                val hasPrayers = false // Replace with actual check from PrayerRepository

                if (!hasWorkouts && !hasRuns && !hasJournals && !hasPrayers) {
                    // No activities today, reset streak
                    streakRepository.resetStreak(userId)
                }
            }
        }
    }

    /**
     * Get meal suggestions based on user preferences.
     */
    suspend fun getMealSuggestions(
        userId: String,
        mealType: MealType,
        dietaryPreferences: List<String>? = null
    ): List<Meal> {
        // This would typically call the DeepseekService to get AI-generated meal suggestions
        // For now, return an empty list
        return emptyList()
    }

    /**
     * Sync meals with Supabase.
     */
    suspend fun syncWithRemote(userId: String) {
        try {
            // Get meals from Supabase
            val remoteMeals = supabaseService.getMealsForUser(userId)
            
            // Get local meals
            val localMeals = mealDao.getAllMealsForUserAsList(userId)
            
            // Find meals that are in remote but not in local
            val mealsToAdd = remoteMeals.filter { remoteMeal ->
                localMeals.none { it.id == remoteMeal.id }
            }
            
            // Find meals that are in local but not in remote
            val mealsToUpload = localMeals.filter { localMeal ->
                remoteMeals.none { it.id == localMeal.id }
            }
            
            // Find meals that are in both but might have different data
            val mealsToUpdate = localMeals.filter { localMeal ->
                remoteMeals.any { it.id == localMeal.id && it.updatedAt != localMeal.updatedAt }
            }
            
            // Add remote meals to local
            if (mealsToAdd.isNotEmpty()) {
                mealDao.insertMeals(mealsToAdd)
            }
            
            // Upload local meals to remote
            mealsToUpload.forEach { meal ->
                supabaseService.createMeal(meal)
            }
            
            // Update meals that are different
            mealsToUpdate.forEach { localMeal ->
                val remoteMeal = remoteMeals.first { it.id == localMeal.id }
                
                // Use the most recent version
                if (localMeal.updatedAt.isAfter(remoteMeal.updatedAt)) {
                    supabaseService.updateMeal(localMeal)
                } else {
                    mealDao.updateMeal(remoteMeal)
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    /**
     * Generate a unique ID for a new meal.
     */
    private fun generateMealId(): String {
        return "meal_${System.currentTimeMillis()}_${(0..1000).random()}"
    }
}