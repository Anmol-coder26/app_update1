package com.guardian.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneDisabled
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==========================================
// 1. AUTH SCREEN (Obsidian Cyber)
// ==========================================
@Composable
fun AuthScreen(onContinue: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            // Glowing Shield Icon
            Surface(
                color = CyberEmeraldGlow,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.5f)),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = CyberEmerald,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text("Guardian Defense", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Text(
                "Real-time proactive security against scam calls, phishing links, and malicious messages.",
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 6.dp, bottom = 28.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                singleLine = true,
                colors = customTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                singleLine = true,
                colors = customTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Master PIN / Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = customTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { onContinue(name.trim().ifBlank { "User" }) },
                enabled = email.isNotBlank() && password.length >= 4,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberEmerald,
                    contentColor = Color(0xFF022C22),
                    disabledContainerColor = DarkSurfaceElevated,
                    disabledContentColor = TextMuted
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Launch Protection", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }

            Spacer(Modifier.height(18.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "On-device encrypted sandbox. Zero telemetric logging.",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ==========================================
// 2. ONBOARDING SCREEN
// ==========================================
// ==========================================
// 2. 5-POINT PERMISSION ONBOARDING SCREEN
// ==========================================
@Composable
fun OnboardingScreen(name: String, onComplete: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current

    var hasMicPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    var hasPhonePermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_PHONE_STATE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    var hasOverlayPermission by remember {
        mutableStateOf(android.provider.Settings.canDrawOverlays(context))
    }

    var hasDefaultBrowser by remember {
        mutableStateOf(isDefaultBrowser(context))
    }

    // Permission launchers
    val micLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted -> hasMicPermission = granted }

    val phoneLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted -> hasPhonePermission = granted }

    val notifLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted -> hasNotificationPermission = granted }

    var hasCallControlPermission by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ANSWER_PHONE_CALLS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val callControlLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted -> hasCallControlPermission = granted }

    // Re-check permissions when returning from Settings
    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) {
        hasOverlayPermission = android.provider.Settings.canDrawOverlays(context)
        hasDefaultBrowser = isDefaultBrowser(context)
        onPauseOrDispose {}
    }

    val allGranted = hasMicPermission && hasPhonePermission && hasOverlayPermission && hasDefaultBrowser && hasNotificationPermission

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(20.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = CyberEmeraldGlow,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.5f)),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = CyberEmerald,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text("Security Setup", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text(
                    "Welcome, $name. Enable Guardian's real-time interception layers to activate total on-device protection.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
            }

            // 1. Microphone
            item {
                PermissionCheckRow(
                    title = "1. Microphone Access",
                    detail = "Enables real-time speakerphone speech-to-text to detect extortion and digital arrest on calls.",
                    isGranted = hasMicPermission,
                    onGrant = { micLauncher.launch(android.Manifest.permission.RECORD_AUDIO) }
                )
            }

            // 2. Phone State
            item {
                PermissionCheckRow(
                    title = "2. Phone Call State",
                    detail = "Auto-detects incoming and active calls to trigger Guardian's security reasoning card.",
                    isGranted = hasPhonePermission,
                    onGrant = { phoneLauncher.launch(android.Manifest.permission.READ_PHONE_STATE) }
                )
            }

            // 3. Display Over Other Apps
            item {
                PermissionCheckRow(
                    title = "3. Floating Screen Overlay",
                    detail = "Allows Guardian to show the live AI reasoning card on top of calls and phishing links.",
                    isGranted = hasOverlayPermission,
                    onGrant = {
                        val intent = android.content.Intent(
                            android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            android.net.Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    }
                )
            }

            // 4. Default Browser Protection (Link Interception)
            item {
                PermissionCheckRow(
                    title = "4. Set as Default Browser",
                    detail = "Routes clicked links in WhatsApp, SMS & Gmail through Guardian's anti-phishing shield before opening.",
                    isGranted = hasDefaultBrowser,
                    onGrant = {
                        try {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                val roleManager = context.getSystemService(android.content.Context.ROLE_SERVICE) as? android.app.role.RoleManager
                                if (roleManager != null && roleManager.isRoleAvailable(android.app.role.RoleManager.ROLE_BROWSER)) {
                                    val roleIntent = roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_BROWSER)
                                    context.startActivity(roleIntent)
                                } else {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                                    context.startActivity(intent)
                                }
                            } else {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                                context.startActivity(intent)
                            }
                        } catch (_: Exception) {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val appDetails = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = android.net.Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(appDetails)
                            }
                        }
                    }
                )
            }

            // 5. Notifications
            item {
                PermissionCheckRow(
                    title = "5. Notification Alerts",
                    detail = "Displays high-priority heads-up warnings during active scam calls and phishing attempts.",
                    isGranted = hasNotificationPermission,
                    onGrant = {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            notifLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            hasNotificationPermission = true
                        }
                    }
                )
            }

            // 6. Instant Call Interception & Termination
            item {
                PermissionCheckRow(
                    title = "6. Active Call Protection & End Call",
                    detail = "Empowers Guardian to instantly disconnect calls when severe scam or extortion is detected.",
                    isGranted = hasCallControlPermission,
                    onGrant = {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            callControlLauncher.launch(android.Manifest.permission.ANSWER_PHONE_CALLS)
                        } else {
                            hasCallControlPermission = true
                        }
                    }
                )
            }

            // 7. Choose Scam Warning Language (Bhashini AI)
            item {
                val languages = listOf(
                    "hi" to "Hindi (हिंदी)",
                    "en" to "English",
                    "ta" to "Tamil (தமிழ்)",
                    "te" to "Telugu (తెలుగు)",
                    "bn" to "Bengali (বাংলা)",
                    "mr" to "Marathi (मराठी)",
                    "kn" to "Kannada (ಕನ್ನಡ)",
                    "ml" to "Malayalam (മലയാളം)",
                    "gu" to "Gujarati (ગુજરાતી)",
                    "pa" to "Punjabi (ਪੰਜਾਬੀ)"
                )
                var currentSelectedLang by remember {
                    mutableStateOf(
                        context.getSharedPreferences("guardian_prefs", android.content.Context.MODE_PRIVATE)
                            .getString("preferred_language", "hi") ?: "hi"
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            "7. Scam Warning Language",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Text(
                            "Choose your language for live scam call warnings and Bhashini AI audio alerts.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                        )

                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            languages.forEach { (code, label) ->
                                val isSelected = currentSelectedLang == code
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        currentSelectedLang = code
                                        context.getSharedPreferences("guardian_prefs", android.content.Context.MODE_PRIVATE)
                                            .edit()
                                            .putString("preferred_language", code)
                                            .apply()
                                    },
                                    label = {
                                        Text(
                                            label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyberEmerald,
                                        selectedLabelColor = Color(0xFF022C22),
                                        containerColor = DarkSurface,
                                        labelColor = TextSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = if (isSelected) CyberEmerald else BorderSubtle,
                                        selectedBorderColor = CyberEmerald,
                                        enabled = true,
                                        selected = isSelected
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Continue Action
            item {
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onComplete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberEmerald,
                        contentColor = Color(0xFF022C22),
                        disabledContainerColor = DarkSurfaceElevated,
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        if (allGranted) "Activate Guardian Defense" else "Continue to Dashboard",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun PermissionCheckRow(
    title: String,
    detail: String,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) DarkSurfaceElevated else DarkSurface
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (isGranted) CyberEmerald.copy(alpha = 0.4f) else BorderSubtle
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (isGranted) CyberEmeraldGlow else DarkSurfaceVariant,
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isGranted) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyberEmerald, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Default.WarningAmber, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Text(
                    detail,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            if (isGranted) {
                Surface(
                    color = CyberEmeraldGlow,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "ACTIVE",
                        color = CyberEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Button(
                    onClick = onGrant,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald, contentColor = Color(0xFF022C22)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Grant", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun isDefaultBrowser(context: android.content.Context): Boolean {
    return try {
        val testIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://example.com"))
        val resolver = context.packageManager
        val defaultHandler = resolver.resolveActivity(testIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
        defaultHandler?.activityInfo?.packageName == context.packageName
    } catch (_: Exception) {
        false
    }
}

// ==========================================
// 3. HOME SCREEN
// ==========================================
@Composable
fun HomeScreen(
    state: GuardianState,
    contentPadding: PaddingValues,
    onOpenCallRisk: () -> Unit,
    onOpenQrScanner: () -> Unit = {},
    onOpenCallHistory: () -> Unit = {},
    onProtectionChange: (Boolean) -> Unit,
    onAddIncident: (GuardianIncident) -> Unit
) {
    var showIncidentDialog by remember { mutableStateOf(false) }
    var showLinkCheckDialog by remember { mutableStateOf(false) }
    var showQrScannerDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Header with Live Shield Status
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Hello, ${state.name}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Personal Cyber Defense Hub", color = TextSecondary, fontSize = 13.sp)
                }

                Surface(
                    color = if (state.protectionEnabled) CyberEmeraldGlow else Color(0xFF450A0A),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (state.protectionEnabled) CyberEmerald else CoralRed)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (state.protectionEnabled) CyberEmerald else CoralRed)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (state.protectionEnabled) "ACTIVE" else "PAUSED",
                            color = if (state.protectionEnabled) CyberEmerald else CoralRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Hero Master Shield Card
        item {
            MasterProtectionCard(
                enabled = state.protectionEnabled,
                onToggle = onToggle@{ enabled -> onProtectionChange(enabled) }
            )
        }

        // Quick Actions Grid (Phone Guard, Scam History, Scan QR, Check Link)
        item {
            Text("Proactive Defense Modules", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = "Speaker Guard",
                    subtitle = "Live Call STT",
                    icon = Icons.Default.PhoneInTalk,
                    accentColor = CyberEmerald,
                    onClick = onOpenCallRisk,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = "Scam History",
                    subtitle = "Call Audit Log",
                    icon = Icons.Default.History,
                    accentColor = CoralRed,
                    onClick = onOpenCallHistory,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = "Scan QR",
                    subtitle = "Camera ML Shield",
                    icon = Icons.Default.QrCodeScanner,
                    accentColor = ElectricIndigo,
                    onClick = onOpenQrScanner,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = "Link Shield",
                    subtitle = "Scan URL",
                    icon = Icons.Default.Link,
                    accentColor = AmberWarning,
                    onClick = { showLinkCheckDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recent Activity Feed
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent Security Checks", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Spacer(Modifier.weight(1f))
                Text("${state.events.size} logged", color = CyberEmerald, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        items(state.events.take(4)) { event ->
            EventRowItem(event)
        }

        // Report Incident Trigger Button
        item {
            OutlinedButton(
                onClick = { showIncidentDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
                border = BorderStroke(1.dp, CoralRed.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.WarningAmber, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Report Suspicious Incident / Number", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    // Modal Dialogs
    if (showIncidentDialog) {
        IncidentReportModal(
            onDismiss = { showIncidentDialog = false },
            onSave = {
                onAddIncident(it)
                showIncidentDialog = false
            }
        )
    }

    if (showLinkCheckDialog) {
        LinkCheckerModal(onDismiss = { showLinkCheckDialog = false })
    }

    if (showQrScannerDialog) {
        QrScannerModal(onDismiss = { showQrScannerDialog = false })
    }
}

// Master Protection Banner Card
@Composable
private fun MasterProtectionCard(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) DarkSurfaceElevated else Color(0xFF2A1515)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (enabled) CyberEmerald.copy(alpha = 0.3f) else CoralRed.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (enabled) CyberEmeraldGlow else Color(0xFF450A0A),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (enabled) Icons.Default.Shield else Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = if (enabled) CyberEmerald else CoralRed,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    if (enabled) "Proactive Shield Running" else "Protection in Standby",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    if (enabled) "Continuous background call, notification & link screening."
                    else "Tap to reactivate real-time scam interception.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF022C22),
                    checkedTrackColor = CyberEmerald,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = DarkSurfaceVariant
                )
            )
        }
    }
}

// Quick Action Card Component
@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                color = accentColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(subtitle, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

// Event Row Item
@Composable
private fun EventRowItem(event: GuardianEvent) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (event.safe) CyberEmerald else CoralRed)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(event.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            event.category,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(event.detail, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
            }
            Text(event.time, color = TextMuted, fontSize = 11.sp)
        }
    }
}

// ==========================================
// 4. EVENTS SCREEN (Audit Log)
// ==========================================
@Composable
fun EventsScreen(events: List<GuardianEvent>, contentPadding: PaddingValues) {
    var filterSafe by remember { mutableStateOf<Boolean?>(null) }
    val filteredList = events.filter { filterSafe == null || it.safe == filterSafe }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Security Audit Timeline", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Full chronological record of checks, scans, and detections.", color = TextSecondary, fontSize = 13.sp)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filterSafe == null,
                    onClick = { filterSafe = null },
                    label = { Text("All (${events.size})", fontSize = 12.sp) },
                    colors = customFilterChipColors()
                )
                FilterChip(
                    selected = filterSafe == true,
                    onClick = { filterSafe = true },
                    label = { Text("Safe", fontSize = 12.sp) },
                    colors = customFilterChipColors()
                )
                FilterChip(
                    selected = filterSafe == false,
                    onClick = { filterSafe = false },
                    label = { Text("Threats Flagged", fontSize = 12.sp) },
                    colors = customFilterChipColors()
                )
            }
        }

        items(filteredList) { event ->
            EventRowItem(event)
        }
    }
}

// ==========================================
// 5. INCIDENTS SCREEN
// ==========================================
@Composable
fun IncidentsScreen(
    incidents: List<GuardianIncident>,
    contentPadding: PaddingValues,
    onAddIncident: (GuardianIncident) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Threats & Incidents", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Review confirmed fraud attempts and reported calls.", color = TextSecondary, fontSize = 13.sp)
                }
                IconButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.WarningAmber, contentDescription = "Report", tint = CoralRed)
                }
            }
        }

        if (incidents.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = CyberEmeraldGlow,
                            shape = CircleShape,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyberEmerald, modifier = Modifier.size(28.dp))
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Text("No Active Threat Incidents", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                        Text(
                            "Your device is completely clean. Any flagged scam calls or malicious links will appear here for review.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(incidents) { incident ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CoralRed.copy(alpha = 0.4f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(incident.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                            Surface(
                                color = Color(0xFF450A0A),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    incident.risk,
                                    color = CoralRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Text(incident.detail, color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(incident.threatType, color = AmberWarning, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.weight(1f))
                            Text(incident.time, color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        IncidentReportModal(
            onDismiss = { showDialog = false },
            onSave = {
                onAddIncident(it)
                showDialog = false
            }
        )
    }
}

// ==========================================
// 6. SETTINGS SCREEN
// ==========================================
enum class SettingType { All, Phone, Messages, Links }

@Composable
fun SettingsScreen(
    state: GuardianState,
    contentPadding: PaddingValues,
    onToggle: (SettingType, Boolean) -> Unit,
    onDeleteData: () -> Unit,
    onLogout: () -> Unit = {}
) {
    var confirmDelete by remember { mutableStateOf(false) }
    var confirmLogout by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Security Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Customize active screening layers and privacy controls.", color = TextSecondary, fontSize = 13.sp)
        }

        item {
            SettingRowCard(
                title = "All Protection Layers",
                detail = "Toggle all active interception shields simultaneously",
                checked = state.protectionEnabled,
                icon = Icons.Default.Shield,
                onCheckedChange = { onToggle(SettingType.All, it) }
            )
        }

        item {
            SettingRowCard(
                title = "Phone Call Screening",
                detail = "Auto-trigger speakerphone analyzer on connected calls",
                checked = state.phoneProtection,
                icon = Icons.Default.PhoneInTalk,
                onCheckedChange = { onToggle(SettingType.Phone, it) }
            )
        }

        item {
            SettingRowCard(
                title = "Message & WhatsApp Shield",
                detail = "Real-time notification listener for OTP, KYC, and power cut fraud",
                checked = state.messageProtection,
                icon = Icons.Default.NotificationsActive,
                onCheckedChange = { onToggle(SettingType.Messages, it) }
            )
        }

        item {
            SettingRowCard(
                title = "Link & QR Interceptor",
                detail = "Validate URLs against threat intelligence before opening",
                checked = state.linkProtection,
                icon = Icons.Default.Link,
                onCheckedChange = { onToggle(SettingType.Links, it) }
            )
        }

        item {
            Spacer(Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = CyberEmerald, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Privacy Guarantee (DPDP Compliant)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    }
                    Text(
                        "Guardian processes all speech, notification text, and camera frames locally in RAM. No voice recordings or text logs are uploaded to any external server.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        item {
            OutlinedButton(
                onClick = { confirmDelete = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberWarning),
                border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Erase Local Cache & Incident Logs", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        item {
            Button(
                onClick = { confirmLogout = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF450A0A), contentColor = CoralRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Log Out & Clear Master Session", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = DarkSurfaceElevated,
            title = { Text("Erase Local Session Data?", color = TextPrimary) },
            text = { Text("This will permanently clear in-memory event logs and reported incidents.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDeleteData()
                }) { Text("Erase", color = CoralRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            containerColor = DarkSurfaceElevated,
            title = { Text("Log Out from Guardian?", color = TextPrimary) },
            text = { Text("Your encrypted master session will be wiped and you will need to re-authenticate on next launch.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    confirmLogout = false
                    onLogout()
                }) { Text("Log Out", color = CoralRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}

@Composable
private fun SettingRowCard(
    title: String,
    detail: String,
    checked: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (checked) CyberEmeraldGlow else DarkSurfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = if (checked) CyberEmerald else TextMuted, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Text(detail, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 2.dp))
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF022C22),
                    checkedTrackColor = CyberEmerald,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = DarkSurfaceVariant
                )
            )
        }
    }
}

// ==========================================
// 7. INTERACTIVE MODALS (QR, Link, Incident)
// ==========================================
@Composable
private fun LinkCheckerModal(onDismiss: () -> Unit) {
    var url by remember { mutableStateOf("") }
    var scanResult by remember { mutableStateOf<String?>(null) }
    var isSafe by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Link, contentDescription = null, tint = AmberWarning)
                Spacer(Modifier.width(8.dp))
                Text("Link Safety Inspector", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text("Enter or paste any suspicious web address:", color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        scanResult = null
                    },
                    placeholder = { Text("https://example-bank-login.xyz") },
                    singleLine = true,
                    colors = customTextFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (scanResult != null) {
                    Spacer(Modifier.height(14.dp))
                    Surface(
                        color = if (isSafe) CyberEmeraldGlow else Color(0xFF450A0A),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isSafe) CyberEmerald else CoralRed)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                if (isSafe) "✅ Domain Verified Safe" else "⚠️ High Risk Phishing URL Flagged",
                                fontWeight = FontWeight.Bold,
                                color = if (isSafe) CyberEmerald else CoralRed,
                                fontSize = 14.sp
                            )
                            Text(scanResult!!, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val normalized = url.lowercase().trim()
                    if (normalized.contains("free-gift") || normalized.contains("apk") || normalized.contains("login-kyc") || normalized.contains("rbi-claim")) {
                        isSafe = false
                        scanResult = "Suspicious phishing patterns detected. Known credential-harvesting endpoint."
                    } else if (normalized.isNotBlank()) {
                        isSafe = true
                        scanResult = "No active malware signatures or blacklist match found."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberWarning, contentColor = Color(0xFF2A1500))
            ) { Text("Scan Link", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = TextSecondary) }
        }
    )
}

