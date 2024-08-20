package com.mustk.newsapp.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.mustk.newsapp.data.datasource.NewsDataSource
import com.mustk.newsapp.shared.Constant.DARK_THEME
import com.mustk.newsapp.shared.Constant.DARK_THEME_ENABLED
import com.mustk.newsapp.shared.Constant.EMAIL_FIELD
import com.mustk.newsapp.shared.Constant.LANGUAGE_EN
import com.mustk.newsapp.shared.Constant.LANGUAGE_ITEM
import com.mustk.newsapp.shared.Constant.LIGHT_THEME
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
import java.util.Locale
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

    private val _lastSelectedLanguageTabPosition = MutableLiveData<Int>()
    val lastSelectedTabLanguagePosition: LiveData<Int> get() = _lastSelectedLanguageTabPosition

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
        setLanguageLastSelectedTabPosition(initTabPosition())
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

    private fun setLanguageLastSelectedTabPosition(position: Int) {
        _lastSelectedLanguageTabPosition.value = position
    }

    private fun getCurrentLanguage(): String {
        val locale: Locale =
            context.resources.configuration.locales.get(0)
        return locale.language
    }

    private fun setSelectedLanguage(language: String) {
        selectedLanguage = language
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
            setSelectedLanguage(language)
            setLanguageLastSelectedTabPosition(position)
            changeAppLanguage(language)
            saveLanguagePreference(language)
        }
    }

    private fun fetchUserInfoFromCloudDatabase() {
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
                        setToastMessage(it)
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
        userEmail.value?.let { email ->
            getUserCollection(email)
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        document.reference.delete()
                            .addOnFailureListener { error ->
                                error.localizedMessage?.let {
                                    setToastMessage(it)
                                }
                            }
                    }
                }
                .addOnFailureListener { error ->
                    error.localizedMessage?.let {
                        setToastMessage(it)
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
                                        setToastMessage(it)
                                    }
                                }
                        }
                    }
                }
                .addOnFailureListener { error ->
                    error.localizedMessage?.let {
                        setToastMessage(it)
                    }
                }
        }
    }

    fun logOutButtonClicked() {
        auth.signOut()
        navigateToLoginScreen()
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

    private fun navigateToLoginScreen() {
        _navigateToLogin.value = Event(true)
    }

    private fun changeAppLanguage(language: String) {
        _changeLanguage.value = Event(language)
    }
}