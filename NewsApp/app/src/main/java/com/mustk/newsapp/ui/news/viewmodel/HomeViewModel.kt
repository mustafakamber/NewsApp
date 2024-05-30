package com.mustk.newsapp.ui.news.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.denzcoskun.imageslider.models.SlideModel
import com.google.firebase.auth.FirebaseAuth
import com.mustk.newsapp.data.datasource.NewsDataSource
import com.mustk.newsapp.data.model.News
import com.mustk.newsapp.shared.Constant.LANGUAGE
import com.mustk.newsapp.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: NewsDataSource) : BaseViewModel() {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _navigateToLogin = MutableLiveData<Event<Boolean>>()
    val navigateToLogin: LiveData<Event<Boolean>>
        get() = _navigateToLogin

    private val _categoryLoading = MutableLiveData<Boolean>()
    val categoryLoading: LiveData<Boolean>
        get() = _categoryLoading

    private val _categoryNewsList = MutableLiveData<List<News>>()
    val categoryNewsList: LiveData<List<News>>
        get() = _categoryNewsList

    private val _headlineNewsList = MutableLiveData<ArrayList<SlideModel>>()
    val headlineNewsList: LiveData<ArrayList<SlideModel>>
        get() = _headlineNewsList

    private val _slideList = ArrayList<SlideModel>()
    private val _headlineLoading = MutableLiveData<Boolean>()
    val headlineLoading: LiveData<Boolean>
        get() = _headlineLoading

    private val initCategory = "general"

    init {
        fetchHeadlineNewsFromAPI()
        fetchCategoryNewsFromAPI(initCategory)
    }

    private fun fetchHeadlineNewsFromAPI() {
        setHeadlineLoading(true)
        safeRequest(
            response = { repository.fetchNewsDataForHeadline(LANGUAGE, 1) },
            successStatusData = { newsData ->
                newsData.data.forEach {
                    _slideList.add(SlideModel(it.imageUrl, it.title))
                    setHeadlineLoading(false)
                    _headlineNewsList.value = _slideList
                }
            })
    }

    fun fetchCategoryNewsFromAPI(category: String) {
        setCategoryLoading(true)
        safeRequest(
            response = { repository.fetchNewsDataForCategories(LANGUAGE, category) },
            successStatusData = { newsData ->
                setCategoryLoading(false)
                _categoryNewsList.value = newsData.data
            })
    }

    private fun setCategoryLoading(boolean: Boolean) {
        _categoryLoading.value = boolean
    }

    private fun setHeadlineLoading(boolean: Boolean) {
        _headlineLoading.value = boolean
    }

    fun currentUserLogOut() {
        auth.signOut()
        navigateToLoginScreen(true)
    }

    private fun navigateToLoginScreen(isSuccess: Boolean) {
        _navigateToLogin.value = Event(isSuccess)
    }
}