package com.example.realmadrid

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.json.JSONObject
import java.io.InputStream

class DatabaseInitializer(context: Context) {

    private val context: Context = context

    // Метод для инициализации базы данных из JSON
    fun initializeDatabaseFromJson(db: SQLiteDatabase, jsonFileName: String) {
        try {
            // Чтение JSON-файла из assets
            val inputStream: InputStream = context.assets.open(jsonFileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val jsonString = String(buffer, Charsets.UTF_8)

            // Парсинг JSON
            val json = JSONObject(jsonString)
            val tables = json.getJSONArray("objects")

            // Создание таблиц и вставка данных
            for (i in 0 until tables.length()) {
                val table = tables.getJSONObject(i)
                val ddl = table.getString("ddl")
                val rows = table.getJSONArray("rows")

                // Создание таблицы
                db.execSQL(ddl)

                // Вставка данных
                for (j in 0 until rows.length()) {
                    val row = rows.getJSONArray(j)
                    val placeholders = List(row.length()) { "?" }.joinToString(", ")
                    val query = "INSERT INTO ${table.getString("name")} VALUES ($placeholders)"
                    val args = Array(row.length()) { index -> row.getString(index) }
                    db.execSQL(query, args)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}