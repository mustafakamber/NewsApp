package com.mustk.newsapp.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mustk.newsapp.data.datasource.NewsDataSource
import com.mustk.newsapp.shared.Constant.DARK_THEME_ENABLED
import com.mustk.newsapp.shared.Constant.EMAIL_FIELD
import com.mustk.newsapp.shared.Constant.LANGUAGE_EN
import com.mustk.newsapp.shared.Constant.LANGUAGE_ITEM
import com.mustk.newsapp.shared.Constant.NEWS_COLLECTION
import com.mustk.newsapp.shared.Constant.NOTIFICATIONS_ENABLED
import com.mustk.newsapp.shared.Constant.PHOTO_FIELD
import com.mustk.newsapp.shared.Constant.SHARED_PREFS_NAME
import com.mustk.newsapp.shared.Constant.USERS_COLLECTION
import com.mustk.newsapp.shared.Constant.USER_FIELD
import com.mustk.newsapp.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseFirestore,
    private val repository: NewsDataSource,
    @ApplicationContext private val context: Context
) : BaseViewModel() {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val editor: SharedPreferences.Editor by lazy {
        sharedPreferences.edit()
    }

    private val _userPhoto = MutableLiveData<String>()
    val userPhoto: LiveData<String>
        get() = _userPhoto

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String>
        get() = _userEmail

    private val _navigateToLogin = MutableLiveData<Event<Boolean>>()
    val navigateToLogin: LiveData<Event<Boolean>>
        get() = _navigateToLogin

    private val _notificationEnabled = MutableLiveData<Boolean>()
    val notificationEnabled : LiveData<Boolean>
        get() = _notificationEnabled

    private val _darkThemeEnabled = MutableLiveData<Boolean>()
    val darkThemeEnabled : LiveData<Boolean>
        get() = _darkThemeEnabled

    private val _languageItem = MutableLiveData<String>()
    val languageItem : LiveData<String>
        get() = _languageItem

    init {
        loadPreferences()
        fetchUserInfoFromCloudDatabase()
    }

    private fun loadPreferences() {
        sharedPreferences.apply {
            _notificationEnabled.value = getBoolean(NOTIFICATIONS_ENABLED, true)
            _darkThemeEnabled.value = getBoolean(DARK_THEME_ENABLED, false)
            _languageItem.value = getString(LANGUAGE_ITEM, LANGUAGE_EN)
        }
    }

    fun saveNotificationPreference(enabled: Boolean) {
        _notificationEnabled.value = enabled
        editor.putBoolean(NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun saveDarkThemePreference(enabled: Boolean) {
        _darkThemeEnabled.value = enabled
        editor.putBoolean(DARK_THEME_ENABLED, enabled).apply()
    }

    fun saveLanguagePreference(languageCode: String) {
        _languageItem.value = languageCode
        editor.putString(LANGUAGE_ITEM, languageCode).apply()
    }

    private fun fetchUserInfoFromCloudDatabase() {
        val currentUser = auth.currentUser
        if (currentUser?.email != null) {
            database.collection(USERS_COLLECTION)
                .whereEqualTo(EMAIL_FIELD, currentUser.email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        _userPhoto.value = document.getString(PHOTO_FIELD)
                        _userEmail.value = document.getString(EMAIL_FIELD)
                    }
                }
                .addOnFailureListener { error ->
                    error.localizedMessage?.let {
                        showToastMessage(it)
                    }
                }
        }
    }

    fun deleteAccountButtonClicked() {
        deleteAllNewsFromLocal()
        deleteAllNewsFromCloud()
        deleteAccountFromCloud()
        deleteAccountFromAuth()
    }

    private fun deleteAccountFromCloud() {
        _userEmail.value?.let { email ->
            val userRef = database.collection(USERS_COLLECTION)
                .whereEqualTo(EMAIL_FIELD, email)
            userRef.get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        document.reference.delete()
                            .addOnFailureListener { error ->
                                error.localizedMessage?.let {
                                    showToastMessage(it)
                                }
                            }
                    }
                }
                .addOnFailureListener { error ->
                    error.localizedMessage?.let {
                        showToastMessage(it)
                    }
                }
        }
    }

    private fun deleteAccountFromAuth(){
        val currentUser = auth.currentUser
        currentUser?.let { firebaseUser ->
            firebaseUser.delete()
                .addOnSuccessListener {
                    navigateToLoginScreen()
                }
        }
    }

    private fun deleteAllNewsFromLocal() {
        _userEmail.value?.let {
            viewModelScope.launch {
                repository.deleteNewsList(it)
            }
        }
    }

    private fun deleteAllNewsFromCloud() {
        _userEmail.value?.let { email ->
            database.collection(NEWS_COLLECTION)
                .whereEqualTo(USER_FIELD, email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val documents = querySnapshot.documents
                    if (documents.isNotEmpty()) {
                        for (document in documents) {
                            document.reference.delete()
                                .addOnFailureListener { error ->
                                    error.localizedMessage?.let {
                                        showToastMessage(it)
                                    }
                                }
                        }
                    }
                }.addOnFailureListener { error ->
                    error.localizedMessage?.let {
                        showToastMessage(it)
                    }
                }
        }
    }

    fun logOutButtonClicked() {
        auth.signOut()
        navigateToLoginScreen()
    }

    private fun navigateToLoginScreen() {
        _navigateToLogin.value = Event(true)
    }
}