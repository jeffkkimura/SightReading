package com.example.sightreading.model

enum class LessonLevel(
    val title: String,
    val description: String
) {
    RIGHT_HAND_PENTATONIC("Right Hand Basics", "5-note pentascale exercises starting on Middle C"),
    RIGHT_HAND_OCTAVE("Right Hand One Octave", "Expanded single octave sight reading for Right Hand"),
    KEY_SIGNATURES_BASIC("Basic Key Signatures", "Lessons in 1 and 2 Sharps / Flats"),
    KEY_SIGNATURES_ADVANCED("Advanced Key Signatures", "Lessons in 3 to 6 Sharps / Flats across all 12 Major & Minor keys"),
    RHYTHM_PRACTICE("Rhythm & Timing Mastery", "Varied tempos, quarter, half, eighth notes and rest timing"),
    TWO_HANDS_GRAND_STAFF("Two-Hand Expansion", "Sight reading across Treble & Bass Clefs (Grand Staff)")
}

data class Lesson(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val level: LessonLevel,
    val keySignature: KeySignature,
    val handMode: Hand = Hand.RIGHT_HAND,
    val scorePiece: ScorePiece,
    val difficultyStars: Int = 1
)

data class KeyProficiency(
    val keySignature: KeySignature,
    val totalAttempts: Int = 0,
    val completedLessonsCount: Int = 0,
    val totalLessonsCount: Int = 0,
    val averageAccuracy: Float = 0f,
    val bestAccuracy: Float = 0f
) {
    val proficiencyPercentage: Int get() = (bestAccuracy * 100).toInt().coerceIn(0, 100)
}
