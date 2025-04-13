package com.example.chronosyne.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.chronosyne.database.entities.Reminder

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Query("SELECT * FROM reminders WHERE eventId = :eventId")
    suspend fun getRemindersForEvent(eventId: Int): List<Reminder>

    @Query("SELECT * FROM reminders WHERE isSent = 0 AND remindAt <= :currentTime")
    suspend fun getPendingReminders(currentTime: Long): List<Reminder>

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)
}