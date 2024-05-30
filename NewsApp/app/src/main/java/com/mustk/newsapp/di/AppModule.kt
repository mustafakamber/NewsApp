package com.mustk.newsapp.di

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mustk.newsapp.data.datasource.NewsDataSource
import com.mustk.newsapp.data.service.NewsService
import com.mustk.newsapp.domain.NewsRepository
import com.mustk.newsapp.shared.Constant.APIKEY_QUERY_PARAM
import com.mustk.newsapp.shared.Constant.API_KEY
import com.mustk.newsapp.shared.Constant.BASE_URL
import com.mustk.newsapp.shared.Constant.WEB_CLIENT_ID
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
    fun provideTokenInterceptor(): Interceptor {
        return Interceptor { chain ->
            var original = chain.request()
            val url = original.url().newBuilder()
                .addQueryParameter(APIKEY_QUERY_PARAM, API_KEY)
                .build()
            original = original.newBuilder()
                .url(url)
                .build()
            chain.proceed(original)
        }
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
    fun provideNewsService(retrofit: Retrofit): NewsService {
        return retrofit.create(NewsService::class.java)
    }

    @Provides
    fun provideNewsDataSource(newsService : NewsService): NewsDataSource {
        return NewsRepository(newsService)
    }

    /*
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
     */

    @Singleton
    @Provides
    fun provideGoogleSignInClient(@ApplicationContext context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }
}

