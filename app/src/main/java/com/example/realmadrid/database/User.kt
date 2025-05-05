package com.example.realmadrid.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "telephone") val telephone: String,
    @ColumnInfo(name = "birthdate") val birthdate: String,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "role") val role: String,
    @ColumnInfo(name = "is_current") val isCurrent: Boolean = false // флаг текущего пользователя
)