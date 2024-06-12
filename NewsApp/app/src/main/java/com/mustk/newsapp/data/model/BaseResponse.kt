package com.mustk.newsapp.data.model

import com.google.gson.annotations.SerializedName
import com.mustk.newsapp.shared.Constant.DATA_FIELD
import com.mustk.newsapp.shared.Constant.ERROR_FIELD
import com.mustk.newsapp.shared.Constant.META_FIELD

data class BaseResponse(
    @SerializedName(META_FIELD) val meta: Meta,
    @SerializedName(DATA_FIELD) val data: List<News>,
    @SerializedName(ERROR_FIELD) val error: Error,
)
