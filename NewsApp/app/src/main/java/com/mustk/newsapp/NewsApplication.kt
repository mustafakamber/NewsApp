package com.mustk.newsapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.mustk.newsapp.broadcastreceiver.NotificationReceiver
import com.mustk.newsapp.shared.Constant.CHANNEL_ID
import com.mustk.newsapp.shared.Constant.NOTIFICATION_NAME
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NewsApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        scheduleNextAlarm()
    }
    private fun scheduleNextAlarm(){
        NotificationReceiver.scheduleNextAlarm(this)
    }
    private fun createNotificationChannel(){
        val channel = NotificationChannel(
                CHANNEL_ID,
                NOTIFICATION_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = applicationContext.getString(R.string.channel_description)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}