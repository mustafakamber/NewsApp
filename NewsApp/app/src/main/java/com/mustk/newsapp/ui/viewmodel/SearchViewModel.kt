package com.mustk.newsapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mustk.newsapp.data.datasource.NewsDataSource
import com.mustk.newsapp.data.model.News
import com.mustk.newsapp.shared.Constant.SEARCH_THRESHOLD_LENGTH
import com.mustk.newsapp.shared.Constant.TIME_OUT_MILLIS
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
class SearchViewModel @Inject constructor(private val repository: NewsDataSource) :
    BaseViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _initMessage = MutableLiveData<Boolean>()
    val initMessage: LiveData<Boolean>
        get() = _initMessage

    private val _notFoundMessage = MutableLiveData<Boolean>()
    val notFoundMessage: LiveData<Boolean>
        get() = _notFoundMessage

    private val _recyclerView = MutableLiveData<Boolean>()
    val recyclerView: LiveData<Boolean>
        get() = _recyclerView

    private val _newsList = MutableLiveData<List<News>>()
    val newsList: LiveData<List<News>>
        get() = _newsList

    private val _searchText = MutableStateFlow("")
    private val searchText = _searchText.asStateFlow()

    init {
        initState()
        observeSearchTextChanges()
    }

    private fun setInitMessageVisibility(boolean: Boolean) {
        _initMessage.value = boolean
    }

    private fun setNotFoundMessageVisibility(boolean: Boolean) {
        _notFoundMessage.value = boolean
    }

    private fun setRecyclerViewVisibility(boolean: Boolean) {
        _recyclerView.value = boolean
    }

    private fun setSearchLoadingBarVisibility(boolean: Boolean) {
        _loading.value = boolean
    }

    private fun setNewsList(news: List<News>) {
        _newsList.value = news
    }

    fun onSearchTextChange(text: String) {
        checkEmptyQuery(text)
    }

    private fun initState() {
        setRecyclerViewVisibility(false)
        setNotFoundMessageVisibility(false)
        setSearchLoadingBarVisibility(false)
        setInitMessageVisibility(true)
    }

    private fun loadingState() {
        setRecyclerViewVisibility(false)
        setNotFoundMessageVisibility(false)
        setInitMessageVisibility(false)
        setSearchLoadingBarVisibility(true)
    }

    private fun resultFoundState() {
        setNotFoundMessageVisibility(false)
        setInitMessageVisibility(false)
        setSearchLoadingBarVisibility(false)
        setRecyclerViewVisibility(true)
    }

    private fun resultNotFoundState() {
        setInitMessageVisibility(false)
        setSearchLoadingBarVisibility(false)
        setRecyclerViewVisibility(false)
        setNotFoundMessageVisibility(true)
    }

    private fun checkEmptyQuery(text: String) {
        if (text.trim().isEmpty()) {
            initState()
        } else {
            _searchText.value = text.lowercase()
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

    private fun fetchSearchNewsListFromAPI(newsKey: String) {
        loadingState()
        safeRequest(
            response = { repository.searchNews(newsKey) },
            successStatusData = { searchData ->
                if (searchData.data.isEmpty()) {
                    resultNotFoundState()
                } else {
                    resultFoundState()
                    setNewsList(searchData.data)
                }
            })
    }
}