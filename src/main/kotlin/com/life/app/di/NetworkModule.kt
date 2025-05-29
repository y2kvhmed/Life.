package com.life.app.di

import android.content.Context
import com.life.app.data.remote.DeepseekService
import com.life.app.data.remote.SupabaseService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing network dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideSupabaseClient(@ApplicationContext context: Context): SupabaseClient {
        // In a real app, these would be stored in BuildConfig or a secure storage
        val supabaseUrl = "https://your-supabase-url.supabase.co"
        val supabaseKey = "your-supabase-anon-key"

        return createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            install(Postgrest)
            install(GoTrue)
            install(Realtime)
        }
    }

    @Provides
    @Singleton
    fun provideSupabaseService(supabaseClient: SupabaseClient): SupabaseService {
        return SupabaseService(supabaseClient)
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.deepseek.ai/") // Replace with actual Deepseek API URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideDeepseekService(): DeepseekService {
        // In a real app, the API key would be stored securely
        val apiKey = "your-deepseek-api-key"
        return DeepseekService(apiKey)
    }
}