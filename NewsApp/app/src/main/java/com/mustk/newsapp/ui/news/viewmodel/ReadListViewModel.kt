package com.mustk.newsapp.ui.news.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mustk.newsapp.R
import com.mustk.newsapp.data.datasource.NewsDataSource
import com.mustk.newsapp.data.model.News
import com.mustk.newsapp.shared.Constant.NEWS_COLLECTION
import com.mustk.newsapp.shared.Constant.USER_FIELD
import com.mustk.newsapp.shared.Constant.UUID_FIELD
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadListViewModel @Inject constructor(
    private val repository: NewsDataSource,
    private val auth: FirebaseAuth,
    private val database: FirebaseFirestore
) : BaseViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _readListNews = MutableLiveData<List<News>>()
    val readListNews: LiveData<List<News>>
        get() = _readListNews

    private val _recyclerViewVisibility = MutableLiveData<Boolean>()
    val recyclerViewVisibility: LiveData<Boolean>
        get() = _recyclerViewVisibility

    private val _emptyMessage = MutableLiveData<Boolean>()
    val emptyMessage: LiveData<Boolean>
        get() = _emptyMessage

    private val _deleteButton = MutableLiveData<Boolean>()
    val deleteButton: LiveData<Boolean>
        get() = _deleteButton

    fun refreshReadListData() {
        initState()
        fetchNewsFromLocal()
    }

    private fun setReadListEmptyMessageVisibility(boolean: Boolean) {
        _emptyMessage.value = boolean
    }

    private fun setDeleteButtonVisibility(boolean: Boolean) {
        _deleteButton.value = boolean
    }

    private fun initState() {
        setDeleteButtonVisibility(false)
        setReadListEmptyMessageVisibility(false)
        setReadListLoading(false)
        setRecyclerViewVisibility(false)
    }

    private fun loadingState() {
        setDeleteButtonVisibility(false)
        setReadListEmptyMessageVisibility(false)
        setReadListLoading(true)
        setRecyclerViewVisibility(false)
    }

    private fun resultDataState() {
        setDeleteButtonVisibility(true)
        setReadListLoading(false)
        setRecyclerViewVisibility(true)
        setReadListEmptyMessageVisibility(false)
    }

    private fun resultNoDataState() {
        setDeleteButtonVisibility(false)
        setReadListLoading(false)
        setRecyclerViewVisibility(false)
        setReadListEmptyMessageVisibility(true)
    }

    private fun setReadListNews(newsList: List<News>) {
        if (newsList.isEmpty()) {
            resultNoDataState()
        } else {
            resultDataState()
            _readListNews.value = newsList
        }
    }

    private fun setReadListLoading(boolean: Boolean) {
        _loading.value = boolean
    }

    private fun setRecyclerViewVisibility(boolean: Boolean) {
        _recyclerViewVisibility.value = boolean
    }

    fun deleteNewsFromLocal(news: News) {
        viewModelScope.launch {
            repository.deleteNewsData(news)
            deleteNewsFromCloud(news)
        }
    }

    fun deleteAllReadListNews() {
        deleteAllNewsFromLocal()
        deleteAllNewsFromCloud()
    }

    private fun deleteAllNewsFromLocal() {
        _readListNews.value?.let { newsList ->
            viewModelScope.launch {
                repository.deleteAllNewsData(newsList)
            }
        }
    }

    private fun deleteAllNewsFromCloud() {
        _readListNews.value?.let {
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
                                            showSnackBarMessage(R.string.all_deleted)
                                            refreshReadListData()
                                        }
                                    }
                                    .addOnFailureListener { error ->
                                        error.localizedMessage?.let {
                                            showToastMessage(it)
                                        }
                                    }
                            }
                        }
                    }.addOnFailureListener { error ->
                        error.localizedMessage?.let {
                            showToastMessage(it)
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
                                showSnackBarMessage(R.string.deleted)
                                refreshReadListData()
                            }
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

    private fun fetchNewsFromLocal() {
        loadingState()
        val currentUser = auth.currentUser
        val email = currentUser?.email
        if (email != null) {
            viewModelScope.launch {
                val newsData = repository.fetchNewsDataLocal(email)
                setReadListNews(newsData)
            }
        }
    }
}