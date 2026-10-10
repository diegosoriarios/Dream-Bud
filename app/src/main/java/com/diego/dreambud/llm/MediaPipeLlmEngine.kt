package com.diego.dreambud.llm

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaPipeLlmEngine(
    private val context: Context,
    private val modelPath: String
) : DreamAnalyzer {

    private var llmInference: LlmInference? = null
    private var isInitialized = false
    private var initError: String? = null

    private fun initializeIfNeeded() {
        if (isInitialized) return
        try {
            val file = File(modelPath)
            if (!file.exists()) {
                initError = "Model file not found at: $modelPath"
                return
            }

            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(512)
                .setTemperature(0.7f)
                .setTopK(40)
                .build()

            llmInference = LlmInference.createFromOptions(context, options)
            isInitialized = true
        } catch (e: Exception) {
            initError = "Failed to initialize MediaPipe LLM: ${e.localizedMessage}"
        }
    }

    override suspend fun analyzeDream(dreamContent: String): DreamAnalysisResult = withContext(Dispatchers.IO) {
        initializeIfNeeded()

        if (!isInitialized || llmInference == null) {
            val fallback = FallbackDreamAnalyzer().analyzeDream(dreamContent)
            return@withContext fallback.copy(
                fullAnalysis = "⚠️ Local LLM Notice: ${initError ?: "LLM not initialized"}\n\n" + fallback.fullAnalysis
            )
        }

        try {
            val systemPrompt = """
                You are an expert dream analyst using local offline AI.
                Analyze the following dream text. Provide concise insights formatted as:
                ### 🤖 Local Offline LLM Dream Analysis

                **Overall Sentiment:** [Joyful/Anxious/Transformative/Mysterious]
                **Key Symbols:** [List 2-4 key symbols separated by commas]

                #### 🗝️ Symbol Meanings & Core Themes
                [Explanation of symbols and narrative]

                #### 🧠 Psychological Insight
                [Psychological perspective]

                #### 💡 Personal Reflection Prompt
                [One thought-provoking question]

                Dream Text: "$dreamContent"
            """.trimIndent()

            val responseText = llmInference?.generateResponse(systemPrompt) ?: ""

            // Extract sentiment if present in response
            val lowerResponse = responseText.lowercase()
            val extractedSentiment = when {
                lowerResponse.contains("anxious") -> "Anxious"
                lowerResponse.contains("joyful") || lowerResponse.contains("liberating") -> "Joyful"
                lowerResponse.contains("transformative") -> "Transformative"
                else -> "Mysterious"
            }

            // Extract symbols if present
            val extractedSymbols = mutableListOf<String>()
            val symbolsRegex = Regex("""Key Symbols:\s*\*?([^\n]+)""", RegexOption.IGNORE_CASE)
            val match = symbolsRegex.find(responseText)
            if (match != null) {
                val symbolsStr = match.groupValues[1]
                symbolsStr.split(",").forEach { s ->
                    val clean = s.trim().removePrefix("-").removePrefix("*").trim()
                    if (clean.isNotEmpty()) extractedSymbols.add(clean)
                }
            }

            if (extractedSymbols.isEmpty()) {
                extractedSymbols.add("Local AI Analysis")
            }

            DreamAnalysisResult(
                fullAnalysis = responseText,
                sentiment = extractedSentiment,
                symbols = extractedSymbols,
                isLocalLlm = true
            )
        } catch (e: Exception) {
            val fallback = FallbackDreamAnalyzer().analyzeDream(dreamContent)
            fallback.copy(
                fullAnalysis = "⚠️ MediaPipe LLM Execution Error: ${e.localizedMessage}\n\n" + fallback.fullAnalysis
            )
        }
    }

    fun close() {
        try {
            llmInference?.close()
        } catch (_: Exception) {}
        llmInference = null
        isInitialized = false
    }
}
