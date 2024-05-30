package com.mustk.newsapp.data.model

import com.google.gson.annotations.SerializedName

data class News(
    @SerializedName("uuid") val uuid : String?,
    @SerializedName("title") val title : String?,
    @SerializedName("description") val description : String?,
    @SerializedName("image_url") val imageUrl : String?,
    @SerializedName("url") val  newsUrl : String?,
    @SerializedName("published_at") val publishedTime : String?,
    @SerializedName("source") val source : String?,
    @SerializedName("categories") val category: List<String>?,
    @SerializedName("snippet") val snippet: String?,
)
