package com.diego.dreambud.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.diego.dreambud.audio.VoiceMemoManager
import com.diego.dreambud.data.analytics.DreamAnalyticsData
import com.diego.dreambud.data.local.DreamDatabase
import com.diego.dreambud.data.model.DreamEntry
import com.diego.dreambud.data.model.LlmModelPreset
import com.diego.dreambud.data.preferences.SettingsRepository
import com.diego.dreambud.data.repository.DreamRepository
import com.diego.dreambud.export.JournalExporter
import com.diego.dreambud.llm.CompositeDreamAnalyzer
import com.diego.dreambud.llm.DownloadState
import com.diego.dreambud.llm.DreamAnalysisResult
import com.diego.dreambud.llm.ModelDownloadManager
import com.diego.dreambud.lucid.LucidStats
import com.diego.dreambud.reminder.DreamAlarmManager
import com.diego.dreambud.reminder.DreamReminderManager
import com.diego.dreambud.reminder.RealityCheckManager
import com.diego.dreambud.security.BiometricPromptManager
import com.diego.dreambud.speech.SpeechToTextManager
import com.diego.dreambud.speech.SpeechState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DreamViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DreamDatabase.getDatabase(application)
    private val dreamRepository = DreamRepository(database.dreamDao())
    val settingsRepository = SettingsRepository(application)
    private val dreamAnalyzer = CompositeDreamAnalyzer(application, settingsRepository)

    val reminderManager = DreamReminderManager(application)
    val isReminderEnabled: StateFlow<Boolean> = settingsRepository.isReminderEnabled
    val reminderHour: StateFlow<Int> = settingsRepository.reminderHour
    val reminderMinute: StateFlow<Int> = settingsRepository.reminderMinute

    private val dreamAlarmManager = DreamAlarmManager(application)
    val isAlarmEnabled: StateFlow<Boolean> = settingsRepository.isAlarmEnabled
    val alarmHour: StateFlow<Int> = settingsRepository.alarmHour
    val alarmMinute: StateFlow<Int> = settingsRepository.alarmMinute

    private val realityCheckManager = RealityCheckManager(application)
    val realityCheckIntervalMinutes: StateFlow<Int> = settingsRepository.realityCheckIntervalMinutes

    val biometricPromptManager = BiometricPromptManager(application)
    val isBiometricLockEnabled: StateFlow<Boolean> = settingsRepository.isBiometricLockEnabled

    private val _isAppUnlocked = MutableStateFlow(false)
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked.asStateFlow()

    val modelDownloadManager = ModelDownloadManager(application)
    val downloadState: StateFlow<DownloadState> = modelDownloadManager.downloadState

    val speechToTextManager = SpeechToTextManager(application)
    val speechState: StateFlow<SpeechState> = speechToTextManager.speechState
    val partialSpeechText: StateFlow<String> = speechToTextManager.partialText

    val voiceMemoManager = VoiceMemoManager(application)
    val voiceMemoRecordState: StateFlow<VoiceMemoManager.RecordState> = voiceMemoManager.recordState
    val voiceMemoPlaybackState: StateFlow<VoiceMemoManager.PlaybackState> = voiceMemoManager.playbackState

    val modelPath: StateFlow<String?> = settingsRepository.modelPath
    val useLocalLlm: StateFlow<Boolean> = settingsRepository.useLocalLlm

    val searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val dreams: StateFlow<List<DreamEntry>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                dreamRepository.getAllDreams()
            } else {
                dreamRepository.searchDreams(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val analyticsData: StateFlow<DreamAnalyticsData> = dreamRepository.getAllDreams()
        .map { list -> DreamAnalyticsData.calculateFromDreams(list) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DreamAnalyticsData()
        )

    val lucidStats: StateFlow<LucidStats> = dreamRepository.getAllDreams()
        .map { list -> LucidStats.calculateFromDreams(list) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LucidStats()
        )

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _currentAnalysisResult = MutableStateFlow<DreamAnalysisResult?>(null)
    val currentAnalysisResult: StateFlow<DreamAnalysisResult?> = _currentAnalysisResult.asStateFlow()

    fun updateReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
        settingsRepository.setReminderSettings(enabled, hour, minute)
        if (enabled) {
            reminderManager.scheduleDailyReminder(hour, minute)
        } else {
            reminderManager.cancelReminder()
        }
    }

    fun updateAlarmSettings(enabled: Boolean, hour: Int, minute: Int) {
        settingsRepository.setAlarmSettings(enabled, hour, minute)
        if (enabled) {
            dreamAlarmManager.scheduleNextAlarm(hour, minute)
        } else {
            dreamAlarmManager.cancelAlarm()
        }
    }

    fun updateRealityCheckInterval(minutes: Int) {
        settingsRepository.setRealityCheckInterval(minutes)
        if (minutes > 0) {
            realityCheckManager.scheduleRepeating(minutes)
        } else {
            realityCheckManager.cancel()
        }
    }

    fun sendTestReminderNotification() {
        reminderManager.sendTestNotification()
    }

    fun setBiometricLockEnabled(enabled: Boolean) {
        settingsRepository.setBiometricLockEnabled(enabled)
        if (!enabled) {
            _isAppUnlocked.value = true
        }
    }

    fun setAppUnlocked(unlocked: Boolean) {
        _isAppUnlocked.value = unlocked
    }

    fun lockApp() {
        if (isBiometricLockEnabled.value) {
            _isAppUnlocked.value = false
        }
    }

    fun getDreamById(id: Long): StateFlow<DreamEntry?> {
        return dreamRepository.getDreamById(id)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    fun saveDream(
        id: Long = 0,
        title: String,
        content: String,
        sentiment: String? = "Mysterious",
        symbols: String? = null,
        tags: String? = null,
        analysis: String? = null,
        audioPath: String? = null,
        timestamp: Long = 0L,
        onSaved: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val entry = DreamEntry(
                id = id,
                title = title.ifBlank { "Untitled Dream" },
                content = content,
                timestamp = if (timestamp > 0L) timestamp else System.currentTimeMillis(),
                sentiment = sentiment,
                symbols = symbols,
                tags = tags,
                analysis = analysis ?: _currentAnalysisResult.value?.fullAnalysis,
                audioPath = audioPath
            )
            val savedId = if (id == 0L) {
                dreamRepository.insertDream(entry)
            } else {
                dreamRepository.updateDream(entry)
                id
            }
            onSaved(savedId)
        }
    }

    fun deleteDream(dream: DreamEntry) {
        viewModelScope.launch {
            dreamRepository.deleteDream(dream)
            dream.audioPath?.let { voiceMemoManager.deleteMemoFile(it) }
        }
    }

    fun analyzeDreamContent(content: String, onCompleted: (DreamAnalysisResult) -> Unit = {}) {
        if (content.isBlank()) return
        viewModelScope.launch {
            _isAnalyzing.value = true
            val result = dreamAnalyzer.analyzeDream(content)
            _currentAnalysisResult.value = result
            _isAnalyzing.value = false
            onCompleted(result)
        }
    }

    fun clearCurrentAnalysis() {
        _currentAnalysisResult.value = null
    }

    fun startSpeechRecognition() {
        speechToTextManager.startListening()
    }

    fun stopSpeechRecognition() {
        speechToTextManager.stopListening()
    }

    fun startVoiceMemoRecording(): Boolean = voiceMemoManager.startRecording()

    fun stopVoiceMemoRecording(): String? = voiceMemoManager.stopRecording()

    fun cancelVoiceMemoRecording() {
        voiceMemoManager.cancelRecording()
    }

    fun getVoiceMemoDuration(path: String): Int = voiceMemoManager.getMemoDuration(path)

    fun playVoiceMemo(path: String) {
        voiceMemoManager.play(path)
    }

    fun pauseVoiceMemo() {
        voiceMemoManager.pausePlayback()
    }

    fun stopVoiceMemoPlayback() {
        voiceMemoManager.stopPlayback()
    }

    fun deleteVoiceMemoFile(path: String) {
        voiceMemoManager.deleteMemoFile(path)
    }

    suspend fun buildJournalMarkdown(): String {
        val allDreams = dreamRepository.getAllDreams().first()
        return JournalExporter.buildMarkdown(allDreams)
    }

    fun updateModelPath(path: String?) {
        settingsRepository.setModelPath(path)
    }

    fun setUseLocalLlm(enabled: Boolean) {
        settingsRepository.setUseLocalLlm(enabled)
    }

    fun isModelPresetDownloaded(preset: LlmModelPreset): Boolean {
        return modelDownloadManager.isModelDownloaded(preset)
    }

    fun downloadModelPreset(preset: LlmModelPreset) {
        viewModelScope.launch {
            modelDownloadManager.downloadModelPreset(preset)
            val path = modelDownloadManager.getDownloadedModelPath(preset)
            if (path != null) {
                updateModelPath(path)
            }
        }
    }

    fun cancelDownload() {
        modelDownloadManager.cancelDownload()
    }

    fun deleteModelPreset(preset: LlmModelPreset) {
        modelDownloadManager.deleteDownloadedModel(preset)
        if (modelPath.value == modelDownloadManager.getDownloadedModelPath(preset)) {
            updateModelPath(null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechToTextManager.destroy()
        voiceMemoManager.release()
    }
}
