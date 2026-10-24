package com.example.sightreading.audio

import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.roundToInt

object NoteUtils {
    val NOTE_NAMES_SHARP = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val NOTE_NAMES_FLAT = arrayOf("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B")

    // Reference pitch A4 = 440Hz, MIDI note 69
    const val A4_FREQUENCY = 440.0f
    const val A4_MIDI = 69

    fun midiToFrequency(midi: Int): Float {
        return A4_FREQUENCY * Math.pow(2.0, (midi - A4_MIDI) / 12.0).toFloat()
    }

    fun frequencyToMidi(freq: Float): Float {
        if (freq <= 0f) return 0f
        return A4_MIDI + 12.0f * log2(freq / A4_FREQUENCY)
    }

    fun frequencyToClosestMidi(freq: Float): Int {
        val midiFloat = frequencyToMidi(freq)
        return midiFloat.roundToInt()
    }

    fun midiToNoteName(midi: Int, useFlats: Boolean = false): String {
        if (midi < 0 || midi > 127) return "Unknown"
        val noteIdx = Math.floorMod(midi, 12)
        val octave = (midi / 12) - 1
        val noteName = if (useFlats) NOTE_NAMES_FLAT[noteIdx] else NOTE_NAMES_SHARP[noteIdx]
        return "$noteName$octave"
    }

    fun midiToPitchClass(midi: Int): Int {
        return Math.floorMod(midi, 12)
    }

    fun midiToOctave(midi: Int): Int {
        return (midi / 12) - 1
    }

    /**
     * Calculates cent deviation from nearest integer MIDI note (-50 to +50 cents).
     */
    fun centsFromNearestNote(freq: Float): Float {
        if (freq <= 0f) return 0f
        val midiFloat = frequencyToMidi(freq)
        val closestMidi = midiFloat.roundToInt()
        return (midiFloat - closestMidi) * 100.0f
    }

    /**
     * Determines whether a detected pitch matches an expected MIDI note within tolerance cents.
     */
    fun isNoteMatch(detectedMidi: Int, expectedMidi: Int, toleranceSemitones: Int = 0): Boolean {
        return abs(detectedMidi - expectedMidi) <= toleranceSemitones
    }
}
