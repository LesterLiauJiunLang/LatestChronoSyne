package com.example.chronosyne.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chronosyne.database.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Query("SELECT * FROM categories WHERE userId = :userId")
    fun getCategoriesByUser(userId: String): Flow<List<Category>>

    @Delete
    suspend fun deleteCategory(category: Category)
}