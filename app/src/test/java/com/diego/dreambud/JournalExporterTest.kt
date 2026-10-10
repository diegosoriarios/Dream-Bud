package com.diego.dreambud

import com.diego.dreambud.data.model.DreamEntry
import com.diego.dreambud.export.JournalExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JournalExporterTest {

    @Test
    fun buildMarkdown_includesHeaderCountAndEntryDetails() {
        val dreams = listOf(
            DreamEntry(
                id = 1,
                title = "Flying Castle",
                content = "I soared above the ramparts.",
                timestamp = 1_700_000_000_000L,
                sentiment = "Joyful",
                symbols = "Flying, Castle",
                tags = "Lucid",
                analysis = "A dream of freedom.",
                audioPath = "/data/voice_memos/memo_20261009_074500.m4a"
            )
        )

        val markdown = JournalExporter.buildMarkdown(dreams, exportedAtMillis = 1_700_000_100_000L)

        assertTrue(markdown.startsWith("# 🌙 Dream Journal"))
        assertTrue(markdown.contains("1 dream recorded"))
        assertTrue(markdown.contains("## Flying Castle"))
        assertTrue(markdown.contains("**Date:**"))
        assertTrue(markdown.contains("**Mood:** Joyful"))
        assertTrue(markdown.contains("**Symbols:** Flying, Castle"))
        assertTrue(markdown.contains("**Tags:** Lucid"))
        assertTrue(markdown.contains("### Dream Narrative"))
        assertTrue(markdown.contains("I soared above the ramparts."))
        assertTrue(markdown.contains("### AI Dream Analysis"))
        assertTrue(markdown.contains("A dream of freedom."))
        assertTrue(markdown.contains("Voice memo attached: `memo_20261009_074500.m4a`"))
        assertFalse(markdown.contains("/data/voice_memos"))
    }

    @Test
    fun buildMarkdown_omitsBlankOptionalFields() {
        val dreams = listOf(
            DreamEntry(
                id = 1,
                title = "Quiet Night",
                content = "Rain on a tin roof.",
                timestamp = 1L,
                sentiment = null
            )
        )

        val markdown = JournalExporter.buildMarkdown(dreams, exportedAtMillis = 1L)

        assertFalse(markdown.contains("**Mood:**"))
        assertFalse(markdown.contains("**Symbols:**"))
        assertFalse(markdown.contains("**Tags:**"))
        assertFalse(markdown.contains("### AI Dream Analysis"))
        assertFalse(markdown.contains("Voice memo attached"))
    }

    @Test
    fun buildMarkdown_sortsEntriesNewestFirst() {
        val dreams = listOf(
            DreamEntry(id = 1, title = "Older", content = "a", timestamp = 1_000L),
            DreamEntry(id = 2, title = "Newer", content = "b", timestamp = 2_000L),
            DreamEntry(id = 3, title = "Middle", content = "c", timestamp = 1_500L)
        )

        val markdown = JournalExporter.buildMarkdown(dreams, exportedAtMillis = 0L)

        val olderIndex = markdown.indexOf("## Older")
        val newerIndex = markdown.indexOf("## Newer")
        val middleIndex = markdown.indexOf("## Middle")
        assertTrue(newerIndex < middleIndex && middleIndex < olderIndex)
        val separatorCount = Regex("(?m)^---$").findAll(markdown).count()
        assertEquals(3, separatorCount)
    }

    @Test
    fun buildMarkdown_emptyJournal() {
        val markdown = JournalExporter.buildMarkdown(emptyList(), exportedAtMillis = 0L)

        assertTrue(markdown.contains("No dreams recorded yet."))
        assertFalse(markdown.contains("## "))
    }

    @Test
    fun buildMarkdown_sanitizesHeadingMarkersInTitles() {
        val dreams = listOf(
            DreamEntry(id = 1, title = "## Injected Heading", content = "x", timestamp = 1L)
        )

        val markdown = JournalExporter.buildMarkdown(dreams, exportedAtMillis = 0L)

        assertTrue(markdown.contains("## Injected Heading"))
        assertFalse(markdown.contains("#### Injected Heading"))
    }

    @Test
    fun buildMarkdown_usesUntitledForBlankTitle() {
        val dreams = listOf(
            DreamEntry(id = 1, title = "   ", content = "x", timestamp = 1L)
        )

        val markdown = JournalExporter.buildMarkdown(dreams, exportedAtMillis = 0L)

        assertTrue(markdown.contains("## Untitled Dream"))
    }
}
