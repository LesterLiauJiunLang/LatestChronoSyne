package com.example.chronosyne.database

import androidx.room.Entity

@Entity(primaryKeys = ["eventId", "categoryId"])
data class EventCategoryCrossRef(
    val eventId: Int,
    val categoryId: Int
)
