package com.diego.dreambud.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Records voice memos as AAC (.m4a) files in app-private storage and plays them back.
 * Single recording and single playback at a time.
 */
class VoiceMemoManager(private val context: Context) {

    sealed class RecordState {
        data object Idle : RecordState()
        data class Recording(val startedAt: Long) : RecordState()
        data class Error(val message: String) : RecordState()
    }

    sealed class PlaybackState {
        data object Idle : PlaybackState()
        data class Playing(val positionMs: Int, val durationMs: Int) : PlaybackState()
        data class Paused(val positionMs: Int, val durationMs: Int) : PlaybackState()
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var recorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null

    private var player: MediaPlayer? = null
    private var currentPlayingPath: String? = null
    private var positionTicker: Job? = null

    private val _recordState = MutableStateFlow<RecordState>(RecordState.Idle)
    val recordState: StateFlow<RecordState> = _recordState.asStateFlow()

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val memoDir: File
        get() = File(context.filesDir, MEMO_DIR_NAME).apply { if (!exists()) mkdirs() }

    /** Starts recording a new memo. Returns false if recording could not start. */
    fun startRecording(): Boolean {
        if (_recordState.value is RecordState.Recording) return false
        stopPlayback()
        return try {
            val file = File(memoDir, "memo_${memoTimestampFormat.format(Date())}.m4a")
            val mediaRecorder = createRecorder()
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder.setAudioEncodingBitRate(128_000)
            mediaRecorder.setAudioSamplingRate(44_100)
            mediaRecorder.setOutputFile(file.absolutePath)
            mediaRecorder.prepare()
            mediaRecorder.start()
            recorder = mediaRecorder
            currentRecordingFile = file
            _recordState.value = RecordState.Recording(System.currentTimeMillis())
            true
        } catch (e: Exception) {
            recorder?.release()
            recorder = null
            _recordState.value = RecordState.Error(e.message ?: "Could not start recording")
            false
        }
    }

    /** Stops the active recording. Returns the saved file path, or null if discarded. */
    fun stopRecording(): String? {
        val mediaRecorder = recorder ?: return null
        recorder = null
        val file = currentRecordingFile
        currentRecordingFile = null
        _recordState.value = RecordState.Idle
        return try {
            mediaRecorder.stop()
            file?.takeIf { it.exists() && it.length() > 0 }?.absolutePath
        } catch (_: RuntimeException) {
            // stop() throws if no valid audio data was captured
            file?.delete()
            _recordState.value = RecordState.Error("Recording was too short to save")
            null
        } finally {
            mediaRecorder.release()
        }
    }

    /** Cancels the active recording, discarding the captured audio. */
    fun cancelRecording() {
        val mediaRecorder = recorder ?: return
        recorder = null
        try {
            mediaRecorder.stop()
        } catch (_: RuntimeException) {
        }
        mediaRecorder.release()
        currentRecordingFile?.delete()
        currentRecordingFile = null
        _recordState.value = RecordState.Idle
    }

    /** Returns duration in ms, or 0 if the file is missing/unreadable. */
    fun getMemoDuration(path: String): Int {
        if (!File(path).exists()) return 0
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(path)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toIntOrNull() ?: 0
        } catch (_: Exception) {
            0
        } finally {
            runCatching { retriever.release() }
        }
    }

    /** Plays (or resumes) the memo at path. Restarting another memo stops the previous one. */
    fun play(path: String) {
        if (!File(path).exists()) return
        try {
            if (player == null || currentPlayingPath != path) {
                stopPlayback()
                player = MediaPlayer().apply {
                    setDataSource(path)
                    setOnCompletionListener { resetPlaybackToIdle() }
                    prepare()
                }
                currentPlayingPath = path
            }
            player?.start()
            startPositionTicker()
            _playbackState.value = PlaybackState.Playing(player?.currentPosition ?: 0, player?.duration ?: 0)
        } catch (_: Exception) {
            stopPlayback()
        }
    }

    fun pausePlayback() {
        val mediaPlayer = player ?: return
        try {
            mediaPlayer.pause()
            positionTicker?.cancel()
            positionTicker = null
            _playbackState.value = PlaybackState.Paused(mediaPlayer.currentPosition, mediaPlayer.duration)
        } catch (_: Exception) {
        }
    }

    fun stopPlayback() {
        positionTicker?.cancel()
        positionTicker = null
        player?.release()
        player = null
        currentPlayingPath = null
        _playbackState.value = PlaybackState.Idle
    }

    /** Deletes the memo file on disk, stopping any recording/playback that uses it. */
    fun deleteMemoFile(path: String) {
        if (currentPlayingPath == path) stopPlayback()
        if (currentRecordingFile?.absolutePath == path) cancelRecording()
        File(path).delete()
    }

    fun release() {
        cancelRecording()
        stopPlayback()
        scope.cancel()
    }

    private fun resetPlaybackToIdle() {
        positionTicker?.cancel()
        positionTicker = null
        player?.release()
        player = null
        currentPlayingPath = null
        _playbackState.value = PlaybackState.Idle
    }

    private fun startPositionTicker() {
        positionTicker?.cancel()
        positionTicker = scope.launch {
            while (isActive && player?.isPlaying == true) {
                val mediaPlayer = player ?: break
                _playbackState.value = PlaybackState.Playing(mediaPlayer.currentPosition, mediaPlayer.duration)
                delay(POSITION_TICK_MS)
            }
        }
    }

    private fun createRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

    companion object {
        private const val MEMO_DIR_NAME = "voice_memos"
        private const val POSITION_TICK_MS = 250L
        private val memoTimestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

        fun formatDuration(millis: Long): String {
            val totalSeconds = millis.coerceAtLeast(0) / 1000
            return String.format(Locale.US, "%d:%02d", totalSeconds / 60, totalSeconds % 60)
        }
    }
}
