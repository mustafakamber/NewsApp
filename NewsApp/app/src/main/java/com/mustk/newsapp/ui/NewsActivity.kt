package com.mustk.newsapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.mustk.newsapp.R
import com.mustk.newsapp.databinding.ActivityNewsBinding
import com.mustk.newsapp.di.factory.NewsFragmentFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewsBinding
    @Inject
    lateinit var fragmentFactory: NewsFragmentFactory

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
    }
}