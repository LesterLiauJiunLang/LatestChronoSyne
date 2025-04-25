package com.example.chronosyne.database.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.chronosyne.database.EventCategoryCrossRef
import com.example.chronosyne.database.Event
import com.example.chronosyne.database.Category

data class EventWithCategories(
    @Embedded
    val event: Event,  // Embedded Event entity

    @Relation(
        parentColumn = "eventId",         // Event's ID field
        entityColumn = "categoryId",      // Category's ID field
        associateBy = Junction(EventCategoryCrossRef::class)  // Junction table
    )
    val categories: List<Category>  // List of associated categories
)