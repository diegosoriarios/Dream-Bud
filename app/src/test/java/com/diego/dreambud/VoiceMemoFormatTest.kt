package com.diego.dreambud

import com.diego.dreambud.audio.VoiceMemoManager
import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceMemoFormatTest {

    @Test
    fun formatDuration_formatsMinutesAndSeconds() {
        assertEquals("0:00", VoiceMemoManager.formatDuration(0))
        assertEquals("0:05", VoiceMemoManager.formatDuration(5_000))
        assertEquals("0:59", VoiceMemoManager.formatDuration(59_999))
        assertEquals("1:00", VoiceMemoManager.formatDuration(60_000))
        assertEquals("2:07", VoiceMemoManager.formatDuration(127_400))
        assertEquals("12:34", VoiceMemoManager.formatDuration(754_000))
    }

    @Test
    fun formatDuration_handlesNegativeInput() {
        assertEquals("0:00", VoiceMemoManager.formatDuration(-1_000))
    }
}
