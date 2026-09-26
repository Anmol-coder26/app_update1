package com.guardian.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
    private val conversationHistory = mutableStateListOf<String>()
    private val transcriptBuffer = StringBuilder()

    private var transcriber: StreamingTranscriber? = null
    private var agoraEngine: AgoraEngine? = null
    private lateinit var semanticAnalyzer: SemanticAnalyzer
    private var analysisJob: Job? = null
    private var isDemoModePlaying by mutableStateOf(false)
    private var demoJob: Job? = null
    private var warningPlayer: com.guardian.app.callprotect.CriticalWarningPlayer? = null
    private var currentCallerNumber by mutableStateOf("")

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

        setContent {
            val agoraCallState by agoraEngine?.callState?.collectAsState() ?: remember { mutableStateOf(AgoraCallState.DISCONNECTED) }

            GuardianTheme {
                CallRiskScreen(
                    isDetecting = isDetecting || isDemoModePlaying || agoraCallState == AgoraCallState.IN_CALL || agoraCallState == AgoraCallState.CONNECTING,
                    isAnalyzing = isAnalyzing,
                    isAgoraMode = isAgoraMode,
                    agoraCallState = agoraCallState,
                    transcript = transcript,
                    conversationHistory = conversationHistory,
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
                "Good afternoon, this is SBI Card Fraud Prevention unit calling.",
                "An unauthorized overseas debit of ₹45,000 was initiated from Dubai.",
                "To freeze your card and cancel the transfer, please verify the 6-digit OTP code sent to your phone."
            )
            2 -> listOf(
                "Urgent notification: State Electricity Board power management team.",
                "Your residential power supply meter is scheduled for disconnection in 20 minutes due to unpaid dues.",
                "Download the AnyDesk remote screen application and pay ₹2,500 security deposit immediately."
            )
            else -> listOf(
                "I am Inspector Rajesh Kumar from Delhi Cyber Crime Cell HQ.",
                "A seized DHL narcotics parcel with 12 fake passports was found registered under your Aadhaar number.",
                "You are under immediate digital arrest. Transfer ₹50,000 verification bail money immediately to avoid police custody."
            )
        }

        demoJob = lifecycleScope.launch {
            for (turn in scenarioTurns) {
                if (!isDemoModePlaying) break
                transcript = turn
                conversationHistory.add(0, turn)
                transcriptBuffer.append(" ").append(turn)
                isAnalyzing = true
                riskReport = semanticAnalyzer.analyzeChunk(transcriptBuffer.toString())
                isAnalyzing = false
                checkCallWarning(riskReport)
                kotlinx.coroutines.delay(2500)
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
                        handleIncomingTranscript(text, isFinal)
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
        transcriptBuffer.clear()
        conversationHistory.clear()
        riskReport = RiskReport()
        errorMessage = null
        isDetecting = true

        transcriber = AndroidSpeechTranscriber(this).also { speech ->
            speech.start(selectedLanguage, object : TranscriptListener {
                override fun onTranscript(text: String, isFinal: Boolean) {
                    handleIncomingTranscript(text, isFinal)
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

    private fun handleIncomingTranscript(text: String, isFinal: Boolean) {
        transcript = text
        if (isFinal && text.isNotBlank()) {
            conversationHistory.add(0, text)
            transcriptBuffer.append(" ").append(text)
            if (transcriptBuffer.length > 800) {
                transcriptBuffer.delete(0, transcriptBuffer.length - 800)
            }
        }

        val fullContext = if (transcriptBuffer.isNotEmpty()) {
            "$transcriptBuffer $text".trim()
        } else {
            text
        }

        analysisJob?.cancel()
        analysisJob = lifecycleScope.launch {
            isAnalyzing = true
            riskReport = semanticAnalyzer.analyzeChunk(fullContext)
            isAnalyzing = false
            checkCallWarning(riskReport)
        }
    }

    private fun stopDetection() {
        if (isDetecting || conversationHistory.isNotEmpty()) {
            val finalScore = riskReport.riskScore
            val signals = riskReport.topSignals.joinToString(", ")
            val summary = transcriptBuffer.toString().trim().take(300)
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
        isDetecting = false
        isAnalyzing = false
        isAgoraMode = false
        audioLevel = 0f
    }

    private fun resetDemo() {
        stopDetection()
        transcript = ""
        transcriptBuffer.clear()
        conversationHistory.clear()
        riskReport = RiskReport()
        errorMessage = null
        semanticAnalyzer.reset()
    }

    private fun simulateScenario(sampleText: String) {
        transcript = sampleText
        conversationHistory.add(0, sampleText)
        transcriptBuffer.append(" ").append(sampleText)
        lifecycleScope.launch {
            isAnalyzing = true
            riskReport = semanticAnalyzer.analyzeChunk(transcriptBuffer.toString())
            isAnalyzing = false
            checkCallWarning(riskReport)
        }
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
private fun CallRiskScreen(
    isDetecting: Boolean,
    isAnalyzing: Boolean,
    isAgoraMode: Boolean,
    agoraCallState: AgoraCallState,
    transcript: String,
    conversationHistory: List<String>,
    riskReport: RiskReport,
    errorMessage: String?,
    selectedLanguage: LanguageMode,
    audioLevel: Float,
    isDemoModePlaying: Boolean = false,
    callerNumber: String = "",
    onLanguageSelect: (LanguageMode) -> Unit,
    onStartSpeaker: () -> Unit,
    onStartAgoraVoip: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit,
    onEndCall: () -> Unit = {},
    onBlockNumber: () -> Unit = {},
    onSimulateScenario: (String) -> Unit,
    onPlayFullDemo: (Int) -> Unit = {}
) {
    val riskColor by animateColorAsState(
        targetValue = when (riskReport.status) {
            RiskStatus.Low -> CyberEmerald
            RiskStatus.Suspicious -> AmberWarning
            RiskStatus.High -> CoralRed
        },
        label = "callRiskColor"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header with Agora Badge
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = CyberEmeraldGlow,
                    shape = CircleShape,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = CyberEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Call Scam Intelligence", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        if (isAgoraMode) "Agora RTC Voice Stream active" else "Real-time Dual-Engine AI analysis",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "⚡ Powered by Agora",
                        color = ElectricIndigo,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Language Mode Filter Chips
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Language:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LanguageMode.values().forEach { lang ->
                        FilterChip(
                            selected = selectedLanguage == lang,
                            onClick = { onLanguageSelect(lang) },
                            label = { Text(lang.label, fontSize = 11.sp) },
                            leadingIcon = if (selectedLanguage == lang) {
                                { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }
                }
            }
        }

        // AI REASONING CARD (Core Deliverable Component)
        item {
            AIReasoningCard(
                report = riskReport,
                rawTranscript = transcript,
                isAnalyzing = isAnalyzing,
                onReset = onReset,
                onEndCall = onEndCall,
                onBlockNumber = onBlockNumber,
                callerNumber = callerNumber
            )
        }

        // High Risk Urgent Banner
        if (riskReport.status == RiskStatus.High) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CoralRed)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = CoralRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "CRITICAL FRAUD THREAT DETECTED",
                                fontWeight = FontWeight.Bold,
                                color = CoralRed,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "• HANG UP THE CALL IMMEDIATELY.\n• Do NOT share OTP, PIN, or banking passwords.\n• Police / CBI / Banks NEVER demand money or digital arrest over phone calls.\n• Do NOT install AnyDesk or screen sharing apps.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = Color(0xFFFECACA)
                        )
                    }
                }
            }
        }

        // Action Buttons (Agora VoIP & Speakerphone AI)
        item {
            if (isDetecting) {
                Button(
                    onClick = onStop,
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CallEnd, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isAgoraMode) "End Agora VoIP Call" else "Stop Live Listening", fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
                        Text("Start Agora VoIP Call", fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                        Text("Speakerphone AI", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live Audio Stream Status
        if (isDetecting) {
            item {
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = CyberEmerald, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (isAgoraMode) "Agora RTC Audio streaming... (${(audioLevel * 100).toInt()}% mic volume)"
                            else "Live acoustic stream listening... (${(audioLevel * 100).toInt()}% mic activity)",
                            color = CyberEmerald,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Raw Transcript Feed Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("Live Utterance Stream", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (transcript.isNotBlank()) "\"$transcript\""
                        else "Awaiting caller speech...",
                        fontWeight = if (transcript.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (transcript.isNotBlank()) CyberEmerald else TextSecondary,
                        fontSize = 13.sp
                    )

                    if (conversationHistory.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text("Conversation Dialogue Log:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextMuted)
                        conversationHistory.take(4).forEach { phrase ->
                            Text("• $phrase", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }
        }

        // Instant Hackathon Demo Simulator
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
                            if (isDemoModePlaying) "🔴 Live Stage Demo Playing (Auto Dialogue)..." else "Instant Stage Demo Triggers",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isDemoModePlaying) CoralRed else TextPrimary
                        )
                    }
                    Text(
                        "Run full multi-turn simulated scam dialogues to demonstrate evolving AI risk score on stage:",
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
                            Text("⚡ Electricity Cut", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}