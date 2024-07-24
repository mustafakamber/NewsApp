package com.mustk.newsapp.ui.viewmodel

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mustk.newsapp.R
import com.mustk.newsapp.shared.Constant.EMAIL_FIELD
import com.mustk.newsapp.shared.Constant.USERS_COLLECTION
import com.mustk.newsapp.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PasswordViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseFirestore
): BaseViewModel() {

    private val _titleText = MutableLiveData<Int>()
    val titleText: LiveData<Int>
        get() = _titleText

    private val _sendButtonVisibility = MutableLiveData<Boolean>()
    val sendButtonVisibility: LiveData<Boolean>
        get() = _sendButtonVisibility

    private val _backButtonVisibility = MutableLiveData<Boolean>()
    val backButtonVisibility: LiveData<Boolean>
        get() = _backButtonVisibility

    private val _emailErrorText = MutableLiveData<Event<Int>>()
    val emailErrorText: LiveData<Event<Int>>
        get() = _emailErrorText

    private val _emailEndIconVisibility = MutableLiveData<Boolean>()
    val emailEndIconVisibility: LiveData<Boolean>
        get() = _emailEndIconVisibility

    init {
        initViewState()
    }

    private fun setButtonState(boolean: Boolean){
        _sendButtonVisibility.value = boolean
        _backButtonVisibility.value = !boolean
    }

    private fun initViewState() {
        setTitleText(R.string.enter_email_message)
        setButtonState(true)
    }

    private fun setTitleText(int : Int){
        _titleText.value = int
    }

    private fun successSendViewState() {
        setTitleText(R.string.reset_email_success)
        setButtonState(false)
    }

    fun setEmailEndIcon(boolean: Boolean){
        _emailEndIconVisibility.value = boolean
    }

    private fun setEmailErrorText(int: Int){
        _emailErrorText.value = Event(int)
    }

    fun inputCheckForResetPassword(emailAddress: String) {
        val isEmailEmpty = TextUtils.isEmpty(emailAddress)
        if (isEmailEmpty){
            setEmailEndIcon(false)
            setEmailErrorText(R.string.enter_email_message)
            return
        }else{
            checkIfEmailExistsForPasswordReset(emailAddress)
        }
    }

    private fun checkIfEmailExistsForPasswordReset(email: String) {
        checkEmailExistInDatabase(
            email,
            onEmailNotFound = {
                setTitleText(R.string.email_un_correct_message)
            },
            onEmailFound = {
                sendPasswordResetEmail(email)
            },
            onErrorMessage = { error ->
                showToastMessage(error)
            }
        )
    }

    private fun checkEmailExistInDatabase(
        email: String,
        onEmailNotFound: () -> Unit,
        onEmailFound: () -> Unit,
        onErrorMessage: (String) -> Unit,
    ) {
        database.collection(USERS_COLLECTION)
            .whereEqualTo(EMAIL_FIELD, email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    onEmailNotFound()
                } else {
                    onEmailFound()
                }
            }
            .addOnFailureListener { error ->
                error.localizedMessage?.let {
                    onErrorMessage(it)
                }
            }
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