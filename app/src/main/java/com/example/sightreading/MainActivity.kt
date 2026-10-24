package com.example.sightreading

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sightreading.data.LessonRepository
import com.example.sightreading.data.UserPreferencesRepository
import com.example.sightreading.model.KeySignature
import com.example.sightreading.ui.home.HomeScreen
import com.example.sightreading.ui.lessons.LessonListScreen
import com.example.sightreading.ui.practice.PracticeScreen
import com.example.sightreading.ui.practice.PracticeViewModel
import com.example.sightreading.ui.theme.SightReadingTheme
import com.example.sightreading.ui.tuner.TunerScreen
import com.example.sightreading.ui.world.WorldMapScreen

sealed class Screen {
    object Home : Screen()
    data class WorldMap(val keySignature: KeySignature = KeySignature.C_MAJOR) : Screen()
    data class LessonList(val keySignature: KeySignature? = null) : Screen()
    data class Practice(val lessonId: String) : Screen()
    object Tuner : Screen()
}

class MainActivity : ComponentActivity() {

    private lateinit var userPrefsRepo: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // MUST initialize userPrefsRepo BEFORE setContent!
        userPrefsRepo = UserPreferencesRepository(applicationContext)

        setContent {
            SightReadingTheme {
                SightReadingApp(userPrefsRepo = userPrefsRepo)
            }
        }
    }
}

@Composable
fun SightReadingApp(userPrefsRepo: UserPreferencesRepository) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val practiceViewModel: PracticeViewModel = viewModel()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (val screen = currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        userPrefsRepo = userPrefsRepo,
                        onSelectKey = { keySig ->
                            currentScreen = Screen.WorldMap(keySignature = keySig)
                        },
                        onOpenTuner = {
                            currentScreen = Screen.Tuner
                        },
                        onStartRecommendedLesson = {
                            val proficiencies = userPrefsRepo.keyProficiencies.value
                            val lowestKey = proficiencies.minByOrNull { it.value.proficiencyPercentage }?.key
                                ?: KeySignature.C_MAJOR
                            val recommendedLesson = LessonRepository.getLessonsForKey(lowestKey).firstOrNull()
                                ?: LessonRepository.getAllLessons().first()

                            currentScreen = Screen.Practice(lessonId = recommendedLesson.id)
                        }
                    )
                }

                is Screen.WorldMap -> {
                    WorldMapScreen(
                        userPrefsRepo = userPrefsRepo,
                        initialKey = screen.keySignature,
                        onSelectLesson = { lesson ->
                            currentScreen = Screen.Practice(lessonId = lesson.id)
                        },
                        onOpenTuner = {
                            currentScreen = Screen.Tuner
                        },
                        onBackClick = {
                            currentScreen = Screen.Home
                        }
                    )
                }

                is Screen.LessonList -> {
                    LessonListScreen(
                        userPrefsRepo = userPrefsRepo,
                        initialKeyFilter = screen.keySignature,
                        onSelectLesson = { lesson ->
                            currentScreen = Screen.Practice(lessonId = lesson.id)
                        },
                        onBackClick = {
                            currentScreen = Screen.Home
                        }
                    )
                }

                is Screen.Practice -> {
                    PracticeScreen(
                        viewModel = practiceViewModel,
                        lessonId = screen.lessonId,
                        onBackClick = {
                            currentScreen = Screen.WorldMap()
                        }
                    )
                }

                is Screen.Tuner -> {
                    TunerScreen(
                        onBackClick = {
                            currentScreen = Screen.Home
                        }
                    )
                }
            }
        }
    }
}
