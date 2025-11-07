// app/src/main/java/com/example/saweather/service/WeatherNotificationService.kt
package com.example.saweather.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.saweather.R

class WeatherNotificationService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Handle new FCM token if needed
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Check if notifications are enabled
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)

        if (notificationsEnabled) {
            remoteMessage.notification?.let { notification ->
                sendNotification(
                    notification.title ?: getString(R.string.weather_update),
                    notification.body ?: getString(R.string.new_weather_information_available)
                )
            }
        }
    }

    private fun sendNotification(title: String, message: String) {
        createNotificationChannel()

        val builder = NotificationCompat.Builder(this, "weather_channel")
            .setSmallIcon(R.drawable.ic_weather_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "weather_channel",
                getString(R.string.weather_updates),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.channel_for_weather_notifications)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}