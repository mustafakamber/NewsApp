package com.mustk.newsapp.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.mustk.newsapp.data.model.News
import com.mustk.newsapp.util.Constant.NEWS_COLLECTION
import com.mustk.newsapp.util.Constant.USER_FIELD
import com.mustk.newsapp.util.Constant.UUID_FIELD

@Dao
interface NewsDao {
    @Insert
    suspend fun insertNews(news: News)

    @Insert
    suspend fun insertNewsList(newsList: List<News>)

    @Delete
    suspend fun deleteNews(news: News)

    @Query("SELECT * FROM $NEWS_COLLECTION WHERE $USER_FIELD = :userEmail")
    suspend fun fetchNewsListByUser(userEmail: String): List<News>

    @Query("DELETE FROM $NEWS_COLLECTION WHERE $USER_FIELD = :userEmail")
    suspend fun deleteNewsListByUser(userEmail: String)

    @Query("SELECT * FROM $NEWS_COLLECTION WHERE $UUID_FIELD = :newsUUID AND $USER_FIELD = :userEmail")
    suspend fun fetchNewsByUUIDAndUser(newsUUID: String, userEmail: String): News
}