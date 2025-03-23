package com.example.realmadrid

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Создание таблицы users
        val createTableQuery = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Здесь можно добавить логику для обновления базы данных при изменении версии
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    // Метод для добавления пользователя
    fun addUser(email: String, password: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("email", email)
            put("password", password)
        }
        return db.insert("users", null, values)
    }

    // Метод для проверки пользователя
    fun checkUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val selection = "email = ? AND password = ?"
        val selectionArgs = arrayOf(email, password)
        val cursor = db.query("users", null, selection, selectionArgs, null, null, null)
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }
}
