package com.diego.dreambud.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.diego.dreambud.R
import com.diego.dreambud.data.preferences.SettingsRepository

class DreamAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // The alarm fires once; schedule the next day's alarm immediately.
        rescheduleNextDailyAlarm(context)

        // Launch the full-screen ring activity directly (works while unlocked).
        try {
            context.startActivity(
                Intent(context, DreamAlarmActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
            )
        } catch (_: Exception) {
        }

        // Full-screen-intent notification: reliable path while the device is locked.
        postFullScreenNotification(context)
    }

    private fun rescheduleNextDailyAlarm(context: Context) {
        val settings = SettingsRepository(context)
        if (settings.isAlarmEnabled.value) {
            DreamAlarmManager(context).scheduleNextAlarm(
                settings.alarmHour.value,
                settings.alarmMinute.value
            )
        }
    }

    private fun postFullScreenNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Dream Alarm",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Full-screen morning alarm to capture your dreams"
                    setBypassDnd(true)
                }
            )
        }

        val fullScreenIntent = Intent(context, DreamAlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            FULL_SCREEN_REQUEST_CODE,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("⏰ Dream Alarm")
            .setContentText("Good morning! Ringing to capture your dreams before they fade.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "dream_alarm_channel"
        const val NOTIFICATION_ID = 2002
        const val FULL_SCREEN_REQUEST_CODE = 2003
    }
}
