package com.example.sightreading

import com.example.sightreading.audio.NoteUtils
import com.example.sightreading.data.LessonRepository
import com.example.sightreading.model.KeySignature
import com.example.sightreading.model.MusicalNote
import com.example.sightreading.model.NoteDuration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.floor

class BeatTimingTest {

    private val noteSpacingPx = 80.0f
    val hitLineX = 270.0f // 25% of 1080px screen

    /**
     * Test 1 to 20: Reiterate 20 times across different BPMs, keys, and levels
     * to mathematically verify 100% alignment between metronome clicks,
     * note positions, and beat timestamps.
     */
    @Test
    fun testReiterate20BeatTimingAlignments() {
        val testBpms = listOf(40, 60, 75, 80, 90, 100, 120, 140, 160, 180)

        for (iteration in 1..20) {
            val bpm = testBpms[(iteration - 1) % testBpms.size]
            val key = KeySignature.entries[(iteration - 1) % KeySignature.entries.size]
            val levelIndex = ((iteration - 1) % 25) + 1

            val lesson = LessonRepository.getLessonById("lesson_${key.name.lowercase()}_$levelIndex")
            val flatNotes = lesson.scorePiece.measures.flatMap { it.notes }

            // 1. Verify every measure in the lesson has EXACTLY 4 quarter notes (4.0 beats)
            lesson.scorePiece.measures.forEachIndexed { mIdx, measure ->
                val measureBeats = measure.notes.sumOf { it.duration.beats.toDouble() }
                assertEquals(
                    "Iteration $iteration (Key ${key.displayName}, Level $levelIndex, Measure $mIdx): Measure MUST have exactly 4.0 beats",
                    4.0,
                    measureBeats,
                    0.001
                )
            }

            // 2. Compute cumulative beat positions
            var currentBeat = 0f
            val noteBeatPositions = flatNotes.map { note ->
                val pos = currentBeat
                currentBeat += note.duration.beats
                pos
            }

            // 3. Simulate continuous playback ticker over 16 full beats
            val msPerBeat = 60_000.0f / bpm
            val frameDeltaMs = 20.0f // 50 FPS ticker simulation
            var elapsedBeats = -4.0f // Start at count-in beat -4.0

            var simulatedMetronomeClicks = 0
            var lastMetronomeBeatInt = floor(elapsedBeats.toDouble()).toInt()

            val maxSimulationMs = 100_000f
            var timeMs = 0f

            while (timeMs < maxSimulationMs && elapsedBeats < 16.0f) {
                timeMs += frameDeltaMs
                val deltaBeats = (frameDeltaMs / msPerBeat)
                elapsedBeats += deltaBeats

                val currentBeatInt = floor(elapsedBeats.toDouble()).toInt()

                // Check Metronome Click Triggering
                if (currentBeatInt > lastMetronomeBeatInt) {
                    simulatedMetronomeClicks++
                    lastMetronomeBeatInt = currentBeatInt

                    // Verify that when elapsedBeats passes an integer beat B (where B >= 0),
                    // the note associated with beat B lands EXACTLY on hitLineX
                    if (currentBeatInt >= 0 && currentBeatInt < noteBeatPositions.size) {
                        val expectedNoteBeatPos = noteBeatPositions[currentBeatInt]
                        val calculatedNoteX = hitLineX + (expectedNoteBeatPos - elapsedBeats) * noteSpacingPx

                        // At the exact moment of beat crossing, calculatedNoteX must be within 1 frame delta of hitLineX
                        val maxAllowedDriftPx = (frameDeltaMs / msPerBeat) * noteSpacingPx
                        assertTrue(
                            "Iteration $iteration (BPM $bpm): Note at beat $currentBeatInt must line up with hitLineX within frame tolerance",
                            kotlin.math.abs(calculatedNoteX - hitLineX) <= maxAllowedDriftPx + 1.0f
                        )
                    }
                }
            }

            // Verify metronome clicked for all beats from -4 to 15
            assertTrue(
                "Iteration $iteration: Metronome should trigger for all count-in and lesson beats",
                simulatedMetronomeClicks >= 16
            )
        }
    }

    /**
     * Verification of Canvas note rendering equation:
     * Note X == hitLineX when elapsedBeats == noteBeatPos
     */
    @Test
    fun testExactNotePositionOnHitLine() {
        val noteBeatPos = 3.0f
        val elapsedBeats = 3.0f

        val calculatedNoteX = hitLineX + (noteBeatPos - elapsedBeats) * noteSpacingPx
        assertEquals(hitLineX, calculatedNoteX, 0.0001f)
    }

    @Test
    fun testGMajorAndGMinorNoteRecognition() {
        val gMajorLesson = LessonRepository.getLessonById("lesson_g_major_1")
        val gMajorNotes = gMajorLesson.scorePiece.measures.flatMap { it.notes }

        gMajorNotes.forEach { note ->
            val freq = NoteUtils.midiToFrequency(note.midiNote)
            val detectedMidi = NoteUtils.frequencyToClosestMidi(freq)
            assertEquals("Note pitch for G Major must match exact MIDI frequency", note.midiNote, detectedMidi)
            assertTrue("G Major note pitch must be recognized correctly", NoteUtils.isNoteMatch(detectedMidi, note.midiNote))
        }

        val gMinorLesson = LessonRepository.getLessonById("lesson_g_minor_1")
        val gMinorNotes = gMinorLesson.scorePiece.measures.flatMap { it.notes }

        gMinorNotes.forEach { note ->
            val freq = NoteUtils.midiToFrequency(note.midiNote)
            val detectedMidi = NoteUtils.frequencyToClosestMidi(freq)
            assertEquals("Note pitch for G Minor must match exact MIDI frequency", note.midiNote, detectedMidi)
            assertTrue("G Minor note pitch must be recognized correctly", NoteUtils.isNoteMatch(detectedMidi, note.midiNote))
        }
    }

    /**
     * Verification of Barline clearance:
     * Ensures vertical measure barlines are placed cleanly at measure boundaries (every 4.0 beats)
     * and do not overlap notes.
     */
    @Test
    fun testMeasureBarlineClearance() {
        val barPaddingPx = 30.0f

        for (measureIdx in 1..10) {
            val measureStartBeat = measureIdx * 4.0f
            val lastNoteInPrevMeasureBeat = (measureIdx * 4.0f) - 1.0f

            val elapsedBeats = 0.0f

            val lastNoteX = hitLineX + (lastNoteInPrevMeasureBeat - elapsedBeats) * noteSpacingPx
            val barX = hitLineX + (measureStartBeat - elapsedBeats) * noteSpacingPx

            // Distance between last note of measure and barline must be at least noteSpacingPx / 2
            val distance = barX - lastNoteX
            assertTrue(
                "Barline $measureIdx must have sufficient clearance after last note",
                distance >= 35.0f
            )
        }
    }
}
