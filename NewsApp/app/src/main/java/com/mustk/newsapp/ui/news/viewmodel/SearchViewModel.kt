package com.mustk.newsapp.ui.news.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mustk.newsapp.data.datasource.NewsDataSource
import com.mustk.newsapp.data.model.News
import com.mustk.newsapp.shared.Constant.LANGUAGE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: NewsDataSource) :
    BaseViewModel() {

    private val _searchLoading = MutableLiveData<Boolean>()
    val searchLoading: LiveData<Boolean>
        get() = _searchLoading

    private val _searchInitMessage = MutableLiveData<Boolean>()
    val searchInitMessage: LiveData<Boolean>
        get() = _searchInitMessage

    private val _searchNewsList = MutableLiveData<List<News>>()
    val searchNewsList: LiveData<List<News>>
        get() = _searchNewsList

    private val _searchText = MutableStateFlow("")
    private val searchText = _searchText.asStateFlow()

    init {
        setInitMessage(true)
        setSearchLoading(false)
        observeSearchTextChanges()
    }

    private fun setInitMessage(boolean: Boolean) {
        _searchInitMessage.value = boolean
    }

    private fun setSearchLoading(boolean: Boolean) {
        _searchLoading.value = boolean
    }

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchTextChanges() {
        viewModelScope.launch {
            searchText
                .debounce(500L)
                .distinctUntilChanged()
                .filter {
                    it.isNotEmpty()
                }
                .filter {
                    it.length > 3
                }
                .map {
                    it.lowercase().trim()
                }
                .collectLatest {
                    fetchSearchNewsListFromAPI(it)
                }
        }
    }

    private fun fetchSearchNewsListFromAPI(newsKey: String) {
        setInitMessage(false)
        setSearchLoading(true)
        safeRequest(
            response = { repository.fetchNewsDataForSearch(LANGUAGE, newsKey) },
            successStatusData = { searchData ->
                setSearchLoading(false)
                _searchNewsList.value = searchData.data
            })
    }
}