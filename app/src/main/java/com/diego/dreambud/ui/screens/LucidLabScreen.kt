package com.diego.dreambud.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diego.dreambud.lucid.LucidStats
import com.diego.dreambud.reminder.RealityCheckManager
import com.diego.dreambud.ui.viewmodel.DreamViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LucidLabScreen(
    viewModel: DreamViewModel,
    onNavigateBack: () -> Unit
) {
    val stats by viewModel.lucidStats.collectAsState()
    val realityCheckInterval by viewModel.realityCheckIntervalMinutes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lucid Lab")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Lucid Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Your Lucid Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatItem(
                            label = "Lucid Dreams",
                            value = "${stats.lucidDreams}",
                            modifier = Modifier.weight(1f)
                        )
                        StatItem(
                            label = "Lucidity Rate",
                            value = "${stats.lucidityRatePercent}%",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatItem(
                            label = "Current Streak",
                            value = "${stats.currentStreakDays} day" + if (stats.currentStreakDays == 1) "" else "s",
                            modifier = Modifier.weight(1f)
                        )
                        StatItem(
                            label = "Last Lucid",
                            value = when (val days = stats.daysSinceLastLucid) {
                                null -> "—"
                                0 -> "Today"
                                1 -> "Yesterday"
                                else -> "$days days ago"
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        text = "Tag a dream as \"Lucid\" when recording it to track your progress here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Reality Check Reminders Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (realityCheckInterval > 0)
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reality Check Reminders",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Periodic notifications asking \"Are you dreaming?\" build the habit of questioning reality — the #1 trigger for becoming lucid inside a dream.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RealityCheckManager.INTERVAL_CHOICES_MINUTES.forEach { minutes ->
                            val label = when (minutes) {
                                0 -> "Off"
                                30 -> "Every 30 min"
                                60 -> "Every 60 min"
                                90 -> "Every 90 min"
                                120 -> "Every 2 hours"
                                else -> "Every $minutes min"
                            }
                            FilterChip(
                                selected = realityCheckInterval == minutes,
                                onClick = { viewModel.updateRealityCheckInterval(minutes) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            // Technique Guides
            Text(
                text = "🧪 Lucid Dreaming Techniques",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            TECHNIQUES.forEach { technique ->
                ExpandableGuideCard(technique = technique)
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExpandableGuideCard(technique: TechniqueGuide) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = technique.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = technique.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = technique.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess
                    else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    technique.steps.forEachIndexed { index, step ->
                        Row {
                            Text(
                                text = "${index + 1}. ",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class TechniqueGuide(
    val title: String,
    val icon: ImageVector,
    val summary: String,
    val steps: List<String>
)

private val TECHNIQUES = listOf(
    TechniqueGuide(
        title = "Reality Checks",
        icon = Icons.Default.Visibility,
        summary = "Question reality throughout the day so it becomes a habit inside dreams",
        steps = listOf(
            "Look at your hands and count your fingers — they often look wrong in dreams.",
            "Read a sentence twice — in dreams, text usually changes between reads.",
            "Try to push a finger through your opposite palm.",
            "Check a digital clock, look away, then check again — clocks glitch in dreams.",
            "Perform checks around 10 times a day, especially whenever something odd happens."
        )
    ),
    TechniqueGuide(
        title = "MILD (Mnemonic Induction)",
        icon = Icons.Default.SelfImprovement,
        summary = "Set a strong intention to recognize when you are dreaming",
        steps = listOf(
            "Wake up after a dream and recall it in as much detail as possible.",
            "Repeat the phrase: \"Next time I'm dreaming, I will realize I'm dreaming.\"",
            "Visualize yourself back in that dream, becoming lucid this time.",
            "Drift back to sleep holding that intention.",
            "Combine with daytime reality checks for the strongest effect."
        )
    ),
    TechniqueGuide(
        title = "WBTB (Wake Back To Bed)",
        icon = Icons.Default.Bedtime,
        summary = "Wake briefly during the night, then return to sleep primed for lucidity",
        steps = listOf(
            "Sleep for about 5–6 hours first.",
            "Wake with an alarm and stay awake for 20–30 minutes.",
            "Read your dream journal or review the guides in this lab.",
            "Go back to bed and practice MILD as you fall asleep.",
            "Tip: schedule your Dream Alarm for this window to wake up reliably."
        )
    ),
    TechniqueGuide(
        title = "Dream Signs",
        icon = Icons.Default.AutoStories,
        summary = "Learn your recurring dream themes and use them as lucidity triggers",
        steps = listOf(
            "Review past journal entries for recurring themes — flying, water, certain places or people.",
            "Tag those entries so patterns emerge over time in your analytics.",
            "Whenever you encounter a sign while awake, perform a reality check.",
            "When you meet a sign inside a dream, trained suspicion can spark lucidity."
        )
    )
)
