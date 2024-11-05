package com.mustk.newsapp.data.datasource

import com.mustk.newsapp.data.model.BaseResponse
import com.mustk.newsapp.data.model.News
import com.mustk.newsapp.util.Resource
import retrofit2.Response

interface NewsDataSource {

    suspend fun fetchNewsListByCategory(
        language: String,
        category: String
    ): Response<BaseResponse>

    suspend fun fetchHeadlineNews(
        language: String,
    ): Response<BaseResponse>

    suspend fun searchNews(
        search : String
    ) : Response<BaseResponse>

    suspend fun fetchNewsDetail(
        uuid: String
    ): Resource<News>

    suspend fun fetchSimilarNews(
        language: String,
        search: String,
        category: String
    ): Resource<BaseResponse>

    suspend fun saveNewsToRoom(news: News)

    suspend fun saveNewsListToRoom(newsList : List<News>)

    suspend fun deleteNewsFromRoom(news: News)

    suspend fun deleteNewsListByUserFromRoom(user: String)

    suspend fun fetchNewsListByUserFromRoom(user: String): List<News>

    suspend fun fetchNewsByUUIDAndUserFromRoom(uuid: String, user : String): News
}