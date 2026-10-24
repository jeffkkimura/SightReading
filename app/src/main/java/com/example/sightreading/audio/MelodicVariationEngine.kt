package com.example.sightreading.audio

import com.example.sightreading.model.Hand
import com.example.sightreading.model.KeySignature
import com.example.sightreading.model.KeyType
import com.example.sightreading.model.Measure
import com.example.sightreading.model.MusicalNote
import com.example.sightreading.model.NoteDuration
import com.example.sightreading.model.ScorePiece
import kotlin.random.Random

object MelodicVariationEngine {

    /**
     * Generates a fresh, musically coherent variation of a ScorePiece
     * for a given key and level so the player cannot memorize note sequences.
     */
    fun createDynamicVariation(
        originalPiece: ScorePiece,
        retrySeed: Long = System.currentTimeMillis()
    ): ScorePiece {
        val random = Random(retrySeed)
        val key = originalPiece.keySignature
        val root = key.rootMidiRightHand
        val lhRoot = key.rootMidiLeftHand
        val isMajor = key.type == KeyType.MAJOR

        val diatonicScaleOffsets = if (isMajor) {
            intArrayOf(0, 2, 4, 5, 7, 9, 11, 12, 14, 16)
        } else {
            intArrayOf(0, 2, 3, 5, 7, 8, 10, 12, 14, 15)
        }

        val mutatedMeasures = originalPiece.measures.mapIndexed { mIdx, measure ->
            val mutatedNotes = measure.notes.map { note ->
                if (note.isRest) {
                    note
                } else {
                    val isLeftHand = note.hand == Hand.LEFT_HAND
                    if (isLeftHand) {
                        // Mutate LH accompaniment chord degrees
                        val lhOffsets = if (isMajor) intArrayOf(0, 7, 4, 7, 5, 7) else intArrayOf(0, 7, 3, 7, 5, 7)
                        val shift = random.nextInt(lhOffsets.size)
                        val newLhMidi = lhRoot + lhOffsets[(mIdx + shift) % lhOffsets.size]
                        note.copy(midiNote = newLhMidi)
                    } else {
                        // Mutate RH melody using diatonic scale degree shifts or interval inversions
                        val currentStepIndex = findClosestScaleStep(note.midiNote - root, diatonicScaleOffsets)
                        val stepShift = when (random.nextInt(4)) {
                            0 -> 2  // Shift up a third (chordal tone)
                            1 -> -2 // Shift down a third
                            2 -> 1  // Shift step up
                            else -> 0 // Keep original
                        }
                        val newStepIndex = (currentStepIndex + stepShift).coerceIn(0, diatonicScaleOffsets.size - 1)
                        val newRhMidi = root + diatonicScaleOffsets[newStepIndex]
                        note.copy(midiNote = newRhMidi)
                    }
                }
            }
            Measure(mutatedNotes)
        }

        return originalPiece.copy(
            title = "${originalPiece.title} (Variation)",
            measures = mutatedMeasures
        )
    }

    private fun findClosestScaleStep(offset: Int, scaleOffsets: IntArray): Int {
        var closestIdx = 0
        var minDiff = Int.MAX_VALUE
        scaleOffsets.forEachIndexed { idx, scaleOffset ->
            val diff = kotlin.math.abs(scaleOffset - offset)
            if (diff < minDiff) {
                minDiff = diff
                closestIdx = idx
            }
        }
        return closestIdx
    }
}
