package com.example.chronosyne.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.chronosyne.database.Category
import com.example.chronosyne.database.EventCategoryCrossRef
import com.example.chronosyne.database.relations.CategoryWithEvents
import com.example.chronosyne.database.relations.EventWithCategories

@Dao
interface EventCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventCategoryCrossRef(crossRef: EventCategoryCrossRef)

    @Transaction
    @Query("SELECT * FROM events WHERE eventId = :eventId")
    suspend fun getEventWithCategories(eventId: Int): EventWithCategories

    @Transaction
    @Query("SELECT * FROM categories WHERE categoryId = :categoryId")
    suspend fun getCategoryWithEvents(categoryId: Int): CategoryWithEvents

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): Category?
}