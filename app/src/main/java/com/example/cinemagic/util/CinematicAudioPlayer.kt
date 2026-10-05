package com.example.cinemagic.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

class CinematicAudioPlayer(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isTtsReady = false

    private var audioTrack: AudioTrack? = null
    private var ambientJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    var onDialogueCompleted: (() -> Unit)? = null
    var onDialogueStarted: (() -> Unit)? = null

    init {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                onDialogueStarted?.invoke()
            }

            override fun onDone(utteranceId: String?) {
                onDialogueCompleted?.invoke()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                onDialogueCompleted?.invoke()
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                onDialogueCompleted?.invoke()
            }
        })
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("CinematicAudio", "Locale not supported, using default")
                tts?.setLanguage(Locale.getDefault())
            }
            tts?.setSpeechRate(0.88f) // Slightly slower, cinematic delivery
            tts?.setPitch(0.92f) // Deeper, more dramatic tone
            isTtsReady = true
        }
    }

    fun speakDialogue(text: String, utteranceId: String = "scene_dialogue") {
        if (text.isBlank()) {
            onDialogueCompleted?.invoke()
            return
        }
        if (isTtsReady) {
            tts?.stop()
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            // If TTS not ready yet, simulate completion
            scope.launch {
                kotlinx.coroutines.delay(2500)
                onDialogueCompleted?.invoke()
            }
        }
    }

    fun stopDialogue() {
        if (isTtsReady) {
            tts?.stop()
        }
    }

    /**
     * Starts a subtle cinematic ambient score using real-time audio synthesis.
     * Generates a rich, warm low-frequency drone (sub-bass 65Hz + fifth 97.5Hz + octave 130Hz)
     * with slow LFO modulation for true Hollywood tension.
     */
    fun startCinematicAmbientDrone(volume: Float = 0.35f) {
        stopAmbient()

        ambientJob = scope.launch {
            val sampleRate = 44100
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            try {
                audioTrack = AudioTrack.Builder()
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
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack?.setVolume(volume)
                audioTrack?.play()

                val buffer = ShortArray(bufferSize / 2)
                var phase1 = 0.0
                var phase2 = 0.0
                var phase3 = 0.0
                var lfoPhase = 0.0

                val freq1 = 65.41 // C2 sub-drone
                val freq2 = 98.00 // G2 fifth
                val freq3 = 130.81 // C3 octave
                val lfoFreq = 0.15 // slow breathing modulation

                while (isActive) {
                    for (i in buffer.indices) {
                        val lfo = 0.7 + 0.3 * sin(2.0 * PI * lfoPhase)
                        val sample1 = sin(2.0 * PI * phase1) * 0.5
                        val sample2 = sin(2.0 * PI * phase2) * 0.25
                        val sample3 = sin(2.0 * PI * phase3) * 0.15

                        val mixed = (sample1 + sample2 + sample3) * lfo * 0.7
                        buffer[i] = (mixed * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

                        phase1 += freq1 / sampleRate
                        if (phase1 > 1.0) phase1 -= 1.0

                        phase2 += freq2 / sampleRate
                        if (phase2 > 1.0) phase2 -= 1.0

                        phase3 += freq3 / sampleRate
                        if (phase3 > 1.0) phase3 -= 1.0

                        lfoPhase += lfoFreq / sampleRate
                        if (lfoPhase > 1.0) lfoPhase -= 1.0
                    }
                    audioTrack?.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                Log.w("CinematicAudio", "Ambient drone interrupted or not supported: ${e.message}")
            }
        }
    }

    fun stopAmbient() {
        ambientJob?.cancel()
        ambientJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // ignore
        }
        audioTrack = null
    }

    fun release() {
        stopDialogue()
        stopAmbient()
        try {
            tts?.shutdown()
        } catch (e: Exception) {
            // ignore
        }
        tts = null
    }
}
