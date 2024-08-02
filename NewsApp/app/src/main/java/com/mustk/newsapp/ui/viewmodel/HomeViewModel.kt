package com.mustk.newsapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.denzcoskun.imageslider.models.SlideModel
import com.mustk.newsapp.data.datasource.NewsDataSource
import com.mustk.newsapp.data.model.News
import com.mustk.newsapp.shared.Constant.CATEGORY_GENERAL
import com.mustk.newsapp.shared.Constant.HEADLINE_SIZE
import com.mustk.newsapp.shared.Constant.LANGUAGE_EN
import com.mustk.newsapp.shared.Constant.NULL_PATH_IMAGE
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: NewsDataSource
) : BaseViewModel() {

    private val _categoryLoading = MutableLiveData<Boolean>()
    val categoryLoading: LiveData<Boolean>
        get() = _categoryLoading

    private val _categoryNullMessage = MutableLiveData<Boolean>()
    val categoryNullMessage: LiveData<Boolean>
        get() = _categoryNullMessage

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

    private val _lastSelectedCategoryTabPosition = MutableLiveData<Int>()
    val lastSelectedTabCategoryPosition: LiveData<Int> get() = _lastSelectedCategoryTabPosition

    private val _lastSelectedLanguageTabPosition = MutableLiveData<Int>()
    val lastSelectedTabLanguagePosition: LiveData<Int> get() = _lastSelectedLanguageTabPosition

    private val initCategory = CATEGORY_GENERAL
    private val initLanguage = LANGUAGE_EN

    private var selectedCategory = initCategory
    private var selectedLanguage = initLanguage

    init {
        refreshHomeData()
        println("SA")
    }

    fun swipeRefreshClicked() {
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

    private fun setCategoryNullMessage(boolean: Boolean) {
        _categoryNullMessage.value = boolean
    }

    fun setCategoryLastSelectedTabPosition(position: Int) {
        _lastSelectedCategoryTabPosition.value = position
    }

    fun setCountryLastSelectedTabPosition(position: Int) {
        _lastSelectedLanguageTabPosition.value = position
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
        setCategoryNullMessage(false)
        setRecyclerViewVisibility(false)
        setCategoryLoadingVisibility(false)
    }

    private fun loadingCategoryState() {
        setCategoryNullMessage(false)
        setRecyclerViewVisibility(false)
        setCategoryLoadingVisibility(true)
    }

    private fun resultCategoryState(){
        setRecyclerViewVisibility(true)
        setCategoryNullMessage(false)
        setCategoryLoadingVisibility(false)
    }

    private fun nullCategoryState() {
        setRecyclerViewVisibility(false)
        setCategoryLoadingVisibility(false)
        setCategoryNullMessage(true)
    }

    fun setSlideListClear(){
        _slideList.clear()
        setHeadlineNewsList(_slideList)
    }

    private fun fetchHeadlineNewsFromAPI() {
        setSlideListClear()
        loadingHeadLineState()
        safeRequest(
            response = { repository.fetchNewsDataForHeadline(selectedLanguage) },
            successStatusData = { newsData ->
                newsData.data.forEach {
                    if (it.imageUrl.isNullOrEmpty()) {
                        _slideList.add(SlideModel(NULL_PATH_IMAGE, it.title))
                    }else{
                        _slideList.add(SlideModel(it.imageUrl, it.title))
                    }
                    if (_slideList.size == HEADLINE_SIZE){
                        resultHeadlineState()
                        setHeadlineNewsList(_slideList)
                    }
                }
            })
    }

    fun fetchCategoryNewsFromAPI(category: String) {
        setSelectedCategory(category)
        loadingCategoryState()
        safeRequest(
            response = {
                repository.fetchNewsDataForCategories(
                    selectedLanguage,
                    selectedCategory
                )
            },
            successStatusData = { newsData ->
                if (newsData.data.isEmpty()) {
                    nullCategoryState()
                } else {
                    resultCategoryState()
                    setCategoryNewsList(newsData.data)
                }
            }
        )
    }

    private fun setSelectedCategory(category: String) {
        selectedCategory = category
    }
    private fun setSelectedLanguage(language: String) {
        selectedLanguage = language
    }
    fun onLanguageChanged(language: String){
        if (selectedLanguage != language){
            setSelectedLanguage(language)
            refreshHomeData()
        }
    }
}