package com.example.chronosyne.repository

import com.example.chronosyne.database.dao.CategoryDao
import com.example.chronosyne.database.dao.EventCategoryDao

class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val crossRefDao: EventCategoryDao
) {
    suspend fun getCategoriesForUser(userId: Int) = categoryDao.getCategoriesForUser(userId)
    suspend fun getCategoryWithEvents(categoryId: Int) =
        crossRefDao.getCategoryWithEvents(categoryId)
}