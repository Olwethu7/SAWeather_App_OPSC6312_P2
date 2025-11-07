// app/src/main/java/com/example/saweather/service/WeatherNotificationWorker.kt
package com.example.saweather.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.saweather.R
import com.example.saweather.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherNotificationWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                // Check if notifications are enabled
                val prefs = applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)

                if (notificationsEnabled) {
                    sendDailyWeatherNotification()
                }

                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }

    private fun sendDailyWeatherNotification() {
        val title = applicationContext.getString(R.string.daily_weather_update)
        val message = applicationContext.getString(R.string.check_todays_weather_forecast)

        NotificationHelper.sendNotification(
            applicationContext,
            title,
            message
        )
    }
}