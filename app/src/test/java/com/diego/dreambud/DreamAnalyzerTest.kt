package com.diego.dreambud

import com.diego.dreambud.llm.FallbackDreamAnalyzer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DreamAnalyzerTest {

    @Test
    fun fallbackDreamAnalyzer_detectsWaterAndFlyingSymbols() = runBlocking {
        val analyzer = FallbackDreamAnalyzer()
        val dreamText = "I was flying high over a stormy ocean river with doors into houses."

        val result = analyzer.analyzeDream(dreamText)

        assertNotNull(result)
        assertTrue(result.symbols.contains("Flying"))
        assertTrue(result.symbols.contains("Ocean") || result.symbols.contains("Water") || result.symbols.contains("River"))
        assertNotNull(result.fullAnalysis)
        assertTrue(result.fullAnalysis.contains("Offline Dream Analysis Breakdown"))
    }

    @Test
    fun fallbackDreamAnalyzer_detectsAnxiousSentiment() = runBlocking {
        val analyzer = FallbackDreamAnalyzer()
        val dreamText = "I was chased by a monster in the dark and felt trapped."

        val result = analyzer.analyzeDream(dreamText)

        assertEquals("Anxious", result.sentiment)
        assertTrue(result.symbols.contains("Being Chased") || result.symbols.contains("Darkness"))
    }
}
