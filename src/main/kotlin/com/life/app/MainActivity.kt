package com.life.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.life.app.ui.LifeApp
import com.life.app.ui.theme.LifeTheme
import com.life.app.util.Config
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import javax.inject.Inject

/**
 * Main entry point for the life. app.
 * Sets up the Compose UI and navigation.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var config: Config
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Supabase client
        val supabase = createSupabaseClient(
            supabaseUrl = config.supabaseUrl,
            supabaseKey = config.supabaseAnonKey
        ) {
            install(Postgrest)
            install(GoTrue)
            install(Realtime)
        }
        
        setContent {
            LifeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LifeApp()
                }
            }
        }
    }
}