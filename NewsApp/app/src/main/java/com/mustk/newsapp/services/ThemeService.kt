package com.mustk.newsapp.services

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.mustk.newsapp.shared.Constant.DARK_THEME
import com.mustk.newsapp.shared.Constant.DARK_THEME_ENABLED
import com.mustk.newsapp.shared.Constant.DEFAULT_THEME
import com.mustk.newsapp.shared.Constant.LIGHT_THEME
import com.mustk.newsapp.shared.Constant.SHARED_PREFS_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ThemeService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val sharedPreferences by lazy {
        context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }

     fun checkUITheme() {
        val themePref = getThemePreferences()
        if (themePref == DARK_THEME && !isNightTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else if (themePref == LIGHT_THEME && isNightTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun isNightTheme(): Boolean {
        val currentTheme =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentTheme == Configuration.UI_MODE_NIGHT_YES
    }

    private fun getThemePreferences(): String? {
        return sharedPreferences.getString(DARK_THEME_ENABLED, DEFAULT_THEME)
    }
}