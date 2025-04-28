package com.example.chronosyne.fake

import com.example.chronosyne.database.Category
import com.example.chronosyne.database.Event
import com.example.chronosyne.database.EventCategoryCrossRef
import com.example.chronosyne.database.dao.EventCategoryDao
import com.example.chronosyne.database.relations.CategoryWithEvents
import com.example.chronosyne.database.relations.EventWithCategories
import java.util.Date

class FakeCrossRefDao : EventCategoryDao {

    override suspend fun insertEventCategoryCrossRef(crossRef: EventCategoryCrossRef) {
        // Add functionality here if needed
    }

    override suspend fun getEventWithCategories(eventId: Int): EventWithCategories? {
        val defaultEvent = Event(
            eventId = 0,
            userId = "fakeUser",
            title = "Default Event",
            description = "Default Description",
            location = "Default Location",
            startTime = Date(),
            endTime = Date(),
            isAllDay = false,
            createdAt = Date(),
            updatedAt = Date()
        )

        return EventWithCategories(event = defaultEvent, categories = emptyList())
    }

    override suspend fun getCategoryWithEvents(categoryId: Int): CategoryWithEvents {
        val defaultCategory = Category(
            categoryId = 0,
            name = "Default Category",
            userId = "fakeUser",
            color = "defaultColor"
        ) // Provide a default Category
        return CategoryWithEvents(category = defaultCategory, events = emptyList())
    }

    override suspend fun getCategoryByName(name: String): Category {
        return Category(
            categoryId = 1,
            name = name,
            userId = "fakeUser",
            color = "fakeColor"
        ) // Provide a valid color value
    }
}
