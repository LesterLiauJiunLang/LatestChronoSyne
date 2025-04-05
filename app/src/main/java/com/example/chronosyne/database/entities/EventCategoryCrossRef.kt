package com.example.chronosyne.database.entities

import androidx.room.Entity

@Entity(primaryKeys = ["eventId", "categoryId"])
data class EventCategoryCrossRef(
    val eventId: Int,
    val categoryId: Int
)
