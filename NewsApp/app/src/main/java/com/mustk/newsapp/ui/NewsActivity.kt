package com.mustk.newsapp.ui

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.mustk.newsapp.R
import com.mustk.newsapp.databinding.ActivityNewsBinding
import com.mustk.newsapp.shared.Constant.LANGUAGE_EN
import com.mustk.newsapp.shared.Constant.LANGUAGE_TR
import com.mustk.newsapp.ui.viewmodel.NewsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class NewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewsBinding
    private val viewModel: NewsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSplashScreen()
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //checkUILanguage()
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

    private fun checkUILanguage() {
        val languagePref = getLanguagePreferences()
        if (languagePref == LANGUAGE_TR && currentLanguage() != LANGUAGE_TR) {
            changeUILanguage(languagePref)
        } else if (languagePref == LANGUAGE_EN && currentLanguage() != LANGUAGE_EN) {
            changeUILanguage(languagePref)
        }
    }

    private fun currentLanguage(): String {
        val locale: Locale =
            this.resources.configuration.locales.get(0)
        return locale.language
    }

    private fun getLanguagePreferences() : String? {
        return viewModel.languageString.value
    }

    private fun changeUILanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        this.resources.updateConfiguration(
            config,
            this.resources.displayMetrics
        )
        recreate()
    }
}