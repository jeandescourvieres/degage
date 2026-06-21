package com.degage.tts

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin

/**
 * Petit jingle joue une fois a l'ouverture de l'accueil — pas de fichier audio, synthese a la volee.
 * Inspire des 9 premieres notes du theme principal de Star Wars (transcription simplifiee,
 * non garantie fidele a 100% a la partition originale) : clin d'oeil demande par Jean,
 * en connaissance du risque de droits d'auteur sur la melodie si l'appli est publiee.
 */
object WelcomeChime {

    // Frequence (Hz) et duree (s) de chaque note : G G G C G D C B A
    private val notes = listOf(
        392.00f to 0.15, // G4
        392.00f to 0.15, // G4
        392.00f to 0.15, // G4
        523.25f to 0.55, // C5 — la grande tenue
        392.00f to 0.35, // G4
        587.33f to 0.35, // D5
        523.25f to 0.15, // C5
        493.88f to 0.15, // B4
        440.00f to 0.45, // A4 — note finale, laissee sonner
    )
    private const val gapDurationSec = 0.03

    fun play() {
        Thread {
            val sampleRate = 44100
            val gapSamples = (sampleRate * gapDurationSec).toInt()
            val totalSamples = notes.sumOf { (_, dur) -> (sampleRate * dur).toInt() + gapSamples }
            // Assez de place pour toutes les notes en entier : un buffer trop petit forcait stop()
            // a couper la fin de la derniere note avant qu'elle ait fini de jouer.
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(totalSamples * 2)

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

            track.play()
            var totalDurationSec = 0.0
            notes.forEach { (freq, durationSec) ->
                val noteSamples = (sampleRate * durationSec).toInt()
                val buffer = ShortArray(noteSamples + gapSamples)
                for (i in 0 until noteSamples) {
                    val t = i.toDouble() / sampleRate
                    val env = when {
                        i < noteSamples * 0.1 -> i / (noteSamples * 0.1)
                        i > noteSamples * 0.7 -> (noteSamples - i) / (noteSamples * 0.3)
                        else -> 1.0
                    }
                    buffer[i] = (sin(2.0 * PI * freq * t) * 26000 * env).toInt().toShort()
                }
                // bref silence entre les notes pour eviter les clics de raccord
                for (i in noteSamples until buffer.size) buffer[i] = 0
                track.write(buffer, 0, buffer.size)
                totalDurationSec += durationSec + gapDurationSec
            }
            // Laisse le temps a la derniere note de jouer reellement avant de couper la piste :
            // write() ne garantit que la mise en file, pas la lecture effective.
            Thread.sleep((totalDurationSec * 1000).toLong() + 80)
            track.stop()
            track.release()
        }.apply { isDaemon = true }.start()
    }
}
