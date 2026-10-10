package com.diego.dreambud.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class SpeechState {
    object Idle : SpeechState()
    object Listening : SpeechState()
    data class PartialResult(val text: String) : SpeechState()
    data class Success(val text: String) : SpeechState()
    data class Error(val message: String) : SpeechState()
}

class SpeechToTextManager(private val context: Context) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(languageLocale: Locale = Locale.getDefault()) {
        if (!isRecognitionAvailable()) {
            _speechState.value = SpeechState.Error("Speech recognition is not available on this device.")
            return
        }

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@SpeechToTextManager)
            }
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageLocale.toString())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        _speechState.value = SpeechState.Listening
        _partialText.value = ""
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _speechState.value = SpeechState.Idle
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        _speechState.value = SpeechState.Idle
    }

    override fun onReadyForSpeech(params: Bundle?) {
        _speechState.value = SpeechState.Listening
    }

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _speechState.value = SpeechState.Idle
    }

    override fun onError(error: Int) {
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Audio permission denied"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try speaking again."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service is busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
            else -> "Unknown speech error ($error)"
        }
        _speechState.value = SpeechState.Error(errorMessage)
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val resultText = matches[0]
            _speechState.value = SpeechState.Success(resultText)
        } else {
            _speechState.value = SpeechState.Idle
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val partial = matches[0]
            _partialText.value = partial
            _speechState.value = SpeechState.PartialResult(partial)
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
