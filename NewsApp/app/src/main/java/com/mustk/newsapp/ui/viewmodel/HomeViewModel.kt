package com.mustk.newsapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustk.newsapp.data.datasource.NewsDataSource
import com.mustk.newsapp.data.model.News
import com.mustk.newsapp.util.Constant.CATEGORY_GENERAL
import com.mustk.newsapp.util.Constant.HEADLINE_SIZE
import com.mustk.newsapp.util.Constant.LANGUAGE_EN
import com.mustk.newsapp.util.Constant.NO_CONNECTION
import com.mustk.newsapp.util.Constant.NULL_JSON
import com.mustk.newsapp.util.NetworkHelper
import com.mustk.newsapp.util.Resource
import com.mustk.newsapp.util.RetrofitErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: NewsDataSource,
    private val networkHelper: NetworkHelper,
    private val retrofitErrorHandler: RetrofitErrorHandler
) : ViewModel() {

    private val _categoryNewsList = MutableLiveData<Resource<List<News>>>()
    val categoryNewsList: LiveData<Resource<List<News>>>
        get() = _categoryNewsList

    private val _headlineNewsList = MutableLiveData<Resource<ArrayList<News>>>()
    val headlineNewsList: LiveData<Resource<ArrayList<News>>>
        get() = _headlineNewsList

    private val slideHeadlineList = ArrayList<News>()

    private val _lastSelectedCategoryTabPosition = MutableLiveData<Int>()
    val lastSelectedTabCategoryPosition: LiveData<Int>
        get() = _lastSelectedCategoryTabPosition

    private val _lastSelectedLanguageTabPosition = MutableLiveData<Int>()
    val lastSelectedTabLanguagePosition: LiveData<Int>
        get() = _lastSelectedLanguageTabPosition

    private val initCategory = CATEGORY_GENERAL
    private val initLanguage = LANGUAGE_EN
    private var selectedCategory = initCategory
    private var selectedLanguage = initLanguage

    val isConnectedNetwork: Boolean
        get() = networkHelper.isNetworkConnected()

    init {
        refreshData()
    }

    fun refreshScreen() {
        refreshData()
    }

    fun refreshData() {
        fetchHeadlineNewsFromAPI()
        fetchCategoryNewsFromAPI()
    }

    private fun fetchHeadlineNewsFromAPI() {
        slideHeadlineList.clear()
        viewModelScope.launch {
            _headlineNewsList.postValue(Resource.loading(null))
            if (isConnectedNetwork) {
                try {
                    val response = repository.fetchHeadlineNews(selectedLanguage)
                    if (response.isSuccessful) {
                        response.body()?.let { baseResponse ->
                            val headlineList = baseResponse.data
                            headlineList.forEach {
                                slideHeadlineList.add(it)
                                if (slideHeadlineList.size == HEADLINE_SIZE) {
                                    _headlineNewsList.postValue(Resource.success(slideHeadlineList))
                                }
                            }
                        }
                    } else {
                        val errorCode = response.code().toString()
                        val errorMessage = retrofitErrorHandler.handleRetrofitCode(errorCode)
                        _headlineNewsList.postValue(Resource.error(errorMessage, null))
                    }
                } catch (e: Exception) {
                    _headlineNewsList.postValue(Resource.error(e.localizedMessage, null))
                }
            } else {
                _headlineNewsList.postValue(Resource.error(NO_CONNECTION, null))
            }
        }
    }

    private fun fetchCategoryNewsFromAPI() {
        viewModelScope.launch {
            _categoryNewsList.postValue(Resource.loading(null))
            if (isConnectedNetwork) {
                try {
                    val response =
                        repository.fetchNewsListByCategory(selectedLanguage, selectedCategory)
                    if (response.isSuccessful) {
                        response.body()?.let { baseResponse ->
                            val categoryNews = baseResponse.data
                            if (categoryNews.isEmpty()) {
                                _categoryNewsList.postValue(Resource.error(NULL_JSON, null))
                            } else {
                                _categoryNewsList.postValue(Resource.success(categoryNews))
                            }
                        }
                    } else {
                        val errorCode = response.code().toString()
                        val errorMessage = retrofitErrorHandler.handleRetrofitCode(errorCode)
                        _categoryNewsList.postValue(Resource.error(errorMessage, null))
                    }
                } catch (e: Exception) {
                    _categoryNewsList.postValue(Resource.error(e.localizedMessage, null))
                }
            } else {
                _categoryNewsList.postValue(Resource.error(NO_CONNECTION, null))
            }
        }
    }

    fun onLanguageChange(language: String, position: Int){
        if (language != selectedLanguage){
            selectedLanguage = language
            _lastSelectedLanguageTabPosition.value = position
            refreshData()
        }
    }

    fun onCategoryChange(category: String, position: Int){
        if (category != selectedCategory){
            selectedCategory = category
            _lastSelectedCategoryTabPosition.value = position
            fetchCategoryNewsFromAPI()
        }
    }
}