package com.diego.dreambud.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.diego.dreambud.audio.VoiceMemoManager
import com.diego.dreambud.lucid.LucidStats
import com.diego.dreambud.speech.SpeechState
import com.diego.dreambud.ui.viewmodel.DreamViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DreamEditScreen(
    dreamId: Long,
    viewModel: DreamViewModel,
    onNavigateBack: () -> Unit,
    initialTitle: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var title by remember { mutableStateOf(initialTitle ?: "") }
    var content by remember { mutableStateOf("") }
    var sentiment by remember { mutableStateOf("Mysterious") }
    var symbols by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var analysisText by remember { mutableStateOf<String?>(null) }
    var audioPath by remember { mutableStateOf<String?>(null) }
    var originalTimestamp by remember { mutableStateOf(0L) }
    var showDeleteMemoDialog by remember { mutableStateOf(false) }

    val speechState by viewModel.speechState.collectAsState()
    val partialSpeechText by viewModel.partialSpeechText.collectAsState()
    val recordState by viewModel.voiceMemoRecordState.collectAsState()
    val playbackState by viewModel.voiceMemoPlaybackState.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val currentAnalysisResult by viewModel.currentAnalysisResult.collectAsState()

    val isRecordingMemo = recordState is VoiceMemoManager.RecordState.Recording

    // Recording elapsed time ticker
    var recordingElapsedSec by remember { mutableStateOf(0L) }
    LaunchedEffect(recordState) {
        val state = recordState
        if (state is VoiceMemoManager.RecordState.Recording) {
            while (true) {
                recordingElapsedSec = (System.currentTimeMillis() - state.startedAt) / 1000
                delay(500)
            }
        } else {
            recordingElapsedSec = 0
        }
    }

    LaunchedEffect(recordState) {
        val state = recordState
        if (state is VoiceMemoManager.RecordState.Error) {
            snackbarHostState.showSnackbar("Voice memo error: ${state.message}")
        }
    }

    // Attached memo duration
    var memoDurationMs by remember { mutableStateOf(0) }
    LaunchedEffect(audioPath) {
        memoDurationMs = audioPath?.let { viewModel.getVoiceMemoDuration(it) } ?: 0
    }

    // If editing existing dream, load details
    if (dreamId != 0L) {
        val existingDream by viewModel.getDreamById(dreamId).collectAsState()
        LaunchedEffect(existingDream) {
            existingDream?.let {
                title = it.title
                content = it.content
                sentiment = it.sentiment ?: "Mysterious"
                symbols = it.symbols ?: ""
                tags = it.tags ?: ""
                analysisText = it.analysis
                audioPath = it.audioPath
                originalTimestamp = it.timestamp
            }
        }
    }

    LaunchedEffect(currentAnalysisResult) {
        currentAnalysisResult?.let { res ->
            analysisText = res.fullAnalysis
            sentiment = res.sentiment
            if (res.symbols.isNotEmpty() && symbols.isBlank()) {
                symbols = res.symbols.joinToString(", ")
            }
        }
    }

    // Permission launcher for RECORD_AUDIO
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startSpeechRecognition()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Microphone permission required for speech transcription")
            }
        }
    }

    // Permission launcher for voice memo recording
    val memoPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceMemoRecording()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Microphone permission required for voice memos")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopSpeechRecognition()
            viewModel.cancelVoiceMemoRecording()
            viewModel.stopVoiceMemoPlayback()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (dreamId == 0L) "New Dream" else "Edit Dream") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (content.isBlank()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please enter or dictate a dream description")
                                }
                                return@IconButton
                            }
                            viewModel.saveDream(
                                id = dreamId,
                                title = title,
                                content = content,
                                sentiment = sentiment,
                                symbols = symbols,
                                tags = tags,
                                analysis = analysisText,
                                audioPath = audioPath,
                                timestamp = originalTimestamp,
                                onSaved = { onNavigateBack() }
                            )
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save Dream")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Dream Title") },
                placeholder = { Text("e.g., Flying through the Castle") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Speech-To-Text Dictation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Speech Dictation",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val isListening = speechState is SpeechState.Listening || speechState is SpeechState.PartialResult
                        Button(
                            onClick = {
                                if (isListening) {
                                    viewModel.stopSpeechRecognition()
                                } else {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        viewModel.startSpeechRecognition()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isListening) "Stop Dictating" else "Start Microphone")
                        }
                    }

                    when (val state = speechState) {
                        is SpeechState.Listening -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(MaterialTheme.colorScheme.error, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Listening... Speak clearly into microphone",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        is SpeechState.PartialResult -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Recognized: \"${state.text}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        is SpeechState.Success -> {
                            LaunchedEffect(state.text) {
                                if (content.isBlank()) {
                                    content = state.text
                                } else {
                                    content += " " + state.text
                                }
                            }
                        }

                        is SpeechState.Error -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Speech error: ${state.message}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        else -> {}
                    }
                }
            }

            // Voice Memo Recording Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isRecordingMemo)
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Voice Memo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isRecordingMemo) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton(
                                    onClick = { viewModel.cancelVoiceMemoRecording() },
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text("Cancel")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val newPath = viewModel.stopVoiceMemoRecording()
                                        if (newPath != null) {
                                            audioPath?.let { viewModel.deleteVoiceMemoFile(it) }
                                            audioPath = newPath
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save Memo")
                                }
                            }
                        } else if (audioPath == null) {
                            Button(
                                onClick = {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (hasPermission) {
                                        viewModel.startVoiceMemoRecording()
                                    } else {
                                        memoPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Record")
                            }
                        }
                    }

                    if (isRecordingMemo) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(MaterialTheme.colorScheme.error, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recording... ${VoiceMemoManager.formatDuration(recordingElapsedSec * 1000)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Attached memo player
                    val attachedMemo = audioPath
                    if (!isRecordingMemo && attachedMemo != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                if (playbackState is VoiceMemoManager.PlaybackState.Playing) {
                                    viewModel.pauseVoiceMemo()
                                } else {
                                    viewModel.playVoiceMemo(attachedMemo)
                                }
                            }) {
                                Icon(
                                    imageVector = if (playbackState is VoiceMemoManager.PlaybackState.Playing)
                                        Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (playbackState is VoiceMemoManager.PlaybackState.Playing)
                                        "Pause memo" else "Play memo",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Voice memo attached",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = when (val ps = playbackState) {
                                        is VoiceMemoManager.PlaybackState.Playing ->
                                            "${VoiceMemoManager.formatDuration(ps.positionMs.toLong())} / ${VoiceMemoManager.formatDuration(ps.durationMs.toLong())}"
                                        is VoiceMemoManager.PlaybackState.Paused ->
                                            "Paused at ${VoiceMemoManager.formatDuration(ps.positionMs.toLong())} / ${VoiceMemoManager.formatDuration(ps.durationMs.toLong())}"
                                        else ->
                                            VoiceMemoManager.formatDuration(memoDurationMs.toLong())
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { showDeleteMemoDialog = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete voice memo",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // Dream Content Text Area
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("What did you dream?") },
                placeholder = { Text("Describe the events, feelings, people, and symbols in your dream...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Lucidity Quick Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = tags.split(",").any { it.trim().equals(LucidStats.LUCID_TAG, ignoreCase = true) },
                    onClick = { tags = LucidStats.toggleTag(tags, LucidStats.LUCID_TAG) },
                    label = { Text("⚡ Lucid") }
                )
                Text(
                    text = "Became aware you were dreaming?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Local LLM Analysis Action Button
            Button(
                onClick = {
                    if (content.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Enter a dream description to analyze!")
                        }
                    } else {
                        viewModel.analyzeDreamContent(content)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAnalyzing,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onSecondary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyzing dream with Local AI...")
                } else {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🔮 Analyze Dream with Local LLM")
                }
            }

            // LLM Analysis Result Display
            AnimatedVisibility(visible = !analysisText.isNullOrBlank()) {
                analysisText?.let { analysis ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Dream Analysis",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = analysis,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Save Entry Button
            Button(
                onClick = {
                    if (content.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please enter a dream description before saving")
                        }
                        return@Button
                    }
                    viewModel.saveDream(
                        id = dreamId,
                        title = title,
                        content = content,
                        sentiment = sentiment,
                        symbols = symbols,
                        tags = tags,
                        analysis = analysisText,
                        audioPath = audioPath,
                        timestamp = originalTimestamp,
                        onSaved = { onNavigateBack() }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Journal Entry", style = MaterialTheme.typography.titleMedium)
            }
        }

        // Delete Voice Memo Confirmation Dialog
        if (showDeleteMemoDialog && audioPath != null) {
            AlertDialog(
                onDismissRequest = { showDeleteMemoDialog = false },
                title = { Text("Delete Voice Memo?") },
                text = { Text("The recorded audio will be permanently removed from this dream entry.") },
                confirmButton = {
                    Button(
                        onClick = {
                            audioPath?.let { viewModel.deleteVoiceMemoFile(it) }
                            audioPath = null
                            showDeleteMemoDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteMemoDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
