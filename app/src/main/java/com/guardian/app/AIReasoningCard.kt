package com.guardian.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AIReasoningCard(
    report: RiskReport,
    rawTranscript: String,
    isAnalyzing: Boolean = false,
    onReset: () -> Unit = {},
    onEndCall: (() -> Unit)? = null,
    onBlockNumber: (() -> Unit)? = null,
    callerNumber: String = "",
    modifier: Modifier = Modifier
) {
    var showHindiExplanation by remember { mutableStateOf(false) }

    val riskColor by animateColorAsState(
        targetValue = when (report.status) {
            RiskStatus.Low -> CyberEmerald
            RiskStatus.Suspicious -> AmberWarning
            RiskStatus.High -> CoralRed
        },
        label = "aiRiskColor"
    )

    // Pulsing Animation for Live In-Flight Analysis Feedback
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, riskColor.copy(alpha = 0.6f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ----------------------------------------------------
            // 1. TOP HEADER: Dual-Engine Status & Live Pulsing Indicator
            // ----------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = riskColor.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = null,
                            tint = riskColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(Modifier.width(10.dp))

                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Dual-Engine Semantic AI",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        if (isAnalyzing) {
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CyberEmerald)
                                    .alpha(pulseAlpha)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (report.isOffline) Icons.Default.CloudOff else Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = if (report.isOffline) AmberWarning else CyberEmerald,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (report.isOffline) "Offline Scorer (${report.latencyMs}ms)"
                            else "Gemini 1.5 Flash (${report.latencyMs}ms)",
                            color = if (report.isOffline) AmberWarning else CyberEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Capped Risk Score Badge
                Surface(
                    color = riskColor,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${report.riskScore}%",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // ----------------------------------------------------
            // 2. "WHY THIS SCORE?" TREE BREAKDOWN (For Judges & Transparency)
            // ----------------------------------------------------
            if (report.topSignals.isNotEmpty()) {
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "Why This Score? (Signal Breakdown)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(6.dp))
                        report.topSignals.forEachIndexed { index, signal ->
                            val prefix = if (index == report.topSignals.lastIndex) "└─" else "├─"
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(prefix, color = TextMuted, fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                Spacer(Modifier.width(6.dp))
                                Text(signal.icon, fontSize = 12.sp)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "${signal.title}: ",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    signal.detail,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // 3. COGNITIVE VECTORS BREAKDOWN
            // ----------------------------------------------------
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Pretext Legitimacy
                EngineSignalRow(
                    icon = Icons.Default.AccountBalance,
                    title = "Pretext Legitimacy",
                    detected = report.engines.pretextLegitimacy.detected,
                    detail = if (report.engines.pretextLegitimacy.detected) report.engines.pretextLegitimacy.summary else "No fabricated authority pretext",
                    accentColor = ElectricIndigo
                )

                // Intent & Action Risk
                EngineSignalRow(
                    icon = Icons.Default.Lock,
                    title = "Intent & Action Risk",
                    detected = report.engines.intentRisk.detected,
                    detail = if (report.engines.intentRisk.detected) report.engines.intentRisk.summary else "No credential or financial demand",
                    accentColor = CoralRed
                )

                // Psychological Pressure
                EngineSignalRow(
                    icon = Icons.Default.Timer,
                    title = "Psychological Pressure",
                    detected = report.engines.psychologicalPressure.detected,
                    detail = if (report.engines.psychologicalPressure.detected) report.engines.psychologicalPressure.summary else "Normal conversational pacing",
                    accentColor = AmberWarning
                )
            }

            // ----------------------------------------------------
            // 4. TACTICAL BILINGUAL AI EXPLANATION
            // ----------------------------------------------------
            Surface(
                color = DarkSurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = CyberEmerald, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Tactical AI Explanation", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        Spacer(Modifier.weight(1f))

                        // Language Switcher (EN / HI)
                        Surface(
                            color = DarkSurfaceVariant,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { showHindiExplanation = !showHindiExplanation }
                        ) {
                            Text(
                                if (showHindiExplanation) "🇮🇳 हिंदी" else "🇬🇧 English",
                                color = CyberEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        if (showHindiExplanation) report.explanationHi else report.explanationEn,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ----------------------------------------------------
            // 5. Highlighted Risky Phrases
            // ----------------------------------------------------
            if (report.highlightedPhrases.isNotEmpty()) {
                Column {
                    Text("Highlighted Risky Phrases", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                    Spacer(Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        report.highlightedPhrases.forEach { phrase ->
                            Surface(
                                color = CoralRed.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, CoralRed.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    "⚠️ $phrase",
                                    color = CoralRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // 6. CALL PROTECTION ACTIONS (End Call / Block Number)
            // ----------------------------------------------------
            val context = androidx.compose.ui.platform.LocalContext.current
            val canEndCall = com.guardian.app.callprotect.CallActionHelper.canEndCall(context)
            val isCritical = report.riskScore >= 85

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (canEndCall && onEndCall != null) {
                        Button(
                            onClick = onEndCall,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCritical) CoralRed else Color(0xFF7F1D1D),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .then(if (isCritical) Modifier.alpha(pulseAlpha) else Modifier)
                        ) {
                            Icon(
                                Icons.Default.WarningAmber,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("END CALL", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    } else if (onEndCall != null) {
                        Surface(
                            color = DarkSurfaceElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CoralRed.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                                Text(
                                    "Tap phone's end button to stop call",
                                    color = CoralRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    if (onBlockNumber != null) {
                        OutlinedButton(
                            onClick = onBlockNumber,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberWarning),
                            border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("BLOCK NUMBER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // 7. Reset Demo Button & Disclaimer
            // ----------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onReset,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reset Demo", fontSize = 10.sp, color = TextSecondary)
                }

                Spacer(Modifier.width(10.dp))

                Text(
                    "Heuristic AI assistant. Always verify with official channels. Audio in-memory only.",
                    color = TextMuted,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EngineSignalRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    detected: Boolean,
    detail: String,
    accentColor: Color
) {
    Surface(
        color = if (detected) accentColor.copy(alpha = 0.12f) else DarkSurfaceElevated,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (detected) accentColor.copy(alpha = 0.4f) else BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (detected) accentColor else DarkSurfaceVariant,
                shape = CircleShape,
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (detected) Color.White else TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (detected) accentColor else TextPrimary
                )
                Text(
                    detail,
                    fontSize = 11.sp,
                    color = if (detected) TextPrimary else TextSecondary,
                    lineHeight = 14.sp
                )
            }

            if (detected) {
                Surface(
                    color = accentColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "FLAGGED",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
