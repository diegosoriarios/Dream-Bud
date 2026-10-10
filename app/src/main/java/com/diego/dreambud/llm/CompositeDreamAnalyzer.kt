package com.diego.dreambud.llm

import android.content.Context
import com.diego.dreambud.data.preferences.SettingsRepository

class CompositeDreamAnalyzer(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) : DreamAnalyzer {

    private val fallbackAnalyzer = FallbackDreamAnalyzer()
    private var activeMediaPipeEngine: MediaPipeLlmEngine? = null
    private var cachedModelPath: String? = null

    override suspend fun analyzeDream(dreamContent: String): DreamAnalysisResult {
        val modelPath = settingsRepository.modelPath.value
        val useLocalLlm = settingsRepository.useLocalLlm.value

        if (useLocalLlm && !modelPath.isNullOrBlank()) {
            if (activeMediaPipeEngine == null || cachedModelPath != modelPath) {
                activeMediaPipeEngine?.close()
                activeMediaPipeEngine = MediaPipeLlmEngine(context, modelPath)
                cachedModelPath = modelPath
            }
            return activeMediaPipeEngine?.analyzeDream(dreamContent)
                ?: fallbackAnalyzer.analyzeDream(dreamContent)
        }

        return fallbackAnalyzer.analyzeDream(dreamContent)
    }
}
