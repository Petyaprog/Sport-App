package com.example.realmadrid.database

import androidx.room.*

@Dao
interface NewsDao {
    @Insert
    suspend fun insertNews(news: News): Long

    @Update
    suspend fun updateNews(news: News)

    @Delete
    suspend fun deleteNews(news: News)

    @Query("SELECT * FROM news ORDER BY date DESC")
    suspend fun getAllNews(): List<News>

    @Query("SELECT * FROM news WHERE id = :id")
    suspend fun getNewsById(id: Int): News?
}