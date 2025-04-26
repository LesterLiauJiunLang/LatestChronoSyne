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

    override suspend fun getEventWithCategories(eventId: Int): EventWithCategories {
        // Create a default Event object with necessary values
        val defaultEvent = Event(
            eventId = 0,
            userId = "fakeUser",  // Example userId
            title = "Default Event",
            description = "Default Description",  // You can also set it to null if you want it optional
            location = "Default Location",  // You can set this to null as well if optional
            startTime = Date(),  // Current date as the start time (you can adjust this)
            endTime = Date(),  // Current date as the end time (you can adjust this)
            isAllDay = false,  // Default to not all day
            createdAt = Date(),  // Current date as creation date
            updatedAt = Date()  // Current date as the last updated date
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
