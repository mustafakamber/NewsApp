package com.mustk.newsapp.data.model

import com.google.gson.annotations.SerializedName
import com.mustk.newsapp.util.Constant.CODE_FIELD
import com.mustk.newsapp.util.Constant.MESSAGE_FIELD

data class Error(
    @SerializedName(CODE_FIELD) val code : String?,
    @SerializedName(MESSAGE_FIELD) val message : String?,
)
