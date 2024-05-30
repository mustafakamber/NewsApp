package com.mustk.newsapp.domain

import com.mustk.newsapp.data.datasource.NewsDataSource
import com.mustk.newsapp.data.model.BaseResponse
import com.mustk.newsapp.data.service.NewsService
import com.mustk.newsapp.shared.Constant.NULL_JSON
import com.mustk.newsapp.shared.Resource
import retrofit2.Response
import javax.inject.Inject

class NewsRepository @Inject constructor(private val newsService: NewsService) : NewsDataSource {

    private suspend fun performApiCall(apiCall: suspend () -> Response<BaseResponse>): Resource<BaseResponse> {
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

    override suspend fun fetchNewsDataForCategories(
        language: String,
        category: String
    ): Resource<BaseResponse> {
        return performApiCall { newsService.fetchNewsDataForCategories(language, category) }
    }

    override suspend fun fetchNewsDataForHeadline(
        language: String,
        page: Int
    ): Resource<BaseResponse> {
        return performApiCall { newsService.fetchNewsDataForHeadline(language, page) }
    }

    override suspend fun fetchNewsDataForSearch(
        language: String,
        search: String
    ): Resource<BaseResponse> {
        return performApiCall { newsService.fetchNewsDataForSearch(language, search) }
    }
}