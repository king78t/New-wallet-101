package com.example.services

import android.util.Log
import com.example.notifications.FcmTokenManager
import com.example.notifications.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class BPFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "Refreshed FCM token: $token")
        FcmTokenManager.saveToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val isAdminAlert = data["is_admin_alert"]?.toBoolean() == true ||
                data["type"] in listOf("USER_CREATED", "DEPOSIT_CREATED", "WITHDRAWAL_CREATED", "ADMIN_ALERT") ||
                remoteMessage.from?.contains("admin_alerts") == true

        if (isAdminAlert) {
            // STRICT SECURITY RULE: Only authorized Admin devices receive Admin alerts
            if (!com.example.notifications.AdminDeviceManager.isDeviceAuthorized(applicationContext)) {
                Log.w(TAG, "Security check failed: Ignored Admin alert on unauthorized device.")
                return
            }

            val eventId = data["event_id"] ?: data["id"] ?: "EVT_${System.currentTimeMillis()}"
            val target = data["nav_target"] ?: when (data["type"]) {
                "USER_CREATED" -> "admin_user"
                "DEPOSIT_CREATED" -> "admin_deposit"
                "WITHDRAWAL_CREATED" -> "admin_withdrawal"
                else -> "admin_notifications"
            }
            val refId = data["reference_id"] ?: data["tx_id"] ?: data["user_id"]
            val title = data["title"] ?: notification?.title ?: "🔔 Admin Alert"
            val body = data["body"] ?: notification?.body ?: "New executive event requires attention."

            NotificationHelper.showAdminNotification(
                context = applicationContext,
                eventId = eventId,
                title = title,
                message = body,
                navTarget = target,
                referenceId = refId
            )
            return
        }

        // Extract transaction properties from FCM payload for standard user transactions
        val type = data["type"] ?: data["tx_type"] ?: "TRANSACTION"
        val status = data["status"] ?: "UPDATED"
        val txId = data["tx_id"] ?: data["id"]
        val amount = data["amount"]
        val currency = data["currency"] ?: "PKR"
        val gateway = data["gateway_name"] ?: data["gateway"]

        // Determine title and body
        val title: String = data["title"]
            ?: notification?.title
            ?: when {
                type.equals("DEPOSIT", ignoreCase = true) && status.equals("APPROVED", ignoreCase = true) -> "Deposit Approved! ✅"
                type.equals("DEPOSIT", ignoreCase = true) && status.equals("REJECTED", ignoreCase = true) -> "Deposit Update ⚠️"
                type.equals("WITHDRAWAL", ignoreCase = true) && status.equals("APPROVED", ignoreCase = true) -> "Withdrawal Approved! 💸"
                type.equals("WITHDRAWAL", ignoreCase = true) && status.equals("REJECTED", ignoreCase = true) -> "Withdrawal Update ⚠️"
                else -> "Wallet Transaction Update"
            }

        val body: String = data["body"]
            ?: notification?.body
            ?: when {
                type.equals("DEPOSIT", ignoreCase = true) && status.equals("APPROVED", ignoreCase = true) -> {
                    if (amount != null) "Your deposit of $currency $amount ${if (gateway != null) "via $gateway" else ""} has been approved and credited."
                    else "Your deposit request has been approved and credited to your wallet balance."
                }
                type.equals("DEPOSIT", ignoreCase = true) && status.equals("REJECTED", ignoreCase = true) -> {
                    "Your deposit request was rejected. Please check your transaction proof or contact support."
                }
                type.equals("WITHDRAWAL", ignoreCase = true) && status.equals("APPROVED", ignoreCase = true) -> {
                    if (amount != null) "Your withdrawal of $currency $amount has been dispatched to your account."
                    else "Your withdrawal request has been successfully processed."
                }
                type.equals("WITHDRAWAL", ignoreCase = true) && status.equals("REJECTED", ignoreCase = true) -> {
                    "Your withdrawal request was rejected. The funds have been refunded to your wallet balance."
                }
                else -> "Your transaction ($type) status has changed to $status."
            }

        // Show rich system push notification
        NotificationHelper.showTransactionNotification(
            context = applicationContext,
            title = title,
            message = body,
            txId = txId,
            txType = type,
            status = status
        )
    }

    companion object {
        private const val TAG = "BPFirebaseMsgService"
    }
}
