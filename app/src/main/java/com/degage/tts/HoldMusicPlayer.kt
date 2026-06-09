package com.degage.tts

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin

class HoldMusicPlayer {

    private var audioTrack: AudioTrack? = null
    private var thread: Thread? = null
    @Volatile private var playing = false

    // Mélodie "musique d'attente téléphonique" — 8 notes en boucle
    private val melody = floatArrayOf(
        523.25f, 659.25f, 783.99f, 659.25f,   // Do Mi Sol Mi
        523.25f, 440.00f, 493.88f, 523.25f    // Do La Si Do
    )

    fun start() {
        if (playing) return
        playing = true
        thread = Thread {
            val sampleRate = 44100
            val noteDurationSec = 0.35
            val pauseDurationSec = 0.05
            val totalPerNote = sampleRate * (noteDurationSec + pauseDurationSec)
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(sampleRate * 2)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .build()

            audioTrack = track
            track.play()

            var noteIndex = 0
            while (playing) {
                val freq = melody[noteIndex % melody.size]
                val noteSamples = (sampleRate * noteDurationSec).toInt()
                val pauseSamples = (sampleRate * pauseDurationSec).toInt()
                val buffer = ShortArray(noteSamples + pauseSamples)

                for (i in 0 until noteSamples) {
                    val t = i.toDouble() / sampleRate
                    val env = when {
                        i < noteSamples * 0.08 -> i / (noteSamples * 0.08)
                        i > noteSamples * 0.75 -> (noteSamples - i) / (noteSamples * 0.25)
                        else -> 1.0
                    }
                    buffer[i] = (sin(2.0 * PI * freq * t) * 28000 * env).toInt().toShort()
                }
                // pause silencieuse à la fin de la note
                for (i in noteSamples until buffer.size) buffer[i] = 0

                if (playing) track.write(buffer, 0, buffer.size)
                noteIndex++
            }

            track.stop()
            track.release()
            audioTrack = null
        }.also { it.isDaemon = true }
        thread?.start()
    }

    fun stop() {
        playing = false
        thread?.join(500)
        thread = null
    }
}
