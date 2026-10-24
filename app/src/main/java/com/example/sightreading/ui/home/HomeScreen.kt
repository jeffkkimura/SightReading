package com.example.sightreading.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sightreading.data.UserPreferencesRepository
import com.example.sightreading.model.KeySignature
import com.example.sightreading.model.KeyType
import com.example.sightreading.ui.components.MasterOverworldCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userPrefsRepo: UserPreferencesRepository,
    onSelectKey: (KeySignature) -> Unit,
    onOpenTuner: () -> Unit,
    onStartRecommendedLesson: () -> Unit
) {
    val proficiencies by userPrefsRepo.keyProficiencies.collectAsState()
    val completedLessonIds by userPrefsRepo.completedLessonIds.collectAsState()

    var selectedTabIdx by remember { mutableStateOf(0) } // 0 = Major Keys, 1 = Minor Keys

    val filteredKeys = remember(selectedTabIdx, proficiencies) {
        val type = if (selectedTabIdx == 0) KeyType.MAJOR else KeyType.MINOR
        KeySignature.entries.filter { it.type == type }
    }

    val overallAvgAccuracy = remember(proficiencies) {
        val nonZero = proficiencies.values.filter { it.totalAttempts > 0 }
        if (nonZero.isNotEmpty()) nonZero.map { it.averageAccuracy }.average().toFloat() * 100f else 0f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Piano Sight Reading",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Master Sight Reading & Pitch Recognition",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(onClick = onOpenTuner) {
                Text("🎵 Tuner")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(title = "Completed", value = "${completedLessonIds.size}")
                StatItem(title = "Avg Accuracy", value = "${overallAvgAccuracy.toInt()}%")
                StatItem(title = "Active Keys", value = "${proficiencies.values.count { it.completedLessonsCount > 0 }}/24")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Practice Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onStartRecommendedLesson() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎹", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Quick Practice Lesson",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Start tailored practice for your weakest key signature",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text("Start ▶", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Master Overworld Map Header
        Text(
            text = "24 Key World Islands Map",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Text(
            text = "Tap any World Island on the Overworld Map below to enter its 25 stages!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Major / Minor Tab Selector
        PrimaryTabRow(selectedTabIndex = selectedTabIdx) {
            Tab(
                selected = selectedTabIdx == 0,
                onClick = { selectedTabIdx = 0 },
                text = { Text("12 Major Worlds") }
            )
            Tab(
                selected = selectedTabIdx == 1,
                onClick = { selectedTabIdx = 1 },
                text = { Text("12 Minor Worlds") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Master Overworld Archipelago Canvas
        MasterOverworldCanvas(
            keysList = filteredKeys,
            proficiencies = proficiencies,
            onSelectKey = onSelectKey,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}
