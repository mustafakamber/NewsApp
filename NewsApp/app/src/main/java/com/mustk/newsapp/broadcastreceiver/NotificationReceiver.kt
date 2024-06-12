package com.mustk.newsapp.broadcastreceiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mustk.newsapp.services.NotificationService
import com.mustk.newsapp.shared.Constant.NOTIFICATION_HOUR
import com.mustk.newsapp.shared.Constant.NOTIFICATION_MINUTE
import com.mustk.newsapp.shared.Constant.NOTIFICATION_NEXT_DAY
import com.mustk.newsapp.shared.Constant.NOTIFICATION_SECOND
import com.mustk.newsapp.shared.Constant.PENDING_INTENT_REQUEST_CODE
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationService: NotificationService
    override fun onReceive(context: Context, intent: Intent?) {
        notificationService.showNotification()
        scheduleNextAlarm(context)
    }
    companion object {
        fun scheduleNextAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                PENDING_INTENT_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, NOTIFICATION_HOUR)
                set(Calendar.MINUTE, NOTIFICATION_MINUTE)
                set(Calendar.SECOND, NOTIFICATION_SECOND)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DATE, NOTIFICATION_NEXT_DAY)
                }
            }
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}