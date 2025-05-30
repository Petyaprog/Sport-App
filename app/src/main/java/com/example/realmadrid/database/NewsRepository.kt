package com.example.realmadrid.database

class NewsRepository(private val newsDao: NewsDao) {
    suspend fun addNews(title: String, date: String, previewText: String, fullText: String, imageUrl: String): Long {
        val news = News(
            title = title,
            date = date,
            previewText = previewText,
            fullText = fullText,
            imageUrl = imageUrl
        )
        return newsDao.insertNews(news)
    }

    suspend fun updateNews(news: News) {
        newsDao.updateNews(news)
    }

    suspend fun deleteNews(news: News) {
        newsDao.deleteNews(news)
    }

    suspend fun getAllNews(): List<News> {
        return newsDao.getAllNews()
    }

    suspend fun getNewsById(id: Int): News? {
        return newsDao.getNewsById(id)
    }
}