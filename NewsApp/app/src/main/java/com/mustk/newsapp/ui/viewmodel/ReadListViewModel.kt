package com.mustk.newsapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mustk.newsapp.R
import com.mustk.newsapp.data.datasource.NewsDataSource
import com.mustk.newsapp.data.model.News
import com.mustk.newsapp.util.Constant.CATEGORIES_FIELD
import com.mustk.newsapp.util.Constant.DESCRIPTION_FIELD
import com.mustk.newsapp.util.Constant.IMAGE_URL_FIELD
import com.mustk.newsapp.util.Constant.LANGUAGE_FIELD
import com.mustk.newsapp.util.Constant.NEWS_COLLECTION
import com.mustk.newsapp.util.Constant.NEWS_URL_FIELD
import com.mustk.newsapp.util.Constant.NULL_JSON
import com.mustk.newsapp.util.Constant.PUBLISHED_AT_FIELD
import com.mustk.newsapp.util.Constant.SNIPPET_FIELD
import com.mustk.newsapp.util.Constant.SOURCE_FIELD
import com.mustk.newsapp.util.Constant.TITLE_FIELD
import com.mustk.newsapp.util.Constant.USER_FIELD
import com.mustk.newsapp.util.Constant.UUID_FIELD
import com.mustk.newsapp.util.Event
import com.mustk.newsapp.util.NetworkHelper
import com.mustk.newsapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadListViewModel @Inject constructor(
    private val repository: NewsDataSource,
    private val auth: FirebaseAuth,
    private val database: FirebaseFirestore,
    private val networkHelper: NetworkHelper,
) : ViewModel() {

    private val _readListNews = MutableLiveData<Resource<List<News>>>()
    val readListNews: LiveData<Resource<List<News>>>
        get() = _readListNews

    private val _checkConnection = MutableLiveData<Event<Boolean>>()
    val checkConnection: LiveData<Event<Boolean>>
        get() = _checkConnection

    private val _snackBarMessage = MutableLiveData<Event<Int>>()
    val snackBarMessage: LiveData<Event<Int>>
        get() = _snackBarMessage

    private val _toastError = MutableLiveData<String>()
    val toastError: LiveData<String>
        get() = _toastError

    val isConnectedNetwork: Boolean
        get() = networkHelper.isNetworkConnected()

    init {
        refreshScreen()
    }

    fun refreshScreen() {
        if (isConnectedNetwork) {
            fetchNewsListFromCloud()
        } else {
            fetchNewsListFromLocal()
        }
    }

    fun deleteNews(news: News) {
        if (isConnectedNetwork) {
            deleteNewsFromLocal(news)
            deleteNewsFromCloud(news)
        } else {
            _checkConnection.value = Event(true)
            refreshScreen()
        }
    }

    private fun deleteNewsFromLocal(news: News) {
        try {
            viewModelScope.launch {
                repository.deleteNewsFromRoom(news)
            }
        } catch (e: Exception) {
            _toastError.postValue(e.localizedMessage)
        }
    }

    fun deleteAllNews() {
        if (isConnectedNetwork) {
            deleteAllNewsFromLocal()
            deleteAllNewsFromCloud()
        } else {
            _checkConnection.value = Event(true)
        }
    }

    private fun deleteAllNewsFromLocal() {
        _readListNews.value?.data?.forEach { newsItem ->
            newsItem.user?.let { user ->
                try {
                    viewModelScope.launch {
                        repository.deleteNewsListByUserFromRoom(user)
                    }
                } catch (e: Exception) {
                    _toastError.postValue(e.localizedMessage)
                }
            }
        }
    }

    private fun deleteAllNewsFromCloud() {
        _readListNews.value?.data?.let {
            val currentUser = auth.currentUser
            if (currentUser?.email != null) {
                database.collection(NEWS_COLLECTION)
                    .whereEqualTo(USER_FIELD, currentUser.email)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val documents = querySnapshot.documents
                        if (documents.isNotEmpty()) {
                            val totalDocuments = documents.size
                            var deletedCount = 0
                            for (document in documents) {
                                document.reference.delete()
                                    .addOnSuccessListener {
                                        deletedCount++
                                        if (deletedCount == totalDocuments) {
                                            _snackBarMessage.value = Event(R.string.all_deleted)
                                            refreshScreen()
                                        }
                                    }
                                    .addOnFailureListener { error ->
                                        error.localizedMessage?.let {
                                            _readListNews.value = Resource.error(it, null)
                                        }
                                    }
                            }
                        }
                    }.addOnFailureListener { error ->
                        error.localizedMessage?.let {
                            _readListNews.value = Resource.error(it, null)
                        }
                    }
            }
        }
    }

    private fun deleteNewsFromCloud(news: News) {
        val currentUser = auth.currentUser
        val newsUUID = news.uuid
        if (currentUser?.email != null) {
            database.collection(NEWS_COLLECTION)
                .whereEqualTo(UUID_FIELD, newsUUID)
                .whereEqualTo(USER_FIELD, currentUser.email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        document.reference.delete()
                            .addOnSuccessListener {
                                _snackBarMessage.value = Event(R.string.deleted)
                                refreshScreen()
                            }
                            .addOnFailureListener { error ->
                                error.localizedMessage?.let {
                                    _readListNews.value = Resource.error(it, null)
                                }
                            }
                    }
                }
                .addOnFailureListener { error ->
                    error.localizedMessage?.let {
                        _readListNews.value = Resource.error(it, null)
                    }
                }
        }
    }

    private fun fetchNewsListFromCloud() {
        _readListNews.value = Resource.loading(null)
        val currentUser = auth.currentUser
        val email = currentUser?.email
        if (email != null) {
            database.collection(NEWS_COLLECTION)
                .whereEqualTo(USER_FIELD, email)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val newsListFromCloud = ArrayList<News>()
                        for (document in documents) {
                            val newsData = document.data
                            val newsUser = newsData[USER_FIELD] as String
                            val newsUUID = newsData[UUID_FIELD] as String
                            val newsTitle = newsData[TITLE_FIELD] as String
                            val newsDescription = newsData[DESCRIPTION_FIELD] as String
                            val newsImageUrl = newsData[IMAGE_URL_FIELD] as String
                            val newsUrl = newsData[NEWS_URL_FIELD] as String
                            val newsPublishedAt = newsData[PUBLISHED_AT_FIELD] as String
                            val newsSource = newsData[SOURCE_FIELD] as String
                            val newsCategories = newsData[CATEGORIES_FIELD] as List<String>
                            val newsSnippet = newsData[SNIPPET_FIELD] as String
                            val newsLanguage = newsData[LANGUAGE_FIELD] as String
                            val news = News(
                                null, newsUUID, newsUser,
                                newsTitle, newsDescription, newsImageUrl,
                                newsUrl, newsPublishedAt, newsSource,
                                newsCategories, newsSnippet, newsLanguage
                            )
                            newsListFromCloud.add(news)
                        }
                        _snackBarMessage.value = Event(R.string.fetch_readlist_cloud)
                        _readListNews.value = Resource.success(newsListFromCloud)
                    } else {
                        _readListNews.value = Resource.error(NULL_JSON, null)
                    }
                }
                .addOnFailureListener { exception ->
                    exception.localizedMessage?.let {
                        _readListNews.value = Resource.error(it, null)
                    }
                }
        }
    }

    private fun fetchNewsListFromLocal() {
        val currentUser = auth.currentUser
        val email = currentUser?.email
        if (email != null) {
            viewModelScope.launch {
                _readListNews.postValue(Resource.loading(null))
                try {
                    val newsData = repository.fetchNewsListByUserFromRoom(email)
                    if (newsData.isEmpty()) {
                        _readListNews.postValue(Resource.error(NULL_JSON, null))
                    } else {
                        _snackBarMessage.value = Event(R.string.fetch_readlist_local)
                        _readListNews.postValue(Resource.success(newsData))
                    }
                } catch (e: Exception) {
                    _readListNews.postValue(Resource.error(e.localizedMessage, null))
                }
            }
        }
    }
}