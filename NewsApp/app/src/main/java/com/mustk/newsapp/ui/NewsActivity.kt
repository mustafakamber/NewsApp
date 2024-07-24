package com.mustk.newsapp.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.mustk.newsapp.R
import com.mustk.newsapp.databinding.ActivityNewsBinding
import com.mustk.newsapp.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewsBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSplashScreen()
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkCurrentUser()
        setupNavigationBottomView()
    }

    private fun setupSplashScreen() {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                return@setKeepOnScreenCondition viewModel.splashLoading.value
            }
        }
    }

    private fun checkCurrentUser() {
        viewModel.currentUserCheck()
    }

    private fun setupNavigationBottomView() = with(binding) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.newsFragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        navigationBottomView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.passwordFragment, R.id.signupFragment,
                R.id.detailFragment, R.id.seeMoreFragment -> {
                    updateBottomNavigationViewVisibility(false)
                }
                R.id.homeFragment, R.id.searchFragment, R.id.readListFragment,
                R.id.settingsFragment -> {
                    updateBottomNavigationViewVisibility(true)
                }
            }
        }
    }

    private fun updateBottomNavigationViewVisibility(isVisible: Boolean) {
        binding.navigationBottomView.isVisible = isVisible
    }
}