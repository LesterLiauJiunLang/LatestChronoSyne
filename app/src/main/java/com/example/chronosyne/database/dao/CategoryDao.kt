package com.example.chronosyne.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chronosyne.database.entities.Category

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Query("SELECT * FROM categories WHERE userId = :userId")
    suspend fun getCategoriesForUser(userId: Int): List<Category>

    @Delete
    suspend fun deleteCategory(category: Category)
}