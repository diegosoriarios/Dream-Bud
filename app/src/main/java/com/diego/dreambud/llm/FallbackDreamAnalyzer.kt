package com.diego.dreambud.llm

import java.util.Locale

class FallbackDreamAnalyzer : DreamAnalyzer {

    private val symbolDictionary = mapOf(
        "water" to ("Water" to "Emotional depth, subconscious feelings, and fluid life transitions."),
        "ocean" to ("Ocean" to "Vast unconscious emotions or feeling overwhelmed by life circumstances."),
        "river" to ("River" to "The ongoing flow of time, personal journey, or direction in life."),
        "fly" to ("Flying" to "Desire for freedom, transcendence over daily problems, or ambition."),
        "flying" to ("Flying" to "Elevated perspective, liberation, or overcoming constraints."),
        "fall" to ("Falling" to "Fear of losing control, feeling unsupported, or anxiety about failure."),
        "falling" to ("Falling" to "Insecurity or a situation where you feel groundless."),
        "chase" to ("Being Chased" to "Avoiding an unresolved issue, emotion, or aspect of yourself."),
        "chased" to ("Being Chased" to "Running away from an impending confrontation or self-realization."),
        "teeth" to ("Teeth" to "Anxiety about personal power, communication, or aging/loss."),
        "house" to ("House" to "The self/mind. Rooms represent different aspects of your personality."),
        "door" to ("Door" to "Opportunities, choices, or transitioning into a new phase of life."),
        "fire" to ("Fire" to "Transformation, intense passion, creative energy, or anger."),
        "snake" to ("Snake" to "Hidden fears, healing, wisdom, or unexpected personal transformation."),
        "cat" to ("Cat" to "Intuition, independence, or subtle mystery."),
        "dog" to ("Dog" to "Loyalty, friendship, instinctual protection, or trust."),
        "forest" to ("Forest" to "The unknown subconscious, exploration, or feeling lost/searching."),
        "darkness" to ("Darkness" to "The unknown, hidden thoughts, or fear of what lies ahead."),
        "light" to ("Light" to "Clarity, awakening, truth, or sudden realization."),
        "car" to ("Car / Vehicle" to "Your path or sense of agency and control in life's journey."),
        "mirror" to ("Mirror" to "Self-reflection, identity, or how you truly view yourself."),
        "stair" to ("Stairs" to "Personal growth (ascending) or delving into deeper memories (descending)."),
        "exam" to ("Exam / Test" to "Self-scrutiny, fear of unpreparedness, or feeling judged.")
    )

    override suspend fun analyzeDream(dreamContent: String): DreamAnalysisResult {
        val lowerContent = dreamContent.lowercase(Locale.getDefault())

        val detectedSymbolsMap = mutableMapOf<String, String>()
        symbolDictionary.forEach { (keyword, pair) ->
            if (lowerContent.contains(keyword)) {
                detectedSymbolsMap[pair.first] = pair.second
            }
        }

        val symbolsList = if (detectedSymbolsMap.isNotEmpty()) {
            detectedSymbolsMap.keys.toList()
        } else {
            listOf("Intuition", "Self-Reflection")
        }

        // Determine sentiment tone
        val sentiment = when {
            lowerContent.contains("monster") || lowerContent.contains("scared") || lowerContent.contains("fear") ||
                    lowerContent.contains("fall") || lowerContent.contains("chase") || lowerContent.contains("dark") ||
                    lowerContent.contains("trap") -> "Anxious"

            lowerContent.contains("fly") || lowerContent.contains("happy") || lowerContent.contains("light") ||
                    lowerContent.contains("sun") || lowerContent.contains("peace") || lowerContent.contains("joy") -> "Liberating"

            lowerContent.contains("water") || lowerContent.contains("door") || lowerContent.contains("house") ||
                    lowerContent.contains("fire") || lowerContent.contains("mirror") -> "Transformative"

            else -> "Mysterious"
        }

        val fullAnalysis = buildString {
            append("### 🔮 Offline Dream Analysis Breakdown\n\n")

            append("**🌟 Overall Tone:** ").append(sentiment).append("\n\n")

            append("#### 🗝️ Identified Key Symbols & Meanings:\n")
            if (detectedSymbolsMap.isNotEmpty()) {
                detectedSymbolsMap.forEach { (symbolName, symbolDesc) ->
                    append("- **").append(symbolName).append("**: ").append(symbolDesc).append("\n")
                }
            } else {
                append("- **General Narrative**: This dream reflects subtle subconscious processing of recent waking experiences and personal reflection.\n")
            }
            append("\n")

            append("#### 🧠 Psychological Insight (Jungian Perspective):\n")
            when (sentiment) {
                "Anxious" -> append("The dream highlights unresolved tension or a waking stressor that your subconscious is attempting to process and integrate. Giving voice to this fear helps diminish its hold.\n\n")
                "Liberating" -> append("This dream signifies an expansion of conscious awareness, a feeling of mastery over recent obstacles, or newfound optimism.\n\n")
                "Transformative" -> append("You appear to be experiencing an internal threshold shift—letting go of old beliefs or stepping into a new phase of personal evolution.\n\n")
                else -> append("Your dream weaves subconscious imagery to process underlying emotions, offering a reflective space to observe hidden desires or quiet intuition.\n\n")
            }

            append("#### 💡 Reflection Prompt:\n")
            append("*\"What emotion in this dream felt most prominent, and where in your waking life have you recently felt a similar dynamic?\"*")
        }

        return DreamAnalysisResult(
            fullAnalysis = fullAnalysis,
            sentiment = sentiment,
            symbols = symbolsList,
            isLocalLlm = false
        )
    }
}
