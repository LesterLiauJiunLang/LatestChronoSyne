package com.example.chronosyne.database.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.chronosyne.database.entities.EventCategoryCrossRef
import com.example.chronosyne.database.entities.Event
import com.example.chronosyne.database.entities.Category

data class CategoryWithEvents(
    @Embedded val category: Category,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "eventId",
        associateBy = Junction(EventCategoryCrossRef::class)
    )
    val events: List<Event>
)