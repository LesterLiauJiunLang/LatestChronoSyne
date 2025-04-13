package com.example.chronosyne.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)

data class Category(
    @PrimaryKey(autoGenerate = true) val categoryId: Int = 0,
    val userId: Int,
    val name: String,
    val color: String
)
