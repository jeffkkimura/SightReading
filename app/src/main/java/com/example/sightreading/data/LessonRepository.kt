package com.example.sightreading.data

import com.example.sightreading.model.Hand
import com.example.sightreading.model.KeySignature
import com.example.sightreading.model.KeyType
import com.example.sightreading.model.Lesson
import com.example.sightreading.model.LessonLevel
import com.example.sightreading.model.Measure
import com.example.sightreading.model.MusicalNote
import com.example.sightreading.model.NoteDuration
import com.example.sightreading.model.ScorePiece
import com.example.sightreading.model.TimeSignature

object LessonRepository {

    fun getAllLessons(): List<Lesson> {
        val lessons = mutableListOf<Lesson>()

        KeySignature.entries.forEach { key ->
            for (levelIndex in 1..25) {
                lessons.add(createWorldLesson(key, levelIndex))
            }
        }

        return lessons
    }

    fun getLessonsForKey(key: KeySignature): List<Lesson> {
        return (1..25).map { levelIndex -> createWorldLesson(key, levelIndex) }
    }

    fun getLessonById(id: String): Lesson {
        val parts = id.split("_")
        if (parts.size >= 3 && parts[0] == "lesson") {
            val keyName = parts[1]
            val levelIdx = parts[2].toIntOrNull() ?: 1
            val key = KeySignature.entries.find { it.name.lowercase() == keyName.lowercase() }
            if (key != null) {
                return createWorldLesson(key, levelIdx)
            }
        }
        return getAllLessons().find { it.id == id } ?: getAllLessons().first()
    }

    // --- 25-Level Curriculum with Expressive, Highly Melodic Piano Compositions ---

