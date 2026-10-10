package com.diego.dreambud.lucid

import com.diego.dreambud.data.model.DreamEntry
import java.util.Calendar
import kotlin.math.roundToInt

/**
 * Lucid dreaming statistics derived from the "Lucid" journal tag.
 * Pure Kotlin, unit-testable.
 */
data class LucidStats(
    val totalDreams: Int = 0,
    val lucidDreams: Int = 0,
    val lucidityRatePercent: Int = 0,
    val daysSinceLastLucid: Int? = null,
    val currentStreakDays: Int = 0
) {
    companion object {
        const val LUCID_TAG = "Lucid"

        fun isLucid(dream: DreamEntry): Boolean {
            return dream.tags?.split(",")?.any { it.trim().equals(LUCID_TAG, ignoreCase = true) } == true
        }

        /** Adds or removes [tag] from a comma-separated tag string, returning the new string. */
        fun toggleTag(tags: String, tag: String): String {
            val current = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            return if (current.any { it.equals(tag, ignoreCase = true) }) {
                current.filterNot { it.equals(tag, ignoreCase = true) }.joinToString(", ")
            } else {
                (current + tag).joinToString(", ")
            }
        }

        fun calculateFromDreams(
            dreams: List<DreamEntry>,
            nowMillis: Long = System.currentTimeMillis()
        ): LucidStats {
            val lucidEntries = dreams.filter { isLucid(it) }
            if (lucidEntries.isEmpty() || dreams.isEmpty()) {
                return LucidStats(totalDreams = dreams.size)
            }

            val lucidDays = lucidEntries.map { startOfDay(it.timestamp) }.toSortedSet(reverseOrder())
            val today = startOfDay(nowMillis)
            val yesterday = addDays(today, -1)

            val daysSinceLastLucid = daysBetween(lucidDays.first(), today)

            // Streak: consecutive lucid days ending today or yesterday
            var streak = 0
            var cursor = when (lucidDays.first()) {
                today -> today
                yesterday -> yesterday
                else -> null
            }
            if (cursor != null) {
                var current: Long = cursor
                for (day in lucidDays) {
                    when {
                        day == current -> {
                            streak++
                            current = addDays(current, -1)
                        }
                        day > current -> continue // duplicate above the cursor, skip
                        else -> break // gap
                    }
                }
            }

            return LucidStats(
                totalDreams = dreams.size,
                lucidDreams = lucidEntries.size,
                lucidityRatePercent = ((lucidEntries.size * 100.0) / dreams.size).roundToInt(),
                daysSinceLastLucid = daysSinceLastLucid,
                currentStreakDays = streak
            )
        }

        private fun startOfDay(millis: Long): Long {
            val cal = Calendar.getInstance()
            cal.timeInMillis = millis
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        private fun addDays(millis: Long, days: Int): Long {
            val cal = Calendar.getInstance()
            cal.timeInMillis = millis
            cal.add(Calendar.DAY_OF_YEAR, days)
            return cal.timeInMillis
        }

        private fun daysBetween(fromMillis: Long, toMillis: Long): Int {
            var from = startOfDay(fromMillis)
            val to = startOfDay(toMillis)
            var diff = 0
            while (from < to) {
                from = addDays(from, 1)
                diff++
            }
            while (from > to) {
                from = addDays(from, -1)
                diff++
            }
            return diff
        }
    }
}
