package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    const val CHANNEL_ID_TRANSACTIONS = "channel_transactions"
    const val CHANNEL_NAME_TRANSACTIONS = "Transactions & Wallet Alerts"
    const val CHANNEL_DESC_TRANSACTIONS = "Real-time updates for deposit approvals, withdrawals, and account status"

    const val CHANNEL_ID_ADMIN_ALERTS = "channel_admin_alerts"
    const val CHANNEL_NAME_ADMIN_ALERTS = "Admin Executive Alerts"
    const val CHANNEL_DESC_ADMIN_ALERTS = "Urgent alerts for new user registrations, pending deposits, and withdrawals for authorized Admin devices"

    const val EXTRA_NAV_TARGET = "extra_nav_target"
    const val EXTRA_TRANSACTION_ID = "extra_transaction_id"
    const val EXTRA_TRANSACTION_TYPE = "extra_transaction_type"
    const val EXTRA_TRANSACTION_STATUS = "extra_transaction_status"
    const val EXTRA_REFERENCE_ID = "extra_reference_id"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            // Transaction channel
            val txChannel = NotificationChannel(
                CHANNEL_ID_TRANSACTIONS,
                CHANNEL_NAME_TRANSACTIONS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_TRANSACTIONS
                enableLights(true)
                lightColor = Color.parseColor("#10B981")
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setShowBadge(true)
            }
            manager.createNotificationChannel(txChannel)

            // Admin alerts channel
            val adminChannel = NotificationChannel(
                CHANNEL_ID_ADMIN_ALERTS,
                CHANNEL_NAME_ADMIN_ALERTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_ADMIN_ALERTS
                enableLights(true)
                lightColor = Color.parseColor("#EF4444")
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 400)
                setShowBadge(true)
            }
            manager.createNotificationChannel(adminChannel)
        }
    }

    /**
     * Shows Admin Notification ONLY if this device is authorized as an active Admin device.
     * Normal users will NEVER receive or see this notification.
     */
    fun showAdminNotification(
        context: Context,
        eventId: String,
        title: String,
        message: String,
        navTarget: String, // "admin_user", "admin_deposit", "admin_withdrawal", "admin_notifications"
        referenceId: String? = null
    ) {
        // SECURITY ENFORCEMENT: Verify device is registered & authorized as Admin
        if (!AdminDeviceManager.isDeviceAuthorized(context)) {
            return
        }

        // DUPLICATE PROTECTION: Avoid repeatedly showing same event
        if (AdminDeviceManager.isEventProcessed(context, eventId)) {
            return
        }
        AdminDeviceManager.markEventProcessed(context, eventId)

        // Ensure channels are created
        createNotificationChannels(context)

        // Check POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAV_TARGET, navTarget)
            referenceId?.let { putExtra(EXTRA_REFERENCE_ID, it) }
        }

        val notificationId = (System.currentTimeMillis() % 100000).toInt() + 10000

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val accentColor = when (navTarget) {
            "admin_deposit" -> Color.parseColor("#10B981") // Emerald Green
            "admin_withdrawal" -> Color.parseColor("#F59E0B") // Amber
            "admin_user" -> Color.parseColor("#3B82F6") // Blue
            else -> Color.parseColor("#6366F1") // Indigo
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ADMIN_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setColor(accentColor)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }

    fun showTransactionNotification(
        context: Context,
        title: String,
        message: String,
        txId: String? = null,
        txType: String? = null,
        status: String? = null
    ) {
        // Ensure channels are created
        createNotificationChannels(context)

        // Check POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAV_TARGET, if (txType.equals("WITHDRAWAL", ignoreCase = true)) "withdrawal" else "deposit")
            txId?.let { putExtra(EXTRA_TRANSACTION_ID, it) }
            txType?.let { putExtra(EXTRA_TRANSACTION_TYPE, it) }
            status?.let { putExtra(EXTRA_TRANSACTION_STATUS, it) }
        }

        val notificationId = (System.currentTimeMillis() % 100000).toInt()

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Accent color matching status
        val accentColor = when (status?.uppercase()) {
            "APPROVED", "COMPLETED" -> Color.parseColor("#10B981") // Green
            "REJECTED", "FAILED" -> Color.parseColor("#EF4444") // Red
            "PROCESSING" -> Color.parseColor("#3B82F6") // Blue
            else -> Color.parseColor("#F59E0B") // Amber
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TRANSACTIONS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setColor(accentColor)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }
}