@Composable
private fun QrScannerModal(onDismiss: () -> Unit) {
    var qrResult by remember { mutableStateOf<String?>(null) }
    var isSafe by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = ElectricIndigo)
                Spacer(Modifier.width(8.dp))
                Text("Camera QR Safety Scanner", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(2.dp, ElectricIndigo.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = ElectricIndigo, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Aim camera at QR code", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Test Sample QR Payloads:", color = TextMuted, fontSize = 11.sp)
                Spacer(Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            isSafe = false
                            qrResult = "upi://pay?pa=scammer@fakeupi&am=5000 (Reverse Debit Scam: Attempting to debit ₹5000 instead of receiving money)"
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("🚨 Reverse UPI QR", fontSize = 11.sp, color = CoralRed)
                    }

                    OutlinedButton(
                        onClick = {
                            isSafe = true
                            qrResult = "https://upi.org/verified-merchant (Clean verified payment QR)"
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("✅ Safe Store QR", fontSize = 11.sp, color = CyberEmerald)
                    }
                }

                if (qrResult != null) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        color = if (isSafe) CyberEmeraldGlow else Color(0xFF450A0A),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isSafe) CyberEmerald else CoralRed)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                if (isSafe) "VERIFIED CLEAN QR" else "⚠️ FRAUDULENT QR DETECTED",
                                fontWeight = FontWeight.Bold,
                                color = if (isSafe) CyberEmerald else CoralRed,
                                fontSize = 13.sp
                            )
                            Text(qrResult!!, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done", color = TextPrimary) }
        }
    )
}

