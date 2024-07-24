package com.mustk.newsapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.mustk.newsapp.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val auth: FirebaseAuth) : BaseViewModel() {

    private val _navigateToHome = MutableLiveData<Event<Boolean>>()
    val navigateToHome: LiveData<Event<Boolean>>
        get() = _navigateToHome

    private val _splashLoading = MutableStateFlow(true)
    val splashLoading = _splashLoading.asStateFlow()

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