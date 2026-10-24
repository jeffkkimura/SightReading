package com.example.sightreading.ui.tuner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.sightreading.audio.PitchDetector
import kotlin.math.abs

@Composable
fun TunerScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pitchDetector = remember { PitchDetector() }

    val pitchResult by pitchDetector.pitchResult.collectAsState()
    val isListening by pitchDetector.isListening.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pitchDetector.startListening(scope)
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            pitchDetector.startListening(scope)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            pitchDetector.stopListening()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
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
                text = "Piano Tuner & Audio Visualizer",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main Note Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (pitchResult.isSilence) "--" else pitchResult.noteName,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (pitchResult.isSilence) MaterialTheme.colorScheme.onSurfaceVariant else {
                        if (abs(pitchResult.cents) < 10) Color(0xFF2E7D32) else Color(0xFFE53935)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (pitchResult.isSilence) "Play a note on your piano" else "${pitchResult.frequency.toInt()} Hz (MIDI ${pitchResult.midiNote})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Cent Deviation Indicator
                val tuningStatus = when {
                    pitchResult.isSilence -> "Listening..."
                    abs(pitchResult.cents) < 10 -> "Perfectly In Tune! ✅"
                    pitchResult.cents < 0 -> "Too Flat ♭ (${pitchResult.cents.toInt()} cents)"
                    else -> "Too Sharp ♯ (+${pitchResult.cents.toInt()} cents)"
                }

                Text(
                    text = tuningStatus,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (abs(pitchResult.cents) < 10 && !pitchResult.isSilence) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Gauge / Tuning Needle Visualizer
        TunerGaugeCanvas(
            cents = if (pitchResult.isSilence) 0f else pitchResult.cents,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        if (!isListening) {
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enable Microphone 🎙")
            }
        }
    }
}

@Composable
private fun TunerGaugeCanvas(
    cents: Float, // -50 to +50
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height * 0.8f
        val centerX = width * 0.5f

        // Draw Arc Dial
        drawLine(
            color = Color.Gray,
            start = Offset(20f, centerY),
            end = Offset(width - 20f, centerY),
            strokeWidth = 4.dp.toPx()
        )

        // Center Marker (0 Cents / Perfect Tune)
        drawLine(
            color = Color(0xFF2E7D32),
            start = Offset(centerX, centerY - 30.dp.toPx()),
            end = Offset(centerX, centerY + 10.dp.toPx()),
            strokeWidth = 4.dp.toPx()
        )

        // Needle position
        val needleX = centerX + (cents / 50f) * (width * 0.4f)
        drawLine(
            color = if (abs(cents) < 10) Color(0xFF2E7D32) else Color(0xFFE53935),
            start = Offset(needleX, centerY - 50.dp.toPx()),
            end = Offset(needleX, centerY + 10.dp.toPx()),
            strokeWidth = 6.dp.toPx()
        )
    }
}
