package com.meditech.hemav

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp
import com.meditech.hemav.data.remote.HemavApiClient
import com.meditech.hemav.data.repository.AuthRepository
import io.sentry.android.core.SentryAndroid

class HemaVApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AuthRepository().restoreSession(this)  // Restore backend JWT token
        initSentry()
        createNotificationChannels()
    }

    private fun initSentry() {
        SentryAndroid.init(this) { options ->
            options.dsn = BuildConfig.SENTRY_DSN
            options.isEnableAutoSessionTracking = true
            options.tracesSampleRate = 1.0 // Capture 100% for POC, reduce in production
            options.environment = if (BuildConfig.DEBUG) "development" else "production"
            options.release = "hemav@${BuildConfig.VERSION_NAME}"
        }
    }

    private fun createNotificationChannels() {
        val channels = listOf(
            NotificationChannel(
                "hemav_appointments",
                "Appointments",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Appointment reminders and updates" },
            NotificationChannel(
                "hemav_medications",
                "Medications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Medication dose reminders" },
            NotificationChannel(
                "hemav_messages",
                "Messages",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "New messages from doctors/patients" },
            NotificationChannel(
                "hemav_default",
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "General notifications" }
        )
        val manager = getSystemService(NotificationManager::class.java)
        channels.forEach { manager.createNotificationChannel(it) }
    }
}
