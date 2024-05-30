package com.mustk.newsapp.ui.news.viewmodel

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mustk.newsapp.R
import com.mustk.newsapp.shared.Constant.EMAIL_FIELD
import com.mustk.newsapp.shared.Constant.USERS_COLLECTION
import com.mustk.newsapp.shared.Event
import com.mustk.newsapp.shared.Resource
import com.mustk.newsapp.shared.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class BaseViewModel @Inject constructor() : ViewModel() {

    private var auth = FirebaseAuth.getInstance()
    private var database = FirebaseFirestore.getInstance()

    private var currentUser = auth.currentUser

    private val _navigateToHome = MutableLiveData<Event<Boolean>>()
    val navigateToHome: LiveData<Event<Boolean>>
        get() = _navigateToHome

    private val _networkErrorMessage = MutableLiveData<Event<String>>()
    val networkErrorMessage: LiveData<Event<String>>
        get() = _networkErrorMessage

    private val _emailErrorText = MutableLiveData<Int>()
    val emailErrorText: LiveData<Int>
        get() = _emailErrorText

    private val _emailEndIconVisible = MutableLiveData<Boolean>()
    val emailEndIconVisible: LiveData<Boolean>
        get() = _emailEndIconVisible

    private val _passwordErrorText = MutableLiveData<Int>()
    val passwordErrorText: LiveData<Int>
        get() = _passwordErrorText

    private val _passwordEndIconVisible = MutableLiveData<Boolean>()
    val passwordEndIconVisible: LiveData<Boolean>
        get() = _passwordEndIconVisible

    private val _confirmPasswordErrorText = MutableLiveData<Int>()
    val confirmPasswordErrorText: LiveData<Int>
        get() = _confirmPasswordErrorText

    private val _confirmPasswordEndIconVisible = MutableLiveData<Boolean>()
    val confirmPasswordEndIconVisible: LiveData<Boolean>
        get() = _confirmPasswordEndIconVisible

    private val _splashScreenBoolean = MutableLiveData<Boolean>()
    val splashScreenBoolean: LiveData<Boolean>
        get() = _splashScreenBoolean

    init {
        setVisibilitySplashScreen(true)
    }

    private fun setVisibilitySplashScreen(boolean: Boolean) {
        _splashScreenBoolean.value = boolean
    }

    fun currentUserCheck() {
        if (currentUser != null) {
            setVisibilitySplashScreen(false)
            navigateToHomeScreen(true)
        } else {
            setVisibilitySplashScreen(false)
        }
    }

    fun navigateToHomeScreen(isSuccess: Boolean) {
        _navigateToHome.value = Event(isSuccess)
    }

    fun setVisibleEmailEndIcon() {
        _emailEndIconVisible.value = true
    }

    private fun setInvisibleEmailEndIcon() {
        _emailEndIconVisible.value = false
        _emailErrorText.value = R.string.enter_email_message
    }

    fun setVisiblePasswordEndIcon() {
        _passwordEndIconVisible.value = true
    }

    private fun setInvisiblePasswordEndIcon(warning: Int) {
        _passwordEndIconVisible.value = false
        _passwordErrorText.value = warning
    }

    fun setVisibleConfirmPasswordEndIcon() {
        _confirmPasswordEndIconVisible.value = true
    }

    private fun setInvisibleConfirmPasswordEndIcon() {
        _confirmPasswordEndIconVisible.value = false
        _confirmPasswordErrorText.value = R.string.enter_confirm_password_message
    }

    fun showToastMessage(message: String) {
        _networkErrorMessage.value = Event(message)
    }

    fun handleEmailCheck(
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

    fun addUserToDatabase(
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

    fun inputCheck(
        context: Context? = null, emailAddress: String,
        password: String? = null, confirmPassword: String? = null, onSuccess: () -> Unit
    ) {
        var hasError = false
        if (TextUtils.isEmpty(emailAddress)) {
            setInvisibleEmailEndIcon()
            hasError = true
        }
        password?.let {
            if (TextUtils.isEmpty(it)) {
                setInvisiblePasswordEndIcon(R.string.enter_password_message)
                hasError = true
            }
        }
        confirmPassword?.let {
            if (TextUtils.isEmpty(it)) {
                setInvisibleConfirmPasswordEndIcon()
                hasError = true
            } else if (it != password) {
                context?.let { context ->
                    showToastMessage(context.getString(R.string.password_not_match_message))
                    hasError = true
                }
            }
        }
        if (password != null && password.length >= 12) {
            setInvisiblePasswordEndIcon(R.string.password_over_limit_message)
            hasError = true
        }
        if (!hasError) {
            onSuccess()
        }
    }
}