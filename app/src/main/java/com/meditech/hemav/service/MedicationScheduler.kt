package com.meditech.hemav.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

object MedicationScheduler {

    fun scheduleReminder(context: Context, medicineName: String, dosage: String, timeInMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Check for exact alarm permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("MedicationScheduler", "Exact alarm permission not granted")
                // In a real app, guide user to settings
                return
            }
        }

        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra("medicine_name", medicineName)
            putExtra("dosage", dosage)
            // Use time as ID ensures uniqueness for different times
            putExtra("notification_id", timeInMillis.toInt()) 
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            timeInMillis.toInt(), // RequestCode must be unique per alarm
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
            Log.d("MedicationScheduler", "Alarm set for $medicineName at $timeInMillis")
        } catch (e: SecurityException) {
            Log.e("MedicationScheduler", "Failed to set alarm: ${e.message}")
        }
    }

    fun cancelReminder(context: Context, timeInMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicationAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            timeInMillis.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }
}
