// app/src/main/java/com/example/saweather/SAWeatherApplication.kt
package com.example.saweather

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class SAWeatherApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize theme based on saved preference
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val theme = prefs.getString("app_theme", "system") ?: "system"

        when (theme) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}