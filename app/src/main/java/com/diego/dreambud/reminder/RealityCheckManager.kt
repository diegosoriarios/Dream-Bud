package com.diego.dreambud.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

/**
 * Repeating daytime "Are you dreaming?" reality-check notifications.
 * Uses inexact repeating alarms on purpose: battery friendly and no special permission needed.
 * intervalMinutes <= 0 means disabled.
 */
class RealityCheckManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(context, RealityCheckReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun scheduleRepeating(intervalMinutes: Int) {
        if (intervalMinutes <= 0) {
            cancel()
            return
        }
        val intervalMillis = intervalMinutes * 60_000L
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + intervalMillis,
            intervalMillis,
            getPendingIntent()
        )
    }

    fun cancel() {
        alarmManager.cancel(getPendingIntent())
    }

    companion object {
        const val REQUEST_CODE = 4004
        val INTERVAL_CHOICES_MINUTES = listOf(0, 30, 60, 90, 120)
    }
}
