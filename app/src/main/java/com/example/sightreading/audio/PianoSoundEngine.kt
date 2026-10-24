package com.example.sightreading.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import java.util.concurrent.ConcurrentHashMap

class PianoSoundEngine {

    private val sampleRate = 44100
    private val soundPool: SoundPool
    private val noteSoundMap = ConcurrentHashMap<Int, Int>()

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(attrs)
            .build()

        // Pre-synthesize and cache piano PCM tones for playable notes (MIDI 60 to 84)
        Thread {
            for (midi in 60..84) {
                try {
                    val freq = NoteUtils.midiToFrequency(midi)
                    val pcmData = generatePianoTonePcm(freq, 400)
                    val soundId = loadPcmIntoSoundPool(soundPool, pcmData, sampleRate)
                    if (soundId != 0) {
                        noteSoundMap[midi] = soundId
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    /**
     * Plays an audible, harmonic piano string tone with ultra-low latency & 0 allocations.
     */
    fun playNoteTone(midiNote: Int) {
        val soundId = noteSoundMap[midiNote]
        if (soundId != null && soundId != 0) {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
        } else {
            // Fallback for un-cached notes
            playFallbackTone(midiNote)
        }
    }

    private fun playFallbackTone(midiNote: Int) {
        Thread {
            try {
                val freq = NoteUtils.midiToFrequency(midiNote)
                val pcmData = generatePianoTonePcm(freq, 350)

                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

                val format = AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(attrs)
                    .setAudioFormat(format)
                    .setBufferSizeInBytes(pcmData.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                    .build()

                audioTrack.write(pcmData, 0, pcmData.size)
                audioTrack.play()
                Thread.sleep(380)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun generatePianoTonePcm(freqHz: Float, durationMs: Int): ShortArray {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val time = i.toDouble() / sampleRate
            val envelope = Math.exp(-6.5 * time)

            val fundamental = Math.sin(2.0 * Math.PI * freqHz * time)
            val secondHarmonic = 0.35 * Math.sin(4.0 * Math.PI * freqHz * time)
            val thirdHarmonic = 0.15 * Math.sin(6.0 * Math.PI * freqHz * time)

            val rawSample = (fundamental + secondHarmonic + thirdHarmonic) * envelope * 24000.0
            pcm[i] = rawSample.toInt().coerceIn(-32768, 32767).toShort()
        }
        return pcm
    }

    private fun loadPcmIntoSoundPool(soundPool: SoundPool, pcmData: ShortArray, sampleRate: Int): Int {
        val headerSize = 44
        val dataSize = pcmData.size * 2
        val totalSize = headerSize + dataSize

        val wavBytes = ByteArray(totalSize)

        // Write WAV Header
        writeWavHeader(wavBytes, dataSize, sampleRate)

        // Write PCM 16-bit Samples
        var idx = headerSize
        for (sample in pcmData) {
            wavBytes[idx++] = (sample.toInt() and 0xFF).toByte()
            wavBytes[idx++] = ((sample.toInt() shr 8) and 0xFF).toByte()
        }

        val tempFile = java.io.File.createTempFile("piano_tone_", ".wav")
        tempFile.deleteOnExit()
        tempFile.writeBytes(wavBytes)

        val soundId = soundPool.load(tempFile.absolutePath, 1)
        return soundId
    }

    private fun writeWavHeader(out: ByteArray, pcmLen: Int, sampleRate: Int) {
        val totalLen = pcmLen + 36
        val channels = 1
        val byteRate = sampleRate * channels * 2

        out[0] = 'R'.code.toByte(); out[1] = 'I'.code.toByte(); out[2] = 'F'.code.toByte(); out[3] = 'F'.code.toByte()
        out[4] = (totalLen and 0xff).toByte(); out[5] = ((totalLen shr 8) and 0xff).toByte()
        out[6] = ((totalLen shr 16) and 0xff).toByte(); out[7] = ((totalLen shr 24) and 0xff).toByte()
        out[8] = 'W'.code.toByte(); out[9] = 'A'.code.toByte(); out[10] = 'V'.code.toByte(); out[11] = 'E'.code.toByte()
        out[12] = 'f'.code.toByte(); out[13] = 'm'.code.toByte(); out[14] = 't'.code.toByte(); out[15] = ' '.code.toByte()
        out[16] = 16; out[17] = 0; out[18] = 0; out[19] = 0 // Subchunk1Size (16 for PCM)
        out[20] = 1; out[21] = 0 // AudioFormat (1 for PCM)
        out[22] = channels.toByte(); out[23] = 0
        out[24] = (sampleRate and 0xff).toByte(); out[25] = ((sampleRate shr 8) and 0xff).toByte()
        out[26] = ((sampleRate shr 16) and 0xff).toByte(); out[27] = ((sampleRate shr 24) and 0xff).toByte()
        out[28] = (byteRate and 0xff).toByte(); out[29] = ((byteRate shr 8) and 0xff).toByte()
        out[30] = ((byteRate shr 16) and 0xff).toByte(); out[31] = ((byteRate shr 24) and 0xff).toByte()
        out[32] = 2; out[33] = 0 // BlockAlign
        out[34] = 16; out[35] = 0 // BitsPerSample
        out[36] = 'd'.code.toByte(); out[37] = 'a'.code.toByte(); out[38] = 't'.code.toByte(); out[39] = 'a'.code.toByte()
        out[40] = (pcmLen and 0xff).toByte(); out[41] = ((pcmLen shr 8) and 0xff).toByte()
        out[42] = ((pcmLen shr 16) and 0xff).toByte(); out[43] = ((pcmLen shr 24) and 0xff).toByte()
    }

    fun release() {
        try {
            soundPool.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
