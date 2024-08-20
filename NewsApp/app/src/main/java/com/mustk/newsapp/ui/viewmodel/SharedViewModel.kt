package com.mustk.newsapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(): BaseViewModel(){

    private val _changedUserPhoto = MutableLiveData<String>()
    val changedUserPhoto: LiveData<String>
        get() = _changedUserPhoto

    fun setChangedUserPhoto(photo: String) {
        _changedUserPhoto.value = photo
    }
}