package com.example.sightreading.model

enum class Clef {
    TREBLE,
    BASS,
    GRAND
}

enum class Hand {
    RIGHT_HAND,
    LEFT_HAND,
    BOTH_HANDS
}

enum class NoteDuration(val beats: Float) {
    WHOLE(4.0f),
    DOTTED_HALF(3.0f),
    HALF(2.0f),
    DOTTED_QUARTER(1.5f),
    QUARTER(1.0f),
    EIGHTH(0.5f),
    SIXTEENTH(0.25f)
}

enum class Accidental {
    NONE,
    SHARP,
    FLAT,
    NATURAL
}

enum class KeyType {
    MAJOR,
    MINOR
}

enum class KeySignature(
    val displayName: String,
    val type: KeyType,
    val accidentals: Int, // Positive for sharps, negative for flats
    val rootMidiRightHand: Int, // E.g., C4 = 60, G4 = 67, etc.
    val rootMidiLeftHand: Int  // E.g., C3 = 48
) {
    // Major Keys
    C_MAJOR("C Major", KeyType.MAJOR, 0, 60, 48),
    G_MAJOR("G Major", KeyType.MAJOR, 1, 67, 55),
    D_MAJOR("D Major", KeyType.MAJOR, 2, 62, 50),
    A_MAJOR("A Major", KeyType.MAJOR, 3, 69, 57),
    E_MAJOR("E Major", KeyType.MAJOR, 4, 64, 52),
    B_MAJOR("B Major", KeyType.MAJOR, 5, 71, 59),
    F_SHARP_MAJOR("F# Major", KeyType.MAJOR, 6, 66, 54),
    D_FLAT_MAJOR("D♭ Major", KeyType.MAJOR, -5, 61, 49),
    A_FLAT_MAJOR("A♭ Major", KeyType.MAJOR, -4, 68, 56),
    E_FLAT_MAJOR("E♭ Major", KeyType.MAJOR, -3, 63, 51),
    B_FLAT_MAJOR("B♭ Major", KeyType.MAJOR, -2, 70, 58),
    F_MAJOR("F Major", KeyType.MAJOR, -1, 65, 53),

    // Minor Keys
    A_MINOR("A Minor", KeyType.MINOR, 0, 69, 57),
    E_MINOR("E Minor", KeyType.MINOR, 1, 64, 52),
    B_MINOR("B Minor", KeyType.MINOR, 2, 71, 59),
    F_SHARP_MINOR("F# Minor", KeyType.MINOR, 3, 66, 54),
    C_SHARP_MINOR("C# Minor", KeyType.MINOR, 4, 61, 49),
    G_SHARP_MINOR("G# Minor", KeyType.MINOR, 5, 68, 56),
    D_SHARP_MINOR("D# Minor", KeyType.MINOR, 6, 63, 51),
    B_FLAT_MINOR("B♭ Minor", KeyType.MINOR, -5, 70, 58),
    F_MINOR("F Minor", KeyType.MINOR, -4, 65, 53),
    C_MINOR("C Minor", KeyType.MINOR, -3, 60, 48),
    G_MINOR("G Minor", KeyType.MINOR, -2, 67, 55),
    D_MINOR("D Minor", KeyType.MINOR, -1, 62, 50);

    val isFlatKey: Boolean get() = accidentals < 0
    val totalAccidentals: Int get() = kotlin.math.abs(accidentals)
}

data class TimeSignature(
    val beatsPerMeasure: Int = 4,
    val beatValue: Int = 4
)

data class MusicalNote(
    val midiNote: Int,
    val duration: NoteDuration = NoteDuration.QUARTER,
    val accidental: Accidental = Accidental.NONE,
    val hand: Hand = Hand.RIGHT_HAND,
    val isRest: Boolean = false
) {
    val isTrebleClef: Boolean get() = midiNote >= 60 // C4 and above default to treble
}

data class Measure(
    val notes: List<MusicalNote>
)

data class ScorePiece(
    val id: String,
    val title: String,
    val composer: String = "Traditional",
    val keySignature: KeySignature = KeySignature.C_MAJOR,
    val timeSignature: TimeSignature = TimeSignature(4, 4),
    val handMode: Hand = Hand.RIGHT_HAND,
    val measures: List<Measure>,
    val defaultBpm: Int = 80
) {
    val totalNotes: Int get() = measures.sumOf { m -> m.notes.count { !it.isRest } }
}
