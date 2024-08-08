package com.mustk.newsapp.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.mustk.newsapp.shared.Constant.DEFAULT_LANGUAGE
import com.mustk.newsapp.shared.Constant.LANGUAGE_ITEM
import com.mustk.newsapp.shared.Constant.SHARED_PREFS_NAME
import com.mustk.newsapp.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : BaseViewModel() {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _navigateToHome = MutableLiveData<Event<Boolean>>()
    val navigateToHome: LiveData<Event<Boolean>>
        get() = _navigateToHome

    private val _splashLoading = MutableStateFlow(true)
    val splashLoading = _splashLoading.asStateFlow()

    private val _languageString = MutableLiveData<String>()
    val languageString: LiveData<String>
        get() = _languageString

    init {
        //loadPreferences()
    }

    private fun loadPreferences() {
        _languageString.value = sharedPreferences.getString(LANGUAGE_ITEM, DEFAULT_LANGUAGE)
    }

    private fun setLoadingFalse(){
        _splashLoading.value = false
    }

    private fun navigateToHomeScreen() {
        _navigateToHome.value = Event(true)
    }

    fun currentUserCheck() {
        if (auth.currentUser != null) {
            setLoadingFalse()
            navigateToHomeScreen()
        } else {
            setLoadingFalse()
        }
    }
}