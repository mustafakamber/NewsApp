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

    private val _snackBarMessage = MutableLiveData<Event<Int>>()
    val snackBarMessage: LiveData<Event<Int>>
        get() = _snackBarMessage

    fun setToastMessage(message: String) {
        _errorMessage.value = Event(message)
    }

    fun setSnackBarMessage(message: Int) {
        _snackBarMessage.value = Event(message)
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
                        setToastMessage(it)
                    }
                }
                Status.LOADING -> {

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