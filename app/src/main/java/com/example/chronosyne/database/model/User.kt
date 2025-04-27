package com.example.chronosyne.database.model

data class User(
    val userId: String = "",          // Firebase Auth UID (auto-generated)
    val username: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis() // Use timestamp instead of Date
)