package com.mustk.newsapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mustk.newsapp.R
import com.mustk.newsapp.util.Constant.EMAIL_FIELD
import com.mustk.newsapp.util.Constant.USERS_COLLECTION
import com.mustk.newsapp.util.Event
import com.mustk.newsapp.util.NetworkHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class PasswordViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseFirestore,
    private val networkHelper: NetworkHelper,
    @ApplicationContext private val context: Context
): ViewModel() {

    private val _emailError = MutableLiveData<Int>()
    val emailError: LiveData <Int>
        get() = _emailError

    private val _authError = MutableLiveData<String>()
    val authError: LiveData<String>
        get() = _authError

    private val _loading = MutableLiveData<Boolean>()
    val loading : LiveData <Boolean>
        get() = _loading

    private val _success = MutableLiveData<Event<Boolean>>()
    val success : LiveData <Event<Boolean>>
        get() = _success

    private val _checkConnection = MutableLiveData<Event<Boolean>>()
    val checkConnection: LiveData<Event<Boolean>>
        get() = _checkConnection

    val isConnectedNetwork: Boolean
        get() = networkHelper.isNetworkConnected()

    fun validateInput(emailAddress: String) {
        if (emailAddress.isBlank()) {
            _emailError.value = R.string.enter_email_message
            return
        } else {
            if (isConnectedNetwork){
                _loading.value = true
                isRegisteredUserInDb(emailAddress) { isRegistered ->
                    if (isRegistered) {
                        sendResetMail(emailAddress)
                    } else {
                        _loading.value = true
                        _authError.value = context.getString(R.string.email_un_correct_message)
                    }
                }
            }else {
                _checkConnection.value = Event(true)
            }
        }
    }

    private fun isRegisteredUserInDb(email: String, callback: (Boolean) -> Unit) {
        database.collection(USERS_COLLECTION)
            .whereEqualTo(EMAIL_FIELD, email)
            .get()
            .addOnSuccessListener { documents ->
                callback(documents.isEmpty.not())
            }
            .addOnFailureListener { error ->
                callback(false)
                _loading.value = false
                error.localizedMessage?.let {
                    _authError.value = it
                }
            }
    }

    private fun sendResetMail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                _loading.value = false
                _authError.value = context.getString(R.string.reset_email_success)
                _success.value = Event(true)
            }
            .addOnFailureListener { error ->
                _loading.value = false
                error.localizedMessage?.let {
                    _authError.value = it
                }
            }
    }
}