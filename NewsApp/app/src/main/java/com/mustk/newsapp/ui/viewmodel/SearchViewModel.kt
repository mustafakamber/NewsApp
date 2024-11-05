package com.mustk.newsapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustk.newsapp.data.datasource.NewsDataSource
import com.mustk.newsapp.data.model.News
import com.mustk.newsapp.util.Constant.NO_CONNECTION
import com.mustk.newsapp.util.Constant.NULL_JSON
import com.mustk.newsapp.util.Constant.SEARCH_THRESHOLD_LENGTH
import com.mustk.newsapp.util.Constant.TIME_OUT_MILLIS
import com.mustk.newsapp.util.NetworkHelper
import com.mustk.newsapp.util.Resource
import com.mustk.newsapp.util.RetrofitErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: NewsDataSource,
    private val networkHelper: NetworkHelper,
    private val retrofitErrorHandler: RetrofitErrorHandler
) : ViewModel() {

    private val _initStatus = MutableLiveData<Boolean>()
    val initStatus: LiveData<Boolean>
        get() = _initStatus

    private val _newsList = MutableLiveData<Resource<List<News>>>()
    val newsList: LiveData<Resource<List<News>>>
        get() = _newsList

    private val _searchText = MutableStateFlow("")
    private val searchText = _searchText.asStateFlow()

    val isConnectedNetwork: Boolean
        get() = networkHelper.isNetworkConnected()

    init {
        _initStatus.value = true
        observeSearchTextChanges()
    }

    fun onSearchTextChange(text: String) {
        if (text.trim().isNotBlank()) {
            _searchText.value = text.lowercase()
        } else {
            _initStatus.value = true
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchTextChanges() {
        viewModelScope.launch {
            searchText
                .debounce(TIME_OUT_MILLIS)
                .distinctUntilChanged()
                .filter {
                    it.length > SEARCH_THRESHOLD_LENGTH
                }
                .collectLatest {
                    fetchSearchNewsListFromAPI(it)
                }
        }
    }

    fun fetchSearchNewsListFromAPI(newsKey: String) {
        viewModelScope.launch {
            _newsList.postValue(Resource.loading(null))
            if (isConnectedNetwork) {
                try {
                    val response =
                        repository.searchNews(newsKey)
                    if (response.isSuccessful) {
                        response.body()?.let { baseResponse ->
                            val categoryNews = baseResponse.data
                            if (categoryNews.isEmpty()) {
                                _newsList.postValue(Resource.error(NULL_JSON, null))
                            } else {
                                _newsList.postValue(Resource.success(categoryNews))
                            }
                        }
                    } else {
                        val errorCode = response.code().toString()
                        val errorMessage = retrofitErrorHandler.handleRetrofitCode(errorCode)
                        _newsList.postValue(Resource.error(errorMessage, null))
                    }
                } catch (e: Exception) {
                    _newsList.postValue(Resource.error(e.localizedMessage, null))
                }
            } else {
                _newsList.postValue(Resource.error(NO_CONNECTION, null))
            }
        }
    }
}