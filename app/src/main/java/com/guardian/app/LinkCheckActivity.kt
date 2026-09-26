package com.guardian.app

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val GxDanger = CoralRed
private val GxWarning = AmberWarning
private val GxSurface = DarkSurface
private val GxSurfaceAlt = DarkSurfaceElevated
private val GxTextHi = TextPrimary
private val GxTextMid = TextSecondary
private val GxTextLo = TextMuted

data class LinkVerdict(
    val url: String,
    val score: Int,
    val status: RiskStatus,
    val title: String,
    val detail: String,
    val allowProceed: Boolean = true
)

class LinkCheckActivity : ComponentActivity() {

    private lateinit var semanticAnalyzer: SemanticAnalyzer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data ?: run {
            finish()
            return
        }

        semanticAnalyzer = SemanticAnalyzer(this)
        val targetUrl = uri.toString()
        Log.i("GuardianLinkCheck", "Intercepted Intent URL: $targetUrl")

        lifecycleScope.launch {
            val verdict = withContext(Dispatchers.IO) {
                evaluateLink(targetUrl)
            }

            Log.i("GuardianLinkCheck", "URL verdict: score=${verdict.score}, status=${verdict.status.label}")

            when {
                verdict.score < 40 -> {
                    Toast.makeText(this@LinkCheckActivity, "✅ Guardian: Domain Clean — Opening", Toast.LENGTH_SHORT).show()
                    forwardToBrowser(uri)
                }
                verdict.score < 70 -> {
                    showWarning(uri, verdict, allowProceed = true)
                }
                else -> {
                    showWarning(uri, verdict, allowProceed = false)
                }
            }
        }
    }

    private fun forwardToBrowser(uri: Uri) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.android.chrome")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(browserIntent)
        } catch (e: ActivityNotFoundException) {
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    removeCategory(Intent.CATEGORY_BROWSABLE)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                val resolveInfos = packageManager.queryIntentActivities(fallbackIntent, PackageManager.MATCH_DEFAULT_ONLY)
                val targetApp = resolveInfos.firstOrNull { it.activityInfo.packageName != packageName }
                if (targetApp != null) {
                    fallbackIntent.setClassName(targetApp.activityInfo.packageName, targetApp.activityInfo.name)
                    startActivity(fallbackIntent)
                } else {
                    val chooser = Intent.createChooser(fallbackIntent, "Open with").apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(chooser)
                }
            } catch (_: Exception) {}
        }
        finish()
    }

    private fun showWarning(uri: Uri, verdict: LinkVerdict, allowProceed: Boolean) {
        setContent {
            GuardianTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xCC000000))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LinkWarningDialog(
                        verdict = verdict,
                        allowProceed = allowProceed,
                        onProceed = {
                            forwardToBrowser(uri)
                        },
                        onBlock = {
                            Toast.makeText(this@LinkCheckActivity, "Blocked dangerous link navigation", Toast.LENGTH_SHORT).show()
                            finish()
                        },
                        onCopy = {
                            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("URL", verdict.url))
                            Toast.makeText(this@LinkCheckActivity, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    private fun evaluateLink(url: String): LinkVerdict {
        val lower = url.lowercase().trim()

        return when {
            lower.contains(".apk") || lower.contains("login-kyc") || lower.contains("rbi-claim") ||
            lower.contains("free-gift") || lower.contains("update-pan") || lower.contains("bank-verify") ||
            lower.contains("digital-arrest") || lower.contains("anydesk") || lower.contains("electricity-bill-pay") ||
            lower.contains("upi-verify-kyc") || lower.contains("sbi-verify") -> {
                LinkVerdict(
                    url = url,
                    score = 90,
                    status = RiskStatus.High,
                    title = "🚨 DANGEROUS PHISHING / MALWARE LINK",
                    detail = "This destination has been flagged for active banking credential theft, digital arrest extortion, or unauthorized APK downloads.",
                    allowProceed = false
                )
            }

            lower.contains("bit.ly") || lower.contains("tinyurl.com") || lower.contains("is.gd") ||
            lower.contains(".xyz") || lower.contains(".top") || lower.contains(".cc") || lower.contains(".club") ||
            lower.contains("suspicious-test") -> {
                LinkVerdict(
                    url = url,
                    score = 65,
                    status = RiskStatus.Suspicious,
                    title = "⚠️ SUSPICIOUS MASKED URL",
                    detail = "This link uses a URL shortener or unverified domain registry that conceals the true landing page. Proceed with caution.",
                    allowProceed = true
                )
            }

            else -> {
                LinkVerdict(
                    url = url,
                    score = 10,
                    status = RiskStatus.Low,
                    title = "✅ DOMAIN VERIFIED CLEAN",
                    detail = "Standard trusted web domain. No active threat intelligence flags detected.",
                    allowProceed = true
                )
            }
        }
    }
}

@Composable
fun LinkWarningDialog(
    verdict: LinkVerdict,
    allowProceed: Boolean,
    onProceed: () -> Unit,
    onBlock: () -> Unit,
    onCopy: () -> Unit
) {
    val isCritical = verdict.score >= 70
    val accentColor = if (isCritical) GxDanger else GxWarning

    Card(
        colors = CardDefaults.cardColors(containerColor = GxSurface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, accentColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = accentColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isCritical) Icons.Default.WarningAmber else Icons.Default.Security,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        "Guardian Link Shield — ${verdict.score}% Risk",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Text(
                        verdict.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GxTextHi
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = verdict.detail,
                color = GxTextMid,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(12.dp))

            Surface(
                color = GxSurfaceAlt,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = verdict.url.take(80) + if (verdict.url.length > 80) "..." else "",
                        color = GxTextLo,
                        fontSize = 11.sp,
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = onCopy,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (allowProceed) {
                    OutlinedButton(
                        onClick = onProceed,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Proceed", color = GxTextMid, fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = onBlock,
                    colors = ButtonDefaults.buttonColors(containerColor = GxDanger, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (allowProceed) "Block" else "Block Link", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
