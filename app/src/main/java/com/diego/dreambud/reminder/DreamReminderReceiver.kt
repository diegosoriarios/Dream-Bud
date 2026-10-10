package com.diego.dreambud.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.diego.dreambud.MainActivity
import com.diego.dreambud.R
import com.diego.dreambud.data.preferences.SettingsRepository

class DreamReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // AlarmManager schedules do not survive a reboot; restore all of ours.
            rescheduleAllFromSettings(context)
            return
        }

        showReminderNotification(context)
    }

    private fun rescheduleAllFromSettings(context: Context) {
        val settings = SettingsRepository(context)

        // Morning journaling reminder notification
        if (settings.isReminderEnabled.value) {
            DreamReminderManager(context).scheduleDailyReminder(
                settings.reminderHour.value,
                settings.reminderMinute.value
            )
        }

        // Dream alarm clock
        if (settings.isAlarmEnabled.value) {
            DreamAlarmManager(context).scheduleNextAlarm(
                settings.alarmHour.value,
                settings.alarmMinute.value
            )
        }

        // Reality check reminders
        val realityCheckInterval = settings.realityCheckIntervalMinutes.value
        if (realityCheckInterval > 0) {
            RealityCheckManager(context).scheduleRepeating(realityCheckInterval)
        }
    }

    private fun showReminderNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "dream_reminder_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Dream Journal Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily morning reminders to record or dictate your dreams"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Did you dream last night? 🌙")
            .setContentText("Tap to speak or write your dream before the memory fades!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}
