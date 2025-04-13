package com.example.chronosyne.repository

import com.example.chronosyne.database.dao.EventCategoryDao
import com.example.chronosyne.database.dao.EventDao
import com.example.chronosyne.database.Event

class EventRepository(
    private val eventDao: EventDao,
    private val crossRefDao: EventCategoryDao
) {
    suspend fun getEventsForUser(userId: Int) = eventDao.getEventsForUser(userId)
    suspend fun insertEvent(event: Event) = eventDao.insertEvent(event)
    suspend fun getEventWithCategories(eventId: Int) =
        crossRefDao.getEventWithCategories(eventId)
}