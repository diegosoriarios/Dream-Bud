package com.diego.dreambud.export

import com.diego.dreambud.data.model.DreamEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Builds a Markdown representation of the dream journal.
 * Pure Kotlin, no Android dependencies, so it is fully unit-testable.
 */
object JournalExporter {

    private val entryDateFormat = SimpleDateFormat("EEEE, MMM d, yyyy • h:mm a", Locale.US)
    private val exportDateFormat = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US)

    fun buildMarkdown(
        dreams: List<DreamEntry>,
        exportedAtMillis: Long = System.currentTimeMillis()
    ): String {
        val sb = StringBuilder()
        sb.appendLine("# 🌙 Dream Journal")
        sb.appendLine()
        sb.appendLine(
            if (dreams.isEmpty()) {
                "*No dreams recorded yet.*"
            } else {
                val noun = if (dreams.size == 1) "dream" else "dreams"
                "*Exported from DreamBud on ${exportDateFormat.format(Date(exportedAtMillis))} — ${dreams.size} $noun recorded*"
            }
        )
        sb.appendLine()

        dreams.sortedByDescending { it.timestamp }.forEach { dream ->
            sb.appendLine("---")
            sb.appendLine()
            sb.appendLine("## ${sanitizeHeading(dream.title)}")
            sb.appendLine()
            sb.appendLine("**Date:** ${entryDateFormat.format(Date(dream.timestamp))}")
            dream.sentiment?.takeIf { it.isNotBlank() }?.let { sb.appendLine("**Mood:** $it") }
            dream.symbols?.takeIf { it.isNotBlank() }?.let { sb.appendLine("**Symbols:** $it") }
            dream.tags?.takeIf { it.isNotBlank() }?.let { sb.appendLine("**Tags:** $it") }
            sb.appendLine()
            sb.appendLine("### Dream Narrative")
            sb.appendLine()
            sb.appendLine(dream.content.ifBlank { "*(no narrative recorded)*" })
            sb.appendLine()
            dream.analysis?.takeIf { it.isNotBlank() }?.let { analysis ->
                sb.appendLine("### AI Dream Analysis")
                sb.appendLine()
                sb.appendLine(analysis)
                sb.appendLine()
            }
            dream.audioPath?.takeIf { it.isNotBlank() }?.let { path ->
                sb.appendLine("*🎙️ Voice memo attached: `${path.substringAfterLast('/')}`*")
                sb.appendLine()
            }
        }

        return sb.toString().trimEnd() + "\n"
    }

    /** Strips leading markdown heading markers so a crafted title cannot break document structure. */
    private fun sanitizeHeading(title: String): String =
        title.replace(Regex("^#+\\s*"), "").trim().ifBlank { "Untitled Dream" }
}
