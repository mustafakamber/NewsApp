package com.mustk.newsapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.mustk.newsapp.shared.Constant.CATEGORIES_FIELD
import com.mustk.newsapp.shared.Constant.CATEGORY_BRACKET
import com.mustk.newsapp.shared.Constant.DATE_BRACKET
import com.mustk.newsapp.shared.Constant.DESCRIPTION_FIELD
import com.mustk.newsapp.shared.Constant.IMAGE_URL_FIELD
import com.mustk.newsapp.shared.Constant.LANGUAGE_FIELD
import com.mustk.newsapp.shared.Constant.NEWS_COLLECTION
import com.mustk.newsapp.shared.Constant.NEWS_URL_FIELD
import com.mustk.newsapp.shared.Constant.PUBLISHED_AT_DATE_MAX
import com.mustk.newsapp.shared.Constant.PUBLISHED_AT_DATE_MIN
import com.mustk.newsapp.shared.Constant.PUBLISHED_AT_FIELD
import com.mustk.newsapp.shared.Constant.PUBLISHED_AT_TIME_MAX
import com.mustk.newsapp.shared.Constant.PUBLISHED_AT_TIME_MIN
import com.mustk.newsapp.shared.Constant.SHORT_SOURCE_MAX_SIZE
import com.mustk.newsapp.shared.Constant.SNIPPET_FIELD
import com.mustk.newsapp.shared.Constant.SOURCE_FIELD
import com.mustk.newsapp.shared.Constant.TITLE_FIELD
import com.mustk.newsapp.shared.Constant.UUID_FIELD
import com.mustk.newsapp.util.capitalized
import com.mustk.newsapp.util.truncateString

@Entity(NEWS_COLLECTION)
data class News(
    @PrimaryKey(autoGenerate = true) val id : Int? = null,
    @SerializedName(UUID_FIELD) val uuid : String,
    var user: String? = null,
    @SerializedName(TITLE_FIELD) val title : String?,
    @SerializedName(DESCRIPTION_FIELD) val description : String?,
    @SerializedName(IMAGE_URL_FIELD) val imageUrl : String?,
    @SerializedName(NEWS_URL_FIELD) val  newsUrl : String?,
    @SerializedName(PUBLISHED_AT_FIELD) val publishedAt : String?,
    @SerializedName(SOURCE_FIELD) val source : String?,
    @SerializedName(CATEGORIES_FIELD) val category: List<String>?,
    @SerializedName(SNIPPET_FIELD) val snippet: String?,
    @SerializedName(LANGUAGE_FIELD) val language : String?,
){

    fun getShortSource() : ShortSource? {
        return source?.let {
            val formattedSource = it.truncateString(SHORT_SOURCE_MAX_SIZE)
            ShortSource(formattedSource)
        }
    }

    fun getPublishedDateAndTime(): PublishedDateTime? {
        return publishedAt?.let {
            val date: String = it.substring(PUBLISHED_AT_DATE_MIN, PUBLISHED_AT_DATE_MAX)
            val time: String = it.substring(PUBLISHED_AT_TIME_MIN, PUBLISHED_AT_TIME_MAX)
            val parts = date.split(DATE_BRACKET)
            val formattedDate = parts[2] + DATE_BRACKET + parts[1] + DATE_BRACKET + parts[0]
            PublishedDateTime(formattedDate, time)
        }
    }

    fun getFormattedCategories(): FormattedCategories? {
        return category?.let {
            val formattedCategories: String = it.joinToString(CATEGORY_BRACKET) { categoryItem ->
                categoryItem.capitalized()
            }
            FormattedCategories(formattedCategories)
        }
    }

    fun updateUserModel(news: News, user : String): News {
        news.user = user
        return news
    }
}