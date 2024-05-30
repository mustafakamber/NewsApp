package com.mustk.newsapp.data.model

import com.google.gson.annotations.SerializedName

data class Meta(
    @SerializedName("found") val found: Int?,
    @SerializedName("returned") val returned: Int?,
    @SerializedName("limit") val limit: Int?,
    @SerializedName("page") val page: Int?
)
