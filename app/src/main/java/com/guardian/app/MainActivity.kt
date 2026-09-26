package com.guardian.app

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    private val protectionPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.READ_PHONE_STATE] == true) {
                try {
                    startService(Intent(this, CallMonitorService::class.java))
                } catch (_: Exception) {}
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GuardianTheme {
                GuardianApp(
                    onOpenCallRisk = {
                        startActivity(Intent(this, CallRiskActivity::class.java))
                    },
                    onOpenQrScanner = {
                        startActivity(Intent(this, QrScannerActivity::class.java))
                    },
                    onPhoneProtectionToggle = { enabled ->
                        if (enabled) {
                            protectionPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                            )
                        } else {
                            try {
                                stopService(Intent(this, CallMonitorService::class.java))
                            } catch (_: Exception) {}
                        }
                    },
                    onMessageProtectionToggle = { enabled ->
                        if (enabled) {
                            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        } else {
                            try {
                                stopService(Intent(this, MessageListenerService::class.java))
                            } catch (_: Exception) {}
                        }
                    }
                )
            }
        }
    }
}

// Sophisticated Obsidian Cyber Palette (Pleasing, Modern, Non-Generic)
val DarkBackground = Color(0xFF090D16)
val DarkSurface = Color(0xFF111726)
val DarkSurfaceElevated = Color(0xFF192237)
val DarkSurfaceVariant = Color(0xFF222F4C)
val CyberEmerald = Color(0xFF10B981)
val CyberEmeraldGlow = Color(0xFF064E3B)
val ElectricIndigo = Color(0xFF6366F1)
val CoralRed = Color(0xFFEF4444)
val AmberWarning = Color(0xFFF59E0B)
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)
val BorderSubtle = Color(0xFF1E293B)

@Composable
fun GuardianTheme(content: @Composable () -> Unit) {
    val darkColors = darkColorScheme(
        primary = CyberEmerald,
        onPrimary = Color(0xFF022C22),
        primaryContainer = CyberEmeraldGlow,
        onPrimaryContainer = Color(0xFFA7F3D0),
        secondary = ElectricIndigo,
        onSecondary = Color.White,
        background = DarkBackground,
        surface = DarkSurface,
        surfaceVariant = DarkSurfaceVariant,
        onSurface = TextPrimary,
        onSurfaceVariant = TextSecondary,
        error = CoralRed,
        errorContainer = Color(0xFF450A0A),
        onErrorContainer = Color(0xFFFECACA),
        outline = BorderSubtle
    )

    MaterialTheme(
        colorScheme = darkColors,
        content = content
    )
}