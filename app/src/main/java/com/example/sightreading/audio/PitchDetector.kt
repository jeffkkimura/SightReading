package com.example.sightreading.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

data class PitchResult(
    val frequency: Float = 0f,
    val midiNote: Int = 0,
    val noteName: String = "",
    val cents: Float = 0f,
    val rms: Float = 0f,
    val isSilence: Boolean = true
)

class PitchDetector(
    private val sampleRate: Int = 44100,
    private val bufferSize: Int = 4096
) {
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null

    private val _pitchResult = MutableStateFlow(PitchResult())
    val pitchResult: StateFlow<PitchResult> = _pitchResult.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    // Minimum RMS audio amplitude to trigger note detection (ignores room background noise)
    private val silenceRmsThreshold = 0.015f

    // YIN algorithm absolute threshold
    private val yinThreshold = 0.15f

    @SuppressLint("MissingPermission")
    fun startListening(scope: CoroutineScope) {
        if (_isListening.value) return

        val minBufSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val actualBufSize = maxOf(bufferSize * 2, minBufSize)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                actualBufSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                _isListening.value = false
                return
            }

            audioRecord?.startRecording()
            _isListening.value = true

            recordingJob = scope.launch(Dispatchers.IO) {
                val audioBuffer = ShortArray(bufferSize)
                val floatBuffer = FloatArray(bufferSize)

                while (isActive && _isListening.value) {
                    val readSize = audioRecord?.read(audioBuffer, 0, bufferSize) ?: 0
                    if (readSize > 0) {
                        // Convert PCM 16-bit to Normalized Float [-1.0, 1.0]
                        var sumSq = 0f
                        for (i in 0 until readSize) {
                            val sample = audioBuffer[i] / 32768.0f
                            floatBuffer[i] = sample
                            sumSq += sample * sample
                        }

                        val rms = sqrt(sumSq / readSize)

                        if (rms < silenceRmsThreshold) {
                            _pitchResult.value = PitchResult(rms = rms, isSilence = true)
                        } else {
                            val pitchFreq = processYin(floatBuffer, readSize)
                            if (pitchFreq > 30f && pitchFreq < 4200f) { // Piano range: ~A0 to C8
                                val midi = NoteUtils.frequencyToClosestMidi(pitchFreq)
                                val noteName = NoteUtils.midiToNoteName(midi)
                                val cents = NoteUtils.centsFromNearestNote(pitchFreq)
                                _pitchResult.value = PitchResult(
                                    frequency = pitchFreq,
                                    midiNote = midi,
                                    noteName = noteName,
                                    cents = cents,
                                    rms = rms,
                                    isSilence = false
                                )
                            } else {
                                _pitchResult.value = PitchResult(rms = rms, isSilence = true)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isListening.value = false
        }
    }

    fun stopListening() {
        _isListening.value = false
        recordingJob?.cancel()
        recordingJob = null

        try {
            if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord?.stop()
            }
            audioRecord?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            audioRecord = null
            _pitchResult.value = PitchResult(isSilence = true)
        }
    }

    /**
     * Implementation of YIN Pitch Detection algorithm.
     * Returns detected pitch frequency in Hz, or -1f if no clear pitch found.
     */
    private fun processYin(buffer: FloatArray, size: Int): Float {
        val halfSize = size / 2
        val yinBuffer = FloatArray(halfSize)

        // Step 1: Difference function
        for (tau in 1 until halfSize) {
            var diff = 0f
            for (i in 0 until halfSize) {
                val delta = buffer[i] - buffer[i + tau]
                diff += delta * delta
            }
            yinBuffer[tau] = diff
        }

        // Step 2: Cumulative mean normalized difference function
        yinBuffer[0] = 1f
        var runningSum = 0f
        for (tau in 1 until halfSize) {
            runningSum += yinBuffer[tau]
            yinBuffer[tau] *= tau / runningSum
        }

        // Step 3: Absolute thresholding
        var tauEstimate = -1
        for (tau in 2 until halfSize) {
            if (yinBuffer[tau] < yinThreshold) {
                var localMinTau = tau
                while (localMinTau + 1 < halfSize && yinBuffer[localMinTau + 1] < yinBuffer[localMinTau]) {
                    localMinTau++
                }
                tauEstimate = localMinTau
                break
            }
        }

        if (tauEstimate == -1) {
            // Find global minimum if below threshold not met
            var minVal = Float.MAX_VALUE
            for (tau in 2 until halfSize) {
                if (yinBuffer[tau] < minVal) {
                    minVal = yinBuffer[tau]
                    tauEstimate = tau
                }
            }
            if (minVal > 0.4f) { // Too noisy or uncertain
                return -1f
            }
        }

        // Step 4: Parabolic interpolation
        var betterTau = tauEstimate.toFloat()
        if (tauEstimate > 0 && tauEstimate < halfSize - 1) {
            val s0 = yinBuffer[tauEstimate - 1]
            val s1 = yinBuffer[tauEstimate]
            val s2 = yinBuffer[tauEstimate + 1]
            val denominator = 2 * (s2 - 2 * s1 + s0)
            if (denominator != 0f) {
                betterTau += (s0 - s2) / denominator
            }
        }

        return if (betterTau > 0) sampleRate / betterTau else -1f
    }
}
