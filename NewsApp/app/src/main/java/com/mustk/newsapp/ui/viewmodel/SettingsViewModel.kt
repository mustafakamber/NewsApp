package com.mustk.newsapp.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.mustk.newsapp.data.datasource.NewsDataSource
import com.mustk.newsapp.util.Constant.DARK_THEME
import com.mustk.newsapp.util.Constant.DARK_THEME_ENABLED
import com.mustk.newsapp.util.Constant.EMAIL_FIELD
import com.mustk.newsapp.util.Constant.LANGUAGE_EN
import com.mustk.newsapp.util.Constant.LANGUAGE_ITEM
import com.mustk.newsapp.util.Constant.LIGHT_THEME
import com.mustk.newsapp.util.Constant.NEWS_COLLECTION
import com.mustk.newsapp.util.Constant.NOTIFICATIONS_ENABLED
import com.mustk.newsapp.util.Constant.PHOTO_FIELD
import com.mustk.newsapp.util.Constant.SHARED_PREFS_NAME
import com.mustk.newsapp.util.Constant.USERS_COLLECTION
import com.mustk.newsapp.util.Constant.USER_FIELD
import com.mustk.newsapp.util.Event
import com.mustk.newsapp.util.NetworkHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseFirestore,
    private val repository: NewsDataSource,
    private val networkHelper: NetworkHelper,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val editor: SharedPreferences.Editor by lazy {
        sharedPreferences.edit()
    }

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String>
        get() = _userEmail

    private val _navigateToLogin = MutableLiveData<Event<Boolean>>()
    val navigateToLogin: LiveData<Event<Boolean>>
        get() = _navigateToLogin

    private val _changeLanguage = MutableLiveData<Event<String>>()
    val changeLanguage: LiveData<Event<String>>
        get() = _changeLanguage

    private val _notificationEnabled = MutableLiveData<Boolean>()
    val notificationEnabled : LiveData<Boolean>
        get() = _notificationEnabled

    private val _userPhoto = MutableLiveData<String>()
    val userPhoto: LiveData<String>
        get() = _userPhoto

    private val _toastError = MutableLiveData<String>()
    val toastError: LiveData<String>
        get() = _toastError

    private val _snackBarError = MutableLiveData<Event<Int>>()
    val snackBarError: LiveData<Event<Int>>
        get() = _snackBarError

    private val _checkConnection = MutableLiveData<Event<Boolean>>()
    val checkConnection: LiveData<Event<Boolean>>
        get() = _checkConnection

    private val _lastSelectedLanguageTabPosition = MutableLiveData<Int>()
    val lastSelectedTabLanguagePosition: LiveData<Int> get() = _lastSelectedLanguageTabPosition

    val isConnectedNetwork: Boolean
        get() = networkHelper.isNetworkConnected()

    /*
    private var initLanguage = sharedPreferences.getString(LANGUAGE_ITEM, DEFAULT_LANGUAGE)

    private var selectedLanguage = if (initLanguage == DEFAULT_LANGUAGE){
        getCurrentLanguage()
    }else{
        initLanguage
    }
     */

    private var selectedLanguage = getCurrentLanguage()

    init {
        _lastSelectedLanguageTabPosition.value = initTabPosition()
        fetchUserInfoFromCloudDatabase()
        loadPreferences()
    }

    private fun initTabPosition() : Int {
        return if (selectedLanguage == LANGUAGE_EN){
            0
        } else {
            1
        }
    }

    private fun getCurrentLanguage(): String {
        val locale: Locale =
            context.resources.configuration.locales.get(0)
        return locale.language
    }

    private fun loadPreferences() {
        sharedPreferences.apply {
            _notificationEnabled.value = getBoolean(NOTIFICATIONS_ENABLED, true)
        }
    }

    fun saveNotificationPreference(enabled: Boolean) {
        _notificationEnabled.value = enabled
        editor.putBoolean(NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun saveDarkThemePreference(enabled: Boolean) {
        val themeString = if (enabled) {
            DARK_THEME
        } else {
            LIGHT_THEME
        }
        editor.putString(DARK_THEME_ENABLED, themeString).apply()
    }

    private fun saveLanguagePreference(languageCode: String) {
        editor.putString(LANGUAGE_ITEM, languageCode).apply()
    }

    fun onLanguageChange(language: String, position: Int) {
        if (language != selectedLanguage) {
            selectedLanguage = language
            _lastSelectedLanguageTabPosition.value = position
            _changeLanguage.value = Event(language)
            saveLanguagePreference(language)
        }
    }

    private fun fetchUserInfoFromCloudDatabase() {
        if (!isConnectedNetwork) {
            _checkConnection.value = Event(true)
        } else {
            val currentUser = auth.currentUser
            if (currentUser?.email != null) {
                getUserCollection(currentUser.email.toString())
                    .addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot.documents) {
                            document.getString(PHOTO_FIELD)?.let {
                                _userPhoto.value = it
                            }
                            _userEmail.value = document.getString(EMAIL_FIELD)
                        }
                    }
                    .addOnFailureListener { error ->
                        error.localizedMessage?.let {
                            _toastError.value = it
                        }
                    }
            }
        }
    }

    fun deleteAccountButtonClicked() {
        if (isConnectedNetwork) {
            deleteAllNewsFromLocal()
            deleteAllNewsFromCloud()
            deleteAccountFromCloud()
            deleteAccountFromAuth()
        } else {
            _checkConnection.value = Event(true)
        }
    }

    private fun deleteAccountFromCloud() {
        userEmail.value?.let { email ->
            getUserCollection(email)
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        document.reference.delete()
                            .addOnFailureListener { error ->
                                error.localizedMessage?.let {
                                    _toastError.value = it
                                }
                            }
                    }
                }
                .addOnFailureListener { error ->
                    error.localizedMessage?.let {
                        _toastError.value = it
                    }
                }
        }
    }

    private fun deleteAccountFromAuth(){
        val currentUser = auth.currentUser
        currentUser?.let { firebaseUser ->
            firebaseUser.delete()
                .addOnSuccessListener {
                    _navigateToLogin.value = Event(true)
                }
        }
    }

    private fun deleteAllNewsFromLocal() {
        _userEmail.value?.let {
            viewModelScope.launch {
                repository.deleteNewsListByUserFromRoom(it)
            }
        }
    }

    private fun deleteAllNewsFromCloud() {
        userEmail.value?.let { email ->
            getNewsCollection(email)
                .addOnSuccessListener { querySnapshot ->
                    val documents = querySnapshot.documents
                    if (documents.isNotEmpty()) {
                        for (document in documents) {
                            document.reference.delete()
                                .addOnFailureListener { error ->
                                    error.localizedMessage?.let {
                                        _toastError.value = it
                                    }
                                }
                        }
                    }
                }
                .addOnFailureListener { error ->
                    error.localizedMessage?.let {
                        _toastError.value = it
                    }
                }
        }
    }

    fun logOutButtonClicked() {
        if (isConnectedNetwork) {
            auth.signOut()
            _navigateToLogin.value = Event(true)
        } else {
            _checkConnection.value = Event(true)
        }
    }

    private fun getUserCollection(email: String): Task<QuerySnapshot> {
        val userDocRef = database.collection(USERS_COLLECTION)
            .whereEqualTo(EMAIL_FIELD, email)
            .get()
        return userDocRef
    }

    private fun getNewsCollection(email: String): Task<QuerySnapshot> {
        val newsDocRef = database.collection(NEWS_COLLECTION)
            .whereEqualTo(USER_FIELD, email)
            .get()
        return newsDocRef
    }
}