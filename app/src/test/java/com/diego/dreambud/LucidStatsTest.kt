package com.diego.dreambud

import com.diego.dreambud.data.model.DreamEntry
import com.diego.dreambud.lucid.LucidStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class LucidStatsTest {

    private fun millisOn(dayOffset: Int, hour: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, dayOffset)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun now(): Long = millisOn(0, 12)

    private fun dream(timestamp: Long, tags: String?): DreamEntry =
        DreamEntry(title = "t", content = "c", timestamp = timestamp, tags = tags)

    @Test
    fun isLucid_matchesTagExactlyIgnoringCase() {
        assertTrue(LucidStats.isLucid(dream(1, "Lucid")))
        assertTrue(LucidStats.isLucid(dream(1, "lucid")))
        assertTrue(LucidStats.isLucid(dream(1, "Nightmare, Lucid, Recurring")))
        assertFalse(LucidStats.isLucid(dream(1, "Lucidity")))
        assertFalse(LucidStats.isLucid(dream(1, "Nightmare")))
        assertFalse(LucidStats.isLucid(dream(1, null)))
    }

    @Test
    fun toggleTag_addsAndRemoves() {
        assertEquals("Lucid", LucidStats.toggleTag("", "Lucid"))
        assertEquals("Nightmare, Lucid", LucidStats.toggleTag("Nightmare", "Lucid"))
        assertEquals("Nightmare", LucidStats.toggleTag("Nightmare, Lucid", "Lucid"))
        // Case-insensitive removal, canonical casing preserved from existing entry
        assertEquals("Nightmare", LucidStats.toggleTag("Nightmare, lucid", "Lucid"))
        // Toggling an already-present tag removes it
        assertEquals("a", LucidStats.toggleTag("a, Lucid", "Lucid"))
    }

    @Test
    fun calculate_emptyJournal() {
        val stats = LucidStats.calculateFromDreams(emptyList(), now())
        assertEquals(0, stats.totalDreams)
        assertEquals(0, stats.lucidDreams)
        assertEquals(0, stats.lucidityRatePercent)
        assertNull(stats.daysSinceLastLucid)
        assertEquals(0, stats.currentStreakDays)
    }

    @Test
    fun calculate_noLucidDreams() {
        val dreams = listOf(
            dream(millisOn(0, 8), "Nightmare"),
            dream(millisOn(-1, 8), "Recurring")
        )
        val stats = LucidStats.calculateFromDreams(dreams, now())
        assertEquals(2, stats.totalDreams)
        assertEquals(0, stats.lucidDreams)
        assertEquals(0, stats.lucidityRatePercent)
        assertNull(stats.daysSinceLastLucid)
        assertEquals(0, stats.currentStreakDays)
    }

    @Test
    fun calculate_lucidToday_givesStreakOneAndZeroDaysSince() {
        val dreams = listOf(
            dream(millisOn(0, 7), "Lucid"),
            dream(millisOn(-1, 8), "Nightmare")
        )
        val stats = LucidStats.calculateFromDreams(dreams, now())
        assertEquals(1, stats.lucidDreams)
        assertEquals(50, stats.lucidityRatePercent)
        assertEquals(0, stats.daysSinceLastLucid)
        assertEquals(1, stats.currentStreakDays)
    }

    @Test
    fun calculate_consecutiveDays_givesStreakOfTwo() {
        val dreams = listOf(
            dream(millisOn(0, 7), "Lucid"),
            dream(millisOn(-1, 6), "Lucid, Nightmare"),
            dream(millisOn(-2, 9), "Recurring")
        )
        val stats = LucidStats.calculateFromDreams(dreams, now())
        assertEquals(2, stats.lucidDreams)
        assertEquals(2, stats.currentStreakDays)
        assertEquals(0, stats.daysSinceLastLucid)
    }

    @Test
    fun calculate_lucidOnlyYesterday_givesStreakOne() {
        val dreams = listOf(dream(millisOn(-1, 7), "Lucid"))
        val stats = LucidStats.calculateFromDreams(dreams, now())
        assertEquals(1, stats.currentStreakDays)
        assertEquals(1, stats.daysSinceLastLucid)
    }

    @Test
    fun calculate_oldLucidDream_breaksStreakButCounts() {
        val dreams = listOf(
            dream(millisOn(-3, 7), "Lucid"),
            dream(millisOn(-10, 7), "Lucid")
        )
        val stats = LucidStats.calculateFromDreams(dreams, now())
        assertEquals(0, stats.currentStreakDays)
        assertEquals(3, stats.daysSinceLastLucid)
        assertEquals(100, stats.lucidityRatePercent)
    }

    @Test
    fun calculate_rateRoundsHalfUp() {
        val dreams = (1..6).map { dream(millisOn(0, 8), null) } + dream(millisOn(0, 9), "Lucid")
        // 1 lucid of 7 = 14.28% -> 14
        val stats = LucidStats.calculateFromDreams(dreams, now())
        assertEquals(14, stats.lucidityRatePercent)
    }
}