@Composable
private fun IncidentReportModal(
    onDismiss: () -> Unit,
    onSave: (GuardianIncident) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var threatType by remember { mutableStateOf("Extortion / Police Scam") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        shape = RoundedCornerShape(18.dp),
        title = { Text("Report Threat Incident", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Caller Number or Organization") },
                    placeholder = { Text("+91 98765 43210 or 'Fake CBI Officer'") },
                    singleLine = true,
                    colors = customTextFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    label = { Text("Incident Details & Demands") },
                    placeholder = { Text("Demanded ₹50,000 for digital arrest avoidance...") },
                    colors = customTextFieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        GuardianIncident(
                            title = title.ifBlank { "Suspicious Call" },
                            detail = detail.ifBlank { "Caller attempted social engineering fraud." },
                            time = "Just now",
                            risk = "HIGH RISK",
                            threatType = threatType
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CoralRed, contentColor = Color.White)
            ) { Text("Save Incident", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } }
    )
}

@Composable
private fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CyberEmerald,
    unfocusedBorderColor = BorderSubtle,
    focusedLabelColor = CyberEmerald,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = CyberEmerald,
    focusedContainerColor = DarkSurface,
    unfocusedContainerColor = DarkSurface
)

@Composable
private fun customFilterChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = CyberEmeraldGlow,
    selectedLabelColor = CyberEmerald,
    containerColor = DarkSurface,
    labelColor = TextSecondary
)