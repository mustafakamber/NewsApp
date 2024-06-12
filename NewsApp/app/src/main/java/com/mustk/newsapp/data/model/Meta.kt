package com.mustk.newsapp.data.model

import com.google.gson.annotations.SerializedName
import com.mustk.newsapp.shared.Constant.FOUND_FIELD
import com.mustk.newsapp.shared.Constant.LIMIT_FIELD
import com.mustk.newsapp.shared.Constant.PAGE_FIELD
import com.mustk.newsapp.shared.Constant.RETURNED_FIELD

data class Meta(
    @SerializedName(FOUND_FIELD) val found: Int?,
    @SerializedName(RETURNED_FIELD) val returned: Int?,
    @SerializedName(LIMIT_FIELD) val limit: Int?,
    @SerializedName(PAGE_FIELD) val page: Int?
)
