package com.mustk.newsapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.mustk.newsapp.R
import com.mustk.newsapp.databinding.ActivityNewsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigationBottomView()
    }

    private fun setupNavigationBottomView() = with(binding) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.newsFragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        navigationBottomView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.detailFragment, R.id.seeMoreFragment -> navigationBottomView.isVisible = false
                else -> navigationBottomView.isVisible = true
            }
        }
    }
}