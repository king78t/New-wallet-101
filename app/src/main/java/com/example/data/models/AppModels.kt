package com.example.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    @SerialName("id") val id: String = "",
    @SerialName("email") val email: String? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("country") val country: String? = null,
    @SerialName("currency") val currency: String? = null,
    @SerialName("role") val role: String = "USER",
    @SerialName("wallet_balance") val walletBalance: Double = 0.0,
    @SerialName("is_approved") val isApproved: Boolean = true,
    @SerialName("is_blocked") val isBlocked: Boolean = false,
    @SerialName("betpro_username") val betproUsername: String? = null,
    @SerialName("betpro_password") val betproPassword: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class PaymentGatewayDto(
    @SerialName("id") val id: Long = 0L,
    @SerialName("gateway_name") val gatewayName: String = "",
    @SerialName("currency") val currency: String = "PKR",
    @SerialName("country") val country: String = "Pakistan",
    @SerialName("account_title") val accountTitle: String = "",
    @SerialName("account_number") val accountNumber: String = "",
    @SerialName("min_deposit_amount") val minDepositAmount: Double = 500.0,
    @SerialName("is_enabled") val isEnabled: Boolean = true
)

@Serializable
data class TransactionDto(
    @SerialName("id") val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("user_name") val userName: String = "",
    @SerialName("type") val type: String = "DEPOSIT",
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("currency") val currency: String = "PKR",
    @SerialName("gateway_name") val gatewayName: String = "",
    @SerialName("account_title") val accountTitle: String = "",
    @SerialName("account_number") val accountNumber: String = "",
    @SerialName("sender_name") val senderName: String = "",
    @SerialName("transaction_ref") val transactionRef: String = "",
    @SerialName("screenshot_url") val screenshotUrl: String? = null,
    @SerialName("payment_proof_path") val paymentProofPath: String? = null,
    @SerialName("status") val status: String = "PENDING",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class SystemSettingsDto(
    @SerialName("id") val id: Int = 1,
    @SerialName("admin_password") val adminPassword: String = "Asd1234",
    @SerialName("whatsapp_helpline") val whatsappHelpline: String = "+923259550448",
    @SerialName("exchange_website_url") val exchangeWebsiteUrl: String = "https://bpexch.live",
    @SerialName("announcement_title") val announcementTitle: String = "",
    @SerialName("announcement_message") val announcementMessage: String = "",
    @SerialName("betpro_enabled") val betproEnabled: Boolean = true,
    @SerialName("betpro_display_name") val betproDisplayName: String = "BetPro Live Exchange"
)

@Serializable
data class AdminDeviceDto(
    @SerialName("device_id") val deviceId: String = "",
    @SerialName("admin_id") val adminId: String = "",
    @SerialName("push_token") val pushToken: String = "",
    @SerialName("platform") val platform: String = "Android",
    @SerialName("device_name") val deviceName: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("last_seen_at") val lastSeenAt: String = "",
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class AdminNotificationDto(
    @SerialName("id") val id: String = "",
    @SerialName("type") val type: String = "SYSTEM_ALERT", // "USER_CREATED", "DEPOSIT_CREATED", "WITHDRAWAL_CREATED", "SYSTEM_ALERT"
    @SerialName("title") val title: String = "",
    @SerialName("message") val message: String = "",
    @SerialName("reference_id") val referenceId: String = "", // userId or txId
    @SerialName("username") val username: String = "",
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("currency") val currency: String = "PKR",
    @SerialName("gateway_name") val gatewayName: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("is_read") val isRead: Boolean = false
)
