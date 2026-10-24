package com.example.sightreading.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sightreading.audio.NoteUtils

@Composable
fun VirtualPianoKeyboard(
    modifier: Modifier = Modifier,
    rootMidi: Int = 60,
    latestHeardMidi: Int = 0,
    onNotePressed: (Int) -> Unit
) {
    // 15 White Keys (C4 to C6)
    // C4(60), D4(62), E4(64), F4(65), G4(67), A4(69), B4(71), C5(72), D5(74), E5(76), F5(77), G5(79), A5(81), B5(83), C6(84)
    val whiteKeyMidis = listOf(60, 62, 64, 65, 67, 69, 71, 72, 74, 76, 77, 79, 81, 83, 84)

    // Black Keys: (MidiNote, WhiteKeyGapIndex)
    // C#4(61), D#4(63), F#4(66), G#4(68), A#4(70), C#5(73), D#5(75), F#5(78), G#5(80), A#5(82)
    val blackKeyData = listOf(
        Pair(61, 1),
        Pair(63, 2),
        Pair(66, 4),
        Pair(68, 5),
        Pair(70, 6),
        Pair(73, 8),
        Pair(75, 9),
        Pair(78, 11),
        Pair(80, 12),
        Pair(82, 13)
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(Color(0xFF111111)) // Piano Frame Outer Casing
            .padding(3.dp)
    ) {
        val totalWidth = maxWidth
        val whiteKeyWidth = totalWidth / whiteKeyMidis.size
        val blackKeyWidth = whiteKeyWidth * 0.58f

        // 1. Bottom Layer: White Keys
        Row(modifier = Modifier.fillMaxSize()) {
            whiteKeyMidis.forEach { keyMidi ->
                val noteName = NoteUtils.midiToNoteName(keyMidi)
                val isPressed = latestHeardMidi == keyMidi

                val keyColor = if (isPressed) Color(0xFFFFD54F) else Color(0xFFFAFAFA)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 1.dp)
                        .clip(RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp))
                        .background(keyColor)
                        .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp))
                        .clickable { onNotePressed(keyMidi) },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = noteName,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }

        // 2. Top Layer: Raised Black Keys
        blackKeyData.forEach { (keyMidi, gapIndex) ->
            val noteName = NoteUtils.midiToNoteName(keyMidi)
            val isPressed = latestHeardMidi == keyMidi

            val keyColor = if (isPressed) Color(0xFFFF9800) else Color(0xFF212121)
            val xOffset = (whiteKeyWidth * gapIndex) - (blackKeyWidth / 2f)

            Box(
                modifier = Modifier
                    .offset(x = xOffset)
                    .width(blackKeyWidth)
                    .fillMaxHeight(0.62f)
                    .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                    .background(keyColor)
                    .border(1.dp, Color(0xFF424242), RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                    .clickable { onNotePressed(keyMidi) },
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = noteName.replace(Regex("[0-9]"), ""),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
    }
}
