package com.diego.dreambud.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diego.dreambud.audio.VoiceMemoManager
import com.diego.dreambud.ui.viewmodel.DreamViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DreamDetailScreen(
    dreamId: Long,
    viewModel: DreamViewModel,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val dream by viewModel.getDreamById(dreamId).collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val playbackState by viewModel.voiceMemoPlaybackState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteMemoDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d, yyyy • h:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dream Entry") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(dreamId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Dream")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Dream",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        dream?.let { currentDream ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title and Sentiment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentDream.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    if (!currentDream.sentiment.isNullOrBlank()) {
                        val sentimentColor = when (currentDream.sentiment.lowercase()) {
                            "anxious" -> Color(0xFFE53935)
                            "transformative" -> Color(0xFF43A047)
                            "joyful", "liberating" -> Color(0xFFFB8C00)
                            else -> MaterialTheme.colorScheme.primary
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(sentimentColor.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = currentDream.sentiment,
                                style = MaterialTheme.typography.labelMedium,
                                color = sentimentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Date
                Text(
                    text = dateFormat.format(Date(currentDream.timestamp)),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Symbols tags
                if (!currentDream.symbols.isNullOrBlank()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentDream.symbols.split(",").forEach { sym ->
                            val cleanSym = sym.trim()
                            if (cleanSym.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "🗝️ $cleanSym",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Dream Narrative Content Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Dream Narrative",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentDream.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Voice Memo Attachment Card
                currentDream.audioPath?.let { memoPath ->
                    if (File(memoPath).exists()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {
                                    if (playbackState is VoiceMemoManager.PlaybackState.Playing) {
                                        viewModel.pauseVoiceMemo()
                                    } else {
                                        viewModel.playVoiceMemo(memoPath)
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (playbackState is VoiceMemoManager.PlaybackState.Playing)
                                            Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (playbackState is VoiceMemoManager.PlaybackState.Playing)
                                            "Pause voice memo" else "Play voice memo",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🎙️ Voice Memo",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = when (val ps = playbackState) {
                                            is VoiceMemoManager.PlaybackState.Playing ->
                                                "${VoiceMemoManager.formatDuration(ps.positionMs.toLong())} / ${VoiceMemoManager.formatDuration(ps.durationMs.toLong())}"
                                            is VoiceMemoManager.PlaybackState.Paused ->
                                                "Paused at ${VoiceMemoManager.formatDuration(ps.positionMs.toLong())}"
                                            else ->
                                                VoiceMemoManager.formatDuration(
                                                    viewModel.getVoiceMemoDuration(memoPath).toLong()
                                                )
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

                // Local AI Analysis Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
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
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Local AI Dream Analysis",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (!currentDream.analysis.isNullOrBlank()) {
                            Text(
                                text = currentDream.analysis,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "No local AI analysis generated yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.analyzeDreamContent(currentDream.content) { result ->
                                viewModel.saveDream(
                                    id = currentDream.id,
                                    title = currentDream.title,
                                    content = currentDream.content,
                                    sentiment = result.sentiment,
                                    symbols = result.symbols.joinToString(", "),
                                    tags = currentDream.tags,
                                    analysis = result.fullAnalysis,
                                    audioPath = currentDream.audioPath,
                                    timestamp = currentDream.timestamp
                                )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isAnalyzing,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Running Local AI Analysis...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (currentDream.analysis.isNullOrBlank()) "Analyze Dream with Local AI" else "Re-Analyze Dream")
                            }
                        }
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteDialog && dream != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Dream Entry?") },
                text = { Text("Are you sure you want to delete this dream entry? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            dream?.let { viewModel.deleteDream(it) }
                            showDeleteDialog = false
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Voice Memo Confirmation Dialog
        if (showDeleteMemoDialog && dream?.audioPath != null) {
            AlertDialog(
                onDismissRequest = { showDeleteMemoDialog = false },
                title = { Text("Delete Voice Memo?") },
                text = { Text("The recorded audio will be permanently removed from this dream entry.") },
                confirmButton = {
                    Button(
                        onClick = {
                            dream?.let { current ->
                                current.audioPath?.let { viewModel.deleteVoiceMemoFile(it) }
                                viewModel.saveDream(
                                    id = current.id,
                                    title = current.title,
                                    content = current.content,
                                    sentiment = current.sentiment,
                                    symbols = current.symbols,
                                    tags = current.tags,
                                    analysis = current.analysis,
                                    audioPath = null,
                                    timestamp = current.timestamp
                                )
                            }
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
