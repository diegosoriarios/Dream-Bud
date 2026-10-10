package com.diego.dreambud

import com.diego.dreambud.reminder.DreamAlarmManager
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class DreamAlarmScheduleTest {

    private fun millisAt(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month, day, hour, minute, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    @Test
    fun nextTrigger_beforeTargetTime_returnsToday() {
        val now = millisAt(2026, Calendar.OCTOBER, 10, 6, 0)
        val expected = millisAt(2026, Calendar.OCTOBER, 10, 7, 30)
        assertEquals(expected, DreamAlarmManager.nextTriggerMillis(7, 30, now))
    }

    @Test
    fun nextTrigger_afterTargetTime_returnsTomorrow() {
        val now = millisAt(2026, Calendar.OCTOBER, 10, 8, 0)
        val expected = millisAt(2026, Calendar.OCTOBER, 11, 7, 30)
        assertEquals(expected, DreamAlarmManager.nextTriggerMillis(7, 30, now))
    }

    @Test
    fun nextTrigger_exactlyAtTargetTime_returnsNextDay() {
        val now = millisAt(2026, Calendar.OCTOBER, 10, 7, 30)
        val expected = millisAt(2026, Calendar.OCTOBER, 11, 7, 30)
        assertEquals(expected, DreamAlarmManager.nextTriggerMillis(7, 30, now))
    }

    @Test
    fun nextTrigger_rollsOverMonth() {
        val now = millisAt(2026, Calendar.OCTOBER, 31, 9, 0)
        val expected = millisAt(2026, Calendar.NOVEMBER, 1, 7, 0)
        assertEquals(expected, DreamAlarmManager.nextTriggerMillis(7, 0, now))
    }

    @Test
    fun nextTrigger_midnightTarget_advancesToNextMidnight() {
        val now = millisAt(2026, Calendar.OCTOBER, 10, 0, 0)
        val expected = millisAt(2026, Calendar.OCTOBER, 11, 0, 0)
        assertEquals(expected, DreamAlarmManager.nextTriggerMillis(0, 0, now))
    }
}
