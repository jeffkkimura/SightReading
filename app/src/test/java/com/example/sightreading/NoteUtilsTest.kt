package com.example.sightreading

import com.example.sightreading.audio.NoteUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteUtilsTest {

    @Test
    fun testMidiToFrequencyAndBack() {
        val a4Freq = NoteUtils.midiToFrequency(69)
        assertEquals(440.0f, a4Freq, 0.01f)

        val c4Freq = NoteUtils.midiToFrequency(60)
        assertEquals(261.63f, c4Freq, 0.1f)

        val detectedMidi = NoteUtils.frequencyToClosestMidi(440.0f)
        assertEquals(69, detectedMidi)
    }

    @Test
    fun testNoteNaming() {
        assertEquals("C4", NoteUtils.midiToNoteName(60))
        assertEquals("A4", NoteUtils.midiToNoteName(69))
        assertEquals("G4", NoteUtils.midiToNoteName(67))
        assertEquals("F#4", NoteUtils.midiToNoteName(66))
        assertEquals("Db4", NoteUtils.midiToNoteName(61, useFlats = true))
    }

    @Test
    fun testNoteMatchTolerance() {
        assertTrue(NoteUtils.isNoteMatch(detectedMidi = 60, expectedMidi = 60, toleranceSemitones = 0))
        assertTrue(NoteUtils.isNoteMatch(detectedMidi = 61, expectedMidi = 60, toleranceSemitones = 1))
    }
}