    private fun createWorldLesson(key: KeySignature, levelIndex: Int): Lesson {
        val root = key.rootMidiRightHand
        val lhRoot = key.rootMidiLeftHand
        val isMajor = key.type == KeyType.MAJOR

        val lessonId = "lesson_${key.name.lowercase()}_$levelIndex"

        val (title, subtitle, composer, levelCategory, handMode, stars, defaultBpm) = when (levelIndex) {
            1 -> Tuple7("Level 1-1: Melodic Pentascale Flow", "Flowing 5-Finger Cantabile Pattern", "SightReading Studio", LessonLevel.RIGHT_HAND_PENTATONIC, Hand.RIGHT_HAND, 1, 75)
            2 -> Tuple7("Level 1-2: Twinkle Twinkle Little Star", "Mozart's Lyrical Folk Melody", "W. A. Mozart", LessonLevel.RIGHT_HAND_PENTATONIC, Hand.RIGHT_HAND, 1, 80)
            3 -> Tuple7("Level 1-3: Arpeggiated Echoes", "Melodic Triad & Wave Intervals", "SightReading Studio", LessonLevel.RIGHT_HAND_PENTATONIC, Hand.RIGHT_HAND, 1, 80)
            4 -> Tuple7("Level 1-4: Ode to Joy Concert Theme", "Beethoven's 9th Symphony Main Melody", "L. v. Beethoven", LessonLevel.RIGHT_HAND_PENTATONIC, Hand.RIGHT_HAND, 1, 85)
            5 -> Tuple7("Level 1-5: Grassland Fortress Prelude", "Virtuoso 5-Note Prelude Sprint", "SightReading Studio", LessonLevel.RIGHT_HAND_PENTATONIC, Hand.RIGHT_HAND, 2, 90)

            6 -> Tuple7("Level 2-1: Octave Scale Caprice", "Lyrical 8-Note Scale Run", "SightReading Studio", LessonLevel.RIGHT_HAND_OCTAVE, Hand.RIGHT_HAND, 2, 80)
            7 -> Tuple7("Level 2-2: Minuet in G/Am Melody", "Bach's Notebook Classic Dance", "J. S. Bach", LessonLevel.RIGHT_HAND_OCTAVE, Hand.RIGHT_HAND, 2, 85)
            8 -> Tuple7("Level 2-3: Dotted Rhythm Waltz", "Graceful Lilt in 3/4 Time", "SightReading Studio", LessonLevel.RIGHT_HAND_OCTAVE, Hand.RIGHT_HAND, 2, 85)
            9 -> Tuple7("Level 2-4: Brahms' Lullaby", "Famous Wiegenlied Cradle Song", "J. Brahms", LessonLevel.RIGHT_HAND_OCTAVE, Hand.RIGHT_HAND, 2, 85)
            10 -> Tuple7("Level 2-5: Airship Fortress Vivaldi", "Vivaldi's Spring (Four Seasons) Allegro", "A. Vivaldi", LessonLevel.RIGHT_HAND_OCTAVE, Hand.RIGHT_HAND, 3, 95)

            11 -> Tuple7("Level 3-1: Chromatic Wave Drill", "Expressive Accidental Shifts", "SightReading Studio", LessonLevel.KEY_SIGNATURES_BASIC, Hand.RIGHT_HAND, 2, 80)
            12 -> Tuple7("Level 3-2: New World Symphony Largo", "Dvořák's Lyrical 9th Symphony Theme", "A. Dvořák", LessonLevel.KEY_SIGNATURES_BASIC, Hand.RIGHT_HAND, 2, 85)
            13 -> Tuple7("Level 3-3: Syncopated Ragtime Dance", "Upbeat Off-Beat Rhythm", "SightReading Studio", LessonLevel.RHYTHM_PRACTICE, Hand.RIGHT_HAND, 3, 85)
            14 -> Tuple7("Level 3-4: Canon in D Motif", "Pachelbel's Celebrated Polyphonic Melody", "J. Pachelbel", LessonLevel.RHYTHM_PRACTICE, Hand.RIGHT_HAND, 3, 90)
            15 -> Tuple7("Level 3-5: Phantom Ghost House Morning", "Grieg's Morning Mood Sunrise Theme", "E. Grieg", LessonLevel.RHYTHM_PRACTICE, Hand.RIGHT_HAND, 3, 95)

            16 -> Tuple7("Level 4-1: Dual-Hand Alberti Bass", "LH Alberti Accompaniment & RH Melody", "SightReading Studio", LessonLevel.TWO_HANDS_GRAND_STAFF, Hand.BOTH_HANDS, 3, 70)
            17 -> Tuple7("Level 4-2: Für Elise Main Theme", "Beethoven's Immortal Bagatelle", "L. v. Beethoven", LessonLevel.TWO_HANDS_GRAND_STAFF, Hand.BOTH_HANDS, 3, 75)
            18 -> Tuple7("Level 4-3: Polonaise Harmonic Chords", "Full Triad Chords & Soaring Line", "SightReading Studio", LessonLevel.TWO_HANDS_GRAND_STAFF, Hand.BOTH_HANDS, 3, 80)
            19 -> Tuple7("Level 4-4: Swan Lake Ballet Theme", "Tchaikovsky's Lyrical Minor Drama", "P. I. Tchaikovsky", LessonLevel.TWO_HANDS_GRAND_STAFF, Hand.BOTH_HANDS, 4, 80)
            20 -> Tuple7("Level 4-5: Lava Citadel Ragtime Rondo", "Joplin's Entertainer Virtuoso Rag", "S. Joplin", LessonLevel.TWO_HANDS_GRAND_STAFF, Hand.BOTH_HANDS, 4, 85)

            21 -> Tuple7("Level 5-1: Bach Two-Part Invention", "Imitative Contrapuntal Counterpoint", "J. S. Bach", LessonLevel.KEY_SIGNATURES_ADVANCED, Hand.BOTH_HANDS, 4, 85)
            22 -> Tuple7("Level 5-2: Greensleeves Traditional", "Classic English Renaissance Folk Ballad", "Traditional", LessonLevel.KEY_SIGNATURES_ADVANCED, Hand.BOTH_HANDS, 4, 85)
            23 -> Tuple7("Level 5-3: Surprise Symphony Theme", "Haydn's Andante Theme & Fortissimo", "J. Haydn", LessonLevel.TWO_HANDS_GRAND_STAFF, Hand.BOTH_HANDS, 4, 85)
            24 -> Tuple7("Level 5-4: Chopin Cantabile Nocturne", "Expressive Lyrical Bel-Canto Theme", "F. Chopin", LessonLevel.TWO_HANDS_GRAND_STAFF, Hand.BOTH_HANDS, 5, 85)
            else -> Tuple7("Level 5-5: World Castle Boss Symphony", "Grand Concert Masterpiece Finale", "Beethoven / Mozart", LessonLevel.TWO_HANDS_GRAND_STAFF, Hand.BOTH_HANDS, 5, 90)
        }

        // Relative Midi Melodic Pattern Offsets (Transposed to World Key!)
        val relativeMidis = when (levelIndex) {
            1 -> listOf(0, 2, 4, 5,  7, 5, 4, 2,  0, 2, 4, 5,  7, 5, 4, 2,  0, 2, 4, 5,  7, 5, 4, 2,  0, 2, 4, 2,  0, 0, 0, 0) // C Position 5-Note Step Flow
            2 -> listOf(0, 0, 7, 7,  7, 7, 5, 5,  4, 4, 2, 2,  0, 0, 0, 0,  7, 7, 5, 5,  4, 4, 2, 2,  0, 0, 7, 7,  5, 5, 2, 0) // C Position 5-Note Twinkle
            3 -> listOf(0, 4, 7, 4,  0, 4, 7, 4,  2, 5, 7, 5,  2, 5, 7, 5,  0, 4, 7, 4,  0, 4, 7, 4,  2, 4, 5, 2,  0, 0, 0, 0) // C Position 5-Note Triads
            4 -> listOf(4, 4, 5, 7,  7, 5, 4, 2,  0, 0, 2, 4,  4, 2, 2, 2,  4, 4, 5, 7,  7, 5, 4, 2,  0, 0, 2, 4,  2, 0, 0, 0) // C Position Ode to Joy
            5 -> listOf(0, 2, 4, 5,  7, 4, 2, 0,  2, 4, 5, 7,  5, 4, 2, 0,  0, 2, 4, 5,  7, 4, 2, 0,  2, 4, 2, 0,  0, 0, 0, 0) // C Position Grassland Prelude
            6 -> listOf(0, 2, 4, 5,  7, 9, 11, 12,  11, 9, 7, 5,  4, 2, 0, 0,  0, 4, 7, 12,  11, 9, 7, 5,  4, 2, 0, 2,  4, 2, 0, 0) // Octave Scale Caprice
            7 -> listOf(7, 0, 2, 4,  5, 7, 0, 0,  5, 7, 5, 4,  2, 4, 2, 0,  7, 0, 2, 4,  5, 7, 0, 0,  5, 7, 5, 4,  2, 0, 0, 0) // Minuet in G/Am
            8 -> listOf(0, 4, 7, 12,  9, 7, 4, 2,  0, 2, 4, 5,  7, 9, 7, 5,  0, 4, 7, 12,  9, 7, 4, 2,  0, 2, 4, 2,  0, 0, 0, 0) // Dotted Waltz
            9 -> listOf(4, 4, 7, 4,  4, 7, 4, 7,  9, 7, 5, 4,  2, 0, 2, 4,  4, 4, 7, 4,  4, 7, 4, 7,  9, 7, 5, 4,  2, 0, 0, 0) // Brahms Lullaby
            10 -> listOf(7, 4, 7, 4,  7, 9, 7, 5,  4, 2, 4, 2,  4, 5, 4, 2,  0, 2, 4, 5,  7, 9, 7, 5,  4, 2, 0, 2,  4, 2, 0, 0) // Vivaldi Spring
            11 -> listOf(0, 1, 2, 4,  5, 6, 7, 9,  11, 9, 7, 5,  4, 2, 1, 0,  0, 2, 4, 5,  7, 9, 11, 12,  9, 7, 5, 4,  2, 1, 0, 0) // Chromatic Wave
            12 -> listOf(4, 7, 7, 9,  7, 4, 2, 0,  2, 4, 2, 0,  2, 4, 2, 0,  4, 7, 7, 9,  7, 4, 2, 0,  2, 4, 2, 0,  0, 0, 0, 0) // New World Largo
            13 -> listOf(0, 4, 7, 9,  7, 4, 0, 2,  4, 7, 9, 12,  9, 7, 4, 2,  0, 4, 7, 9,  7, 4, 0, 2,  4, 2, 0, 2,  0, 0, 0, 0) // Syncopated Dance
            14 -> listOf(7, 4, 5, 2,  4, 0, 2, -3,  0, 2, 4, 5,  7, 9, 7, 5,  7, 4, 5, 2,  4, 0, 2, -3,  0, 2, 4, 2,  0, 0, 0, 0) // Canon in D
            15 -> listOf(4, 2, 0, 2,  4, 7, 9, 7,  4, 2, 0, 2,  4, 2, 0, 0,  4, 2, 0, 2,  4, 7, 9, 7,  4, 2, 0, 2,  0, 0, 0, 0) // Morning Mood
            16 -> listOf(0, 4, 7, 12,  9, 7, 4, 2,  0, 4, 7, 9,  7, 4, 2, 0,  0, 4, 7, 12,  9, 7, 4, 2,  0, 4, 2, 0,  0, 0, 0, 0) // Dual-Hand Alberti
            17 -> listOf(7, 6, 7, 6,  7, 2, 5, 3,  0, 0, 2, 4,  2, 0, 2, 4,  7, 6, 7, 6,  7, 2, 5, 3,  0, 0, 2, 4,  0, 0, 0, 0) // Für Elise
            18 -> listOf(0, 4, 7, 12,  9, 7, 4, 2,  0, 4, 7, 9,  7, 5, 4, 2,  0, 4, 7, 12,  9, 7, 4, 2,  0, 4, 2, 0,  0, 0, 0, 0) // Polonaise Chords
            19 -> listOf(0, 5, 7, 8,  7, 5, 0, 0,  5, 7, 8, 7,  5, 3, 2, 0,  0, 5, 7, 8,  7, 5, 0, 0,  5, 7, 5, 3,  2, 0, 0, 0) // Swan Lake
            20 -> listOf(2, 3, 4, 7,  4, 7, 4, 7,  9, 7, 5, 4,  2, 0, 2, 4,  2, 3, 4, 7,  4, 7, 4, 7,  9, 7, 5, 4,  0, 0, 0, 0) // Entertainer
            21 -> listOf(0, 2, 4, 5,  7, 4, 2, 0,  7, 9, 11, 12,  9, 7, 5, 4,  0, 2, 4, 5,  7, 4, 2, 0,  4, 2, 0, 2,  0, 0, 0, 0) // Bach Invention
            22 -> listOf(0, 3, 5, 7,  8, 7, 5, 2,  0, 2, 3, 0,  0, -2, 0, 0,  0, 3, 5, 7,  8, 7, 5, 2,  0, 2, 3, 2,  0, 0, 0, 0) // Greensleeves
            23 -> listOf(0, 0, 4, 4,  7, 7, 4, 0,  2, 2, 4, 2,  0, 0, 2, 0,  0, 0, 4, 4,  7, 7, 4, 0,  2, 2, 4, 2,  0, 0, 0, 0) // Surprise Symphony
            24 -> listOf(4, 7, 9, 11,  9, 7, 4, 2,  0, 2, 4, 5,  7, 5, 4, 2,  4, 7, 9, 11,  9, 7, 4, 2,  0, 2, 4, 2,  0, 0, 0, 0) // Chopin Nocturne
            else -> listOf(4, 4, 5, 7,  7, 5, 4, 2,  0, 0, 2, 4,  4, 2, 2, 2,  7, 7, 9, 9,  7, 5, 4, 2,  0, 2, 4, 2,  0, 0, 0, 0) // World Castle Finale
        }

        // Divide into measures of 4 quarter notes each (4 beats per measure in 4/4 time!)
        val measures = relativeMidis.chunked(4).mapIndexed { mIdx, quarterGroup ->
            val measureNotes = quarterGroup.mapIndexed { nIdx, relMidi ->
                val globalIdx = mIdx * 4 + nIdx
                val isRest = (levelIndex == 15 && globalIdx % 7 == 3)
                val isLeftHand = (handMode == Hand.BOTH_HANDS && (nIdx == 0))

                val noteMidi = if (isLeftHand) {
                    // Left Hand Alberti Bass / Harmony root & fifth accompaniment
                    lhRoot + (if (isMajor) intArrayOf(0, 7, 4, 7)[mIdx % 4] else intArrayOf(0, 7, 3, 7)[mIdx % 4])
                } else {
                    root + relMidi
                }

                MusicalNote(
                    midiNote = noteMidi,
                    duration = NoteDuration.QUARTER,
                    hand = if (isLeftHand) Hand.LEFT_HAND else Hand.RIGHT_HAND,
                    isRest = isRest
                )
            }
            Measure(measureNotes)
        }

        val piece = ScorePiece(
            id = "piece_${key.name.lowercase()}_$levelIndex",
            title = "$title in ${key.displayName}",
            composer = composer,
            keySignature = key,
            timeSignature = TimeSignature(4, 4),
            handMode = handMode,
            measures = measures,
            defaultBpm = defaultBpm
        )

        return Lesson(
            id = lessonId,
            title = title,
            subtitle = subtitle,
            description = "Master $title in ${key.displayName}.",
            level = levelCategory,
            keySignature = key,
            handMode = handMode,
            scorePiece = piece,
            difficultyStars = stars
        )
    }

    private data class Tuple7<A, B, C, D, E, F, G>(
        val val1: A, val val2: B, val val3: C, val val4: D, val val5: E, val val6: F, val val7: G
    )
}
