package com.mustk.newsapp.ui.news.viewmodel

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor() : BaseViewModel() {

    private var auth = FirebaseAuth.getInstance()

    fun inputCheckForSignup(
        context: Context,
        emailAddress: String,
        password: String,
        confirmPassword: String
    ) {
        inputCheck(context, emailAddress, password, confirmPassword) {
            createUserWithEmailAndPassword(emailAddress, password)
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
                navigateToHomeScreen(true)
            },
            onErrorMessage = { error ->
                showToastMessage(error)
            }
        )
    }
}