package com.mustk.newsapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mustk.newsapp.R
import com.mustk.newsapp.ui.news.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSplashScreen()
        observeLiveData()
        checkCurrentUser()
        setContentView(R.layout.activity_auth)
    }

    private fun setupSplashScreen(){
        installSplashScreen().apply {
            setKeepOnScreenCondition{
                return@setKeepOnScreenCondition viewModel.splashLoading.value
            }
        }
    }

    private fun checkCurrentUser() {
        viewModel.currentUserCheck()
    }

    private fun navigateToHomeScreen(){
        val intentToNewsActivity = Intent(this,NewsActivity::class.java)
        startActivity(intentToNewsActivity)
        finish()
    }

    private fun observeLiveData() {
        viewModel.navigateToHome.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                navigateToHomeScreen()
            }
        }
    }
}