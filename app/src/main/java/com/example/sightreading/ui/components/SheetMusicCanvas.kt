package com.example.sightreading.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sightreading.audio.PitchResult
import com.example.sightreading.model.Hand
import com.example.sightreading.model.KeySignature
import com.example.sightreading.model.MusicalNote
import com.example.sightreading.model.NoteDuration
import com.example.sightreading.model.ScorePiece
import com.example.sightreading.ui.practice.HitBurstEvent
import com.example.sightreading.ui.theme.WorldThemeRepository

@Composable
fun SheetMusicCanvas(
    scorePiece: ScorePiece,
    elapsedBeats: Float,
    activeNoteIndex: Int,
    noteResultsMap: Map<Int, Boolean>,
    hitBurstEvent: HitBurstEvent? = null,
    pitchResult: PitchResult = PitchResult(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val textMeasurer = rememberTextMeasurer()
    val isGrandStaff = scorePiece.handMode == Hand.BOTH_HANDS

    val clefTrebleStyle = remember { TextStyle(fontSize = 38.sp, color = Color.Black) }
    val clefBassStyle = remember { TextStyle(fontSize = 32.sp, color = Color.Black) }
    val restStyle = remember { TextStyle(fontSize = 28.sp, color = Color.Black) }
    val keySigStyle = remember { TextStyle(fontSize = 22.sp, color = Color.DarkGray) }
    val comboStyle = remember { TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800)) }

    val worldTheme = remember(scorePiece.keySignature) { WorldThemeRepository.getThemeForKey(scorePiece.keySignature) }
    val drawableResId = remember(worldTheme.spriteType) {
        WorldSpriteBackgrounds.getSpriteDrawableRes(context, worldTheme.spriteType)
    }

    val bgBitmap = remember(drawableResId) {
        if (drawableResId != null) {
            try {
                BitmapFactory.decodeResource(context.resources, drawableResId)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isGrandStaff) 280.dp else 180.dp)
            .background(Color(0xFFFCFBF7))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 0. Draw World PNG Image Background
            if (bgBitmap != null) {
                val scale = maxOf(canvasWidth / bgBitmap.width.toFloat(), canvasHeight / bgBitmap.height.toFloat())
                val scaledW = (bgBitmap.width * scale).toInt()
                val scaledH = (bgBitmap.height * scale).toInt()
                val offsetX = ((canvasWidth - scaledW) / 2f).toInt()
                val offsetY = ((canvasHeight - scaledH) / 2f).toInt()

                drawImage(
                    image = bgBitmap,
                    dstOffset = IntOffset(offsetX, offsetY),
                    dstSize = IntSize(scaledW, scaledH)
                )

                drawRect(color = Color(0xFFFCFBF7).copy(alpha = 0.55f))
            }

            val lineSpacing = 14.dp.toPx()
            val staffLineStroke = 2.dp.toPx()
            val noteRadius = 9.dp.toPx()

            val hitLineX = canvasWidth * 0.25f
            val noteSpacingPerBeat = 80.dp.toPx()

            val trebleTopY = if (isGrandStaff) canvasHeight * 0.22f else canvasHeight * 0.35f
            val bassTopY = if (isGrandStaff) canvasHeight * 0.62f else canvasHeight * 0.35f

            val barTopY = if (isGrandStaff) trebleTopY else if (scorePiece.measures.flatMap { it.notes }.any { it.midiNote < 60 }) bassTopY else trebleTopY
            val barBottomY = if (isGrandStaff) bassTopY + 4 * lineSpacing else barTopY + 4 * lineSpacing

            // 0.5 Sound-Activated Concentric Circular Wave Visualizer
            if (!pitchResult.isSilence && pitchResult.rms > 0.012f) {
                val amplitude = (pitchResult.rms / 0.12f).coerceIn(0.15f, 1.0f)
                val targetCenterY = trebleTopY + lineSpacing * 2f

                val waveColor = when (Math.floorMod(pitchResult.midiNote, 12)) {
                    0, 1 -> Color(0xFF00E5FF) // C Cyan
                    2, 3 -> Color(0xFF7C4DFF) // D Purple
                    4 -> Color(0xFFFF4081)    // E Pink
                    5, 6 -> Color(0xFF00E676) // F Emerald
                    7, 8 -> Color(0xFFFFD54F) // G Gold
                    9, 10 -> Color(0xFFFF6D00)// A Orange
                    11 -> Color(0xFFD500F9)   // B Magenta
                    else -> Color(0xFFFFD54F)
                }

                for (rIdx in 1..3) {
                    val radius = (rIdx * 38.dp.toPx()) * amplitude
                    val alpha = ((1.0f - (rIdx * 0.22f)) * amplitude).coerceIn(0.15f, 0.75f)

                    drawCircle(
                        color = waveColor.copy(alpha = alpha),
                        center = Offset(hitLineX, targetCenterY),
                        radius = radius,
                        style = Stroke(width = (4.5f - rIdx).coerceAtLeast(1.5f).dp.toPx())
                    )
                }
            }

            // 1. Draw Staff Lines
            if (isGrandStaff) {
                drawStaffLines(trebleTopY, lineSpacing, staffLineStroke, canvasWidth)
                drawStaffLines(bassTopY, lineSpacing, staffLineStroke, canvasWidth)
                drawGrandStaffBrace(trebleTopY, bassTopY + 4 * lineSpacing, canvasWidth * 0.05f)
            } else if (scorePiece.measures.flatMap { it.notes }.any { it.midiNote < 60 }) {
                drawStaffLines(bassTopY, lineSpacing, staffLineStroke, canvasWidth)
            } else {
                drawStaffLines(trebleTopY, lineSpacing, staffLineStroke, canvasWidth)
            }

            // 2. Draw Clefs Centered Neatly Inside Staff Measure Lines
            val clefX = canvasWidth * 0.04f
            if (isGrandStaff) {
                drawText(textMeasurer, "𝄞", Offset(clefX, trebleTopY + lineSpacing * 0.15f), style = clefTrebleStyle)
                drawText(textMeasurer, "𝄢", Offset(clefX, bassTopY + lineSpacing * 0.25f), style = clefBassStyle)
            } else if (scorePiece.measures.flatMap { it.notes }.any { it.midiNote < 60 }) {
                drawText(textMeasurer, "𝄢", Offset(clefX, bassTopY + lineSpacing * 0.25f), style = clefBassStyle)
            } else {
                drawText(textMeasurer, "𝄞", Offset(clefX, trebleTopY + lineSpacing * 0.15f), style = clefTrebleStyle)
            }

            // 3. Draw Key Signature
            val keySigX = canvasWidth * 0.12f
            drawKeySignature(scorePiece.keySignature, keySigX, trebleTopY, bassTopY, isGrandStaff, lineSpacing, textMeasurer, keySigStyle)

            // 4. Target Hit Line
            drawLine(
                color = Color(0xFF1E88E5),
                start = Offset(hitLineX, 10.dp.toPx()),
                end = Offset(hitLineX, canvasHeight - 10.dp.toPx()),
                strokeWidth = 3.dp.toPx()
            )

            // 5. Draw Barlines
            val initialBarX = hitLineX + (-0.5f - elapsedBeats) * noteSpacingPerBeat
            if (initialBarX > -60.dp.toPx() && initialBarX < canvasWidth + 60.dp.toPx()) {
                drawLine(
                    color = Color.DarkGray,
                    start = Offset(initialBarX, barTopY),
                    end = Offset(initialBarX, barBottomY),
                    strokeWidth = 2.dp.toPx()
                )
            }

            var measureEndBeatCounter = 0f
            scorePiece.measures.forEach { measure ->
                val measureBeats = measure.notes.sumOf { it.duration.beats.toDouble() }.toFloat()
                measureEndBeatCounter += measureBeats

                val barX = hitLineX + ((measureEndBeatCounter - 0.5f) - elapsedBeats) * noteSpacingPerBeat
                if (barX > -60.dp.toPx() && barX < canvasWidth + 60.dp.toPx()) {
                    drawLine(
                        color = Color.DarkGray,
                        start = Offset(barX, barTopY),
                        end = Offset(barX, barBottomY),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            // 6. Draw Beat-Aligned Scrolling Notes
            var currentNoteGlobalIdx = 0
            var currentNoteBeatPos = 0f

            scorePiece.measures.forEach { measure ->
                measure.notes.forEach { note ->
                    val noteX = hitLineX + (currentNoteBeatPos - elapsedBeats) * noteSpacingPerBeat

                    if (noteX > -60.dp.toPx() && noteX < canvasWidth + 60.dp.toPx()) {
                        val isTreble = note.midiNote >= 60
                        val staffTopY = if (isGrandStaff) {
                            if (isTreble) trebleTopY else bassTopY
                        } else {
                            if (scorePiece.measures.flatMap { it.notes }.any { it.midiNote < 60 }) bassTopY else trebleTopY
                        }

                        val noteY = calculateNoteY(note.midiNote, staffTopY, lineSpacing, isTreble)
                        val isHitCorrect = noteResultsMap[currentNoteGlobalIdx] == true

                        val noteColor = when {
                            currentNoteGlobalIdx == activeNoteIndex -> Color(0xFFFFA000)
                            isHitCorrect -> Color(0xFF00E676)
                            noteResultsMap[currentNoteGlobalIdx] == false -> Color(0xFFE53935)
                            else -> Color(0xFF212121)
                        }

                        if (!note.isRest) {
                            if (isHitCorrect) {
                                drawCircle(
                                    color = Color(0xFF00E676).copy(alpha = 0.35f),
                                    center = Offset(noteX, noteY),
                                    radius = noteRadius * 2.2f
                                )
                            }

                            drawNote(
                                note = note,
                                x = noteX,
                                y = noteY,
                                staffTopY = staffTopY,
                                lineSpacing = lineSpacing,
                                noteRadius = noteRadius,
                                color = noteColor
                            )
                        } else {
                            drawText(
                                textMeasurer = textMeasurer,
                                text = "𝄽",
                                topLeft = Offset(noteX - 8.dp.toPx(), staffTopY + lineSpacing),
                                style = TextStyle(fontSize = 28.sp, color = noteColor)
                            )
                        }
                    }

                    currentNoteBeatPos += note.duration.beats
                    currentNoteGlobalIdx++
                }
            }

            // 7. Draw Hit Particle Burst & Combo Floater
            hitBurstEvent?.let { burst ->
                val timeDelta = System.currentTimeMillis() - burst.timestamp
                if (timeDelta < 800) {
                    val alpha = (1f - (timeDelta / 800f)).coerceIn(0f, 1f)
                    val offsetY = (timeDelta / 800f) * 40.dp.toPx()

                    for (i in 0 until 6) {
                        val angle = (i * 60) * (Math.PI / 180.0)
                        val dist = (timeDelta / 800f) * 30.dp.toPx()
                        val px = hitLineX + (Math.cos(angle) * dist).toFloat()
                        val py = trebleTopY + (Math.sin(angle) * dist).toFloat()

                        drawCircle(
                            color = Color(0xFFFFD54F).copy(alpha = alpha),
                            center = Offset(px, py),
                            radius = 5.dp.toPx()
                        )
                    }

                    val comboText = if (burst.multiplier > 1) "+${burst.points} PERFECT! 🔥 ${burst.multiplier}x COMBO" else "+${burst.points} PERFECT!"
                    drawText(
                        textMeasurer = textMeasurer,
                        text = comboText,
                        topLeft = Offset(hitLineX + 10.dp.toPx(), trebleTopY - 30.dp.toPx() - offsetY),
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800).copy(alpha = alpha))
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawStaffLines(
    topY: Float,
    spacing: Float,
    strokeWidth: Float,
    canvasWidth: Float
) {
    for (i in 0 until 5) {
        val y = topY + i * spacing
        drawLine(
            color = Color(0xFF8D8C88),
            start = Offset(0f, y),
            end = Offset(canvasWidth, y),
            strokeWidth = strokeWidth
        )
    }
}

private fun DrawScope.drawGrandStaffBrace(
    topY: Float,
    bottomY: Float,
    x: Float
) {
    drawLine(
        color = Color.Black,
        start = Offset(x, topY),
        end = Offset(x, bottomY),
        strokeWidth = 4.dp.toPx()
    )
}

private fun calculateNoteY(
    midiNote: Int,
    staffTopY: Float,
    lineSpacing: Float,
    isTreble: Boolean
): Float {
    val stepsFromRef = if (isTreble) {
        val refMidiF5 = 77
        midiToDiatonicStep(refMidiF5) - midiToDiatonicStep(midiNote)
    } else {
        val refMidiA3 = 57
        midiToDiatonicStep(refMidiA3) - midiToDiatonicStep(midiNote)
    }

    return staffTopY + (stepsFromRef * (lineSpacing / 2f))
}

private fun midiToDiatonicStep(midi: Int): Int {
    val octave = (midi / 12) - 1
    val pitchClass = Math.floorMod(midi, 12)
    val diatonicInOctave = when (pitchClass) {
        0, 1 -> 0
        2, 3 -> 1
        4 -> 2
        5, 6 -> 3
        7, 8 -> 4
        9, 10 -> 5
        11 -> 6
        else -> 0
    }
    return octave * 7 + diatonicInOctave
}

private fun DrawScope.drawNote(
    note: MusicalNote,
    x: Float,
    y: Float,
    staffTopY: Float,
    lineSpacing: Float,
    noteRadius: Float,
    color: Color
) {
    val staffBottomY = staffTopY + 4 * lineSpacing
    if (y > staffBottomY + lineSpacing * 0.4f) {
        var ledgerY = staffBottomY + lineSpacing
        while (ledgerY <= y + 2f) {
            drawLine(
                color = color,
                start = Offset(x - noteRadius * 1.5f, ledgerY),
                end = Offset(x + noteRadius * 1.5f, ledgerY),
                strokeWidth = 2.dp.toPx()
            )
            ledgerY += lineSpacing
        }
    } else if (y < staffTopY - lineSpacing * 0.4f) {
        var ledgerY = staffTopY - lineSpacing
        while (ledgerY >= y - 2f) {
            drawLine(
                color = color,
                start = Offset(x - noteRadius * 1.5f, ledgerY),
                end = Offset(x + noteRadius * 1.5f, ledgerY),
                strokeWidth = 2.dp.toPx()
            )
            ledgerY -= lineSpacing
        }
    }

    val isFilled = note.duration != NoteDuration.HALF && note.duration != NoteDuration.WHOLE && note.duration != NoteDuration.DOTTED_HALF
    if (isFilled) {
        drawOval(
            color = color,
            topLeft = Offset(x - noteRadius, y - noteRadius * 0.75f),
            size = Size(noteRadius * 2f, noteRadius * 1.5f)
        )
    } else {
        drawOval(
            color = color,
            topLeft = Offset(x - noteRadius, y - noteRadius * 0.75f),
            size = Size(noteRadius * 2f, noteRadius * 1.5f),
            style = Stroke(width = 2.5.dp.toPx())
        )
    }

    if (note.duration != NoteDuration.WHOLE) {
        val stemUp = y >= staffTopY + 2 * lineSpacing
        val stemX = if (stemUp) x + noteRadius * 0.9f else x - noteRadius * 0.9f
        val stemEndY = if (stemUp) y - lineSpacing * 3.2f else y + lineSpacing * 3.2f

        drawLine(
            color = color,
            start = Offset(stemX, y),
            end = Offset(stemX, stemEndY),
            strokeWidth = 2.dp.toPx()
        )
    }
}

private fun DrawScope.drawKeySignature(
    keySignature: KeySignature,
    startX: Float,
    trebleTopY: Float,
    bassTopY: Float,
    isGrandStaff: Boolean,
    lineSpacing: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    textStyle: TextStyle
) {
    if (keySignature.accidentals == 0) return

    val symbol = if (keySignature.isFlatKey) "♭" else "♯"
    val count = keySignature.totalAccidentals

    var currentX = startX
    for (i in 0 until count) {
        if (isGrandStaff) {
            drawText(textMeasurer, symbol, Offset(currentX, trebleTopY + (i % 3) * lineSpacing - 10.dp.toPx()), style = textStyle)
            drawText(textMeasurer, symbol, Offset(currentX, bassTopY + (i % 3) * lineSpacing - 10.dp.toPx()), style = textStyle)
        } else {
            drawText(textMeasurer, symbol, Offset(currentX, trebleTopY + (i % 3) * lineSpacing - 10.dp.toPx()), style = textStyle)
        }
        currentX += 14.dp.toPx()
    }
}
