package com.guardian.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat

internal object GuardianNotifications {
    private const val channelId = "guardian_protection"

    fun warn(context: Context, title: String, message: String) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                channelId,
                "Guardian protection",
                NotificationManager.IMPORTANCE_HIGH
            )
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify((title + message).hashCode(), notification)
    }

    fun callConnected(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                channelId,
                "Guardian protection",
                NotificationManager.IMPORTANCE_HIGH
            )
        )
        val pendingIntent = PendingIntent.getActivity(
            context,
            420,
            Intent(context, CallRiskActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Call connected")
            .setContentText("Put the call on speaker to check it with Guardian.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Put the call on speaker, then open Guardian to analyze the conversation. " +
                        "Guardian does not record or save call audio."
                )
            )
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_media_play,
                "Analyze call",
                pendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify("call-connected".hashCode(), notification)
    }
}

class CallMonitorService : Service() {
    private val callListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    GuardianNotifications.warn(
                        this@CallMonitorService,
                        "Incoming call to review",
                        "Guardian noticed an incoming call. Verify the caller before sharing information."
                    )
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    GuardianNotifications.callConnected(this@CallMonitorService)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val telephony = getSystemService(TelephonyManager::class.java)
        if (checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            telephony.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
        START_STICKY

    override fun onDestroy() {
        getSystemService(TelephonyManager::class.java)
            .listen(callListener, PhoneStateListener.LISTEN_NONE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

class MessageListenerService : NotificationListenerService() {
    override fun onNotificationPosted(statusBarNotification: StatusBarNotification) {
        if (statusBarNotification.packageName == packageName) return
        val extras = statusBarNotification.notification.extras
        val title = extras.getCharSequence("android.title")?.toString().orEmpty()
        val text = extras.getCharSequence("android.text")?.toString().orEmpty()
        val message = "$title $text".trim()

        if (SuspiciousMessageDetector.isSuspicious(message)) {
            Log.w("GuardianSMS", "Suspicious message notification intercepted from ${statusBarNotification.packageName}: '$message'")
            try {
                // Cancel original message notification
                val notificationKey = statusBarNotification.key
                cancelNotification(notificationKey)

                val originalIntent = statusBarNotification.notification.contentIntent
                val guardianIntent = Intent(this, ScamWarningActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("source", statusBarNotification.packageName)
                    putExtra("title", title.ifBlank { "Suspicious SMS / Notification" })
                    putExtra("body", text.ifBlank { message })
                    putExtra("score", 85)
                    putExtra("explanation", "Guardian detected suspicious urgent keywords or masked link verification requests in this notification.")
                    putExtra("original_pending_intent", originalIntent)
                }

                val builder = NotificationCompat.Builder(this, "guardian_protection")
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle("⚠️ Guardian: Suspicious message detected")
                    .setContentText(message.take(80))
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(
                        PendingIntent.getActivity(
                            this,
                            (0..10000).random(),
                            guardianIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    )

                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(
                    statusBarNotification.id + 100000,
                    builder.build()
                )
            } catch (e: Exception) {
                Log.e("GuardianSMS", "Failed to intercept SMS notification: ${e.message}", e)
                GuardianNotifications.warn(
                    this,
                    "Suspicious SMS intercepted",
                    "Notification contained urgent scam indicators: $message"
                )
            }
        }
    }
}

private object SuspiciousMessageDetector {
    private val warningTerms = listOf(
        // Credentials / OTP
        "otp", "one time password", "verification code", "cvv", "security pin", "password", "ओटीपी",
        // Banking & KYC
        "verify your account", "kyc", "kyc update", "kyc expire", "pan card", "blocked account",
        "account suspended", "debit card block", "khata block", "rbi", "sbi", "hdfc", "icici",
        // Electricity & Utility
        "electricity", "power cut", "bijli bill", "bill unpaid", "disconnection", "bijli cut", "bijli vibhag",
        // Urgency & Law Enforcement
        "urgent", "arrest", "police", "cbi", "fir registered", "digital arrest", "legal action", "immediately",
        // Payments & Rewards
        "upi", "send money", "transfer money", "claim reward", "lottery", "cashback", "you have won",
        "telegram task", "youtube like", "part time job", "kbc",
        // Remote access & malicious downloads
        "click this link", "remote access", "anydesk", "teamviewer", "rustdesk", "quicksupport", "download apk"
    )

    fun isSuspicious(message: String): Boolean {
        val normalized = message.lowercase()
        return warningTerms.any(normalized::contains) ||
            Regex("""https?://\S+""").containsMatchIn(normalized)
    }
}