package com.example.saweather

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class Preferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("SAWeatherPrefs", Context.MODE_PRIVATE)

    fun saveUser(email: String, name: String, location: String = "Johannesburg") {
        sharedPreferences.edit().apply {
            putString("user_email", email)
            putString("user_name", name)
            putString("user_location", location)
            apply()
        }
    }

    fun getCurrentUserName(): String? {
        return sharedPreferences.getString("user_name", "")
    }

    fun getCurrentUserLocation(): String {
        return sharedPreferences.getString("user_location", "Johannesburg") ?: "Johannesburg"
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getString("user_email", null) != null
    }

    fun logout() {
        sharedPreferences.edit { clear() }
    }

    fun updateLocation(location: String) {
        sharedPreferences.edit { putString("user_location", location) }
    }
}