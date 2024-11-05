package com.mustk.newsapp.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.mustk.newsapp.util.Constant.SHARED_PREFS_NAME
import com.mustk.newsapp.util.Event
import com.mustk.newsapp.util.NetworkHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _userAuthenticated = MutableLiveData<Event<Boolean>>()
    val userAuthenticated: LiveData<Event<Boolean>>
        get() = _userAuthenticated

    private val _checkNetworkConnection = MutableLiveData<Event<Boolean>>()
    val checkNetworkConnection: LiveData<Event<Boolean>>
        get() = _checkNetworkConnection

    private val _splashLoading = MutableStateFlow(true)
    val splashLoading = _splashLoading.asStateFlow()

    private val _languageString = MutableLiveData<String>()
    val languageString: LiveData<String>
        get() = _languageString

    init {
        navigateBasedOnNetworkAndUser()
    }

    private fun setSplashLoadingFalse() {
        _splashLoading.value = false
    }

    private fun navigateToHomeScreen() {
        _userAuthenticated.value = Event(true)
    }

    private fun navigateToNetworkConnectionScreen() {
        _checkNetworkConnection.value = Event(true)
    }

    private fun currentUserActivated(): Boolean {
        return auth.currentUser != null
    }

    private fun navigateBasedOnNetworkAndUser() {
        val isConnected = networkHelper.isNetworkConnected()
        val isUserActive = currentUserActivated()
        if (!isConnected) {
            navigateToNetworkConnectionScreen()
            setSplashLoadingFalse()
            return
        }
        if (isUserActive) {
            navigateToHomeScreen()
        }
        setSplashLoadingFalse()
    }
}