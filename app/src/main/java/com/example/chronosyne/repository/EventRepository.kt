package com.example.chronosyne.repository

import com.example.chronosyne.database.dao.EventDao
import com.example.chronosyne.database.dao.EventCategoryDao
import com.example.chronosyne.database.Event
import com.example.chronosyne.database.relations.EventWithCategories
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class EventRepository @Inject constructor(
    private val eventDao: EventDao,
    private val crossRefDao: EventCategoryDao,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // Flow version for real-time updates
    fun getEventsForUser(userId: String): Flow<List<Event>> =
        eventDao.getEventsForUser(userId)

    // Suspended version with Firestore backup
    suspend fun getEventWithCategories(eventId: Int): EventWithCategories {
        val localData = crossRefDao.getEventWithCategories(eventId)

        // Optional: Sync with Firestore
        try {
            val remoteData = firestore.collection("events")
                .document(eventId.toString())
                .get()
                .await()

            // Merge logic here if needed
        } catch (e: Exception) {
            // Fallback to local data
        }

        return localData
    }

    suspend fun insertEvent(event: Event) {
        // Validate ownership
        val currentUserId = auth.currentUser?.uid ?:
        throw SecurityException("Not authenticated")

        if (event.userId != currentUserId) {
            throw SecurityException("User does not own this event")
        }

        // Room insert
        eventDao.insertEvent(event)

        // Firestore sync (optional)
        try {
            firestore.collection("events")
                .document(event.eventId.toString())
                .set(event)
                .await()
        } catch (e: Exception) {
            // Handle sync error or implement retry logic
        }
    }

    suspend fun deleteEvent(event: Event) {
        val currentUserId = auth.currentUser?.uid ?:
        throw SecurityException("Not authenticated")

        if (event.userId != currentUserId) {
            throw SecurityException("Cannot delete other users' events")
        }

        // Room delete
        eventDao.deleteEvent(event)

        // Firestore delete (optional)
        try {
            firestore.collection("events")
                .document(event.eventId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            // Handle sync error
        }
    }

    // Batch operations
    suspend fun syncAllEventsToFirestore(userId: String) {
        val events = eventDao.getEventsForUser(userId).first()
        val batch = firestore.batch()

        events.forEach { event ->
            val docRef = firestore.collection("events")
                .document(event.eventId.toString())
            batch.set(docRef, event)
        }

        batch.commit().await()
    }
}