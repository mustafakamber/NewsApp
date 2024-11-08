package com.mustk.newsapp.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.mustk.newsapp.R
import com.mustk.newsapp.ui.NewsActivity
import com.mustk.newsapp.util.Constant.CHANNEL_ID
import com.mustk.newsapp.util.Constant.NOTIFICATIONS_ENABLED
import com.mustk.newsapp.util.Constant.NOTIFICATION_REQUEST_CODE
import com.mustk.newsapp.util.Constant.SHARED_PREFS_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val sharedPreferences by lazy {
        context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun showNotification() {
        if (isPreviousNotificationVisible()) return
        if (!isNotificationSwitchChecked()) return
        val intentToApp = Intent(context, NewsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_REQUEST_CODE,
            intentToApp,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.news_app_logo)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_content))
            .setContentIntent(pendingIntent)
            .build()
        notificationManager.notify(NOTIFICATION_REQUEST_CODE, notification)
    }

    private fun isPreviousNotificationVisible(): Boolean {
        val activeNotifications = notificationManager.activeNotifications
        for (notification in activeNotifications) {
            if (notification.id == NOTIFICATION_REQUEST_CODE) {
                return true
            }
        }
        return false
    }

    private fun isNotificationSwitchChecked(): Boolean {
        return sharedPreferences.getBoolean(NOTIFICATIONS_ENABLED, true)
    }
}