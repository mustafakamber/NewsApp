package com.mustk.newsapp.data.datasource

import com.mustk.newsapp.data.model.BaseResponse
import com.mustk.newsapp.data.model.News
import com.mustk.newsapp.shared.Resource

interface NewsDataSource {

    suspend fun fetchNewsDataForCategories(
        language: String,
        category: String
    ): Resource<BaseResponse>

    suspend fun fetchNewsDataForHeadline(
        language: String,
    ): Resource<BaseResponse>

    suspend fun fetchNewsDataForSearch(
        search : String
    ) : Resource<BaseResponse>

    suspend fun fetchNewsDataDetail(
        uuid: String
    ): Resource<News>

    suspend fun fetchNewsDataForSimilar(
        language: String,
        search: String,
        category: String
    ): Resource<BaseResponse>

    suspend fun saveNewsData(news: News)

    suspend fun saveNewsListData(newsList : List<News>)

    suspend fun deleteNewsData(news: News)

    suspend fun deleteNewsList(user: String)

    suspend fun fetchNewsDataLocal(user: String): List<News>

    suspend fun fetchNewsDataByUUID(uuid: String, user : String): News
}