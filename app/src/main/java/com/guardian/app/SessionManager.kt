package com.guardian.app

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SessionManager {
    private const val PREFS_NAME = "guardian_secure_prefs"
    private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
    private const val KEY_USER_NAME = "key_user_name"
    private const val KEY_USER_EMAIL = "key_user_email"
    private const val KEY_SESSION_TOKEN = "key_session_token"
    private const val KEY_ONBOARDING_DONE = "key_onboarding_done"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = try {
                val masterKey = MasterKey.Builder(context.applicationContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                EncryptedSharedPreferences.create(
                    context.applicationContext,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: Exception) {
                Log.w("GuardianSession", "EncryptedSharedPreferences fallback to standard: ${e.message}")
                context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            }
        }
    }

    fun saveSession(context: Context, name: String, email: String, token: String = "token_${System.currentTimeMillis()}") {
        init(context)
        prefs?.edit()?.apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_SESSION_TOKEN, token)
            apply()
        }
        Log.d("GuardianSession", "Session persisted for user: $name ($email)")
    }

    fun setOnboardingComplete(context: Context, complete: Boolean) {
        init(context)
        prefs?.edit()?.putBoolean(KEY_ONBOARDING_DONE, complete)?.apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        init(context)
        return prefs?.getBoolean(KEY_IS_LOGGED_IN, false) ?: false
    }

    fun isOnboardingComplete(context: Context): Boolean {
        init(context)
        return prefs?.getBoolean(KEY_ONBOARDING_DONE, false) ?: false
    }

    fun getUserName(context: Context): String {
        init(context)
        return prefs?.getString(KEY_USER_NAME, "") ?: ""
    }

    fun getUserEmail(context: Context): String {
        init(context)
        return prefs?.getString(KEY_USER_EMAIL, "") ?: ""
    }

    fun clearSession(context: Context) {
        init(context)
        prefs?.edit()?.clear()?.apply()
        Log.d("GuardianSession", "Session cleared / logged out.")
    }
}
