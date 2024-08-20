package com.mustk.newsapp.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mustk.newsapp.data.model.ActionSnackBarMessage
import com.mustk.newsapp.data.model.User
import com.mustk.newsapp.shared.Constant.DEFAULT_PHOTO_FIELD
import com.mustk.newsapp.shared.Constant.DEFAULT_PROFILE_IMAGE_URL
import com.mustk.newsapp.shared.Constant.EMAIL_FIELD
import com.mustk.newsapp.shared.Constant.PHOTO_FIELD
import com.mustk.newsapp.shared.Constant.STORAGE_REFERENCE
import com.mustk.newsapp.shared.Constant.USERS_COLLECTION
import com.mustk.newsapp.shared.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChangeProfileViewModel @Inject constructor(
    private val database: FirebaseFirestore,
    private val storage: FirebaseStorage,
) : BaseViewModel() {

    private val userEmail = MutableLiveData<String>()

    private val _navigateToGallery = MutableLiveData<Event<Boolean>>()
    val navigateToGallery: LiveData<Event<Boolean>>
        get() = _navigateToGallery

    private val _backToSettings = MutableLiveData<Event<Boolean>>()
    val backToSettings : LiveData<Event<Boolean>>
        get() = _backToSettings

    private val _userPhoto = MutableLiveData<String>()
    val userPhoto: LiveData<String>
        get() = _userPhoto

    private val _actionSnackBarMessage = MutableLiveData<Event<ActionSnackBarMessage>>()
    val actionSnackBarMessage: LiveData<Event<ActionSnackBarMessage>>
        get() = _actionSnackBarMessage

    fun setUserEmail(string: String) {
        userEmail.value = string
    }

    private fun backToSettings(){
        _backToSettings.value = Event(true)
    }

    fun deleteProfilePhoto() {
        isDefaultProfilePhoto { isDefault ->
            if (!isDefault) {
                deletePhotoFromStorage()
            }
        }
    }

    fun saveNewProfilePhoto(photoUri: Uri?) {
        photoUri?.let { uri ->
            isDefaultProfilePhoto { isDefault ->
                if (isDefault) {
                    addNewPhotoToStorage(uri)
                } else {
                    deletePreviousPhotoFromStorage(uri)
                }
            }
        }
    }

    private fun deletePhotoFromStorage() {
        userEmail.value?.let { email ->
            getImageStorageReference(email)
                .delete()
                .addOnSuccessListener {
                    val updatedUser =
                        updateUserModel(
                            email,
                            DEFAULT_PROFILE_IMAGE_URL,
                            true
                        )
                    setPhotoFieldInDatabase(updatedUser)
                }
                .addOnFailureListener { error ->
                    error.localizedMessage?.let { setToastMessage(it) }
                }
        }
    }

    private fun deletePreviousPhotoFromStorage(newPhotoUri: Uri) {
        userEmail.value?.let { email ->
            getImageStorageReference(email)
                .delete()
                .addOnSuccessListener {
                    addNewPhotoToStorage(newPhotoUri)
                }
                .addOnFailureListener { error ->
                    error.localizedMessage?.let { setToastMessage(it) }
                }
        }
    }

    private fun getImageStorageReference(email: String): StorageReference {
        val imageStorageRef =
            storage.reference.child("$STORAGE_REFERENCE/${email}.jpg")
        return imageStorageRef
    }

    private fun isDefaultProfilePhoto(onResult: (Boolean) -> Unit) {
        userEmail.value?.let { email ->
            getUserCollection(email)
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val isDefaultPhoto = document.getBoolean(DEFAULT_PHOTO_FIELD) ?: false
                        onResult(isDefaultPhoto)
                        return@addOnSuccessListener
                    }
                    onResult(false)
                }
                .addOnFailureListener { error ->
                    error.localizedMessage?.let {
                        setToastMessage(it)
                    }
                    onResult(false)
                }
        }
    }

    private fun updateUserModel(email: String, photoUrl: String, defaultPhoto: Boolean): User {
        val updatedUser = User(email, photoUrl, defaultPhoto)
        return updatedUser
    }

    private fun addNewPhotoToStorage(newPhotoUri: Uri) {
        userEmail.value?.let { email ->
            getImageStorageReference(email)
                .putFile(newPhotoUri)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl
                        .addOnSuccessListener { downloadUrl ->
                            val updatedUser =
                                updateUserModel(
                                    email,
                                    downloadUrl.toString(),
                                    false
                                )
                            setPhotoFieldInDatabase(updatedUser)
                        }
                }
                .addOnFailureListener { error ->
                    error.localizedMessage?.let { setToastMessage(it) }
                }
        }
    }

    private fun setPhotoFieldInDatabase(user: User) {
        userEmail.value?.let { email ->
            getUserCollection(email)
                .addOnSuccessListener { querySnapshot ->
                    updateUserDocument(querySnapshot, user)
                }
                .addOnFailureListener { error ->
                    error.localizedMessage?.let {
                        setToastMessage(it)
                    }
                }
        }
    }

    private fun updateUserDocument(querySnapshot: QuerySnapshot, user: User) {
        for (document in querySnapshot.documents) {
            val documentReference = document.reference
            val updatedUserDoc = hashMapOf<String, Any>(
                PHOTO_FIELD to user.photoUrl,
                DEFAULT_PHOTO_FIELD to user.defaultPhoto
            )
            documentReference.update(updatedUserDoc)
                .addOnSuccessListener {
                    _userPhoto.value = user.photoUrl
                    backToSettings()
                }
                .addOnFailureListener { error ->
                    error.localizedMessage?.let {
                        setToastMessage(it)
                    }
                }
        }
    }

    private fun getUserCollection(email: String): Task<QuerySnapshot> {
        val userDocRef = database.collection(USERS_COLLECTION)
            .whereEqualTo(EMAIL_FIELD, email)
            .get()
        return userDocRef
    }
}