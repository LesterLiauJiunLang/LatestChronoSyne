package com.example.chronosyne.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "categories",
    indices = [Index("userId")]
)

data class Category(
    @PrimaryKey(autoGenerate = true) val categoryId: Int = 0,
    val userId: String,
    val name: String,
    val color: String
)
