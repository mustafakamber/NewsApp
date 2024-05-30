package com.mustk.newsapp.data.model

import com.google.gson.annotations.SerializedName

data class BaseResponse(
    @SerializedName("meta") val meta: Meta,
    @SerializedName("data") val data: List<News>,
    @SerializedName("error") val error: Error,
)
