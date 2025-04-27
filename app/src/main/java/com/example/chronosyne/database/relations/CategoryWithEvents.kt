package com.example.chronosyne.database.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.chronosyne.database.EventCategoryCrossRef
import com.example.chronosyne.database.Event
import com.example.chronosyne.database.Category

data class CategoryWithEvents(
    @Embedded val category: Category,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "eventId",
        associateBy = Junction(EventCategoryCrossRef::class)
    )
    val events: List<Event>
)