package com.example.notifications

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object FcmTokenManager {

    private const val TAG = "FcmTokenManager"
    private const val PREFS_NAME = "fcm_prefs"
    private const val KEY_FCM_TOKEN = "cached_fcm_token"

    private var appContext: Context? = null
    private var sharedPrefs: SharedPreferences? = null

    private val _fcmToken = MutableStateFlow<String?>(null)
    val fcmToken: StateFlow<String?> = _fcmToken.asStateFlow()

    fun init(context: Context) {
        val app = context.applicationContext
        appContext = app
        sharedPrefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val cached = sharedPrefs?.getString(KEY_FCM_TOKEN, null)
        if (!cached.isNullOrBlank()) {
            _fcmToken.value = cached
        }

        NotificationHelper.createNotificationChannels(app)
        fetchToken()
    }

    fun saveToken(token: String) {
        _fcmToken.value = token
        sharedPrefs?.edit()?.putString(KEY_FCM_TOKEN, token)?.apply()
        Log.d(TAG, "FCM Token saved: $token")
    }

    fun getSavedToken(): String? {
        return _fcmToken.value ?: sharedPrefs?.getString(KEY_FCM_TOKEN, null)
    }

    fun fetchToken(onComplete: ((String?) -> Unit)? = null) {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    if (!token.isNullOrBlank()) {
                        saveToken(token)
                        Log.i(TAG, "Successfully retrieved FCM registration token: $token")
                        onComplete?.invoke(token)
                        return@addOnCompleteListener
                    }
                } else {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                }
                onComplete?.invoke(_fcmToken.value)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obtaining FirebaseMessaging token: ${e.message}")
            onComplete?.invoke(_fcmToken.value)
        }
    }

    fun subscribeToUserTopic(userId: String) {
        if (userId.isBlank()) return
        try {
            val sanitized = userId.replace(Regex("[^a-zA-Z0-9-_.~%]"), "_")
            FirebaseMessaging.getInstance().subscribeToTopic("user_$sanitized")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Subscribed to topic: user_$sanitized")
                    }
                }
            FirebaseMessaging.getInstance().subscribeToTopic("transactions")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to subscribe to topic: ${e.message}")
        }
    }

    fun subscribeToAdminAlerts() {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("admin_alerts")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Successfully subscribed to admin_alerts topic")
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to subscribe to admin_alerts: ${e.message}")
        }
    }

    fun unsubscribeFromAdminAlerts() {
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("admin_alerts")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Successfully unsubscribed from admin_alerts topic")
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unsubscribe from admin_alerts: ${e.message}")
        }
    }

    fun sendTestPushNotification(
        context: Context,
        title: String = "Deposit Approved! ✅",
        message: String = "Your deposit of PKR 5,000 via JazzCash has been approved.",
        type: String = "DEPOSIT",
        status: String = "APPROVED"
    ) {
        NotificationHelper.showTransactionNotification(
            context = context,
            title = title,
            message = message,
            txId = "TXN-${System.currentTimeMillis() % 10000}",
            txType = type,
            status = status
        )
    }
}
