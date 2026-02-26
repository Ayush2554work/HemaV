package com.meditech.hemav.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.meditech.hemav.MainActivity
import com.meditech.hemav.R

class HemaVFirebaseMessagingService : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received from: ${message.from}")

        // Check if message contains a data payload.
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${message.data}")
            handleNow(message.data)
        }

        // Check if message contains a notification payload.
        message.notification?.let { notification ->
            val title = notification.title ?: "HemaV"
            val body = notification.body ?: ""
            val type = message.data["type"] ?: "message"
            
            sendNotification(title, body, type)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        // In a real app, send this token to your server
    }

    private fun handleNow(data: Map<String, String>) {
        // Handle background tasks if needed
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     */
    private fun sendNotification(title: String, messageBody: String, type: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("notification_type", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = when (type) {
            "appointment" -> CHANNEL_APPOINTMENTS
            "medication" -> CHANNEL_MEDICATIONS
            else -> CHANNEL_MESSAGES
        }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        // Use a placeholder icon if app icon isn't ready, usually mipmap/ic_launcher
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            //.setSmallIcon(R.mipmap.ic_launcher) // Assuming standard resource exists
            .setSmallIcon(android.R.drawable.ic_dialog_info) 
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val validChannels = listOf(
                NotificationChannel(
                    CHANNEL_APPOINTMENTS,
                    "Appointments",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Appointment updates and reminders" },
                
                NotificationChannel(
                    CHANNEL_MEDICATIONS,
                    "Medications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Medication reminders" },
                
                NotificationChannel(
                    CHANNEL_MESSAGES,
                    "Messages",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Chat messages from doctors" }
            )

            val notificationManager = getSystemService(NotificationManager::class.java)
            validChannels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    companion object {
        private const val TAG = "HemaVFCM"
        const val CHANNEL_APPOINTMENTS = "hemav_appointments"
        const val CHANNEL_MEDICATIONS = "hemav_medications"
        const val CHANNEL_MESSAGES = "hemav_messages"
    }
}
