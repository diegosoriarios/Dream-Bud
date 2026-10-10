package com.diego.dreambud.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.diego.dreambud.MainActivity
import java.util.Calendar

/**
 * Schedules the daily Dream Alarm using [AlarmManager.setAlarmClock], which is exempt from
 * the SCHEDULE_EXACT_ALARM restriction and surfaces the system alarm-clock indicator.
 */
class DreamAlarmManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(context, DreamAlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getShowIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            SHOW_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Schedules the next occurrence of hourOfDay:minute. Returns the trigger time in ms. */
    fun scheduleNextAlarm(hourOfDay: Int, minute: Int): Long {
        val triggerAt = nextTriggerMillis(hourOfDay, minute)
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAt, getShowIntent()),
            getPendingIntent()
        )
        return triggerAt
    }

    /** Re-fires the alarm after [minutes]. Returns the trigger time in ms. */
    fun snooze(minutes: Int = DEFAULT_SNOOZE_MINUTES): Long {
        val triggerAt = System.currentTimeMillis() + minutes * 60_000L
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAt, getShowIntent()),
            getPendingIntent()
        )
        return triggerAt
    }

    fun cancelAlarm() {
        alarmManager.cancel(getPendingIntent())
    }

    companion object {
        const val REQUEST_CODE = 3003
        const val SHOW_REQUEST_CODE = 3004
        const val DEFAULT_SNOOZE_MINUTES = 10

        /** Pure function: next wall-clock occurrence of hourOfDay:minute (unit-testable). */
        fun nextTriggerMillis(hourOfDay: Int, minute: Int, nowMillis: Long = System.currentTimeMillis()): Long {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = nowMillis
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            if (calendar.timeInMillis <= nowMillis) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            return calendar.timeInMillis
        }
    }
}
