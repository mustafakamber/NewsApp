package com.mustk.newsapp.domain

import com.mustk.newsapp.data.datasource.NewsDataSource
import com.mustk.newsapp.data.model.BaseResponse
import com.mustk.newsapp.data.model.News
import com.mustk.newsapp.data.service.NewsService
import com.mustk.newsapp.roomdb.NewsDao
import com.mustk.newsapp.shared.Constant.NULL_JSON
import com.mustk.newsapp.shared.Resource
import retrofit2.Response
import javax.inject.Inject

class NewsRepository @Inject constructor(
    private val newsService: NewsService,
    private val newsDao: NewsDao
) : NewsDataSource {

    private suspend fun <T> performApiCall(apiCall: suspend () -> Response<T>): Resource<T> {
        return try {
            val response = apiCall.invoke()
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.success(it)
                } ?: Resource.error(NULL_JSON, null)
            } else {
                Resource.error(response.errorBody().toString(), null)
            }
        } catch (e: Exception) {
            Resource.error(e.localizedMessage, null)
        }
    }

    override suspend fun fetchNewsListByCategory(
        language: String,
        category: String
    ): Resource<BaseResponse> {
        return performApiCall { newsService.fetchNewsDataForCategories(language, category) }
    }

    override suspend fun fetchHeadlineNews(
        language: String
    ): Resource<BaseResponse> {
        return performApiCall { newsService.fetchNewsDataForHeadline(language) }
    }

    override suspend fun searchNews(
        search: String
    ): Resource<BaseResponse> {
        return performApiCall { newsService.fetchNewsDataForSearch(search) }
    }

    override suspend fun fetchNewsDetail(uuid: String): Resource<News> {
        return performApiCall { newsService.fetchNewsDataForDetail(uuid) }
    }

    override suspend fun fetchSimilarNews(
        language: String,
        search: String,
        category: String
    ): Resource<BaseResponse> {
        return performApiCall { newsService.fetchNewsDataForSimilar(language, search, category) }
    }

    override suspend fun saveNewsToRoom(news: News) {
        newsDao.insertNews(news)
    }

    override suspend fun saveNewsListToRoom(newsList: List<News>) {
        newsDao.insertNewsList(newsList)
    }

    override suspend fun deleteNewsFromRoom(news: News) {
        newsDao.deleteNews(news)
    }

    override suspend fun deleteNewsListByUserFromRoom(user: String) {
        newsDao.deleteNewsListByUser(user)
    }

    override suspend fun fetchNewsListByUserFromRoom(user: String): List<News> {
        return newsDao.fetchNewsListByUser(user)
    }

    override suspend fun fetchNewsByUUIDAndUserFromRoom(uuid: String, user: String): News {
        return newsDao.fetchNewsByUUIDAndUser(uuid, user)
    }
}