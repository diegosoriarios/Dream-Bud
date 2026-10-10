package com.diego.dreambud.data.model

data class LlmModelPreset(
    val id: String,
    val name: String,
    val description: String,
    val sizeDisplay: String,
    val fileName: String,
    val downloadUrl: String
) {
    companion object {
        val PRESETS = listOf(
            LlmModelPreset(
                id = "llama_3_2_1b",
                name = "Llama 3.2 1B Instruct (Recommended)",
                description = "Meta's ultra-fast 1B parameter model optimized for mobile. Fast download & great for symbol extraction.",
                sizeDisplay = "~750 MB",
                fileName = "llama_3_2_1b_instruct.bin",
                downloadUrl = "https://huggingface.co/litert-community/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q4_K_M.gguf"
            ),
            LlmModelPreset(
                id = "gemma_2b",
                name = "Gemma 2B IT (Google MediaPipe)",
                description = "Google's 2B model natively built for MediaPipe GenAI. High empathy and deep psychological analysis.",
                sizeDisplay = "~1.3 GB",
                fileName = "gemma_2b_it_cpu_int4.bin",
                downloadUrl = "https://huggingface.co/google/gemma-2b-it-tflite/resolve/main/gemma-2b-it-cpu-int4.bin"
            )
        )
    }
}
