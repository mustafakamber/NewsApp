package com.mustk.newsapp.ui.viewmodel

import android.app.Activity
import androidx.activity.result.ActivityResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.mustk.newsapp.R
import com.mustk.newsapp.data.model.User
import com.mustk.newsapp.util.Constant.DEFAULT_PHOTO_FIELD
import com.mustk.newsapp.util.Constant.EMAIL_FIELD
import com.mustk.newsapp.util.Constant.PHOTO_FIELD
import com.mustk.newsapp.util.Constant.USERS_COLLECTION
import com.mustk.newsapp.util.Event
import com.mustk.newsapp.util.NetworkHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseFirestore,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    private val _currentUserActive = MutableLiveData<Event<Boolean>>()
    val currentUserActive: LiveData<Event<Boolean>>
        get() = _currentUserActive

    private val _checkNetworkConnection = MutableLiveData<Event<Boolean>>()
    val checkNetworkConnection: LiveData<Event<Boolean>>
        get() = _checkNetworkConnection

    private val _emailError = MutableLiveData<Int>()
    val emailError: LiveData<Int>
        get() = _emailError

    private val _passwordError = MutableLiveData<Int>()
    val passwordError: LiveData<Int>
        get() = _passwordError

    private val _authError = MutableLiveData<String>()
    val authError: LiveData<String>
        get() = _authError

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _success = MutableLiveData<Event<Boolean>>()
    val success: LiveData<Event<Boolean>>
        get() = _success

    private fun setCurrentUserActive() {
        _currentUserActive.value = Event(true)
    }

    private fun setSuccess() {
        _success.value = Event(true)
    }

    private fun setLoading(isLoading: Boolean) {
        _loading.value = isLoading
    }

    private fun setAuthError(error: String) {
        _authError.value = error
    }

    private fun setCheckConnection() {
        _checkNetworkConnection.value = Event(true)
    }

    fun checkCurrentUser() {
        val isActiveUser = currentUserActivated()
        if (isActiveUser) setCurrentUserActive()
    }

    private fun currentUserActivated(): Boolean {
        return auth.currentUser != null
    }

    fun isConnectedNetwork(): Boolean {
        return networkHelper.isNetworkConnected()
    }

    fun inputValidate(emailAddress: String, password: String) {
        if (emailAddress.isBlank()) {
            _emailError.value = R.string.enter_email_message
        }
        if (password.isBlank()) {
            _passwordError.value = R.string.enter_password_message
        }
        if (emailAddress.isNotBlank() && password.isNotBlank()) {
            signInEmailPassword(emailAddress, password)
        }
    }

    fun handleSignInGoogleRequest(result: ActivityResult) {
        if (isConnectedNetwork()){
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResults(task)
            }
        }else{
            setCheckConnection()
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        setLoading(true)
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                signInGoogleAccount(account)
            }
        } else {
            setLoading(false)
            setAuthError(task.exception.toString())
        }
    }

    private fun signInGoogleAccount(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                isRegisteredGoogleUserInDb(account.email.toString()) { isRegistered ->
                    if (isRegistered) {
                        setLoading(false)
                        setSuccess()
                    } else {
                        val newUser = addUserToModel(account)
                        addUserToDatabase(newUser)
                    }
                }
            }
            .addOnFailureListener { error ->
                error.localizedMessage?.let {
                    setLoading(false)
                    setAuthError(it)
                }
            }
    }

    private fun addUserToModel(account: GoogleSignInAccount): User {
        val user = User(account.email.toString(), account.photoUrl.toString(), true)
        return user
    }

    private fun signInEmailPassword(email: String, password: String) {
        if (isConnectedNetwork()){
            setLoading(true)
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    setLoading(false)
                    setSuccess()
                }.addOnFailureListener { error ->
                    error.localizedMessage?.let {
                        setLoading(false)
                        setAuthError(it)
                    }
                }
        }else{
            setCheckConnection()
        }
    }

    private fun isRegisteredGoogleUserInDb(email: String, callback: (Boolean) -> Unit) {
        database.collection(USERS_COLLECTION)
            .whereEqualTo(EMAIL_FIELD, email)
            .get()
            .addOnSuccessListener { documents ->
                val isUserRegistered = documents.isEmpty.not()
                callback(isUserRegistered)
            }
            .addOnFailureListener { error ->
                callback(false)
                error.localizedMessage?.let {
                    setLoading(false)
                    setAuthError(it)
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
                setLoading(false)
                setSuccess()
            }
            .addOnFailureListener { error ->
                error.localizedMessage?.let {
                    setLoading(false)
                    setAuthError(it)
                }
            }
    }
}