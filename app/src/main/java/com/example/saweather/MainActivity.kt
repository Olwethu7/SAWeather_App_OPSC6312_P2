package com.example.saweather

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import com.example.saweather.util.NotificationHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {

    private lateinit var welcomeTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var temperatureTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var humidityTextView: TextView
    private lateinit var windTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var preferences: Preferences
    private lateinit var viewModel: WeatherViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var languageManager: LanguageManager

    // Debug tag
    private val TAG = "MainActivity_Debug"

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply language BEFORE super.onCreate
        languageManager = LanguageManager(this)
        languageManager.updateConfiguration(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase and preferences FIRST
        auth = Firebase.auth
        preferences = Preferences(this) // Initialize preferences here

        // Log app startup AFTER preferences is initialized
        Log.d(TAG, "=== SAWeather App Starting ===")
        Log.d(TAG, "Android Version: ${Build.VERSION.SDK_INT}")
        Log.d(TAG, "User logged in: ${preferences.isLoggedIn()}")

        viewModel = ViewModelProvider(this)[WeatherViewModel::class.java]

        // Check if user is logged in
        if (!preferences.isLoggedIn()) {
            Log.d(TAG, "User not logged in, redirecting to LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        initViews()
        setupObservers()
        setupForecastObservers()

        // Debug notification system
        debugNotificationSystem()

        // Send welcome notification on app launch
        sendWelcomeNotification()

        // Check and request notification permission if needed
        checkAndRequestNotificationPermission()

        loadWeatherData()
        loadWeatherForecast()
        updateAllTexts()

        Log.d(TAG, "MainActivity setup completed successfully")
    }

    private fun debugNotificationSystem() {
        Log.d(TAG, "=== NOTIFICATION SYSTEM DEBUG ===")

        // Check app settings
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        Log.d(TAG, "App notifications enabled: $notificationsEnabled")

        // Check system notification settings
        val notificationManager = NotificationManagerCompat.from(this)
        val systemNotificationsEnabled = notificationManager.areNotificationsEnabled()
        Log.d(TAG, "System notifications enabled: $systemNotificationsEnabled")

        // Check Android 13+ permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = NotificationHelper.hasNotificationPermission(this)
            Log.d(TAG, "Notification permission granted: $hasPermission")
        } else {
            Log.d(TAG, "Notification permission not required (Android < 13)")
        }

        // Check notification channel
        NotificationHelper.checkNotificationChannelStatus(this)

        // Final check using helper method
        val canSendNotifications = NotificationHelper.canSendNotifications(this)
        Log.d(TAG, "Can send notifications (final check): $canSendNotifications")
        Log.d(TAG, "=== END NOTIFICATION DEBUG ===")
    }

    private fun sendWelcomeNotification() {
        // Small delay to ensure app is fully loaded
        Handler(Looper.getMainLooper()).postDelayed({
            val userName = preferences.getCurrentUserName()
            Log.d(TAG, "Sending welcome notification for user: $userName")

            NotificationHelper.sendWelcomeNotification(this, userName)

            // Debug: Check if notifications can be sent
            val canNotify = NotificationHelper.canSendNotifications(this)
            Log.d(TAG, "Welcome notification - Can send: $canNotify")

            if (!canNotify) {
                // Show a toast to inform user about notification settings
                Toast.makeText(this,
                    "Enable notifications in settings for weather alerts",
                    Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Notifications disabled - user informed")
            } else {
                Log.d(TAG, "Welcome notification sent successfully")
            }
        }, 1000) // 1 second delay
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationHelper.hasNotificationPermission(this)) {
                Log.d(TAG, "Notification permission not granted, showing request dialog")
                // Show explanation before requesting permission
                Handler(Looper.getMainLooper()).postDelayed({
                    AlertDialog.Builder(this)
                        .setTitle("Notification Permission")
                        .setMessage("SAWeather needs notification permission to send weather alerts and updates. Would you like to enable notifications?")
                        .setPositiveButton("Enable") { _, _ ->
                            Log.d(TAG, "User agreed to enable notifications, requesting permission")
                            NotificationHelper.requestNotificationPermission(this)
                        }
                        .setNegativeButton("Later") { _, _ ->
                            Log.d(TAG, "User deferred notification permission")
                        }
                        .setOnCancelListener {
                            Log.d(TAG, "User canceled notification permission dialog")
                        }
                        .show()
                }, 2000) // 2 second delay
            } else {
                Log.d(TAG, "Notification permission already granted")
            }
        }
    }

    // Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1001) { // Notification permission request code
            val granted = grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED
            Log.d(TAG, "Notification permission request result: $granted")

            if (granted) {
                Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "User granted notification permission")

                // Send test notification to confirm it works
                Handler(Looper.getMainLooper()).postDelayed({
                    NotificationHelper.sendTestNotification(this)
                    Log.d(TAG, "Sent test notification after permission grant")
                }, 1000)
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_LONG).show()
                Log.w(TAG, "User denied notification permission")
            }
        }
    }

    private fun initViews() {
        welcomeTextView = findViewById(R.id.welcomeTextView)
        locationTextView = findViewById(R.id.locationTextView)
        temperatureTextView = findViewById(R.id.temperatureTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        humidityTextView = findViewById(R.id.humidityTextView)
        windTextView = findViewById(R.id.windTextView)
        progressBar = findViewById(R.id.progressBar)

        Log.d(TAG, "UI views initialized successfully")
    }

    private fun updateAllTexts() {
        // Update welcome message
        updateWelcomeMessage()

        // Update other static texts if any
        supportActionBar?.title = getString(R.string.app_name)

        Log.d(TAG, "UI texts updated")
    }

    private fun updateWelcomeMessage() {
        val userName = preferences.getCurrentUserName()
        val welcomeText = getString(R.string.welcome)

        if (!userName.isNullOrEmpty()) {
            "$welcomeText, $userName! 👋".also { welcomeTextView.text = it }
            Log.d(TAG, "Welcome message set with user name: $userName")
        } else {
            val currentUser = auth.currentUser
            if (currentUser?.email != null) {
                val nameFromEmail = currentUser.email!!.substringBefore("@")
                "$welcomeText, $nameFromEmail! 👋".also { welcomeTextView.text = it }
                Log.d(TAG, "Welcome message set with email-based name: $nameFromEmail")
            } else {
                "$welcomeText to SAWeather! 🌤️".also { welcomeTextView.text = it }
                Log.d(TAG, "Welcome message set with generic greeting")
            }
        }
    }

    private fun setupObservers() {
        viewModel.weatherData.observe(this) { weather ->
            if (weather != null) {
                locationTextView.text = weather.location
                "${weather.temperature}°C".also { temperatureTextView.text = it }
                descriptionTextView.text = weather.description
                "${getString(R.string.humidity)}: ${weather.humidity}%".also { humidityTextView.text = it }
                "${getString(R.string.wind)}: ${weather.windSpeed} km/h".also { windTextView.text = it }

                Log.d(TAG, "Weather data updated: ${weather.location}, ${weather.temperature}°C")

                // Send weather update notification when data is loaded
                sendWeatherUpdateNotification(weather)
            } else {
                // Handle null weather data
                locationTextView.text = getString(R.string.app_name)
                temperatureTextView.text = "--°C"
                descriptionTextView.text = getString(R.string.no_data_available)
                humidityTextView.text = "${getString(R.string.humidity)}: --%"
                windTextView.text = "${getString(R.string.wind)}: -- km/h"

                Log.w(TAG, "Weather data is null, showing placeholder data")
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            Log.d(TAG, "Loading state: $isLoading")
        }

        viewModel.isOffline.observe(this) { isOffline ->
            if (isOffline) {
                // Show offline indicator (optional)
                val currentLocation = preferences.getCurrentUserLocation()
                locationTextView.text = "$currentLocation (Offline)"
                Log.w(TAG, "App is in offline mode")
            } else {
                Log.d(TAG, "App is in online mode")
            }
        }

        // Add error observer
        viewModel.errorMessage.observe(this) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                Log.e(TAG, "Weather data error: $error")
            }
        }

        Log.d(TAG, "Weather observers setup completed")
    }

    private fun sendWeatherUpdateNotification(weather: Weather) {
        Handler(Looper.getMainLooper()).postDelayed({
            Log.d(TAG, "Sending weather update notification for: ${weather.location}")
            NotificationHelper.sendWeatherUpdateNotification(
                this,
                weather.location,
                "${weather.temperature}°C"
            )
        }, 500) // Small delay to ensure UI is updated first
    }

    private fun setupForecastObservers() {
        viewModel.weatherForecast.observe(this) { forecast ->
            if (forecast != null) {
                // Update current weather from forecast
                val current = forecast.currentWeather
                locationTextView.text = current.location
                "${current.temperature}°C".also { temperatureTextView.text = it }
                descriptionTextView.text = current.description
                "${getString(R.string.humidity)}: ${current.humidity}%".also { humidityTextView.text = it }
                "${getString(R.string.wind)}: ${current.windSpeed} km/h".also { windTextView.text = it }

                // Update forecast
                displayForecast(forecast.forecast)

                Log.d(TAG, "Forecast data loaded: ${forecast.forecast.size} days")
            } else {
                // Handle null forecast data
                displayEmptyForecast()
                Log.w(TAG, "Forecast data is null")
            }
        }

        Log.d(TAG, "Forecast observers setup completed")
    }

    private fun displayForecast(forecast: List<ForecastDay>) {
        val forecastContainer = findViewById<LinearLayout>(R.id.forecastContainer)
        forecastContainer.removeAllViews()

        if (forecast.isEmpty()) {
            displayEmptyForecast()
            return
        }

        val inflater = LayoutInflater.from(this)

        forecast.forEach { day ->
            val forecastItemView = inflater.inflate(R.layout.item_forecast, forecastContainer, false)

            val dayTextView = forecastItemView.findViewById<TextView>(R.id.dayTextView)
            val dateTextView = forecastItemView.findViewById<TextView>(R.id.dateTextView)
            val iconTextView = forecastItemView.findViewById<TextView>(R.id.iconTextView)
            val highTempTextView = forecastItemView.findViewById<TextView>(R.id.highTempTextView)
            val lowTempTextView = forecastItemView.findViewById<TextView>(R.id.lowTempTextView)

            dayTextView.text = day.day
            dateTextView.text = day.date
            iconTextView.text = day.icon
            "${day.highTemp.toInt()}°".also { highTempTextView.text = it }
            "${day.lowTemp.toInt()}°".also { lowTempTextView.text = it }

            forecastContainer.addView(forecastItemView)
        }

        Log.d(TAG, "Forecast displayed: ${forecast.size} days")
    }

    private fun displayEmptyForecast() {
        val forecastContainer = findViewById<LinearLayout>(R.id.forecastContainer)
        forecastContainer.removeAllViews()

        val inflater = LayoutInflater.from(this)
        val emptyView = inflater.inflate(R.layout.item_forecast, forecastContainer, false)

        val dayTextView = emptyView.findViewById<TextView>(R.id.dayTextView)
        val dateTextView = emptyView.findViewById<TextView>(R.id.dateTextView)
        val iconTextView = emptyView.findViewById<TextView>(R.id.iconTextView)
        val highTempTextView = emptyView.findViewById<TextView>(R.id.highTempTextView)
        val lowTempTextView = emptyView.findViewById<TextView>(R.id.lowTempTextView)

        dayTextView.text = getString(R.string.no_data)
        dateTextView.text = ""
        iconTextView.text = "❓"
        highTempTextView.text = "--°"
        lowTempTextView.text = "--°"

        forecastContainer.addView(emptyView)

        Log.d(TAG, "Empty forecast displayed (no data available)")
    }

    private fun loadWeatherData() {
        val location = preferences.getCurrentUserLocation()
        Log.d(TAG, "Loading weather data for location: $location")
        viewModel.fetchWeatherData(location)
    }

    private fun loadWeatherForecast() {
        val location = preferences.getCurrentUserLocation()
        Log.d(TAG, "Loading weather forecast for location: $location")
        viewModel.fetchWeatherForecast(location)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        Log.d(TAG, "Options menu created")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "Menu item selected: ${item.title}")
        return when (item.itemId) {
            R.id.action_test_notification -> {
                testNotification()
                true
            }
            R.id.action_settings -> {
                val intent = Intent(this, com.example.saweather.ui.settings.SettingsActivity::class.java)
                startActivity(intent)
                Log.d(TAG, "Settings activity started")
                true
            }
            R.id.action_change_language -> {
                showLanguageDialog()
                true
            }
            R.id.action_change_location -> {
                showChangeLocationDialog()
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            R.id.action_refresh -> {
                refreshWeatherData()
                true
            }
            else -> {
                Log.d(TAG, "Unknown menu item selected")
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun testNotification() {
        Log.d(TAG, "Test notification requested by user")

        // Request permission first if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationHelper.hasNotificationPermission(this)) {
                Log.d(TAG, "No notification permission, requesting...")
                NotificationHelper.requestNotificationPermission(this)
                Toast.makeText(this, "Please grant notification permission and try again", Toast.LENGTH_LONG).show()
                return
            }
        }

        // Send test notification
        Log.d(TAG, "Sending test notification...")
        NotificationHelper.sendTestNotification(this)
        Toast.makeText(this, "Test notification sent! Check your status bar.", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Test notification sent successfully")
    }

    private fun showChangeLocationDialog() {
        val locations = arrayOf("Johannesburg", "Cape Town", "Durban", "Pretoria", "Port Elizabeth", "Bloemfontein")
        Log.d(TAG, "Showing location change dialog with ${locations.size} options")

        AlertDialog.Builder(this)
            .setTitle(R.string.change_location)
            .setItems(locations) { _, which ->
                val selectedLocation = locations[which]
                val oldLocation = preferences.getCurrentUserLocation()

                Log.d(TAG, "Location changed from '$oldLocation' to '$selectedLocation'")

                // Update location in preferences
                preferences.updateLocation(selectedLocation)

                // Send location change notification
                sendLocationChangeNotification(oldLocation, selectedLocation)

                // Refresh weather data
                refreshWeatherData()
            }
            .show()
    }

    private fun sendLocationChangeNotification(oldLocation: String, newLocation: String) {
        Log.d(TAG, "Sending location change notification: $oldLocation -> $newLocation")
        NotificationHelper.sendLocationChangeNotification(this, oldLocation, newLocation)
    }

    private fun refreshWeatherData() {
        val location = preferences.getCurrentUserLocation()
        Log.d(TAG, "Refreshing weather data for: $location")
        viewModel.refreshWeatherData(location)
        Toast.makeText(this, "Refreshing weather data...", Toast.LENGTH_SHORT).show()
    }

    private fun logout() {
        Log.d(TAG, "User logging out...")
        auth.signOut()
        preferences.logout()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
        Log.d(TAG, "User logged out successfully")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "Configuration changed, updating UI texts")
        // Update texts when configuration changes (including language)
        updateAllTexts()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "MainActivity resumed")
        // Update all texts when returning to activity
        updateAllTexts()
        // Refresh data when returning to the app
        refreshWeatherData()

        // Re-check notification status when returning to app
        debugNotificationSystem()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "MainActivity paused")
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Zulu (isiZulu)", "Afrikaans")
        Log.d(TAG, "Showing language selection dialog")

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_language))
            .setItems(languages) { _, which ->
                val languageCode = when (which) {
                    0 -> LanguageManager.LANGUAGE_ENGLISH
                    1 -> LanguageManager.LANGUAGE_ZULU
                    2 -> LanguageManager.LANGUAGE_AFRIKAANS
                    else -> LanguageManager.LANGUAGE_ENGLISH
                }
                Log.d(TAG, "Language selected: ${languages[which]} ($languageCode)")
                setAppLanguage(languageCode)
            }
            .show()
    }

    private fun setAppLanguage(languageCode: String) {
        Log.d(TAG, "Setting app language to: $languageCode")
        languageManager.setLanguage(languageCode)
        // Force complete recreation with new language
        recreate()
    }
}