package com.mustk.newsapp.ui.news.viewmodel

import android.app.Activity
import androidx.activity.result.ActivityResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : BaseViewModel() {

    private var auth = FirebaseAuth.getInstance()

    fun inputCheckForLogin(emailAddress: String, password: String) {
        inputCheck(null, emailAddress, password, null) {
            signInWithEmailAndPassword(emailAddress, password)
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
                signInWithCredential(account)
            }
        } else {
            showToastMessage(task.exception.toString())
        }
    }

    private fun signInWithCredential(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                checkIfEmailExistsForLogin(account.email.toString())
            }
            .addOnFailureListener { error ->
                error.localizedMessage?.let {
                    showToastMessage(it)
                }
            }
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                navigateToHomeScreen(true)
            }.addOnFailureListener { error ->
                error.localizedMessage?.let {
                    showToastMessage(it)
                }
            }
    }

    private fun checkIfEmailExistsForLogin(email: String) {
        handleEmailCheck(
            email,
            onEmailNotFound = {
                addUserForLogin(email)
            },
            onEmailFound = {
                navigateToHomeScreen(true)
            },
            onErrorMessage = { error ->
                showToastMessage(error)
            }
        )
    }

    private fun addUserForLogin(email: String) {
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