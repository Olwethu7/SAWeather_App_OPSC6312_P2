package com.example.saweather

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
import java.util.Locale

class LanguageManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("LanguagePrefs", Context.MODE_PRIVATE)

    companion object {
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_ZULU = "zu"
        const val LANGUAGE_AFRIKAANS = "af"
    }

    fun setLanguage(languageCode: String) {
        sharedPreferences.edit { putString("app_language", languageCode) }
    }

    fun getCurrentLanguage(): String {
        return sharedPreferences.getString("app_language", LANGUAGE_ENGLISH) ?: LANGUAGE_ENGLISH
    }

    fun updateConfiguration(context: Context) {
        val languageCode = getCurrentLanguage()
        val locale = when (languageCode) {
            LANGUAGE_ZULU -> Locale.forLanguageTag("zu-ZA")
            LANGUAGE_AFRIKAANS -> Locale.forLanguageTag("af-ZA")
            else -> Locale.ENGLISH
        }

        Locale.setDefault(locale)

        val resources = context.resources
        val config = resources.configuration

        config.setLocale(locale)
        config.setLayoutDirection(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }
}