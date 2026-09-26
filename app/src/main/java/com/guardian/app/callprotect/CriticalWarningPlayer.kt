package com.guardian.app.callprotect

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import java.util.Locale

class CriticalWarningPlayer(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.language = Locale.ENGLISH
            }
        }
    }

    fun playWarning(message: String, languageTag: String = "en-IN") {
        try {
            val tone = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000)
        } catch (_: Exception) {}

        try {
            if (isInitialized && tts != null) {
                tts?.language = Locale.forLanguageTag(languageTag)
                tts?.speak(message, TextToSpeech.QUEUE_ADD, null, "guardian_warning")
            }
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        isInitialized = false
    }
}
