package com.mustk.newsapp.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mustk.newsapp.data.model.News
import com.mustk.newsapp.util.Constant.DATABASE_VERSION
import com.mustk.newsapp.util.CategoryConverter

@Database(entities = [News::class], version = DATABASE_VERSION)
@TypeConverters(CategoryConverter::class)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun newsDao() : NewsDao
}