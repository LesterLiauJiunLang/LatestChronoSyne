package com.example.chronosyne.fake

import androidx.room.Transaction
import com.example.chronosyne.database.Event
import com.example.chronosyne.database.dao.EventDao
import com.example.chronosyne.database.relations.EventWithCategories
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date

class FakeEventDao : EventDao {
    // Existing methods
    override suspend fun insertEvent(event: Event): Long = 1L

    override fun getEventsForUser(userId: String): Flow<List<Event>> = flow { emit(emptyList()) }

    override suspend fun getEventById(eventId: Int): Event? = null

    override suspend fun updateEvent(event: Event) {}

    override suspend fun deleteEventById(eventId: Long) {} // <--- NEW, correct

    @Transaction
    override fun getEventsBetweenDatesWithCategories(
        userId: String,
        start: Date,
        end: Date
    ): Flow<List<EventWithCategories>> = flow { emit(emptyList()) }

    @Transaction
    override fun getEventsAfterDateWithCategories(
        userId: String,
        cutoffDate: Date
    ): Flow<List<EventWithCategories>> = flow { emit(emptyList()) }
}
