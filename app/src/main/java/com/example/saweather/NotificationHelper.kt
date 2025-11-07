// app/src/main/java/com/example/saweather/util/NotificationHelper.kt
package com.example.saweather.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.saweather.R

object NotificationHelper {

    private const val CHANNEL_ID = "weather_channel"
    private const val CHANNEL_NAME = "Weather Updates"
    private const val TAG = "NotificationHelper"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // Changed to HIGH for better visibility
            ).apply {
                description = context.getString(R.string.channel_for_weather_notifications)
                enableLights(true)
                enableVibration(true)
                lightColor = Color.BLUE
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created: $CHANNEL_NAME")
        }
    }

    // Original method (kept for compatibility)
    fun sendNotification(context: Context, title: String, message: String) {
        sendBasicNotification(context, title, message, System.currentTimeMillis().toInt())
    }

    // Notification for app launch
    fun sendWelcomeNotification(context: Context, userName: String?) {
        createNotificationChannel(context)

        val name = userName ?: "User"
        val title = "Welcome to SAWeather!"
        val message = "Hello $name! Getting your weather data..."

        sendBasicNotification(context, title, message, 1001)
        Log.d(TAG, "Welcome notification sent for user: $name")
    }

    // Notification for location change
    fun sendLocationChangeNotification(context: Context, oldLocation: String, newLocation: String) {
        createNotificationChannel(context)

        val title = "Location Updated 📍"
        val message = "Weather location changed from $oldLocation to $newLocation"

        sendBasicNotification(context, title, message, 1002)
        Log.d(TAG, "Location change notification: $oldLocation -> $newLocation")
    }

    // Notification for weather data refresh
    fun sendWeatherUpdateNotification(context: Context, location: String, temperature: String) {
        createNotificationChannel(context)

        val title = "Weather Updated 🌤️"
        val message = "Current weather in $location: $temperature"

        sendBasicNotification(context, title, message, 1003)
        Log.d(TAG, "Weather update notification for: $location")
    }

    // Test notification method
    fun sendTestNotification(context: Context) {
        createNotificationChannel(context)

        val title = "Test Notification ✅"
        val message = "This is a test notification from SAWeather!"

        sendBasicNotification(context, title, message, 999)
        Log.d(TAG, "Test notification sent")
    }

    // Generic notification method with proper checks
    private fun sendBasicNotification(context: Context, title: String, message: String, notificationId: Int) {
        // Check if notifications are enabled in app settings
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)

        if (!notificationsEnabled) {
            Log.d(TAG, "Notifications disabled in app settings")
            return
        }

        // Check system notification settings
        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) {
            Log.d(TAG, "Notifications disabled system-wide")
            return
        }

        // Check Android 13+ permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                Log.d(TAG, "Notification permission not granted for Android 13+")
                return
            }
            Log.d(TAG, "Notification permission granted")
        }

        // Build and send notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_weather_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        try {
            notificationManager.notify(notificationId, builder.build())
            Log.d(TAG, "Notification sent successfully: $title")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send notification: ${e.message}", e)
        }
    }

    // Method to check if we should show notifications (for debugging)
    fun canSendNotifications(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)

        if (!notificationsEnabled) {
            Log.d(TAG, "Notifications disabled in app settings")
            return false
        }

        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) {
            Log.d(TAG, "Notifications disabled system-wide")
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                Log.d(TAG, "Notification permission not granted for Android 13+")
                return false
            }
        }

        Log.d(TAG, "All notification checks passed - can send notifications")
        return true
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not required below Android 13
        }
    }

    fun requestNotificationPermission(activity: android.app.Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission(activity)) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
                Log.d(TAG, "Requesting notification permission")
            } else {
                Log.d(TAG, "Notification permission already granted")
            }
        }
    }

    // Method to check notification channel status
    fun checkNotificationChannelStatus(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = notificationManager.getNotificationChannel(CHANNEL_ID)

            if (channel == null) {
                Log.w(TAG, "Notification channel doesn't exist!")
            } else {
                Log.d(TAG, "Channel exists: ${channel.name}, Importance: ${channel.importance}")
            }
        }
    }
}