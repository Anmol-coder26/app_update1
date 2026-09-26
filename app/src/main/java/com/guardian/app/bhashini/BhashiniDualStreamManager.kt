package com.guardian.app.bhashini

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

data class TranscriptLine(
    val speaker: Speaker,
    val text: String,
    val isFinal: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

enum class Speaker { LOCAL, REMOTE, UNKNOWN }

class BhashiniDualStreamManager(private val sourceLanguage: String = "hi") {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var localClient: BhashiniSttClient? = null
    private var remoteClient: BhashiniSttClient? = null

    private val _transcripts = MutableSharedFlow<TranscriptLine>(replay = 0, extraBufferCapacity = 256)
    val transcripts: SharedFlow<TranscriptLine> = _transcripts

    fun start() {
        localClient = BhashiniSttClient(
            sourceLanguage = sourceLanguage,
            mode = BhashiniSttClient.Mode.PUSH,
            onTranscript = { text, isFinal ->
                scope.launch {
                    _transcripts.emit(TranscriptLine(Speaker.LOCAL, text, isFinal))
                }
            },
            onError = { err ->
                scope.launch {
                    _transcripts.emit(TranscriptLine(Speaker.LOCAL, "[error: $err]", true))
                }
            }
        ).also { it.start() }

        remoteClient = BhashiniSttClient(
            sourceLanguage = sourceLanguage,
            mode = BhashiniSttClient.Mode.PUSH,
            onTranscript = { text, isFinal ->
                scope.launch {
                    _transcripts.emit(TranscriptLine(Speaker.REMOTE, text, isFinal))
                }
            },
            onError = { err ->
                scope.launch {
                    _transcripts.emit(TranscriptLine(Speaker.REMOTE, "[error: $err]", true))
                }
            }
        ).also { it.start() }
    }

    fun pushLocal(samples: ShortArray, sampleRate: Int) {
        localClient?.pushPcm(samples, sampleRate)
    }

    fun pushRemote(samples: ShortArray, sampleRate: Int) {
        remoteClient?.pushPcm(samples, sampleRate)
    }

    fun stop() {
        localClient?.stop()
        remoteClient?.stop()
        localClient = null
        remoteClient = null
        scope.cancel()
    }
}
