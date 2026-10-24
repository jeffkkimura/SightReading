package com.example.sightreading.data

import android.content.Context
import android.content.SharedPreferences
import com.example.sightreading.audio.NoteUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

data class NoteStatItem(
    val midiNote: Int,
    val noteName: String,
    val correctHits: Int = 0,
    val totalAttempts: Int = 0
) {
    val accuracyPercentage: Float
        get() = if (totalAttempts > 0) (correctHits.toFloat() / totalAttempts.toFloat()) * 100f else 100f
}

data class OverallNoteAnalytics(
    val totalNotesPressed: Int = 0,
    val totalCorrectHits: Int = 0,
    val overallAccuracy: Float = 100f,
    val mostAccurateNote: String = "C4",
    val weakestNote: String = "None",
    val noteStatsList: List<NoteStatItem> = emptyList()
)

class NoteAnalyticsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sightreading_note_analytics", Context.MODE_PRIVATE)

    private val _noteAnalytics = MutableStateFlow(OverallNoteAnalytics())
    val noteAnalytics: StateFlow<OverallNoteAnalytics> = _noteAnalytics.asStateFlow()

    init {
        loadAnalyticsData()
    }

    fun loadAnalyticsData() {
        var totalPressed = 0
        var totalCorrect = 0
        val items = mutableListOf<NoteStatItem>()

        // Check stats for Middle C range notes (MIDI 60 to 84)
        for (midi in 60..84) {
            val jsonStr = prefs.getString("note_stat_$midi", null)
            val noteName = NoteUtils.midiToNoteName(midi)

            if (jsonStr != null) {
                try {
                    val obj = JSONObject(jsonStr)
                    val correct = obj.optInt("correct", 0)
                    val total = obj.optInt("total", 0)

                    totalPressed += total
                    totalCorrect += correct

                    items.add(NoteStatItem(midiNote = midi, noteName = noteName, correctHits = correct, totalAttempts = total))
                } catch (_: Exception) {
                    items.add(NoteStatItem(midiNote = midi, noteName = noteName))
                }
            } else {
                items.add(NoteStatItem(midiNote = midi, noteName = noteName))
            }
        }

        val playedItems = items.filter { it.totalAttempts > 0 }
        val overallAcc = if (totalPressed > 0) (totalCorrect.toFloat() / totalPressed.toFloat()) * 100f else 100f
        val sortedByAcc = playedItems.sortedByDescending { it.accuracyPercentage }

        val bestNote = sortedByAcc.firstOrNull()?.noteName ?: "C4"
        val worstNote = sortedByAcc.lastOrNull()?.noteName ?: "None"

        _noteAnalytics.value = OverallNoteAnalytics(
            totalNotesPressed = totalPressed,
            totalCorrectHits = totalCorrect,
            overallAccuracy = overallAcc,
            mostAccurateNote = bestNote,
            weakestNote = worstNote,
            noteStatsList = items
        )
    }

    fun recordNoteHit(midiNote: Int, isCorrect: Boolean) {
        val currentJson = prefs.getString("note_stat_$midiNote", null)
        var correct = 0
        var total = 0

        if (currentJson != null) {
            try {
                val obj = JSONObject(currentJson)
                correct = obj.optInt("correct", 0)
                total = obj.optInt("total", 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        total += 1
        if (isCorrect) correct += 1

        val updatedObj = JSONObject().apply {
            put("correct", correct)
            put("total", total)
        }

        prefs.edit().putString("note_stat_$midiNote", updatedObj.toString()).apply()
        loadAnalyticsData()
    }
}
