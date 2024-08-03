package com.mustk.newsapp.di

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.navigation.NavOptions
import androidx.room.Room
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mustk.newsapp.data.datasource.NewsDataSource
import com.mustk.newsapp.data.service.NewsService
import com.mustk.newsapp.domain.NewsRepository
import com.mustk.newsapp.roomdb.NewsDao
import com.mustk.newsapp.roomdb.NewsDatabase
import com.mustk.newsapp.shared.Constant.APIKEY_QUERY_PARAM
import com.mustk.newsapp.shared.Constant.BASE_URL
import com.mustk.newsapp.shared.Constant.SQLITE_DATABASE_NAME
import com.mustk.newsapp.ui.adapter.SliderAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideApplicationInfo(@ApplicationContext context: Context): ApplicationInfo {
        return context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
    }

    @Singleton
    @Provides
    fun provideTokenInterceptor(applicationInfo: ApplicationInfo): Interceptor {
        val apikey = applicationInfo.metaData["apikeyValue"].toString()
        return Interceptor { chain ->
            var original = chain.request()
            val url = original.url().newBuilder()
                .addQueryParameter(APIKEY_QUERY_PARAM, apikey)
                .build()
            original = original.newBuilder()
                .url(url)
                .build()
            chain.proceed(original)
        }
    }

    @Singleton
    @Provides
    fun provideGoogleSignInClient(
        @ApplicationContext context: Context,
        applicationInfo: ApplicationInfo
    ): GoogleSignInClient {
        val clientId = applicationInfo.metaData["clientIdValue"].toString()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(tokenInterceptor: Interceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(tokenInterceptor)
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Singleton
    @Provides
    fun provideFirebaseDatabase(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Singleton
    @Provides
    fun provideNewsService(retrofit: Retrofit): NewsService {
        return retrofit.create(NewsService::class.java)
    }

    @Provides
    fun provideNewsDataSource(newsService: NewsService, newsDao: NewsDao): NewsDataSource {
        return NewsRepository(newsService, newsDao)
    }

    @Provides
    @Singleton
    fun provideRoomDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context, NewsDatabase::class.java, SQLITE_DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun provideDao(database: NewsDatabase) = database.newsDao()

    @Provides
    fun provideNavOptions(@ApplicationContext context: Context): NavOptions.Builder {
        return NavOptions.Builder()
    }

    @Singleton
    @Provides
    fun provideAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }
}

