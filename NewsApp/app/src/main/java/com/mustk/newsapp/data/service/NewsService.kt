package com.mustk.newsapp.data.service

import com.mustk.newsapp.data.model.BaseResponse
import com.mustk.newsapp.shared.Constant.ALL_QUERY_PARAM
import com.mustk.newsapp.shared.Constant.CATEGORIES_QUERY_PARAM
import com.mustk.newsapp.shared.Constant.LANGUAGE_QUERY_PARAM
import com.mustk.newsapp.shared.Constant.PAGE_QUERY_PARAM
import com.mustk.newsapp.shared.Constant.SEARCH_QUERY_PARAM
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

interface NewsService {
    @GET(ALL_QUERY_PARAM)
    suspend fun fetchNewsDataForCategories(
        @Query(LANGUAGE_QUERY_PARAM) language: String,
        @Query(CATEGORIES_QUERY_PARAM) category: String
    ): Response<BaseResponse>
    @GET(ALL_QUERY_PARAM)
    suspend fun fetchNewsDataForHeadline(
        @Query(LANGUAGE_QUERY_PARAM) language: String,
        @Query(PAGE_QUERY_PARAM) page : Int,
    ) : Response<BaseResponse>
    @GET(ALL_QUERY_PARAM)
    suspend fun fetchNewsDataForSearch(
        @Query(LANGUAGE_QUERY_PARAM) language: String,
        @Query(SEARCH_QUERY_PARAM) search : String,
    ) : Response<BaseResponse>
}