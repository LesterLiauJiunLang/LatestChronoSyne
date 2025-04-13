package com.example.chronosyne.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "events",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)

data class Event(
    @PrimaryKey(autoGenerate = true) val eventId: Int = 0,
    val userId: Int,
    val title: String,
    val description: String?,
    val location: String?,
    val startTime: Date,
    val endTime: Date,
    val isAllDay: Boolean,
    val createdAt: Date,
    val updatedAt: Date
)
