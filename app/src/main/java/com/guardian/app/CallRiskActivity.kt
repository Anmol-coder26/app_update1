package com.guardian.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.guardian.app.bhashini.Speaker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class LabeledTranscript(
    val speaker: Speaker,
    val text: String,
    val isFinal: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class CallRiskActivity : ComponentActivity() {
    private val microphonePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startDetection() else {
                errorMessage = "Microphone permission is required to analyze call audio."
            }
        }

    private var isDetecting by mutableStateOf(false)
    private var isAnalyzing by mutableStateOf(false)
    private var isAgoraMode by mutableStateOf(false)
    private var transcript by mutableStateOf("")
    private var riskReport by mutableStateOf(RiskReport())
    private var errorMessage by mutableStateOf<String?>(null)
    private var selectedLanguage by mutableStateOf(LanguageMode.AUTO)
    private var audioLevel by mutableFloatStateOf(0f)
    private val labeledTranscripts = mutableStateListOf<LabeledTranscript>()
    private val transcriptBuffer = StringBuilder()

    private var transcriber: StreamingTranscriber? = null
    private var agoraEngine: AgoraEngine? = null
    private lateinit var semanticAnalyzer: SemanticAnalyzer
    private var analysisJob: Job? = null
    private var isDemoModePlaying by mutableStateOf(false)
    private var demoJob: Job? = null
    private var scoreWatcherJob: Job? = null
    private var warningPlayer: com.guardian.app.callprotect.CriticalWarningPlayer? = null
    private var currentCallerNumber by mutableStateOf("")

    private var lastScoreChangeTime = System.currentTimeMillis()
    private var lastScoreValue = 0

    @Suppress("DEPRECATION")
    private val callEndListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            if (state == TelephonyManager.CALL_STATE_IDLE && isDetecting) {
                resetDemo()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        semanticAnalyzer = SemanticAnalyzer(this)
        agoraEngine = AgoraEngine(this)
        warningPlayer = com.guardian.app.callprotect.CriticalWarningPlayer(this)

        currentCallerNumber = intent?.getStringExtra(CallProtectionService.EXTRA_NUMBER).orEmpty()
        if (currentCallerNumber.isNotBlank()) {
            lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val repo = com.guardian.app.callprotect.NumberReputationRepository(applicationContext)
                val flagged = repo.lookup(currentCallerNumber)
                if (flagged != null && flagged.riskLevel in listOf("HIGH_RISK", "SCAM", "SPAM")) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        riskReport = RiskReport(
                            riskScore = 80,
                            explanationEn = "Caller number is flagged in the reputation database for: ${flagged.reason}",
                            explanationHi = "कॉलर नंबर धोखाधड़ी डेटाबेस में दर्ज है: ${flagged.reason}",
                            engines = EngineReports(
                                pretextLegitimacy = PretextReport(
                                    detected = true,
                                    type = "reputation_blacklist",
                                    confidence = 0.95f,
                                    summary = "Flagged ${flagged.riskLevel}: ${flagged.reason}"
                                )
                            )
                        )
                        checkCallWarning(riskReport)
                    }
                }
            }
        }

        @Suppress("DEPRECATION")
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            val telephony = getSystemService(TelephonyManager::class.java)
            telephony.listen(callEndListener, PhoneStateListener.LISTEN_CALL_STATE)
        }

        // Auto-start speakerphone transcription pipeline for live protection
        requestAndStartDetection()

        setContent {
            val agoraCallState by agoraEngine?.callState?.collectAsState() ?: remember { mutableStateOf(AgoraCallState.DISCONNECTED) }

            GuardianTheme {
                CallRiskScreen(
                    isDetecting = isDetecting || isDemoModePlaying || agoraCallState == AgoraCallState.IN_CALL || agoraCallState == AgoraCallState.CONNECTING,
                    isAnalyzing = isAnalyzing,
                    isAgoraMode = isAgoraMode,
                    agoraCallState = agoraCallState,
                    transcript = transcript,
                    labeledTranscripts = labeledTranscripts,
                    riskReport = riskReport,
                    errorMessage = errorMessage,
                    selectedLanguage = selectedLanguage,
                    audioLevel = audioLevel,
                    isDemoModePlaying = isDemoModePlaying,
                    callerNumber = currentCallerNumber,
                    onLanguageSelect = { lang ->
                        selectedLanguage = lang
                        transcriber?.setLanguage(lang)
                    },
                    onStartSpeaker = ::requestAndStartDetection,
                    onStartAgoraVoip = ::startAgoraCall,
                    onStop = ::stopDetection,
                    onReset = ::resetDemo,
                    onEndCall = {
                        com.guardian.app.callprotect.CallActionHelper.endCall(this@CallRiskActivity)
                        stopDetection()
                    },
                    onBlockNumber = {
                        val num = currentCallerNumber.ifBlank { "Unknown" }
                        val ok = com.guardian.app.callprotect.CallActionHelper.blockNumber(this@CallRiskActivity, num)
                        if (ok) {
                            android.widget.Toast.makeText(this@CallRiskActivity, "Blocked $num", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    onSimulateScenario = ::simulateScenario,
                    onPlayFullDemo = ::startFullDemoSimulation
                )
            }
        }
    }

    private fun checkCallWarning(report: RiskReport) {
        if (report.riskScore >= 85) {
            val msg = if (selectedLanguage == LanguageMode.HINDI) {
                "उच्च जोखिम धोखाधड़ी कॉल! आपकी वित्तीय जानकारी खतरे में हो सकती है। अभी कॉल समाप्त करें।"
            } else {
                "High risk scam call! Your financial information may be in danger. End this call now."
            }
            val langTag = if (selectedLanguage == LanguageMode.HINDI) "hi-IN" else "en-IN"
            warningPlayer?.playWarning(msg, langTag)
        }
    }

    private fun startFullDemoSimulation(scenarioIndex: Int) {
        demoJob?.cancel()
        resetDemo()
        isDemoModePlaying = true

        val scenarioTurns = when (scenarioIndex) {
            1 -> listOf(
                Pair(Speaker.REMOTE, "Good afternoon, this is SBI Card Fraud Prevention unit calling."),
                Pair(Speaker.LOCAL, "Yes, what is this regarding?"),
                Pair(Speaker.REMOTE, "An unauthorized overseas debit of ₹45,000 was initiated from Dubai."),
                Pair(Speaker.LOCAL, "I did not make this transaction!"),
                Pair(Speaker.REMOTE, "To freeze your card and cancel the transfer, please verify the 6-digit OTP code sent to your phone.")
            )
            2 -> listOf(
                Pair(Speaker.REMOTE, "Urgent notification: State Electricity Board power management team."),
                Pair(Speaker.LOCAL, "Hello?"),
                Pair(Speaker.REMOTE, "Your residential power supply meter is scheduled for disconnection in 20 minutes due to unpaid dues."),
                Pair(Speaker.REMOTE, "Download the AnyDesk remote screen application and pay ₹2,500 security deposit immediately.")
            )
            else -> listOf(
                Pair(Speaker.REMOTE, "I am Inspector Rajesh Kumar from Delhi Cyber Crime Cell HQ."),
                Pair(Speaker.LOCAL, "Who is this?"),
                Pair(Speaker.REMOTE, "A seized DHL narcotics parcel with 12 fake passports was found registered under your Aadhaar number."),
                Pair(Speaker.LOCAL, "I know nothing about any parcel!"),
                Pair(Speaker.REMOTE, "You are under immediate digital arrest. Transfer ₹50,000 verification bail money immediately to avoid police custody.")
            )
        }

        demoJob = lifecycleScope.launch {
            for ((speaker, turn) in scenarioTurns) {
                if (!isDemoModePlaying) break
                handleIncomingTranscript(turn, isFinal = true, speaker = speaker)
                delay(2500)
            }
            isDemoModePlaying = false
        }
    }

    private fun requestAndStartDetection() {
        errorMessage = null
        isAgoraMode = false
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            startDetection()
        }
    }

    private fun startAgoraCall() {
        errorMessage = null
        isAgoraMode = true
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            startDetection()
            agoraEngine?.startCall(
                channelName = "guardian_secure_call",
                listener = object : AgoraTranscriptListener {
                    override fun onTranscriptReceived(text: String, isFinal: Boolean, speakerUid: Int) {
                        val speaker = if (speakerUid > 0) Speaker.LOCAL else Speaker.REMOTE
                        handleIncomingTranscript(text, isFinal, speaker)
                    }

                    override fun onCallStateChanged(state: AgoraCallState, message: String) {
                        if (state == AgoraCallState.ERROR) {
                            errorMessage = message
                        }
                    }

                    override fun onAudioVolumeChanged(volume: Int) {
                        audioLevel = (volume / 255f).coerceIn(0f, 1f)
                    }
                }
            )
        }
    }

    private fun startDetection() {
        stopDetection()
        transcript = ""
        labeledTranscripts.clear()
        transcriptBuffer.clear()
        riskReport = RiskReport()
        errorMessage = null
        isDetecting = true
        lastScoreChangeTime = System.currentTimeMillis()
        lastScoreValue = 0

        // Start score staleness monitor
        scoreWatcherJob = lifecycleScope.launch {
            while (isDetecting) {
                delay(3000)
                if (isDetecting && System.currentTimeMillis() - lastScoreChangeTime > 5000 && labeledTranscripts.isNotEmpty()) {
                    Log.w("Guardian", "WARN: risk score has not changed — check transcript pipeline")
                }
            }
        }

        transcriber = AndroidSpeechTranscriber(this).also { speech ->
            speech.start(selectedLanguage, object : TranscriptListener {
                override fun onTranscript(text: String, isFinal: Boolean) {
                    handleIncomingTranscript(text, isFinal, Speaker.REMOTE)
                }

                override fun onRmsChanged(rmsdB: Float) {
                    if (!isAgoraMode) {
                        audioLevel = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                    }
                }

                override fun onTranscriptionError(message: String) {
                    if (!isAgoraMode) {
                        errorMessage = message
                    }
                }
            })
        }
    }

    private fun handleIncomingTranscript(text: String, isFinal: Boolean, speaker: Speaker = Speaker.REMOTE) {
        transcript = text
        Log.i("Guardian", "Transcript received ($speaker): '$text' (isFinal=$isFinal)")

        if (text.isNotBlank()) {
            if (isFinal) {
                labeledTranscripts.add(LabeledTranscript(speaker, text, true))
            } else {
                // Update or add interim line
                val lastIdx = labeledTranscripts.indexOfLast { it.speaker == speaker && !it.isFinal }
                if (lastIdx >= 0) {
                    labeledTranscripts[lastIdx] = LabeledTranscript(speaker, text, false)
                } else {
                    labeledTranscripts.add(LabeledTranscript(speaker, text, false))
                }
            }
        }

        // Build labeled transcript prompt
        val fullLabeledContext = labeledTranscripts.joinToString("\n") { line ->
            val prefix = when (line.speaker) {
                Speaker.LOCAL -> "You: "
                Speaker.REMOTE -> "Caller: "
                Speaker.UNKNOWN -> ""
            }
            "$prefix${line.text}"
        }

        analysisJob?.cancel()
        analysisJob = lifecycleScope.launch {
            isAnalyzing = true
            val raw = semanticAnalyzer.analyzeChunk(fullLabeledContext)
            riskReport = raw

            if (raw.riskScore != lastScoreValue) {
                lastScoreValue = raw.riskScore
                lastScoreChangeTime = System.currentTimeMillis()
                Log.i("Guardian", "Risk score updated: ${raw.riskScore} (level=${raw.status.label}) signals=${raw.topSignals.map { it.title }}")
            }

            isAnalyzing = false
            checkCallWarning(riskReport)
        }
    }

    private fun stopDetection() {
        if (isDetecting || labeledTranscripts.isNotEmpty()) {
            val finalScore = riskReport.riskScore
            val signals = riskReport.topSignals.joinToString(", ")
            val summary = labeledTranscripts.takeLast(5).joinToString("; ") { "${it.speaker}: ${it.text}" }.take(300)
            val num = currentCallerNumber.ifBlank { "Live Speaker Audio" }
            lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val repo = com.guardian.app.callprotect.NumberReputationRepository(applicationContext)
                repo.saveCallHistory(
                    com.guardian.app.callprotect.CallHistoryEntry(
                        number = num,
                        timestamp = System.currentTimeMillis(),
                        riskScore = finalScore,
                        topSignals = signals,
                        transcriptSummary = summary,
                        actionTaken = if (finalScore >= 85) "flagged_critical" else "analyzed"
                    )
                )
            }
        }

        transcriber?.stop()
        transcriber = null
        agoraEngine?.leaveCall()
        analysisJob?.cancel()
        analysisJob = null
        scoreWatcherJob?.cancel()
        scoreWatcherJob = null
        isDetecting = false
        isAnalyzing = false
        isAgoraMode = false
        audioLevel = 0f
    }

    private fun resetDemo() {
        stopDetection()
        transcript = ""
        labeledTranscripts.clear()
        transcriptBuffer.clear()
        riskReport = RiskReport()
        errorMessage = null
        semanticAnalyzer.reset()
        lastScoreChangeTime = System.currentTimeMillis()
        lastScoreValue = 0
    }

    private fun simulateScenario(sampleText: String) {
        handleIncomingTranscript(sampleText, isFinal = true, speaker = Speaker.REMOTE)
    }

    override fun onDestroy() {
        @Suppress("DEPRECATION")
        getSystemService(TelephonyManager::class.java)?.listen(callEndListener, PhoneStateListener.LISTEN_NONE)
        stopDetection()
        warningPlayer?.release()
        agoraEngine?.destroy()
        super.onDestroy()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CallRiskScreen(
    isDetecting: Boolean,
    isAnalyzing: Boolean,
    isAgoraMode: Boolean,
    agoraCallState: AgoraCallState,
    transcript: String,
    labeledTranscripts: List<LabeledTranscript>,
    riskReport: RiskReport,
    errorMessage: String?,
    selectedLanguage: LanguageMode,
    audioLevel: Float,
    isDemoModePlaying: Boolean,
    callerNumber: String,
    onLanguageSelect: (LanguageMode) -> Unit,
    onStartSpeaker: () -> Unit,
    onStartAgoraVoip: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit,
    onEndCall: () -> Unit,
    onBlockNumber: () -> Unit,
    onSimulateScenario: (String) -> Unit,
    onPlayFullDemo: (Int) -> Unit
) {
    val score = riskReport.riskScore
    val animatedRiskScore by animateColorAsState(
        targetValue = when (riskReport.status) {
            RiskStatus.Low -> CyberEmerald
            RiskStatus.Suspicious -> AmberWarning
            RiskStatus.High -> CoralRed
        },
        label = "callRiskColor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "analyzingPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val listState = rememberLazyListState()
    LaunchedEffect(labeledTranscripts.size) {
        if (labeledTranscripts.isNotEmpty()) {
            listState.animateScrollToItem(labeledTranscripts.size - 1)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        state = listState
    ) {
        // Header Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isDetecting) CyberEmerald else TextMuted)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isDetecting) {
                            if (isAgoraMode) "DUAL-STREAM VOIP DEFENSE" else "SPEAKERPHONE CALL SHIELD"
                        } else "DEFENSE STANDBY",
                        color = if (isDetecting) CyberEmerald else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                if (isAnalyzing) {
                    Surface(
                        color = ElectricIndigo.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.alpha(pulseAlpha)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = ElectricIndigo, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Analyzing Live...", color = ElectricIndigo, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Circular Live Risk Gauge
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .border(
                            width = 10.dp,
                            color = animatedRiskScore.copy(alpha = 0.25f),
                            shape = CircleShape
                        )
                        .border(
                            width = 3.dp,
                            color = animatedRiskScore,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$score%",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = riskReport.status.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = animatedRiskScore,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // 4 Engine Breakdown Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EngineCard(
                        title = "Pretext Legitimacy",
                        icon = Icons.Default.Security,
                        detected = riskReport.engines.pretextLegitimacy.detected,
                        detail = riskReport.engines.pretextLegitimacy.summary,
                        modifier = Modifier.weight(1f)
                    )
                    EngineCard(
                        title = "Intent Extraction",
                        icon = Icons.Default.WarningAmber,
                        detected = riskReport.engines.intentRisk.detected,
                        detail = riskReport.engines.intentRisk.summary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EngineCard(
                        title = "Psychological Pressure",
                        icon = Icons.Default.GraphicEq,
                        detected = riskReport.engines.psychologicalPressure.detected,
                        detail = riskReport.engines.psychologicalPressure.summary,
                        modifier = Modifier.weight(1f)
                    )
                    EngineCard(
                        title = "Pattern Detection",
                        icon = Icons.Default.Psychology,
                        detected = riskReport.engines.zeroShotVariant.detected,
                        detail = riskReport.engines.zeroShotVariant.explanation.ifBlank { riskReport.engines.zeroShotVariant.variantName },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Plain Language Reasoning Explanation
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        "Why this score?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (selectedLanguage == LanguageMode.HINDI) riskReport.explanationHi else riskReport.explanationEn,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (riskReport.highlightedPhrases.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Flagged Trigger Keywords:", fontSize = 11.sp, color = TextMuted)
                        FlowRow(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            riskReport.highlightedPhrases.forEach { phrase ->
                                Surface(
                                    color = CoralRed.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, CoralRed)
                                ) {
                                    Text(
                                        "⚠ $phrase",
                                        color = CoralRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Controls (End Call, Block)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onEndCall,
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed, contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CallEnd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("END CALL IMMEDIATELY", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onBlockNumber,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Block & Add to Reputation DB", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }

        // Audio Source Selection & Controls
        item {
            if (isDetecting) {
                Button(
                    onClick = onStop,
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed.copy(alpha = 0.85f), contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Stop Live Listening", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onStartAgoraVoip,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo, contentColor = Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.HeadsetMic, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Agora VoIP", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onStartSpeaker,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald, contentColor = Color(0xFF022C22)),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Speakerphone", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Dual-Stream Live Transcript UI (Part 1.7)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("LIVE DUAL-STREAM TRANSCRIPT", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextMuted, letterSpacing = 1.sp)
                        if (isDetecting) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = CyberEmerald, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Live Audio", color = CyberEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    if (labeledTranscripts.isEmpty()) {
                        Surface(
                            color = DarkSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (transcript.isNotBlank()) "\"$transcript\"" else "Awaiting live acoustic or dual-stream speech...",
                                color = if (transcript.isNotBlank()) CyberEmerald else TextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            labeledTranscripts.takeLast(8).forEach { line ->
                                val isLocal = line.speaker == Speaker.LOCAL
                                val alignment = if (isLocal) Alignment.Start else Alignment.End
                                val bubbleBg = if (isLocal) ElectricIndigo.copy(alpha = 0.25f) else DarkSurfaceElevated
                                val borderCol = if (isLocal) ElectricIndigo.copy(alpha = 0.6f) else BorderSubtle
                                val textColor = if (line.isFinal) TextPrimary else TextSecondary
                                val speakerLabel = if (isLocal) "You" else "Caller"

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = alignment
                                ) {
                                    Text(
                                        speakerLabel,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLocal) ElectricIndigo else AmberWarning,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                    Surface(
                                        color = bubbleBg,
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, borderCol),
                                        modifier = Modifier.fillMaxWidth(0.85f)
                                    ) {
                                        Text(
                                            text = line.text,
                                            color = textColor,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Instant Stage Demo Simulator
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.4f))
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ElectricIndigo, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (isDemoModePlaying) "🔴 Live Stage Demo Playing (Auto Dialogue)..." else "STAGE DEMO SCENARIOS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isDemoModePlaying) CoralRed else TextPrimary,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        "Simulate multi-turn dual-stream fraud dialogues to demonstrate real-time AI score escalation:",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { onPlayFullDemo(0) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF450A0A), contentColor = CoralRed),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🚨 Digital Arrest", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onPlayFullDemo(1) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF332005), contentColor = AmberWarning),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🏦 Bank KYC", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onPlayFullDemo(2) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF063A2D), contentColor = CyberEmerald),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("⚡ Power Cut", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EngineCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    detected: Boolean,
    detail: String,
    modifier: Modifier = Modifier
) {
    val statusColor = if (detected) CoralRed else CyberEmerald
    val containerColor = if (detected) Color(0xFF280B0B) else DarkSurface

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (detected) CoralRed.copy(alpha = 0.5f) else BorderSubtle),
        modifier = modifier
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        if (detected) "ALERT" else "SAFE",
                        color = statusColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(
                detail,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 2,
                lineHeight = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}