package com.example.chronosyne.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ForeignKey
import java.util.Date

@Entity(
    tableName = "reminders",
    foreignKeys = [ForeignKey(
        entity = Event::class,
        parentColumns = ["eventId"],
        childColumns = ["eventId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("eventId")]
)

data class Reminder(
    @PrimaryKey(autoGenerate = true) val reminderId: Int = 0,
    val eventId: Int,
    val remindAt: Date,
    val method: String,
    val isSent: Boolean,
    val repeatInterval: String? // null for one-time
)
