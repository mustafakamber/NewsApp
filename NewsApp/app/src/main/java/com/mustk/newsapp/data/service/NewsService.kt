package com.mustk.newsapp.data.service

import com.mustk.newsapp.data.model.BaseResponse
import com.mustk.newsapp.data.model.News
import com.mustk.newsapp.shared.Constant.ALL_QUERY_PARAM
import com.mustk.newsapp.shared.Constant.CATEGORIES_QUERY_PARAM
import com.mustk.newsapp.shared.Constant.LANGUAGE_QUERY_PARAM
import com.mustk.newsapp.shared.Constant.SEARCH_QUERY_PARAM
import com.mustk.newsapp.shared.Constant.UUID_FIELD
import com.mustk.newsapp.shared.Constant.UUID_QUERY_PARAM
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NewsService {

    @GET(ALL_QUERY_PARAM)
    suspend fun fetchNewsDataForCategories(
        @Query(LANGUAGE_QUERY_PARAM) language: String,
        @Query(CATEGORIES_QUERY_PARAM) category: String
    ): Response<BaseResponse>

    @GET(ALL_QUERY_PARAM)
    suspend fun fetchNewsDataForHeadline(
        @Query(LANGUAGE_QUERY_PARAM) language: String
    ) : Response<BaseResponse>

    @GET(ALL_QUERY_PARAM)
    suspend fun fetchNewsDataForSearch(
        @Query(LANGUAGE_QUERY_PARAM) language: String,
        @Query(SEARCH_QUERY_PARAM) search : String,
    ) : Response<BaseResponse>

    @GET("$UUID_QUERY_PARAM{uuid}")
    suspend fun fetchNewsDataForDetail(
        @Path(UUID_FIELD) uuid: String
    ): Response<News>

    @GET(ALL_QUERY_PARAM)
    suspend fun fetchNewsDataForSimilar(
        @Query(LANGUAGE_QUERY_PARAM) language: String,
        @Query(SEARCH_QUERY_PARAM) search : String,
        @Query(CATEGORIES_QUERY_PARAM) category: String
    ) : Response<BaseResponse>
}