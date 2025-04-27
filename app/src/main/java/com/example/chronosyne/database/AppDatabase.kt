package com.example.chronosyne.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.chronosyne.database.dao.CategoryDao
import com.example.chronosyne.database.dao.EventCategoryDao
import com.example.chronosyne.database.dao.EventDao
import com.example.chronosyne.database.dao.ReminderDao
import com.example.chronosyne.database.entities.Converters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    abstract fun crossRefDao(): EventCategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java, "chronosyne-database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Use the newly created instance, not getInstance()
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    database.categoryDao().insertCategories(
                                        listOf(
                                            Category(
                                                name = "Event",
                                                color = "#BEA99B",
                                                userId = "system"
                                            ),
                                            Category(
                                                name = "Task",
                                                color = "#BEA99B",
                                                userId = "system"
                                            ),
                                            Category(
                                                name = "Birthday",
                                                color = "#BEA99B",
                                                userId = "system"
                                            ),
                                            Category(
                                                name = "Custom",
                                                color = "#BEA99B",
                                                userId = "system"
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}