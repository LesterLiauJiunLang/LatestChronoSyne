package com.example.chronosyne.repository

import com.example.chronosyne.database.dao.CategoryDao
import com.example.chronosyne.database.dao.EventCategoryDao
import com.example.chronosyne.database.Category
import com.example.chronosyne.database.relations.CategoryWithEvents
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val crossRefDao: EventCategoryDao
) {
    // Flow version for real-time updates
    fun getCategoriesForUser(userId: String): Flow<List<Category>> =
        categoryDao.getCategoriesByUser(userId)

    // Suspended version for one-time fetch
    suspend fun getCategoryWithEvents(categoryId: Int): CategoryWithEvents =
        crossRefDao.getCategoryWithEvents(categoryId)

    // New: Firebase-integrated save function
    suspend fun saveCategory(category: Category, currentUserId: String) {
        // Validate ownership
        if (category.userId != currentUserId) {
            throw SecurityException("User does not own this category")
        }
        categoryDao.insertCategory(category)
    }

    // New: Delete with validation
    suspend fun deleteCategory(category: Category, currentUserId: String) {
        if (category.userId != currentUserId) {
            throw SecurityException("Cannot delete other users' categories")
        }
        categoryDao.deleteCategory(category)
    }

}