package com.example.notifications

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import java.util.UUID

object AdminDeviceManager {

    private const val PREFS_NAME = "bp_admin_device_prefs"
    private const val KEY_DEVICE_ID = "key_admin_device_id"
    private const val KEY_IS_AUTHORIZED = "key_is_authorized_admin"
    private const val KEY_ADMIN_ID = "key_admin_id"
    private const val KEY_PUSH_TOKEN = "key_admin_push_token"
    private const val KEY_REGISTERED_AT = "key_registered_at"
    private const val KEY_PROCESSED_EVENTS = "key_processed_events"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getDeviceId(context: Context): String {
        val prefs = getPrefs(context)
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        if (deviceId.isNullOrBlank()) {
            deviceId = "DEV-AND-" + UUID.randomUUID().toString().take(8).uppercase()
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        return deviceId
    }

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        val model = Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model"
        }
    }

    fun isDeviceAuthorized(context: Context): Boolean {
        val prefs = getPrefs(context)
        return prefs.getBoolean(KEY_IS_AUTHORIZED, false)
    }

    fun getActiveAdminId(context: Context): String? {
        val prefs = getPrefs(context)
        return prefs.getString(KEY_ADMIN_ID, null)
    }

    fun setDeviceAuthorized(
        context: Context,
        adminId: String,
        pushToken: String,
        authorized: Boolean
    ) {
        val prefs = getPrefs(context)
        prefs.edit()
            .putBoolean(KEY_IS_AUTHORIZED, authorized)
            .putString(KEY_ADMIN_ID, if (authorized) adminId else null)
            .putString(KEY_PUSH_TOKEN, if (authorized) pushToken else null)
            .putString(KEY_REGISTERED_AT, if (authorized) System.currentTimeMillis().toString() else null)
            .apply()
    }

    fun clearAuthorization(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit()
            .putBoolean(KEY_IS_AUTHORIZED, false)
            .remove(KEY_ADMIN_ID)
            .remove(KEY_PUSH_TOKEN)
            .apply()
    }

    /**
     * Deduplication protection: ensures the same event ID (e.g. "USER_CREATED_10284")
     * is not repeatedly alerted to this device on reconnect or reloads.
     */
    fun isEventProcessed(context: Context, eventId: String): Boolean {
        val prefs = getPrefs(context)
        val processed = prefs.getStringSet(KEY_PROCESSED_EVENTS, emptySet()) ?: emptySet()
        return processed.contains(eventId)
    }

    fun markEventProcessed(context: Context, eventId: String) {
        val prefs = getPrefs(context)
        val current = prefs.getStringSet(KEY_PROCESSED_EVENTS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(eventId)
        // Keep set size bounded to 500 events
        if (current.size > 500) {
            val pruned = current.toList().takeLast(250).toSet()
            prefs.edit().putStringSet(KEY_PROCESSED_EVENTS, pruned).apply()
        } else {
            prefs.edit().putStringSet(KEY_PROCESSED_EVENTS, current).apply()
        }
    }
}
