package com.example.sightreading.ui.practice

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.sightreading.audio.NoteUtils
import com.example.sightreading.ui.components.SheetMusicCanvas
import com.example.sightreading.ui.components.VirtualPianoKeyboard

@Composable
fun PracticeScreen(
    viewModel: PracticeViewModel,
    lessonId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListeningAudio()
        }
    }

    LaunchedEffect(lessonId) {
        viewModel.loadLesson(lessonId)
        viewModel.startPractice()

        val hasMicPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasMicPermission) {
            viewModel.startListeningAudio()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Keep Screen Awake while lesson is active
    DisposableEffect(uiState.isPlaying, uiState.isCountInActive) {
        val activity = context as? Activity
        val isLessonActive = uiState.isPlaying || uiState.isCountInActive
        if (isLessonActive) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopListeningAudio()
            viewModel.pausePractice()
        }
    }

    // Zero-Scroll Widescreen Landscape Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // 1. Compact 1-Line Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "◀ Back",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { onBackClick() }
                        .padding(end = 8.dp)
                )

                Text(
                    text = "${uiState.currentLesson?.title ?: "Practice"} (${uiState.currentLesson?.keySignature?.displayName})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Status Banner / Notice
            if (uiState.isCountInActive) {
                Text(
                    text = "GET READY! Count-In: ${uiState.countInBeatsRemaining}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (uiState.wrongNoteAttempt != null) {
                val expectedName = uiState.currentLesson?.scorePiece?.measures?.flatMap { it.notes }?.getOrNull(uiState.activeNoteIndex)?.let { NoteUtils.midiToNoteName(it.midiNote) } ?: ""
                val heardName = NoteUtils.midiToNoteName(uiState.wrongNoteAttempt!!)
                Text(
                    text = "❌ Try $expectedName (Heard $heardName)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                val expectedName = uiState.currentLesson?.scorePiece?.measures?.flatMap { it.notes }?.getOrNull(uiState.activeNoteIndex)?.let { NoteUtils.midiToNoteName(it.midiNote) } ?: ""
                Text(
                    text = if (expectedName.isNotEmpty()) "🎹 Play $expectedName" else "🎙 Play Piano Note...",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = { viewModel.toggleMetronome() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(if (uiState.isMetronomeEnabled) "⏱ On" else "⏱ Off", fontSize = 10.sp)
                }

                Button(
                    onClick = { viewModel.togglePlayPause() },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(if (uiState.isPlaying || uiState.isCountInActive) "⏸ Pause" else "▶ Start", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 2. Middle Area: Sheet Music Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            uiState.currentLesson?.scorePiece?.let { piece ->
                SheetMusicCanvas(
                    scorePiece = piece,
                    elapsedBeats = uiState.elapsedBeats,
                    activeNoteIndex = uiState.activeNoteIndex,
                    noteResultsMap = uiState.noteResultsMap,
                    hitBurstEvent = uiState.latestHitBurst,
                    pitchResult = uiState.latestPitchResult,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 3. Score & Tempo Controls Row (Compact 1-Line Bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ScoreBadge(title = "Accuracy", value = "${uiState.accuracyPercentage.toInt()}%")
                ScoreBadge(title = "Combo", value = "${uiState.currentStreak} 🔥 (${uiState.comboMultiplier}x)")
                ScoreBadge(title = "Score", value = "${uiState.totalScorePoints} pts")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Tempo: ${uiState.bpm} BPM ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(
                    onClick = { viewModel.updateBpm(uiState.bpm - 5) },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp),
                    shape = RoundedCornerShape(4.dp)
                ) { Text("-5", fontSize = 10.sp) }
                Spacer(modifier = Modifier.width(4.dp))
                OutlinedButton(
                    onClick = { viewModel.updateBpm(uiState.bpm + 5) },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp),
                    shape = RoundedCornerShape(4.dp)
                ) { Text("+5", fontSize = 10.sp) }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 4. Fixed Bottom Virtual Piano Keyboard (No Scrolling Needed!)
        val rootMidi = uiState.currentLesson?.keySignature?.rootMidiRightHand ?: 60
        VirtualPianoKeyboard(
            rootMidi = rootMidi,
            latestHeardMidi = uiState.latestPitchResult.midiNote,
            onNotePressed = { keyMidi -> viewModel.simulateNotePlayed(keyMidi) },
            modifier = Modifier
                .fillMaxWidth()
                .height(95.dp)
                .padding(horizontal = 4.dp)
        )
    }

    // Completion Dialog
    if (uiState.isCompleted) {
        val isMastered = uiState.accuracyPercentage >= 90f

        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(if (isMastered) "🏆 Stage Mastered (90%+ Accuracy)!" else "⚠️ 90% Accuracy Required")
            },
            text = {
                Column {
                    Text(
                        text = "Rating: " + "★".repeat(uiState.starsEarned),
                        fontSize = 24.sp,
                        color = Color(0xFFFFB300)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Final Accuracy: ${uiState.accuracyPercentage.toInt()}%")
                    Text("Total Score: ${uiState.totalScorePoints} Pts")
                    Text("Max Combo Streak: ${uiState.maxStreak} Notes 🔥")
                    Spacer(modifier = Modifier.height(8.dp))

                    if (!isMastered) {
                        Text(
                            text = "To unlock the next stage, complete this stage with at least 90% accuracy. Tap below to practice a fresh melodic variation!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text(
                            text = "Great job! You proved your sight reading skill and unlocked the next stage!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isMastered) {
                            onBackClick()
                        } else {
                            viewModel.resetPractice(generateFreshVariation = true)
                            viewModel.startPractice()
                        }
                    }
                ) {
                    Text(if (isMastered) "Continue ▶" else "Retry Fresh Variation 🔄")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { onBackClick() }) {
                    Text("World Map 🗺️")
                }
            }
        )
    }
}

@Composable
private fun ScoreBadge(title: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$title: ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
            Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), fontSize = 10.sp)
        }
    }
}
