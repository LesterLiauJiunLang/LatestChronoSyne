package com.example.chronosyne.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

@Entity(
    tableName = "events",
    indices = [Index("userId")]  // Keep index for query performance
)
data class Event(
    @PrimaryKey(autoGenerate = true) val eventId: Int = 0,
    val userId: String,  // Changed from Int to String (Firebase UID)
    val title: String,
    val description: String?,
    val location: String?,
    val startTime: Date,
    val endTime: Date,
    val isAllDay: Boolean,
    val createdAt: Date,
    val updatedAt: Date
)

fun Date.formatAsDate(): String {
    val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return format.format(this)
}

fun Date.formatAsDateTime(): String {
    val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return format.format(this)
}

