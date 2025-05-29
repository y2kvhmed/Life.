package com.life.app.data.repository

import com.life.app.data.local.PlanDao
import com.life.app.data.model.Plan
import com.life.app.data.remote.SupabaseService
import com.life.app.data.remote.DeepseekService
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing plans and goals.
 */
@Singleton
class PlanRepository @Inject constructor(
    private val planDao: PlanDao,
    private val supabaseService: SupabaseService,
    private val deepseekService: DeepseekService
) {

    /**
     * Get all plans for a user.
     */
    fun getAllPlans(userId: String): Flow<List<Plan>> {
        return planDao.getAllPlansForUser(userId)
    }

    /**
     * Get plans with specific tags.
     */
    fun getPlansByTags(userId: String, tags: List<String>): Flow<List<Plan>> {
        return planDao.getPlansByTags(userId, tags)
    }

    /**
     * Get a specific plan by ID.
     */
    suspend fun getPlanById(planId: String): Plan? {
        return planDao.getPlanById(planId)
    }

    /**
     * Create a new plan.
     */
    suspend fun createPlan(
        userId: String,
        title: String,
        description: String,
        steps: List<String>? = null,
        tags: List<String>? = null,
        deadline: LocalDateTime? = null,
        isCompleted: Boolean = false
    ): Plan {
        val plan = Plan(
            id = generatePlanId(),
            userId = userId,
            title = title,
            description = description,
            steps = steps,
            tags = tags,
            deadline = deadline,
            isCompleted = isCompleted,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // Save locally
        planDao.insertPlan(plan)

        // Save to Supabase
        try {
            supabaseService.createPlan(plan)
        } catch (e: Exception) {
            // Handle error, but don't block the local save
        }

        return plan
    }

    /**
     * Update an existing plan.
     */
    suspend fun updatePlan(plan: Plan): Plan {
        val updatedPlan = plan.copy(updatedAt = LocalDateTime.now())

        // Update locally
        planDao.updatePlan(updatedPlan)

        // Update in Supabase
        try {
            supabaseService.updatePlan(updatedPlan)
        } catch (e: Exception) {
            // Handle error, but don't block the local update
        }

        return updatedPlan
    }

    /**
     * Mark a plan as completed.
     */
    suspend fun completePlan(planId: String): Plan? {
        val plan = getPlanById(planId) ?: return null
        val completedPlan = plan.copy(isCompleted = true, updatedAt = LocalDateTime.now())
        return updatePlan(completedPlan)
    }

    /**
     * Delete a plan.
     */
    suspend fun deletePlan(plan: Plan) {
        // Delete locally
        planDao.deletePlan(plan)

        // Delete from Supabase
        try {
            supabaseService.deletePlan(plan.id)
        } catch (e: Exception) {
            // Handle error, but don't block the local delete
        }
    }

    /**
     * Search plans by title or description.
     */
    fun searchPlans(userId: String, query: String): Flow<List<Plan>> {
        return planDao.searchPlans(userId, query)
    }

    /**
     * Generate a plan using Deepseek AI.
     */
    suspend fun generatePlan(
        userId: String,
        goal: String,
        timeframe: String? = null,
        tags: List<String>? = null
    ): Plan? {
        try {
            // Generate plan using Deepseek AI
            val generatedPlan = deepseekService.generateDailyPlan(
                goal = goal,
                timeframe = timeframe,
                tags = tags
            )
            
            // Extract title, description, and steps from the generated plan
            val title = generatedPlan.title ?: "AI Generated Plan"
            val description = generatedPlan.description ?: goal
            val steps = generatedPlan.steps
            
            // Create and save the plan
            return createPlan(
                userId = userId,
                title = title,
                description = description,
                steps = steps,
                tags = tags,
                deadline = null,
                isCompleted = false
            )
        } catch (e: Exception) {
            // Handle error
            return null
        }
    }

    /**
     * Sync plans with Supabase.
     */
    suspend fun syncWithRemote(userId: String) {
        try {
            // Get plans from Supabase
            val remotePlans = supabaseService.getPlansForUser(userId)
            
            // Get local plans
            val localPlans = planDao.getAllPlansForUserAsList(userId)
            
            // Find plans that are in remote but not in local
            val plansToAdd = remotePlans.filter { remotePlan ->
                localPlans.none { it.id == remotePlan.id }
            }
            
            // Find plans that are in local but not in remote
            val plansToUpload = localPlans.filter { localPlan ->
                remotePlans.none { it.id == localPlan.id }
            }
            
            // Find plans that are in both but might have different data
            val plansToUpdate = localPlans.filter { localPlan ->
                remotePlans.any { it.id == localPlan.id && it.updatedAt != localPlan.updatedAt }
            }
            
            // Add remote plans to local
            if (plansToAdd.isNotEmpty()) {
                planDao.insertPlans(plansToAdd)
            }
            
            // Upload local plans to remote
            plansToUpload.forEach { plan ->
                supabaseService.createPlan(plan)
            }
            
            // Update plans that are different
            plansToUpdate.forEach { localPlan ->
                val remotePlan = remotePlans.first { it.id == localPlan.id }
                
                // Use the most recent version
                if (localPlan.updatedAt.isAfter(remotePlan.updatedAt)) {
                    supabaseService.updatePlan(localPlan)
                } else {
                    planDao.updatePlan(remotePlan)
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    /**
     * Generate a unique ID for a new plan.
     */
    private fun generatePlanId(): String {
        return "plan_${System.currentTimeMillis()}_${(0..1000).random()}"
    }
}