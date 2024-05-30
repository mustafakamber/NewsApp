package com.mustk.newsapp.data.datasource

import com.mustk.newsapp.data.model.BaseResponse
import com.mustk.newsapp.shared.Resource

interface NewsDataSource {
    suspend fun fetchNewsDataForCategories(
        language: String,
        category: String
    ): Resource<BaseResponse>
    suspend fun fetchNewsDataForHeadline(
        language: String,
        page : Int,
    ): Resource<BaseResponse>
    suspend fun fetchNewsDataForSearch(
        language: String,
        search : String
    ) : Resource<BaseResponse>
}