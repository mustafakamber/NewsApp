package com.mustk.newsapp

import android.app.Application
import com.mustk.newsapp.broadcastreceiver.NotificationReceiver
import com.mustk.newsapp.services.ThemeService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NewsApplication : Application(){

    @Inject lateinit var themeService : ThemeService

    override fun onCreate() {
        super.onCreate()
        scheduleNextAlarm()
        themeService.checkUITheme()
    }
    private fun scheduleNextAlarm(){
        NotificationReceiver.scheduleNextAlarm(this)
    }
}