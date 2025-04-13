package com.example.chronosyne.repository

import com.example.chronosyne.database.dao.ReminderDao
import com.example.chronosyne.database.entities.Reminder

class ReminderRepository(private val reminderDao: ReminderDao) {
    suspend fun insertReminder(reminder: Reminder) = reminderDao.insertReminder(reminder)
    suspend fun getRemindersForEvent(eventId: Int) = reminderDao.getRemindersForEvent(eventId)
    suspend fun getPendingReminders(currentTime: Long) = reminderDao.getPendingReminders(currentTime)
}