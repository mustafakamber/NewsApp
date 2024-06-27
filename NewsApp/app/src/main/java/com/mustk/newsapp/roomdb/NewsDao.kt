package com.mustk.newsapp.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.mustk.newsapp.data.model.News
import com.mustk.newsapp.shared.Constant.NEWS_COLLECTION
import com.mustk.newsapp.shared.Constant.USER_FIELD
import com.mustk.newsapp.shared.Constant.UUID_FIELD

@Dao
interface NewsDao {
    @Insert
    suspend fun insertNews(news: News)

    @Insert
    suspend fun insertNewsList(newsList: List<News>)

    @Delete
    suspend fun deleteNews(news: News)

    @Delete
    suspend fun deleteAllNews(newsList: List<News>)

    @Query("SELECT * FROM $NEWS_COLLECTION WHERE $USER_FIELD = :userEmail")
    suspend fun fetchNewsList(userEmail: String): List<News>

    @Query("SELECT * FROM $NEWS_COLLECTION WHERE $UUID_FIELD = :newsUUID AND $USER_FIELD = :userEmail")
    suspend fun fetchNewsByUUID(newsUUID: String, userEmail: String): News
}