package com.mustk.newsapp.ui.viewmodel

import android.app.Activity
import android.text.TextUtils
import androidx.activity.result.ActivityResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.mustk.newsapp.R
import com.mustk.newsapp.data.model.User
import com.mustk.newsapp.shared.Constant.DEFAULT_PHOTO_FIELD
import com.mustk.newsapp.shared.Constant.EMAIL_FIELD
import com.mustk.newsapp.shared.Constant.PHOTO_FIELD
import com.mustk.newsapp.shared.Constant.USERS_COLLECTION
import com.mustk.newsapp.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
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

    fun setEmailEndIcon(boolean: Boolean) {
        _emailEndIconVisibility.value = boolean
    }

    fun setPasswordEndIcon(boolean: Boolean) {
        _passwordEndIconVisibility.value = boolean
    }

    private fun setEmailErrorText(int: Int) {
        _emailErrorText.value = Event(int)
    }

    private fun setPasswordErrorText(int: Int) {
        _passwordErrorText.value = Event(int)
    }

    private fun navigateToHomeScreen() {
        _navigateToHome.value = Event(true)
    }

    fun inputCheckForLogin(emailAddress: String, password: String) {
        val isEmailEmpty = TextUtils.isEmpty(emailAddress)
        val isPasswordEmpty = TextUtils.isEmpty(password)
        when {
            isEmailEmpty -> {
                setEmailEndIcon(false)
                setEmailErrorText(R.string.enter_email_message)
            }
            isPasswordEmpty -> {
                setPasswordEndIcon(false)
                setPasswordErrorText(R.string.enter_password_message)
            }
            else -> {
                signInEmailPassword(emailAddress, password)
            }
        }

    }

    fun handleSignInGoogleRequest(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                signInGoogleAccount(account)
            }
        } else {
            setToastMessage(task.exception.toString())
        }
    }

    private fun signInGoogleAccount(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                isRegisteredGoogleUserInDb(account.email.toString()) { isRegistered ->
                    if (isRegistered) {
                        navigateToHomeScreen()
                    } else {
                        val newUser = addUserToModel(account)
                        addUserToDatabase(newUser)
                    }
                }
            }
            .addOnFailureListener { error ->
                error.localizedMessage?.let {
                    setToastMessage(it)
                }
            }
    }

    private fun addUserToModel(account: GoogleSignInAccount): User {
        val user = User(account.email.toString(), account.photoUrl.toString(), true)
        return user
    }

    private fun signInEmailPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                navigateToHomeScreen()
            }.addOnFailureListener { error ->
                error.localizedMessage?.let {
                    setToastMessage(it)
                }
            }
    }

    private fun isRegisteredGoogleUserInDb(email: String, callback: (Boolean) -> Unit) {
        database.collection(USERS_COLLECTION)
            .whereEqualTo(EMAIL_FIELD, email)
            .get()
            .addOnSuccessListener { documents ->
                callback(documents.isEmpty.not())
            }
            .addOnFailureListener { error ->
                callback(false)
                error.localizedMessage?.let {
                    setToastMessage(it)
                }
            }
    }

    private fun addUserToDatabase(user: User) {
        val userDoc = hashMapOf(
            EMAIL_FIELD to user.email,
            PHOTO_FIELD to user.photoUrl,
            DEFAULT_PHOTO_FIELD to user.defaultPhoto
        )
        database.collection(USERS_COLLECTION)
            .add(userDoc)
            .addOnSuccessListener {
                navigateToHomeScreen()
            }
            .addOnFailureListener { error ->
                error.localizedMessage?.let {
                    setToastMessage(it)
                }
            }
    }
}