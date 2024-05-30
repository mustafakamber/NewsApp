package com.mustk.newsapp.ui.news.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mustk.newsapp.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PasswordViewModel @Inject constructor(): BaseViewModel() {

    private var auth = FirebaseAuth.getInstance()

    private val _resetTitleText = MutableLiveData<Int>()
    val resetTitleText: LiveData<Int>
        get() = _resetTitleText

    private val _sendButtonVisible = MutableLiveData<Boolean>()
    val sendButtonVisible: LiveData<Boolean>
        get() = _sendButtonVisible

    private val _backButtonVisible = MutableLiveData<Boolean>()
    val backButtonVisible: LiveData<Boolean>
        get() = _backButtonVisible

    init {
        initViewState()
    }

    private fun initViewState() {
        _resetTitleText.value = R.string.enter_email_message
        _sendButtonVisible.value = true
        _backButtonVisible.value = false
    }

    private fun successSendViewState() {
        _resetTitleText.value = R.string.reset_email_success
        _sendButtonVisible.value = false
        _backButtonVisible.value = true
    }

    fun inputCheckForResetPassword(emailAddress: String) {
        inputCheck(null, emailAddress, null, null) {
            checkIfEmailExistsForPasswordReset(emailAddress)
        }
    }

    private fun checkIfEmailExistsForPasswordReset(email: String) {
        handleEmailCheck(
            email,
            onEmailNotFound = {
                _resetTitleText.value = R.string.email_un_correct_message
            },
            onEmailFound = {
                sendPasswordResetEmail(email)
            },
            onErrorMessage = { error ->
                showToastMessage(error)
            }
        )
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                successSendViewState()
            }
            .addOnFailureListener { error ->
                error.localizedMessage?.let {
                    showToastMessage(it)
                }
            }
    }
}