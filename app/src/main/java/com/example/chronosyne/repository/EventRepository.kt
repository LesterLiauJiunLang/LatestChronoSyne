package com.example.chronosyne.repository

import com.example.chronosyne.database.dao.EventDao
import com.example.chronosyne.database.dao.EventCategoryDao
import com.example.chronosyne.database.Event
import com.example.chronosyne.database.EventCategoryCrossRef
import com.example.chronosyne.database.relations.EventWithCategories
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.Date

open class EventRepository(
    private val eventDao: EventDao,
    private val crossRefDao: EventCategoryDao,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun getEventsForUser(userId: String): Flow<List<Event>> =
        eventDao.getEventsForUser(userId)

    suspend fun getEventWithCategories(eventId: Int): EventWithCategories {
        val localData = crossRefDao.getEventWithCategories(eventId)
        return localData
    }

    suspend fun insertEvent(event: Event) {
        val currentUserId = auth.currentUser?.uid
            ?: throw SecurityException("Not authenticated")

        if (event.userId != currentUserId) {
            throw SecurityException("User does not own this event")
        }

        eventDao.insertEvent(event)

        try {
            firestore.collection("events")
                .document(event.eventId.toString())
                .set(event)
                .await()
        } catch (_: Exception) {}
    }

    suspend fun deleteEvent(event: Event) {
        val currentUserId = auth.currentUser?.uid
            ?: throw SecurityException("Not authenticated")

        if (event.userId != currentUserId) {
            throw SecurityException("Cannot delete other users' events")
        }

        eventDao.deleteEvent(event)

        try {
            firestore.collection("events")
                .document(event.eventId.toString())
                .delete()
                .await()
        } catch (_: Exception) {}
    }

    open suspend fun insertEventWithCategory(event: Event, categoryName: String): Long {
        val category = crossRefDao.getCategoryByName(categoryName)
            ?: throw IllegalArgumentException("Category '$categoryName' not found")

        val eventId = eventDao.insertEvent(event)

        val crossRef = EventCategoryCrossRef(
            eventId = eventId.toInt(),
            categoryId = category.categoryId
        )
        crossRefDao.insertEventCategoryCrossRef(crossRef)

        return eventId
    }

    fun getUpcomingEvents(userId: String, today: Date, cutoffDate: Date): Flow<List<EventWithCategories>> =
        eventDao.getEventsBetweenDatesWithCategories(userId, today, cutoffDate)

    fun getFutureEvents(userId: String, cutoffDate: Date): Flow<List<EventWithCategories>> =
        eventDao.getEventsAfterDateWithCategories(userId, cutoffDate)
}
