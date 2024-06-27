package com.mustk.newsapp.ui.news.viewmodel

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mustk.newsapp.R
import com.mustk.newsapp.shared.Constant.EMAIL_FIELD
import com.mustk.newsapp.shared.Constant.PASSWORD_LIMIT
import com.mustk.newsapp.shared.Constant.USERS_COLLECTION
import com.mustk.newsapp.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseFirestore
) : BaseViewModel() {

    private val _navigateToHome = MutableLiveData<Event<Boolean>>()
    val navigateToHome: LiveData<Event<Boolean>>
        get() = _navigateToHome

    private val _emailErrorText = MutableLiveData<Event<Int>>()
    val emailErrorText: LiveData<Event<Int>>
        get() = _emailErrorText

    private val _emailEndIconVisibility = MutableLiveData<Boolean>()
    val emailEndIconVisibility: LiveData<Boolean>
        get() = _emailEndIconVisibility

    private val _passwordErrorText = MutableLiveData<Event<Int>>()
    val passwordErrorText: LiveData<Event<Int>>
        get() = _passwordErrorText

    private val _passwordEndIconVisibility = MutableLiveData<Boolean>()
    val passwordEndIconVisibility: LiveData<Boolean>
        get() = _passwordEndIconVisibility

    private val _confirmPasswordErrorText = MutableLiveData<Event<Int>>()
    val confirmPasswordErrorText: LiveData<Event<Int>>
        get() = _confirmPasswordErrorText

    private val _confirmPasswordEndIconVisibility = MutableLiveData<Boolean>()
    val confirmPasswordEndIconVisibility: LiveData<Boolean>
        get() = _confirmPasswordEndIconVisibility

    private fun navigateToHomeScreen() {
        _navigateToHome.value = Event(true)
    }

    fun setEmailEndIcon(boolean: Boolean) {
        _emailEndIconVisibility.value = boolean
    }

    fun setPasswordEndIcon(boolean: Boolean) {
        _passwordEndIconVisibility.value = boolean
    }

    fun setConfirmPasswordEndIcon(boolean: Boolean) {
        _confirmPasswordEndIconVisibility.value = boolean
    }

    private fun setEmailErrorText(int: Int) {
        _emailErrorText.value = Event(int)
    }

    private fun setPasswordErrorText(int: Int) {
        _passwordErrorText.value = Event(int)
    }

    private fun setConfirmPasswordErrorText(int: Int) {
        _confirmPasswordErrorText.value = Event(int)
    }

    fun inputCheckForSignup(
        context: Context,
        emailAddress: String,
        password: String,
        confirmPassword: String
    ) {
        val isEmailEmpty = TextUtils.isEmpty(emailAddress)
        val isPasswordEmpty = TextUtils.isEmpty(password)
        val isConfirmPasswordEmpty = TextUtils.isEmpty(confirmPassword)
        val isNotEqualPasswords = password != confirmPassword
        val isPasswordLimitOver = password.length > PASSWORD_LIMIT
        when {
            isEmailEmpty -> {
                setEmailEndIcon(false)
                setEmailErrorText(R.string.enter_email_message)
            }
            isPasswordEmpty -> {
                setPasswordEndIcon(false)
                setPasswordErrorText(R.string.enter_password_message)
            }
            isConfirmPasswordEmpty -> {
                setConfirmPasswordEndIcon(false)
                setConfirmPasswordErrorText(R.string.enter_confirm_password_message)
            }
            isNotEqualPasswords -> {
                showToastMessage(context.getString(R.string.password_not_match_message))
            }
            isPasswordLimitOver -> {
                setPasswordErrorText(R.string.password_over_limit_message)
            }
            else -> {
                createUserWithEmailAndPassword(emailAddress, password)
            }
        }
    }

    private fun createUserWithEmailAndPassword(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                addUserForSignup(email)
            }
            .addOnFailureListener { error ->
                error.localizedMessage?.let {
                    showToastMessage(it)
                }
            }
    }

    private fun addUserForSignup(email: String) {
        addUserToDatabase(
            email,
            onUserAdded = {
                navigateToHomeScreen()
            },
            onErrorMessage = { error ->
                showToastMessage(error)
            }
        )
    }

    private fun addUserToDatabase(
        email: String,
        onUserAdded: () -> Unit,
        onErrorMessage: (String) -> Unit
    ) {
        val user = hashMapOf(EMAIL_FIELD to email)
        database.collection(USERS_COLLECTION)
            .add(user)
            .addOnSuccessListener {
                onUserAdded()
            }
            .addOnFailureListener { error ->
                error.localizedMessage?.let {
                    onErrorMessage(it)
                }
            }
    }
}