package com.diego.dreambud

import com.diego.dreambud.data.analytics.DreamAnalyticsData
import com.diego.dreambud.data.model.DreamEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DreamAnalyticsTest {

    @Test
    fun calculateFromDreams_calculatesCorrectTotalsAndTopSymbols() {
        val dreams = listOf(
            DreamEntry(id = 1, title = "Dream 1", content = "Flying", sentiment = "Liberating", symbols = "Flying, Water"),
            DreamEntry(id = 2, title = "Dream 2", content = "Water", sentiment = "Transformative", symbols = "Water, House"),
            DreamEntry(id = 3, title = "Dream 3", content = "Ocean", sentiment = "Transformative", symbols = "Water, Ocean")
        )

        val analytics = DreamAnalyticsData.calculateFromDreams(dreams)

        assertEquals(3, analytics.totalDreams)
        assertEquals("Transformative", analytics.mostFrequentSentiment)
        assertTrue(analytics.topSymbols.isNotEmpty())
        assertEquals("Water", analytics.topSymbols.first().symbol)
        assertEquals(3, analytics.topSymbols.first().count)
    }

    @Test
    fun calculateFromDreams_handlesEmptyList() {
        val analytics = DreamAnalyticsData.calculateFromDreams(emptyList())

        assertEquals(0, analytics.totalDreams)
        assertEquals(0, analytics.streakDays)
        assertTrue(analytics.topSymbols.isEmpty())
        assertTrue(analytics.sentimentBreakdown.isEmpty())
    }
}
