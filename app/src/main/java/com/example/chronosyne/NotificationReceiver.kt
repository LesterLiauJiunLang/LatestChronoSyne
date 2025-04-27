package com.example.chronosyne

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventTitle = intent.getStringExtra("eventTitle") ?: "Your event"
        val eventDateMillis = intent.getLongExtra("eventDate", 0L)
        val eventDate = Date(eventDateMillis)

        // Calculate days difference
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val eventDay = Calendar.getInstance().apply {
            time = eventDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }


        val daysUntilEvent = ((eventDay.timeInMillis - today.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

        // Build message based on daysUntilEvent
        val notificationMessage = when (daysUntilEvent) {
            7 -> "📅 $eventTitle is just a week away! Get ready!"
            1 -> "🔔 $eventTitle is happening tomorrow! Prepare yourself!"
            0 -> "🎉 $eventTitle is happening today! Let's go!"
            else -> {
                val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(eventDate)
                "$eventTitle is coming soon on $formattedDate! Are you ready?"
            }
        }

        // Open app when notification clicked
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "event_channel")
            .setSmallIcon(R.drawable.notifications_icon) // Your notification icon
            .setContentTitle("Upcoming Event!")
            .setContentText(notificationMessage)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED) {
            with(NotificationManagerCompat.from(context)) {
                val notificationId = (eventTitle + eventDateMillis).hashCode()
                notify(notificationId, builder.build())
            }
        } else {
            // Permission denied - no notification
        }
    }
}
