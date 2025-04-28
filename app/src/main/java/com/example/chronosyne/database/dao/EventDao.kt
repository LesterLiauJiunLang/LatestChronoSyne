package com.example.chronosyne.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.chronosyne.database.Event
import com.example.chronosyne.database.relations.EventWithCategories
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long

    @Query("SELECT * FROM events WHERE userId = :userId ORDER BY startTime ASC")
    fun getEventsForUser(userId: String): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE eventId = :eventId")
        suspend fun getEventById(eventId: Int): Event?

    @Update
    suspend fun updateEvent(event: Event)

    @Query("DELETE FROM events WHERE eventId = :eventId")
    suspend fun deleteEventById(eventId: Long)

    // Unified range query (use for upcoming events)
    @Transaction
    @Query("SELECT * FROM events WHERE userId = :userId AND startTime >= :start AND startTime <= :end")
    fun getEventsBetweenDatesWithCategories(userId: String, start: Date, end: Date): Flow<List<EventWithCategories>>

    @Transaction
    @Query("SELECT * FROM events WHERE userId = :userId AND startTime > :cutoffDate")
    fun getEventsAfterDateWithCategories(userId: String, cutoffDate: Date): Flow<List<EventWithCategories>>

    @Query("SELECT * FROM events WHERE userId = :userId ORDER BY startTime ASC")
    fun getEventsWithCategoriesForUser(userId: String): Flow<List<EventWithCategories>>
}
