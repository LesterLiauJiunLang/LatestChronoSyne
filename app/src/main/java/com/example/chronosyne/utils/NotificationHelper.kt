package com.example.chronosyne.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.chronosyne.NotificationReceiver
import com.example.chronosyne.database.Event
import java.util.Calendar

object NotificationHelper {

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "event_channel",
                "Event Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies you when an event is coming up."
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleNotification(context: Context, event: Event) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTimes = listOf(
            event.startTime.time - 7 * 24 * 60 * 60 * 1000, // 7 days before
            event.startTime.time - 24 * 60 * 60 * 1000,      // 1 day before
            event.startTime.time                            // Exact time
        )

        val calendar = Calendar.getInstance().apply { time = event.startTime }
        if (event.isAllDay) {
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
        }

        triggerTimes.forEach { triggerAtMillis ->
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("eventTitle", event.title)
                putExtra("eventDate", event.startTime.time)
            }

            val requestCode = (event.eventId.toLong() xor triggerAtMillis).toInt()

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode, // Unique per event + trigger time
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )


            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                Log.e("Notification", "Failed to schedule exact alarm: ${e.message}")
            }
        }
    }
}