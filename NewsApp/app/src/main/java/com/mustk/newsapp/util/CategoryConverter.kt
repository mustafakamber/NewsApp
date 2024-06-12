package com.mustk.newsapp.util

import androidx.room.TypeConverter
import com.mustk.newsapp.shared.Constant.CATEGORY_CONVERT_CHAR

class CategoryConverter {
    @TypeConverter
    fun fromCategoryList(category: List<String>?): String? {
        return category?.joinToString(CATEGORY_CONVERT_CHAR)
    }

    @TypeConverter
    fun toCategoryList(data: String?): List<String>? {
        return data?.split(CATEGORY_CONVERT_CHAR)
    }
}