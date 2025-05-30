package com.example.realmadrid.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news")
data class News(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: String,
    val previewText: String,
    val fullText: String,
    val imageUrl: String
)