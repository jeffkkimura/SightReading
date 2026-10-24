package com.example.sightreading.ui.world

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.sp
import com.example.sightreading.data.LessonRepository
import com.example.sightreading.data.UserPreferencesRepository
import com.example.sightreading.model.KeySignature
import com.example.sightreading.model.Lesson
import com.example.sightreading.ui.components.MarioWorldMapCanvas
import com.example.sightreading.ui.theme.WorldThemeRepository

@Composable
fun WorldMapScreen(
    userPrefsRepo: UserPreferencesRepository,
    initialKey: KeySignature = KeySignature.C_MAJOR,
    onSelectLesson: (Lesson) -> Unit,
    onOpenTuner: () -> Unit,
    onBackClick: () -> Unit
) {
    val completedLessonIds by userPrefsRepo.completedLessonIds.collectAsState()
    val masteredLessonIds by userPrefsRepo.masteredLessonIds.collectAsState()

    val allKeys = remember { KeySignature.entries }
    val initialPageIndex = remember(initialKey) {
        val idx = allKeys.indexOf(initialKey)
        if (idx >= 0) idx else 0
    }

    val pagerState = rememberPagerState(initialPage = initialPageIndex, pageCount = { allKeys.size })

    Box(modifier = Modifier.fillMaxSize()) {
        // Full-Screen Swiping World Map Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val keySig = allKeys[pageIndex]
            val worldTheme = remember(keySig) { WorldThemeRepository.getThemeForKey(keySig) }
            val currentLessons = remember(keySig) { LessonRepository.getLessonsForKey(keySig) }

            var selectedLesson by remember(keySig) { mutableStateOf(currentLessons.first()) }
            val mapScrollState = rememberScrollState()

            // Auto-scroll to active stage (bottom of map) when page opens
            LaunchedEffect(pageIndex, mapScrollState.maxValue) {
                if (mapScrollState.maxValue > 0) {
                    mapScrollState.scrollTo(mapScrollState.maxValue)
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // Vertically Scrollable 25-Stage Overworld Map Canvas
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(mapScrollState)
                ) {
                    MarioWorldMapCanvas(
                        worldTheme = worldTheme,
                        lessons = currentLessons,
                        completedLessonIds = completedLessonIds,
                        masteredLessonIds = masteredLessonIds,
                        onSelectLesson = { lesson ->
                            selectedLesson = lesson
                            onSelectLesson(lesson)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1500.dp)
                    )
                }

                // Sleek Floating "START LEVEL ▶" Button at Bottom Right
                Button(
                    onClick = { onSelectLesson(selectedLesson) },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    Text("START LEVEL ▶", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Sleek Floating "◀ Home" (Back) Button at Top Left
        Button(
            onClick = onBackClick,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.70f)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Text("◀ Home", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
