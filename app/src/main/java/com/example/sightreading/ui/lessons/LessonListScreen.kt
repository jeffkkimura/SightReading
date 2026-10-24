package com.example.sightreading.ui.lessons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sightreading.data.LessonRepository
import com.example.sightreading.data.UserPreferencesRepository
import com.example.sightreading.model.Hand
import com.example.sightreading.model.KeySignature
import com.example.sightreading.model.Lesson
import com.example.sightreading.model.LessonLevel

@Composable
fun LessonListScreen(
    userPrefsRepo: UserPreferencesRepository,
    initialKeyFilter: KeySignature? = null,
    onSelectLesson: (Lesson) -> Unit,
    onBackClick: () -> Unit
) {
    val completedLessonIds by userPrefsRepo.completedLessonIds.collectAsState()

    var selectedKeyFilter by remember { mutableStateOf<KeySignature?>(initialKeyFilter) }
    var selectedLevelFilter by remember { mutableStateOf<LessonLevel?>(null) }

    val allLessons = remember { LessonRepository.getAllLessons() }

    val filteredLessons = remember(selectedKeyFilter, selectedLevelFilter, allLessons) {
        allLessons.filter { lesson ->
            (selectedKeyFilter == null || lesson.keySignature == selectedKeyFilter) &&
            (selectedLevelFilter == null || lesson.level == selectedLevelFilter)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "◀ Back",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onBackClick() }
                    .padding(end = 12.dp)
            )

            Text(
                text = if (selectedKeyFilter != null) "${selectedKeyFilter?.displayName} Lessons" else "All Lessons Catalog",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Level Filter Row
        Text(text = "Filter by Level:", style = MaterialTheme.typography.labelMedium)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedLevelFilter == null,
                    onClick = { selectedLevelFilter = null },
                    label = { Text("All Levels") }
                )
            }
            items(LessonLevel.entries.toTypedArray()) { level ->
                FilterChip(
                    selected = selectedLevelFilter == level,
                    onClick = { selectedLevelFilter = if (selectedLevelFilter == level) null else level },
                    label = { Text(level.title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Key Filter Row
        Text(text = "Filter by Key Signature:", style = MaterialTheme.typography.labelMedium)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedKeyFilter == null,
                    onClick = { selectedKeyFilter = null },
                    label = { Text("All 24 Keys") }
                )
            }
            items(KeySignature.entries) { key ->
                FilterChip(
                    selected = selectedKeyFilter == key,
                    onClick = { selectedKeyFilter = if (selectedKeyFilter == key) null else key },
                    label = { Text(key.displayName) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Lesson List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredLessons) { lesson ->
                val isCompleted = completedLessonIds.contains(lesson.id)
                LessonCard(
                    lesson = lesson,
                    isCompleted = isCompleted,
                    onClick = { onSelectLesson(lesson) }
                )
            }
        }
    }
}

@Composable
private fun LessonCard(
    lesson: Lesson,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val handBadge = if (lesson.handMode == Hand.BOTH_HANDS) "👐 Both Hands" else "🖐 Right Hand"
    val cardColor = if (isCompleted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (isCompleted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "✅", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Text(
                    text = lesson.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionChip(
                        onClick = { },
                        label = { Text(handBadge, style = MaterialTheme.typography.labelSmall) }
                    )
                    SuggestionChip(
                        onClick = { },
                        label = { Text("🔑 ${lesson.keySignature.displayName}", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "★".repeat(lesson.difficultyStars),
                    color = Color(0xFFFFB300),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Play ▶",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
