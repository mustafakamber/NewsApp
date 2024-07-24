package com.mustk.newsapp.util

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData

fun <T> Fragment.observe(liveData: LiveData<T>, observer: (T) -> Unit) {
    liveData.observe(viewLifecycleOwner) { it?.let { t -> observer(t) } }
}

fun Fragment.onBackPressed(backButtonPressed: () -> Unit) {
    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            backButtonPressed()
        }
    }
    this.requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,callback)
}

