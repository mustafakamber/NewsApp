package com.mustk.newsapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mustk.newsapp.R
import com.mustk.newsapp.data.model.User
import com.mustk.newsapp.util.Constant.DEFAULT_PHOTO_FIELD
import com.mustk.newsapp.util.Constant.DEFAULT_PROFILE_IMAGE_URL
import com.mustk.newsapp.util.Constant.EMAIL_FIELD
import com.mustk.newsapp.util.Constant.PASSWORD_LIMIT
import com.mustk.newsapp.util.Constant.PHOTO_FIELD
import com.mustk.newsapp.util.Constant.USERS_COLLECTION
import com.mustk.newsapp.util.Event
import com.mustk.newsapp.util.NetworkHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseFirestore,
    @ApplicationContext private val context: Context,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    private val _checkNetworkConnection = MutableLiveData<Event<Boolean>>()
    val checkNetworkConnection: LiveData<Event<Boolean>>
        get() = _checkNetworkConnection

    private val _emailError = MutableLiveData<Int>()
    val emailError: LiveData<Int>
        get() = _emailError

    private val _passwordError = MutableLiveData<Int>()
    val passwordError: LiveData<Int>
        get() = _passwordError

    private val _confirmPasswordError = MutableLiveData<Int>()
    val confirmPasswordError: LiveData<Int>
        get() = _confirmPasswordError

    private val _authError = MutableLiveData<String>()
    val authError: LiveData<String>
        get() = _authError

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _success = MutableLiveData<Event<Boolean>>()
    val success: LiveData<Event<Boolean>>
        get() = _success

    private fun setCheckConnection() {
        _checkNetworkConnection.value = Event(true)
    }

    fun isConnectedNetwork(): Boolean {
        return networkHelper.isNetworkConnected()
    }

    private fun setSuccess() {
        _success.value = Event(true)
    }

    private fun setLoading(isLoading : Boolean){
        _loading.value = isLoading
    }

    private fun setAuthError(error : String){
        _authError.value = error
    }

    fun validateInput(
        emailAddress: String,
        password: String,
        confirmPassword: String
    ) {
        if (emailAddress.isBlank()){
            _emailError.value = R.string.enter_email_message
        }
        if (password.isBlank()){
            _passwordError.value = R.string.enter_password_message
        }
        if (confirmPassword.isBlank()){
            _confirmPasswordError.value = R.string.enter_confirm_password_message
        }
        if (password != confirmPassword) {
            _authError.value = context.getString(R.string.password_not_match_message)
        }
        if (password.length > PASSWORD_LIMIT ) {
            _passwordError.value = R.string.password_over_limit_message
        }
        if (emailAddress.isNotBlank() && password.isNotBlank()){
            createUser(emailAddress,password)
        }
    }

    private fun createUser(email: String, password: String) {
        if (isConnectedNetwork()){
            setLoading(true)
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val newUser = createUserModel(email)
                    addUserToDatabase(newUser)
                }
                .addOnFailureListener { error ->
                    error.localizedMessage?.let {
                        setLoading(false)
                        setAuthError(it)
                    }
                }
        }else{
            setCheckConnection()
        }
    }

    private fun createUserModel(email: String): User {
        val user = User(email, DEFAULT_PROFILE_IMAGE_URL, true)
        return user
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
                    setAuthError(it)
                }
            }
    }
}