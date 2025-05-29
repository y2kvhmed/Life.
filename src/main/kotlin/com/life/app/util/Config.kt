package com.life.app.util

import android.content.Context
import com.life.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Configuration utility class for accessing environment variables and app settings.
 */
@Singleton
class Config @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Supabase URL from environment variables.
     */
    val supabaseUrl: String
        get() = context.getString(R.string.supabase_url)

    /**
     * Supabase anonymous key from environment variables.
     */
    val supabaseAnonKey: String
        get() = context.getString(R.string.supabase_anon_key)

    /**
     * Deepseek API key from environment variables.
     */
    val deepseekApiKey: String
        get() = context.getString(R.string.deepseek_api_key)

    /**
     * Check if the Deepseek API key is configured.
     */
    val isDeepseekConfigured: Boolean
        get() = deepseekApiKey.isNotBlank() && deepseekApiKey != "your-deepseek-api-key"

    /**
     * Check if Supabase is configured.
     */
    val isSupabaseConfigured: Boolean
        get() = supabaseUrl.isNotBlank() && 
               supabaseUrl != "https://your-supabase-url.supabase.co" &&
               supabaseAnonKey.isNotBlank() && 
               supabaseAnonKey != "your-supabase-anon-key"
}