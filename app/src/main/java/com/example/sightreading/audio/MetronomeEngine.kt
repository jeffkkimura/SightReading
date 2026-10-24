package com.example.sightreading.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

class MetronomeEngine {

    private var audioTrackHigh: AudioTrack? = null
    private var audioTrackLow: AudioTrack? = null
    private var metronomeJob: Job? = null

    private val sampleRate = 44100

    init {
        initAudioTracks()
    }

    private fun initAudioTracks() {
        try {
            val highPcm = generateClickPcm(freqHz = 1200f, durationMs = 20)
            val lowPcm = generateClickPcm(freqHz = 800f, durationMs = 15)

            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val format = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            audioTrackHigh = AudioTrack.Builder()
                .setAudioAttributes(attrs)
                .setAudioFormat(format)
                .setBufferSizeInBytes(highPcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                .build().apply {
                    write(highPcm, 0, highPcm.size)
                }

            audioTrackLow = AudioTrack.Builder()
                .setAudioAttributes(attrs)
                .setAudioFormat(format)
                .setBufferSizeInBytes(lowPcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                .build().apply {
                    write(lowPcm, 0, lowPcm.size)
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playClick(isAccent: Boolean) {
        val track = if (isAccent) audioTrackHigh else audioTrackLow
        track?.let {
            try {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.reloadStaticData()
                it.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startContinuousClick(
        scope: CoroutineScope,
        bpm: Int,
        beatsPerMeasure: Int = 4,
        onBeat: (beatIndex: Int) -> Unit
    ) {
        stopContinuousClick()
        metronomeJob = scope.launch(Dispatchers.Default) {
            var currentBeat = 0
            val intervalMs = (60_000f / bpm.coerceIn(40, 240)).toLong()

            while (isActive) {
                val isAccent = (currentBeat % beatsPerMeasure) == 0
                playClick(isAccent)
                onBeat(currentBeat % beatsPerMeasure)

                currentBeat++
                delay(intervalMs)
            }
        }
    }

    fun stopContinuousClick() {
        metronomeJob?.cancel()
        metronomeJob = null
    }

    fun release() {
        stopContinuousClick()
        try {
            audioTrackHigh?.release()
            audioTrackLow?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioTrackHigh = null
        audioTrackLow = null
    }

    private fun generateClickPcm(freqHz: Float, durationMs: Int): ShortArray {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val time = i.toDouble() / sampleRate
            val envelope = Math.exp(-180.0 * time)
            val sample = (sin(2.0 * Math.PI * freqHz * time) * envelope * 32767.0)
            pcm[i] = sample.toInt().coerceIn(-32768, 32767).toShort()
        }
        return pcm
    }
}
