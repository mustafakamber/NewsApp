package com.mustk.newsapp

import android.app.Application
import android.content.Context
import com.mustk.newsapp.broadcastreceiver.NotificationReceiver
import com.mustk.newsapp.shared.Constant
import com.mustk.newsapp.shared.Constant.NOTIFICATIONS_ENABLED
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