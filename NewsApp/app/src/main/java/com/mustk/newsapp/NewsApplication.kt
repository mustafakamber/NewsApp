package com.mustk.newsapp

import android.app.Application
import com.mustk.newsapp.broadcastreceiver.NotificationReceiver
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NewsApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        scheduleNextAlarm()
    }
    private fun scheduleNextAlarm(){
        NotificationReceiver.scheduleNextAlarm(this)
    }
}