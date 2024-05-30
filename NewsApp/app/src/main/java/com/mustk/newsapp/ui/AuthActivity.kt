package com.mustk.newsapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.mustk.newsapp.R
import com.mustk.newsapp.ui.news.viewmodel.BaseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var viewModel: BaseViewModel
    private lateinit var splashScreen: SplashScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSplashScreen()
        setupViewModel()
        observeLiveData()
        checkCurrentUser()
        setContentView(R.layout.activity_auth)
    }

    private fun setupSplashScreen(){
        splashScreen = installSplashScreen()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[BaseViewModel::class.java]
    }

    private fun checkCurrentUser() {
        viewModel.currentUserCheck()
    }

    private fun navigateToHomeScreen(){
        val intentToNewsScreen = Intent(this,NewsActivity::class.java)
        startActivity(intentToNewsScreen)
        finish()
    }

    private fun observeLiveData() {
        viewModel.navigateToHome.observe(this) { event ->
            event?.let {
                navigateToHomeScreen()
            }
        }
        viewModel.splashScreenBoolean.observe(this) { boolean ->
            splashScreen.setKeepOnScreenCondition {
                if(!boolean){
                    Thread.sleep(2000)
                }
                boolean
            }
        }
    }
}