package com.degage.tts

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin

/** Petit jingle joue une fois a l'ouverture de l'accueil — pas de fichier audio, synthese a la volee. */
object WelcomeChime {

    // Six bips nettement separes : Do, Mi, Sol (montant) puis Sol, Mi, Do (descendant)
    private val notes = floatArrayOf(523.25f, 659.25f, 783.99f, 783.99f, 659.25f, 523.25f)

    fun play() {
        Thread {
            val sampleRate = 44100
            val beepDurationSec = 0.14
            val gapDurationSec = 0.10
            val beepSamples = (sampleRate * beepDurationSec).toInt()
            val gapSamples = (sampleRate * gapDurationSec).toInt()
            // Assez de place pour les 3 bips en entier : un buffer trop petit forcait stop()
            // a couper la fin du dernier bip avant qu'il ait fini de jouer.
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast((beepSamples + gapSamples) * notes.size * 2)

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
            notes.forEachIndexed { index, freq ->
                val buffer = ShortArray(beepSamples + gapSamples)
                for (i in 0 until beepSamples) {
                    val t = i.toDouble() / sampleRate
                    val env = when {
                        i < beepSamples * 0.1 -> i / (beepSamples * 0.1)
                        i > beepSamples * 0.6 -> (beepSamples - i) / (beepSamples * 0.4)
                        else -> 1.0
                    }
                    buffer[i] = (sin(2.0 * PI * freq * t) * 26000 * env).toInt().toShort()
                }
                // silence entre les bips pour qu'ils restent distincts a l'oreille
                for (i in beepSamples until buffer.size) buffer[i] = 0
                track.write(buffer, 0, buffer.size)
            }
            // Laisse le temps au dernier bip de jouer reellement avant de couper la piste :
            // write() ne garantit que la mise en file, pas la lecture effective.
            Thread.sleep(((beepDurationSec + gapDurationSec) * notes.size * 1000).toLong() + 80)
            track.stop()
            track.release()
        }.apply { isDaemon = true }.start()
    }
}
