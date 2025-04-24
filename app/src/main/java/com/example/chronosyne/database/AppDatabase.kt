package com.example.chronosyne.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.chronosyne.database.dao.CategoryDao
import com.example.chronosyne.database.EventCategoryCrossRef
import com.example.chronosyne.database.Reminder
import com.example.chronosyne.database.Event
import com.example.chronosyne.database.Category
import com.example.chronosyne.database.dao.EventCategoryDao
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
    abstract fun eventcategoryDao(): EventCategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Singleton pattern to ensure only one instance of the database
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chrono_syne_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
