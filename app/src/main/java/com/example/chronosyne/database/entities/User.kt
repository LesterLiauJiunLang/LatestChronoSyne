package com.example.chronosyne.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Date
)
