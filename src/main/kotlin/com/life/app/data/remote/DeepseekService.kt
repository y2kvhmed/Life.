package com.life.app.data.remote

import android.content.Context
import com.life.app.R
import com.life.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for interacting with Deepseek AI API.
 */
@Singleton
class DeepseekService @Inject constructor(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val apiKey = context.getString(R.string.deepseek_api_key)
    private val apiUrl = "https://api.deepseek.com/v1/chat/completions"

    /**
     * Generate a daily plan based on user data.
     */
    suspend fun generateDailyPlan(
        user: User,
        streak: Streak?,
        recentWorkouts: List<Workout>,
        recentRuns: List<Run>,
        recentMeals: List<Meal>,
        recentJournals: List<Journal>,
        mood: String?
    ): String = withContext(Dispatchers.IO) {
        val prompt = buildString {
            append("You are an AI coach for the 'life.' app. Create a personalized daily plan for this user based on their data:\n")
            append("Current streak: ${streak?.count ?: 0} days\n")
            append("Recent workouts: ${recentWorkouts.take(3).joinToString { it.name }}\n")
            append("Recent runs: ${recentRuns.take(3).joinToString { "${it.distance}km" }}\n")
            append("Recent meals: ${recentMeals.take(3).joinToString { it.name }}\n")
            append("Recent journal entries: ${recentJournals.take(3).map { it.mood }.joinToString()}\n")
            append("Current mood: $mood\n\n")
            append("Create a motivational, personalized daily plan that includes:\n")
            append("1. A workout suggestion\n")
            append("2. A meal suggestion\n")
            append("3. A mindfulness activity\n")
            append("4. A motivational message\n")
            if (user.religion != null) {
                append("5. A spiritual suggestion related to ${user.religion}\n")
            }
            append("\nKeep it concise, positive, and personalized to their data.")
        }

        callDeepseekApi(prompt)
    }

    /**
     * Generate workout suggestions based on user preferences.
     */
    suspend fun generateWorkoutSuggestions(
        difficulty: Int,
        duration: Int,
        preferredTypes: List<WorkoutType>,
        recentWorkouts: List<Workout>
    ): String = withContext(Dispatchers.IO) {
        val prompt = buildString {
            append("You are an AI fitness coach for the 'life.' app. Generate 3 workout suggestions based on these parameters:\n")
            append("Difficulty level (1-5): $difficulty\n")
            append("Available time: $duration minutes\n")
            append("Preferred workout types: ${preferredTypes.joinToString()}\n")
            append("Recent workouts: ${recentWorkouts.take(3).joinToString { it.name }}\n\n")
            append("For each workout suggestion, include:\n")
            append("1. A name\n")
            append("2. A brief description\n")
            append("3. Estimated duration\n")
            append("4. Difficulty rating\n")
            append("\nKeep suggestions varied from recent workouts and appropriate for the specified difficulty and duration.")
        }

        callDeepseekApi(prompt)
    }

    /**
     * Generate meal suggestions based on user preferences.
     */
    suspend fun generateMealSuggestions(
        mealType: MealType,
        dietaryPreferences: List<String>,
        recentMeals: List<Meal>
    ): String = withContext(Dispatchers.IO) {
        val prompt = buildString {
            append("You are an AI nutrition coach for the 'life.' app. Generate 3 meal suggestions for ${mealType.name.lowercase()} based on these parameters:\n")
            append("Dietary preferences: ${dietaryPreferences.joinToString()}\n")
            append("Recent meals: ${recentMeals.take(3).joinToString { it.name }}\n\n")
            append("For each meal suggestion, include:\n")
            append("1. A name\n")
            append("2. A brief description\n")
            append("3. Estimated calories\n")
            append("4. Main ingredients\n")
            append("\nKeep suggestions varied from recent meals and appropriate for the specified meal type and dietary preferences.")
        }

        callDeepseekApi(prompt)
    }

    /**
     * Generate journal prompts based on user mood.
     */
    suspend fun generateJournalPrompts(mood: String?): String = withContext(Dispatchers.IO) {
        val prompt = buildString {
            append("You are an AI journaling assistant for the 'life.' app. Generate 5 thoughtful journal prompts")
            if (mood != null) {
                append(" for someone who is feeling $mood")
            }
            append(".\n\n")
            append("The prompts should be introspective, encourage self-reflection, and help the user process their thoughts and emotions.")
            append(" Make the prompts varied and suitable for a daily journaling practice.")
        }

        callDeepseekApi(prompt)
    }

    /**
     * Generate a motivational message based on user data.
     */
    suspend fun generateMotivationalMessage(
        streak: Streak?,
        recentActivity: Boolean,
        mood: String?
    ): String = withContext(Dispatchers.IO) {
        val prompt = buildString {
            append("You are an AI motivational coach for the 'life.' app. Generate a short, powerful motivational message based on this user data:\n")
            append("Current streak: ${streak?.count ?: 0} days\n")
            append("Recent activity: ${if (recentActivity) "Active" else "Inactive"}\n")
            if (mood != null) {
                append("Current mood: $mood\n")
            }
            append("\nThe message should be positive, uplifting, and personalized to their current situation. Keep it concise (1-3 sentences) but impactful.")
        }

        callDeepseekApi(prompt)
    }

    /**
     * Generate a religious message based on user's faith.
     */
    suspend fun generateReligiousMessage(religion: ReligionType): String = withContext(Dispatchers.IO) {
        val prompt = buildString {
            append("You are an AI spiritual guide for the 'life.' app. Generate an inspirational message based on ${religion.name.lowercase()} teachings.\n")
            append("The message should be:\n")
            append("1. Respectful and authentic to the faith tradition\n")
            append("2. Uplifting and relevant to daily life\n")
            append("3. Brief but meaningful\n")
            when (religion) {
                ReligionType.ISLAM -> append("4. You may include a short verse from the Quran or a Hadith if appropriate\n")
                ReligionType.CHRISTIANITY -> append("4. You may include a short Bible verse if appropriate\n")
                ReligionType.JUDAISM -> append("4. You may include a short Torah passage or teaching if appropriate\n")
            }
        }

        callDeepseekApi(prompt)
    }

    /**
     * Process a user query and generate a response.
     */
    suspend fun processUserQuery(query: String, userContext: String): String = withContext(Dispatchers.IO) {
        val prompt = buildString {
            append("You are an AI coach for the 'life.' app. Respond to the following user query:\n\n")
            append("USER QUERY: $query\n\n")
            append("USER CONTEXT:\n$userContext\n\n")
            append("Provide a helpful, supportive response that addresses their query directly. ")
            append("If their query is about health, fitness, nutrition, mental wellbeing, or spiritual matters, ")
            append("provide evidence-based advice when possible. If their query is outside your expertise or ")
            append("requires medical attention, kindly suggest they consult a professional.")
        }

        callDeepseekApi(prompt)
    }

    /**
     * Call the Deepseek API with the given prompt.
     */
    private suspend fun callDeepseekApi(prompt: String): String {
        val requestBody = JSONObject().apply {
            put("model", "deepseek-chat")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are a helpful, supportive AI coach for the 'life.' app that helps users with their physical, mental, emotional, and spiritual wellbeing.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
            put("temperature", 0.7)
            put("max_tokens", 500)
        }

        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        return if (response.isSuccessful) {
            val jsonResponse = JSONObject(responseBody)
            val choices = jsonResponse.getJSONArray("choices")
            val firstChoice = choices.getJSONObject(0)
            val message = firstChoice.getJSONObject("message")
            message.getString("content")
        } else {
            "Sorry, I couldn't process your request at this time. Please try again later."
        }
    }
}