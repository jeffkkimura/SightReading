package com.example.sightreading.ui.practice

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sightreading.audio.MelodicVariationEngine
import com.example.sightreading.audio.MetronomeEngine
import com.example.sightreading.audio.NoteUtils
import com.example.sightreading.audio.PianoSoundEngine
import com.example.sightreading.audio.PitchDetector
import com.example.sightreading.audio.PitchResult
import com.example.sightreading.data.LessonRepository
import com.example.sightreading.data.UserPreferencesRepository
import com.example.sightreading.model.Lesson
import com.example.sightreading.model.MusicalNote
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HitBurstEvent(
    val noteIndex: Int,
    val midiNote: Int,
    val comboCount: Int,
    val multiplier: Int,
    val points: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class PracticeUiState(
    val currentLesson: Lesson? = null,
    val isPlaying: Boolean = false,
    val isCountInActive: Boolean = false,
    val countInBeatsRemaining: Int = 4,
    val bpm: Int = 80,
    val elapsedBeats: Float = 0.0f,
    val activeNoteIndex: Int = 0,
    val totalNotesCount: Int = 0,
    val correctNotesCount: Int = 0,
    val missedNotesCount: Int = 0,
    val accuracyPercentage: Float = 100f,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val comboMultiplier: Int = 1,
    val totalScorePoints: Int = 0,
    val noteResultsMap: Map<Int, Boolean> = emptyMap(),
    val latestPitchResult: PitchResult = PitchResult(),
    val latestHitBurst: HitBurstEvent? = null,
    val wrongNoteAttempt: Int? = null,
    val isCompleted: Boolean = false,
    val starsEarned: Int = 0,
    val isMicActive: Boolean = false,
    val isMetronomeEnabled: Boolean = true
)

class PracticeViewModel(application: Application) : AndroidViewModel(application) {

    private val userPrefsRepo = UserPreferencesRepository(application)
    val pitchDetector = PitchDetector()
    val metronomeEngine = MetronomeEngine()
    val pianoSoundEngine = PianoSoundEngine()

    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null
    private var allFlatNotes = listOf<MusicalNote>()
    private var noteBeatPositions = listOf<Float>()
    private var lastNoteEvaluatedTimestamp = 0L

    init {
        // Collect live audio pitch stream
        viewModelScope.launch {
            pitchDetector.pitchResult.collect { pitch ->
                _uiState.update { it.copy(latestPitchResult = pitch) }
                if (!_uiState.value.isPlaying || _uiState.value.isCountInActive || pitch.isSilence || _uiState.value.isCompleted) return@collect

                val activeIdx = _uiState.value.activeNoteIndex
                if (activeIdx < allFlatNotes.size) {
                    val expectedNote = allFlatNotes[activeIdx]
                    if (expectedNote.isRest) {
                        advanceToNextNote(activeIdx)
                        return@collect
                    }

                    if (NoteUtils.isNoteMatch(pitch.midiNote, expectedNote.midiNote, toleranceSemitones = 0)) {
                        onRightNotePlayed(activeIdx)
                    } else {
                        onWrongNotePlayed(pitch.midiNote)
                    }
                }
            }
        }
    }

    fun loadLesson(lessonId: String) {
        val lesson = LessonRepository.getLessonById(lessonId)
        allFlatNotes = lesson.scorePiece.measures.flatMap { it.notes }

        var currentBeat = 0f
        noteBeatPositions = allFlatNotes.map { note ->
            val pos = currentBeat
            currentBeat += note.duration.beats
            pos
        }

        _uiState.value = PracticeUiState(
            currentLesson = lesson,
            bpm = lesson.scorePiece.defaultBpm,
            totalNotesCount = allFlatNotes.count { !it.isRest },
            elapsedBeats = 0.0f
        )
    }

    fun startListeningAudio() {
        pitchDetector.startListening(viewModelScope)
        _uiState.update { it.copy(isMicActive = true) }
    }

    fun stopListeningAudio() {
        pitchDetector.stopListening()
        _uiState.update { it.copy(isMicActive = false) }
    }

    fun toggleMetronome() {
        _uiState.update { it.copy(isMetronomeEnabled = !it.isMetronomeEnabled) }
    }

    fun togglePlayPause() {
        if (_uiState.value.isPlaying || _uiState.value.isCountInActive) {
            pausePractice()
        } else {
            startPractice()
        }
    }

    fun startPractice() {
        if (_uiState.value.isCompleted) {
            resetPractice(generateFreshVariation = true)
        }

        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val beatsPerMeasure = _uiState.value.currentLesson?.scorePiece?.timeSignature?.beatsPerMeasure ?: 4

            // Count-In Phase
            _uiState.update {
                it.copy(
                    isPlaying = false,
                    isCountInActive = true,
                    countInBeatsRemaining = beatsPerMeasure,
                    elapsedBeats = 0.0f
                )
            }

            for (beat in 1..beatsPerMeasure) {
                if (_uiState.value.isMetronomeEnabled) {
                    metronomeEngine.playClick(isAccent = (beat == 1))
                }
                _uiState.update { it.copy(countInBeatsRemaining = beatsPerMeasure - beat + 1) }
                delay(60_000L / _uiState.value.bpm)
            }

            // Transition to Note-Driven Playback (Sheet music stays on note until right note is played!)
            _uiState.update {
                it.copy(
                    isPlaying = true,
                    isCountInActive = false,
                    countInBeatsRemaining = 0,
                    elapsedBeats = noteBeatPositions.getOrElse(it.activeNoteIndex) { 0f }
                )
            }
        }
    }

    fun pausePractice() {
        _uiState.update { it.copy(isPlaying = false, isCountInActive = false) }
        playbackJob?.cancel()
        playbackJob = null
    }

    fun resetPractice(generateFreshVariation: Boolean = false) {
        pausePractice()
        val lesson = _uiState.value.currentLesson
        if (lesson != null) {
            val pieceToPlay = if (generateFreshVariation) {
                MelodicVariationEngine.createDynamicVariation(lesson.scorePiece, System.currentTimeMillis())
            } else {
                lesson.scorePiece
            }

            val variedLesson = lesson.copy(scorePiece = pieceToPlay)
            allFlatNotes = pieceToPlay.measures.flatMap { it.notes }

            var currentBeat = 0f
            noteBeatPositions = allFlatNotes.map { note ->
                val pos = currentBeat
                currentBeat += note.duration.beats
                pos
            }

            _uiState.value = PracticeUiState(
                currentLesson = variedLesson,
                bpm = pieceToPlay.defaultBpm,
                totalNotesCount = allFlatNotes.count { !it.isRest },
                elapsedBeats = 0.0f
            )
        }
    }

    fun updateBpm(newBpm: Int) {
        _uiState.update { it.copy(bpm = newBpm.coerceIn(40, 200)) }
    }

    fun simulateNotePlayed(midiNote: Int) {
        // Play audible piano tone for tapped key!
        pianoSoundEngine.playNoteTone(midiNote)

        if ((!_uiState.value.isPlaying && !_uiState.value.isCountInActive) || _uiState.value.isCompleted) return

        val activeIdx = _uiState.value.activeNoteIndex
        if (activeIdx < allFlatNotes.size) {
            val expectedNote = allFlatNotes[activeIdx]

            if (expectedNote.isRest) {
                advanceToNextNote(activeIdx)
                return
            }

            val isRightNote = NoteUtils.isNoteMatch(midiNote, expectedNote.midiNote)
            if (isRightNote) {
                onRightNotePlayed(activeIdx)
            } else {
                onWrongNotePlayed(midiNote)
            }
        }
    }

    private fun onRightNotePlayed(noteIdx: Int) {
        val now = System.currentTimeMillis()
        if (now - lastNoteEvaluatedTimestamp < 350L) return // Acoustic resonance lockout window to prevent double hits
        lastNoteEvaluatedTimestamp = now

        _uiState.update { state ->
            val updatedMap = state.noteResultsMap.toMutableMap()
            updatedMap[noteIdx] = true

            val newCorrect = state.correctNotesCount + 1
            val evaluatedTotal = newCorrect + state.missedNotesCount
            val accuracy = if (evaluatedTotal > 0) (newCorrect.toFloat() / evaluatedTotal.toFloat()) * 100f else 100f

            val newStreak = state.currentStreak + 1
            val newMaxStreak = maxOf(state.maxStreak, newStreak)

            val multiplier = when {
                newStreak >= 15 -> 5
                newStreak >= 10 -> 3
                newStreak >= 5 -> 2
                else -> 1
            }

            val addedPoints = 100 * multiplier
            val newTotalPoints = state.totalScorePoints + addedPoints

            val hitBurst = HitBurstEvent(
                noteIndex = noteIdx,
                midiNote = allFlatNotes.getOrNull(noteIdx)?.midiNote ?: 60,
                comboCount = newStreak,
                multiplier = multiplier,
                points = addedPoints
            )

            val nextIdx = noteIdx + 1
            val nextBeatPos = noteBeatPositions.getOrElse(nextIdx) { noteBeatPositions.lastOrNull() ?: 0f }

            state.copy(
                noteResultsMap = updatedMap,
                correctNotesCount = newCorrect,
                accuracyPercentage = accuracy,
                currentStreak = newStreak,
                maxStreak = newMaxStreak,
                comboMultiplier = multiplier,
                totalScorePoints = newTotalPoints,
                latestHitBurst = hitBurst,
                wrongNoteAttempt = null,
                activeNoteIndex = nextIdx,
                elapsedBeats = nextBeatPos
            )
        }

        if (_uiState.value.activeNoteIndex >= allFlatNotes.size) {
            completeLesson()
        }
    }

    private fun onWrongNotePlayed(playedMidi: Int) {
        val now = System.currentTimeMillis()
        if (now - lastNoteEvaluatedTimestamp < 350L) return // Acoustic resonance lockout window
        lastNoteEvaluatedTimestamp = now

        _uiState.update { state ->
            val newMissed = state.missedNotesCount + 1
            val evaluatedTotal = state.correctNotesCount + newMissed
            val accuracy = if (evaluatedTotal > 0) (state.correctNotesCount.toFloat() / evaluatedTotal.toFloat()) * 100f else 100f

            state.copy(
                missedNotesCount = newMissed,
                accuracyPercentage = accuracy,
                currentStreak = 0,
                wrongNoteAttempt = playedMidi
            )
        }
    }

    private fun advanceToNextNote(currentIdx: Int) {
        val nextIdx = currentIdx + 1
        val nextBeatPos = noteBeatPositions.getOrElse(nextIdx) { noteBeatPositions.lastOrNull() ?: 0f }

        _uiState.update {
            it.copy(
                activeNoteIndex = nextIdx,
                elapsedBeats = nextBeatPos
            )
        }

        if (nextIdx >= allFlatNotes.size) {
            completeLesson()
        }
    }

    private fun completeLesson() {
        val state = _uiState.value
        val accuracy = state.accuracyPercentage

        val stars = when {
            accuracy >= 90f -> 3
            accuracy >= 75f -> 2
            accuracy >= 60f -> 1
            else -> 0
        }

        _uiState.update {
            it.copy(
                isPlaying = false,
                isCountInActive = false,
                isCompleted = true,
                starsEarned = stars
            )
        }

        state.currentLesson?.let { lesson ->
            userPrefsRepo.saveLessonAttempt(
                lessonId = lesson.id,
                keySignature = lesson.keySignature,
                accuracyFloat = accuracy / 100f
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListeningAudio()
        metronomeEngine.release()
        pianoSoundEngine.release()
        playbackJob?.cancel()
    }
}
