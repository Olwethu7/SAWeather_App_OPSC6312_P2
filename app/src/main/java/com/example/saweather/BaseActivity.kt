package com.example.saweather

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    protected lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        languageManager = LanguageManager(this)

        // Apply language settings - use updateConfiguration instead of updateResources
        languageManager.updateConfiguration(this)
    }

    override fun onResume() {
        super.onResume()
        // Ensure language is applied when returning to activity
        languageManager.updateConfiguration(this)
    }
}