package com.mustk.newsapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.denzcoskun.imageslider.models.SlideModel
import com.google.firebase.auth.FirebaseAuth
import com.mustk.newsapp.data.datasource.NewsDataSource
import com.mustk.newsapp.data.model.News
import com.mustk.newsapp.shared.Constant.CATEGORY_GENERAL
import com.mustk.newsapp.shared.Constant.LANGUAGE
import com.mustk.newsapp.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: NewsDataSource,
    private val auth: FirebaseAuth
) : BaseViewModel() {

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

    private val _swipeRefreshLoading = MutableLiveData<Boolean>()
    val swipeRefreshLoading: LiveData<Boolean>
        get() = _swipeRefreshLoading

    private val _recyclerView = MutableLiveData<Boolean>()
    val recyclerView: LiveData<Boolean>
        get() = _recyclerView

    private val _imageSliderView = MutableLiveData<Boolean>()
    val imageSliderView: LiveData<Boolean>
        get() = _imageSliderView

    private val _lastSelectedTabPosition = MutableLiveData<Int>()
    val lastSelectedTabPosition: LiveData<Int> get() = _lastSelectedTabPosition

    private val initCategory = CATEGORY_GENERAL

    private var selectedCategory = initCategory

    init {
        refreshHomeData()
    }

    fun swipeRefreshState() {
        setSwipeRefreshLoadingVisibility(true)
        refreshHomeData()
        setSwipeRefreshLoadingVisibility(false)
    }

    private fun refreshHomeData() {
        initHeadlineState()
        initCategoryState()
        fetchHeadlineNewsFromAPI()
        fetchCategoryNewsFromAPI(selectedCategory)
    }

    private fun setCategoryLoadingVisibility(boolean: Boolean) {
        _categoryLoading.value = boolean
    }

    private fun setHeadlineLoadingVisibility(boolean: Boolean) {
        _headlineLoading.value = boolean
    }

    private fun setSwipeRefreshLoadingVisibility(boolean: Boolean) {
        _swipeRefreshLoading.value = boolean
    }

    private fun setRecyclerViewVisibility(boolean: Boolean) {
        _recyclerView.value = boolean
    }

    private fun setImageSliderVisibility(boolean: Boolean) {
        _imageSliderView.value = boolean
    }

    private fun setHeadlineNewsList(news: ArrayList<SlideModel>) {
        _headlineNewsList.value = news
    }

    private fun setCategoryNewsList(news: List<News>) {
        _categoryNewsList.value = news
    }

    fun setLastSelectedTabPosition(position: Int) {
        _lastSelectedTabPosition.value = position
    }

    private fun initHeadlineState() {
        setImageSliderVisibility(false)
        setHeadlineLoadingVisibility(false)
    }

    private fun loadingHeadLineState() {
        _headlineNewsList.value?.clear()
        _slideList.clear()
        setImageSliderVisibility(false)
        setHeadlineLoadingVisibility(true)
    }

    private fun resultHeadlineState() {
        setImageSliderVisibility(true)
        setHeadlineLoadingVisibility(false)
    }

    private fun initCategoryState() {
        setRecyclerViewVisibility(false)
        setCategoryLoadingVisibility(false)
    }

    private fun loadingCategoryState() {
        setRecyclerViewVisibility(false)
        setCategoryLoadingVisibility(true)
    }

    private fun resultCategoryState(){
        setRecyclerViewVisibility(true)
        setCategoryLoadingVisibility(false)
    }

    private fun fetchHeadlineNewsFromAPI() {
        loadingHeadLineState()
        safeRequest(
            response = { repository.fetchNewsDataForHeadline(LANGUAGE) },
            successStatusData = { newsData ->
                newsData.data.forEach {
                    if (!it.imageUrl.isNullOrEmpty()) {
                        _slideList.add(SlideModel(it.imageUrl, it.title))
                    }
                    resultHeadlineState()
                    setHeadlineNewsList(_slideList)
                }
            })
    }

    fun fetchCategoryNewsFromAPI(category: String) {
        selectedCategory = category
        loadingCategoryState()
        safeRequest(
            response = { repository.fetchNewsDataForCategories(LANGUAGE, selectedCategory) },
            successStatusData = { newsData ->
                resultCategoryState()
                setCategoryNewsList(newsData.data)
            }
        )
    }

    fun currentUserLogOut() {
        auth.signOut()
        navigateToLoginScreen()
    }

    private fun navigateToLoginScreen() {
        _navigateToLogin.value = Event(true)
    }
}