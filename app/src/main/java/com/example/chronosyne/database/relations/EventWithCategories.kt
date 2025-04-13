package com.example.chronosyne.database.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.chronosyne.database.EventCategoryCrossRef
import com.example.chronosyne.database.Event
import com.example.chronosyne.database.Category


data class EventWithCategories(
    @Embedded val event: Event,
    @Relation(
        parentColumn = "eventId",
        entityColumn = "categoryId",
        associateBy = Junction(EventCategoryCrossRef::class)
    )
    val categories: List<Category>
)