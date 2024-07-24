package com.mustk.newsapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mustk.newsapp.R
import com.mustk.newsapp.data.datasource.NewsDataSource
import com.mustk.newsapp.data.model.News
import com.mustk.newsapp.shared.Constant.CATEGORIES_FIELD
import com.mustk.newsapp.shared.Constant.DESCRIPTION_FIELD
import com.mustk.newsapp.shared.Constant.IMAGE_URL_FIELD
import com.mustk.newsapp.shared.Constant.LANGUAGE
import com.mustk.newsapp.shared.Constant.NEWS_COLLECTION
import com.mustk.newsapp.shared.Constant.NEWS_URL_FIELD
import com.mustk.newsapp.shared.Constant.PUBLISHED_AT_FIELD
import com.mustk.newsapp.shared.Constant.SNIPPET_FIELD
import com.mustk.newsapp.shared.Constant.SOURCE_FIELD
import com.mustk.newsapp.shared.Constant.TITLE_FIELD
import com.mustk.newsapp.shared.Constant.TITLE_FIRST_WORD_CHAR
import com.mustk.newsapp.shared.Constant.USER_FIELD
import com.mustk.newsapp.shared.Constant.UUID_FIELD
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: NewsDataSource,
    private val auth: FirebaseAuth,
    private val database: FirebaseFirestore
) : BaseViewModel() {

    private val _detailLoading = MutableLiveData<Boolean>()
    val detailLoading: LiveData<Boolean>
        get() = _detailLoading

    private val _detailNews = MutableLiveData<News>()
    val detailNews: LiveData<News>
        get() = _detailNews

    private val _detailItems = MutableLiveData<Boolean>()
    val detailItems: LiveData<Boolean>
        get() = _detailItems

    private val _similarLoading = MutableLiveData<Boolean>()
    val similarLoading: LiveData<Boolean>
        get() = _similarLoading

    private val _similarNews = MutableLiveData<List<News>>()
    val similarNews: LiveData<List<News>>
        get() = _similarNews

    private val _saveButtonVisibility = MutableLiveData<Boolean>()
    val saveButtonVisibility: LiveData<Boolean>
        get() = _saveButtonVisibility

    private val _deleteButtonVisibility = MutableLiveData<Boolean>()
    val deleteButtonVisibility: LiveData<Boolean>
        get() = _deleteButtonVisibility

    init {
        initState()
    }

    private fun initState() {
        setDetailItemsVisibility(false)
        setSaveButtonVisibility(false)
        setDeleteButtonVisibility(false)
        setDetailLoadingVisibility(true)
        setSimilarLoadingVisibility(true)
    }

    private fun savedNewsState() {
        setSaveButtonVisibility(false)
        setDeleteButtonVisibility(true)
    }

    private fun unsavedNewsState() {
        setSaveButtonVisibility(true)
        setDeleteButtonVisibility(false)
    }

    fun saveNews() {
        saveLocalDatabase()
        saveCloudDatabase()
    }

    fun deleteNews() {
        deleteLocalDatabase()
        deleteCloudDatabase()
    }

    fun fetchDetailNews(uuid: String, boolean: Boolean) {
        if (boolean) fetchDetailNewsFromSQLite(uuid) else fetchDetailNewsFromAPI(uuid)
    }

    private fun setSaveButtonVisibility(boolean: Boolean) {
        _saveButtonVisibility.value = boolean
    }

    private fun setDeleteButtonVisibility(boolean: Boolean) {
        _deleteButtonVisibility.value = boolean
    }

    private fun setDetailLoadingVisibility(boolean: Boolean) {
        _detailLoading.value = boolean
    }

    private fun setSimilarLoadingVisibility(boolean: Boolean) {
        _similarLoading.value = boolean
    }

    private fun setDetailItemsVisibility(boolean: Boolean) {
        _detailItems.value = boolean
    }

    private fun setDetailNews(news: News) {
        _detailNews.value = news
        news.getFormattedCategories()?.let {
            fetchSimilarNewsFromAPI(news.title, it.categories)
        }
    }

    private fun setSimilarNews(newsList: List<News>) {
        _similarNews.value = newsList
    }

    private fun detailResultState(uuid: String, news: News) {
        setDetailItemsVisibility(true)
        setDetailLoadingVisibility(false)
        checkNewsExistInCloudDatabase(uuid)
        setDetailNews(news)
    }

    private fun checkNewsExistInCloudDatabase(newsUUID: String?) {
        val currentUser = auth.currentUser
        if (newsUUID != null && currentUser?.email != null) {
            database.collection(NEWS_COLLECTION)
                .whereEqualTo(UUID_FIELD, newsUUID)
                .whereEqualTo(USER_FIELD, currentUser.email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        unsavedNewsState()
                    } else {
                        savedNewsState()
                    }
                }.addOnFailureListener { error ->
                    error.localizedMessage?.let {
                        showToastMessage(it)
                    }
                }
        }
    }

    private fun saveCloudDatabase() {
        _detailNews.value?.let { news ->
            val currentUser = auth.currentUser ?: return@let
            val email = currentUser.email ?: return@let
            with(news) {
                val requiredFields = listOf(uuid, title, description,
                    imageUrl, newsUrl, publishedAt,
                    source, category, snippet)
                if (requiredFields.any { it == null }) return@let
                else {
                    val newsMap = hashMapOf(
                        USER_FIELD to email,
                        UUID_FIELD to uuid,
                        TITLE_FIELD to title,
                        DESCRIPTION_FIELD to description,
                        IMAGE_URL_FIELD to imageUrl,
                        NEWS_URL_FIELD to newsUrl,
                        PUBLISHED_AT_FIELD to publishedAt,
                        SOURCE_FIELD to source,
                        CATEGORIES_FIELD to category,
                        SNIPPET_FIELD to snippet
                    )
                    database.collection(NEWS_COLLECTION).add(newsMap)
                        .addOnFailureListener { error ->
                            error.localizedMessage?.let {
                                showToastMessage(it)
                            }
                        }
                }
            }
        }
    }

    private fun saveLocalDatabase() {
        _detailNews.value?.let { news ->
            val currentUser = auth.currentUser ?: return@let
            val email = currentUser.email ?: return@let
            val updatedNews = news.updateUserModel(news, email)
            viewModelScope.launch {
                repository.saveNewsData(updatedNews)
                showSnackBarMessage(R.string.saved)
                savedNewsState()
            }
        }
    }

    private fun deleteCloudDatabase() {
        val currentUser = auth.currentUser
        val newsUUID = _detailNews.value?.uuid
        if (currentUser?.email != null && newsUUID != null) {
            database.collection(NEWS_COLLECTION)
                .whereEqualTo(UUID_FIELD, newsUUID)
                .whereEqualTo(USER_FIELD, currentUser.email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        document.reference.delete()
                            .addOnFailureListener { error ->
                                error.localizedMessage?.let {
                                    showToastMessage(it)
                                }
                            }
                    }
                }
                .addOnFailureListener { error ->
                    error.localizedMessage?.let {
                        showToastMessage(it)
                    }
                }
        }
    }

    private fun deleteLocalDatabase() = viewModelScope.launch {
        _detailNews.value?.let { news ->
            repository.deleteNewsData(news)
            showSnackBarMessage(R.string.deleted)
            unsavedNewsState()
        }
    }

    private fun fetchDetailNewsFromAPI(uuid: String) {
        safeRequest(
            response = { repository.fetchNewsDataDetail(uuid) },
            successStatusData = { newsDetail ->
                detailResultState(uuid, newsDetail)
                showSnackBarMessage(R.string.fetch_from_api)
            })
    }

    private fun fetchDetailNewsFromSQLite(uuid: String) {
        val currentUser = auth.currentUser
        val email = currentUser?.email
        if (email != null) {
            viewModelScope.launch {
                val newsData = repository.fetchNewsDataByUUID(uuid, email)
                detailResultState(uuid, newsData)
                showSnackBarMessage(R.string.fetch_from_sqlite)
            }
        }
    }

    private fun fetchSimilarNewsFromAPI(title: String?, categories: String?) {
        if (title != null && categories != null) {
            val searchTitle = title.replaceFirstChar { it.lowercase() }.substringBefore(
                TITLE_FIRST_WORD_CHAR
            )
            val searchCategories = categories.lowercase()
            safeRequest(
                response = {
                    repository.fetchNewsDataForSimilar(
                        LANGUAGE,
                        searchTitle,
                        searchCategories
                    )
                },
                successStatusData = { similarNews ->
                    setSimilarLoadingVisibility(false)
                    setSimilarNews(similarNews.data)
                }
            )
        }
    }
}