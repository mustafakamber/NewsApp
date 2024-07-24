package com.mustk.newsapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mustk.newsapp.shared.Event
import com.mustk.newsapp.shared.Resource
import com.mustk.newsapp.shared.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class BaseViewModel @Inject constructor() : ViewModel() {

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>>
        get() = _errorMessage

    private val _snackbarMessage = MutableLiveData<Event<Int>>()
    val snackbarMessage: LiveData<Event<Int>>
        get() = _snackbarMessage

    fun showToastMessage(message: String) {
        _errorMessage.value = Event(message)
    }

    fun showSnackBarMessage(message: Int) {
        _snackbarMessage.value = Event(message)
    }

    protected fun <T : Any> safeRequest(
        response: suspend () -> Resource<T>,
        successStatusData: (T) -> Unit
    ) {
        viewModelScope.launch()
        {
            when (response().status) {
                Status.ERROR -> {
                    response().message?.let {
                        showToastMessage(it)
                    }
                }
                Status.SUCCESS -> {
                    response().data?.let {
                        successStatusData(it)
                    }
                }
            }
        }
    }
}