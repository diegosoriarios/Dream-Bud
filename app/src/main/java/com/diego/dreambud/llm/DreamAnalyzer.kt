package com.diego.dreambud.llm

data class DreamAnalysisResult(
    val fullAnalysis: String,
    val sentiment: String,
    val symbols: List<String>,
    val isLocalLlm: Boolean
)

interface DreamAnalyzer {
    suspend fun analyzeDream(dreamContent: String): DreamAnalysisResult
}
