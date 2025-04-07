package com.example.chronosyne.database.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.chronosyne.database.entities.EventCategoryCrossRef
import com.example.chronosyne.database.entities.Event
import com.example.chronosyne.database.entities.Category


data class EventWithCategories(
    @Embedded val event: Event,
    @Relation(
        parentColumn = "eventId",
        entityColumn = "categoryId",
        associateBy = Junction(EventCategoryCrossRef::class)
    )
    val categories: List<Category>
)