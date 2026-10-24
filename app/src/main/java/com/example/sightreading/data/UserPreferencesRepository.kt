package com.example.sightreading.data

import android.content.Context
import android.content.SharedPreferences
import com.example.sightreading.model.KeyProficiency
import com.example.sightreading.model.KeySignature
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sightreading_user_prefs", Context.MODE_PRIVATE)

    private val _keyProficiencies = MutableStateFlow<Map<KeySignature, KeyProficiency>>(emptyMap())
    val keyProficiencies: StateFlow<Map<KeySignature, KeyProficiency>> = _keyProficiencies.asStateFlow()

    private val _completedLessonIds = MutableStateFlow<Set<String>>(emptySet())
    val completedLessonIds: StateFlow<Set<String>> = _completedLessonIds.asStateFlow()

    private val _masteredLessonIds = MutableStateFlow<Set<String>>(emptySet())
    val masteredLessonIds: StateFlow<Set<String>> = _masteredLessonIds.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        val completedSet = prefs.getStringSet(KEY_COMPLETED_LESSONS, emptySet()) ?: emptySet()
        _completedLessonIds.value = completedSet

        val masteredSet = mutableSetOf<String>()
        completedSet.forEach { lessonId ->
            val bestAcc = prefs.getFloat("acc_$lessonId", 0f)
            if (bestAcc >= 0.90f) {
                masteredSet.add(lessonId)
            }
        }
        _masteredLessonIds.value = masteredSet

        val proficiencyMap = mutableMapOf<KeySignature, KeyProficiency>()
        KeySignature.entries.forEach { key ->
            val jsonStr = prefs.getString("prof_${key.name}", null)
            if (jsonStr != null) {
                try {
                    val obj = JSONObject(jsonStr)
                    val prof = KeyProficiency(
                        keySignature = key,
                        totalAttempts = obj.optInt("totalAttempts", 0),
                        completedLessonsCount = obj.optInt("completedLessonsCount", 0),
                        totalLessonsCount = LessonRepository.getLessonsForKey(key).size,
                        averageAccuracy = obj.optDouble("averageAccuracy", 0.0).toFloat(),
                        bestAccuracy = obj.optDouble("bestAccuracy", 0.0).toFloat()
                    )
                    proficiencyMap[key] = prof
                } catch (e: Exception) {
                    proficiencyMap[key] = KeyProficiency(
                        keySignature = key,
                        totalLessonsCount = LessonRepository.getLessonsForKey(key).size
                    )
                }
            } else {
                proficiencyMap[key] = KeyProficiency(
                    keySignature = key,
                    totalLessonsCount = LessonRepository.getLessonsForKey(key).size
                )
            }
        }
        _keyProficiencies.value = proficiencyMap
    }

    fun getLessonBestAccuracy(lessonId: String): Float {
        return prefs.getFloat("acc_$lessonId", 0f)
    }

    fun saveLessonAttempt(lessonId: String, keySignature: KeySignature, accuracyFloat: Float) {
        val previousBestAcc = prefs.getFloat("acc_$lessonId", 0f)
        val newBestAcc = maxOf(previousBestAcc, accuracyFloat)
        prefs.edit().putFloat("acc_$lessonId", newBestAcc).apply()

        // Update completed lesson set
        val currentCompleted = _completedLessonIds.value.toMutableSet()
        currentCompleted.add(lessonId)
        _completedLessonIds.value = currentCompleted
        prefs.edit().putStringSet(KEY_COMPLETED_LESSONS, currentCompleted).apply()

        // Update mastered lesson set (90%+ accuracy required!)
        if (newBestAcc >= 0.90f) {
            val currentMastered = _masteredLessonIds.value.toMutableSet()
            currentMastered.add(lessonId)
            _masteredLessonIds.value = currentMastered
        }

        // Update key proficiency
        val currentProf = _keyProficiencies.value[keySignature] ?: KeyProficiency(
            keySignature = keySignature,
            totalLessonsCount = LessonRepository.getLessonsForKey(keySignature).size
        )

        val newAttempts = currentProf.totalAttempts + 1
        val newBestAccuracy = maxOf(currentProf.bestAccuracy, accuracyFloat)
        val newAvgAccuracy = if (currentProf.totalAttempts == 0) accuracyFloat else {
            ((currentProf.averageAccuracy * currentProf.totalAttempts) + accuracyFloat) / newAttempts
        }

        val completedCountInKey = LessonRepository.getLessonsForKey(keySignature)
            .count { currentCompleted.contains(it.id) }

        val updatedProf = KeyProficiency(
            keySignature = keySignature,
            totalAttempts = newAttempts,
            completedLessonsCount = completedCountInKey,
            totalLessonsCount = LessonRepository.getLessonsForKey(keySignature).size,
            averageAccuracy = newAvgAccuracy,
            bestAccuracy = newBestAccuracy
        )

        val updatedMap = _keyProficiencies.value.toMutableMap()
        updatedMap[keySignature] = updatedProf
        _keyProficiencies.value = updatedMap

        // Persist
        val obj = JSONObject().apply {
            put("totalAttempts", updatedProf.totalAttempts)
            put("completedLessonsCount", updatedProf.completedLessonsCount)
            put("averageAccuracy", updatedProf.averageAccuracy.toDouble())
            put("bestAccuracy", updatedProf.bestAccuracy.toDouble())
        }
        prefs.edit().putString("prof_${keySignature.name}", obj.toString()).apply()
    }

    companion object {
        private const val KEY_COMPLETED_LESSONS = "completed_lesson_ids"
    }
}
