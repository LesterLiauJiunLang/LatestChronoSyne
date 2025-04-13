package com.example.chronosyne.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.chronosyne.database.dao.CategoryDao
import com.example.chronosyne.database.EventCategoryCrossRef
import com.example.chronosyne.database.Reminder
import com.example.chronosyne.database.Event
import com.example.chronosyne.database.Category
import com.example.chronosyne.database.dao.EventDao
import com.example.chronosyne.database.dao.ReminderDao
import com.example.chronosyne.database.entities.Converters
import com.example.chronosyne.database.model.User


@Database(
    entities = [
        Event::class,
        Reminder::class,
        Category::class,
        EventCategoryCrossRef::class
    ],
    version = 3
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun reminderDao(): ReminderDao
    abstract fun categoryDao(): CategoryDao
}