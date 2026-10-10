package com.diego.dreambud.data.analytics

import com.diego.dreambud.data.model.DreamEntry
import java.util.Calendar
import java.util.Locale

data class SymbolCount(
    val symbol: String,
    val count: Int
)

data class SentimentPercentage(
    val sentiment: String,
    val count: Int,
    val percentage: Float
)

data class DreamAnalyticsData(
    val totalDreams: Int = 0,
    val totalAiAnalyses: Int = 0,
    val streakDays: Int = 0,
    val mostFrequentSentiment: String = "None",
    val sentimentBreakdown: List<SentimentPercentage> = emptyList(),
    val topSymbols: List<SymbolCount> = emptyList()
) {
    companion object {
        fun calculateFromDreams(dreams: List<DreamEntry>): DreamAnalyticsData {
            if (dreams.isEmpty()) return DreamAnalyticsData()

            val totalDreams = dreams.size
            val totalAiAnalyses = dreams.count { !it.analysis.isNullOrBlank() }

            // Sentiment breakdown
            val sentimentCounts = mutableMapOf<String, Int>()
            dreams.forEach { dream ->
                val s = dream.sentiment?.lowercase()?.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                } ?: "Mysterious"
                sentimentCounts[s] = (sentimentCounts[s] ?: 0) + 1
            }

            val sentimentBreakdown = sentimentCounts.map { (sentiment, count) ->
                SentimentPercentage(
                    sentiment = sentiment,
                    count = count,
                    percentage = (count.toFloat() / totalDreams) * 100f
                )
            }.sortedByDescending { it.count }

            val mostFrequentSentiment = sentimentBreakdown.firstOrNull()?.sentiment ?: "Mysterious"

            // Symbol frequencies
            val symbolCounts = mutableMapOf<String, Int>()
            dreams.forEach { dream ->
                if (!dream.symbols.isNullOrBlank()) {
                    dream.symbols.split(",").forEach { sym ->
                        val cleanSym = sym.trim().lowercase().replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        }
                        if (cleanSym.isNotEmpty()) {
                            symbolCounts[cleanSym] = (symbolCounts[cleanSym] ?: 0) + 1
                        }
                    }
                }
            }

            val topSymbols = symbolCounts.map { (symbol, count) ->
                SymbolCount(symbol = symbol, count = count)
            }.sortedByDescending { it.count }.take(10)

            // Streak calculation
            val streakDays = calculateStreakDays(dreams)

            return DreamAnalyticsData(
                totalDreams = totalDreams,
                totalAiAnalyses = totalAiAnalyses,
                streakDays = streakDays,
                mostFrequentSentiment = mostFrequentSentiment,
                sentimentBreakdown = sentimentBreakdown,
                topSymbols = topSymbols
            )
        }

        private fun calculateStreakDays(dreams: List<DreamEntry>): Int {
            if (dreams.isEmpty()) return 0

            val dates = dreams.map { dream ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = dream.timestamp
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }.distinct().sortedDescending()

            if (dates.isEmpty()) return 0

            var streak = 0
            val todayCal = Calendar.getInstance()
            todayCal.set(Calendar.HOUR_OF_DAY, 0)
            todayCal.set(Calendar.MINUTE, 0)
            todayCal.set(Calendar.SECOND, 0)
            todayCal.set(Calendar.MILLISECOND, 0)
            var currentExpected = todayCal.timeInMillis

            // Allow streak to count if last dream was today or yesterday
            val latestDate = dates.first()
            val dayInMillis = 86400000L
            if (latestDate < currentExpected - dayInMillis) {
                return 0
            }

            if (latestDate == currentExpected - dayInMillis) {
                currentExpected -= dayInMillis
            }

            for (date in dates) {
                if (date == currentExpected) {
                    streak++
                    currentExpected -= dayInMillis
                } else if (date < currentExpected) {
                    break
                }
            }

            return streak
        }
    }
}
