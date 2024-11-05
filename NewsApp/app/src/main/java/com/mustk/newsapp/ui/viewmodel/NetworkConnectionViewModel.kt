package com.mustk.newsapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.mustk.newsapp.util.Event
import com.mustk.newsapp.util.NetworkHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NetworkConnectionViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    private val _userAuthenticated = MutableLiveData<Event<Boolean>>()
    val userAuthenticated : LiveData<Event<Boolean>>
        get() = _userAuthenticated

    private fun setCurrentUserActive(boolean: Boolean){
        _userAuthenticated.value = Event(boolean)
    }

    fun checkNetworkConnection(): Boolean {
        val isConnected = networkHelper.isNetworkConnected()
        return isConnected
    }

    private fun currentUserActivated(): Boolean {
        return auth.currentUser != null
    }

    fun navigateBasedOnUserActivationState() {
        val isActiveUser = currentUserActivated()
        setCurrentUserActive(isActiveUser)
    }
}