package com.example.chronosyne.repository

class UserRepository(private val userDao: UserDao) {
    suspend fun getUserById(id: Int) = userDao.getUserById(id)
    suspend fun getUserByEmail(email: String) = userDao.getUserByEmail(email)
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    suspend fun deleteUser(user: User) = userDao.deleteUser(user)
}