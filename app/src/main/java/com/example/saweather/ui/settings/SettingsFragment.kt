package com.example.saweather.ui.settings

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.preference.ListPreference
import com.example.saweather.R
import com.example.saweather.util.NotificationHelper

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var notificationPreference: SwitchPreference
    private lateinit var themePreference: ListPreference
    private lateinit var testNotificationPreference: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)

        notificationPreference = findPreference("notifications_enabled")!!
        themePreference = findPreference("app_theme")!!
        testNotificationPreference = findPreference("test_notification")!!

        setupNotificationPreference()
        setupThemePreference()
        setupTestNotificationPreference()
    }

    private fun setupNotificationPreference() {
        // Set initial state
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        notificationPreference.isChecked = notificationsEnabled

        notificationPreference.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            handleNotificationPreferenceChange(enabled)
            true
        }
    }

    private fun setupThemePreference() {
        // Set current theme value
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val currentTheme = prefs.getString("app_theme", "system") ?: "system"
        themePreference.value = currentTheme

        themePreference.setOnPreferenceChangeListener { _, newValue ->
            val theme = newValue as String
            handleThemePreferenceChange(theme)
            true
        }
    }

    private fun setupTestNotificationPreference() {
        testNotificationPreference.setOnPreferenceClickListener {
            // Send test notification
            sendTestNotification()
            true
        }
    }

    private fun handleNotificationPreferenceChange(enabled: Boolean) {
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()

        // Show feedback to user
        if (enabled) {
            Toast.makeText(requireContext(), "Notifications enabled", Toast.LENGTH_SHORT).show()

            // Check if we have permission to send notifications
            if (!NotificationHelper.canSendNotifications(requireContext())) {
                showNotificationPermissionWarning()
            }
        } else {
            Toast.makeText(requireContext(), "Notifications disabled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleThemePreferenceChange(theme: String) {
        when (theme) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        // Save theme preference
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("app_theme", theme).apply()

        // Show feedback to user
        val themeMessage = when (theme) {
            "light" -> "Light theme applied"
            "dark" -> "Dark theme applied"
            else -> "System theme applied"
        }
        Toast.makeText(requireContext(), themeMessage, Toast.LENGTH_SHORT).show()
    }

    private fun sendTestNotification() {
        // Check if notifications are enabled in app settings
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)

        if (!notificationsEnabled) {
            Toast.makeText(
                requireContext(),
                "Enable notifications first to test",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Check system notification settings
        val notificationManager = androidx.core.app.NotificationManagerCompat.from(requireContext())
        if (!notificationManager.areNotificationsEnabled()) {
            Toast.makeText(
                requireContext(),
                "Enable notifications in system settings first",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Check Android 13+ permission
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationHelper.hasNotificationPermission(requireContext())) {
                Toast.makeText(
                    requireContext(),
                    "Grant notification permission first",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }

        // Send test notification
        NotificationHelper.sendTestNotification(requireContext())
        Toast.makeText(requireContext(), "Test notification sent!", Toast.LENGTH_SHORT).show()
    }

    private fun showNotificationPermissionWarning() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Notification Permission Needed")
                .setMessage("Notifications are enabled but you need to grant permission for them to work. The app will request permission when you next use it.")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh the notification preference state when returning to settings
        refreshNotificationPreferenceState()
    }

    private fun refreshNotificationPreferenceState() {
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)

        // Update the switch state
        if (notificationPreference.isChecked != notificationsEnabled) {
            notificationPreference.isChecked = notificationsEnabled
        }

        // Update test notification preference summary based on current state
        val testSummary = if (notificationsEnabled &&
            androidx.core.app.NotificationManagerCompat.from(requireContext()).areNotificationsEnabled() &&
            NotificationHelper.hasNotificationPermission(requireContext())) {
            "Tap to send a test notification"
        } else {
            "Enable notifications to test"
        }
        testNotificationPreference.summary = testSummary
    }
}