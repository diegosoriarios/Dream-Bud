package com.diego.dreambud.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dreams")
data class DreamEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val analysis: String? = null,
    val sentiment: String? = "Mysterious", // e.g., Joyful, Anxious, Mysterious, Transformative
    val symbols: String? = null, // Comma separated symbols e.g. "Water, Flying, Castle"
    val tags: String? = null, // Comma separated tags e.g. "Nightmare, Lucid, Recurring"
    val audioPath: String? = null // Absolute path to attached voice memo (.m4a)
)
