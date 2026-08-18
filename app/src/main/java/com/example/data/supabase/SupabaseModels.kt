package com.example.data.supabase

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JsonClass(generateAdapter = true)
data class SupabaseAuthRequest(
    val email: String,
    val password: String,
    val data: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseAuthResponse(
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "token_type") val tokenType: String? = null,
    @Json(name = "expires_in") val expiresIn: Long? = null,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    val user: SupabaseUser? = null,
    val error: String? = null,
    @Json(name = "error_description") val errorDescription: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseUser(
    val id: String,
    val email: String? = null,
    val phone: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseUserSessionDto(
    val id: String = "USR_1001",
    @Json(name = "full_name") val fullName: String = "Ali Traders",
    val email: String = "alid15618@gmail.com",
    val currency: String = "PKR",
    val country: String = "Pakistan (+92)",
    @Json(name = "betpro_username") val betproUsername: String = "bpexch_ali101",
    @Json(name = "betpro_password") val betproPassword: String = "pass4078",
    @Json(name = "is_active") val isActive: Boolean = true,
    val balance: Double = 35000.00
)

@JsonClass(generateAdapter = true)
data class SupabaseExchangeConfigDto(
    val id: Int = 1,
    @Json(name = "exchange_url") val exchangeUrl: String = "https://bpexch.live",
    @Json(name = "helpline_number") val helplineNumber: String = "+92359550448",
    @Json(name = "updated_at") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseTransactionDto(
    val id: String = "",
    @Json(name = "user_id") val userId: String = "",
    val type: String = "DEPOSIT",
    val amount: Double = 0.0,
    val currency: String = "PKR",
    val status: String = "COMPLETED",
    @Json(name = "created_at") val createdAt: String? = null
)

@Serializable
@JsonClass(generateAdapter = true)
data class ProfileDto(
    @SerialName("id") val id: String = "",
    @SerialName("email") val email: String? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("full_name") @Json(name = "full_name") val fullName: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("country") val country: String? = null,
    @SerialName("currency") val currency: String? = null,
    @SerialName("role") val role: String = "USER",
    @SerialName("wallet_balance") @Json(name = "wallet_balance") val walletBalance: Double = 0.0,
    @SerialName("is_approved") @Json(name = "is_approved") val isApproved: Boolean = true,
    @SerialName("is_blocked") @Json(name = "is_blocked") val isBlocked: Boolean = false,
    @SerialName("created_at") @Json(name = "created_at") val createdAt: String? = null
)

@Serializable
@JsonClass(generateAdapter = true)
data class PaymentGatewayDto(
    @SerialName("id") val id: Long = 0L,
    @SerialName("gateway_name") @Json(name = "gateway_name") val gatewayName: String = "",
    @SerialName("currency") val currency: String = "PKR",
    @SerialName("country") val country: String = "Pakistan",
    @SerialName("account_title") @Json(name = "account_title") val accountTitle: String = "",
    @SerialName("account_number") @Json(name = "account_number") val accountNumber: String = "",
    @SerialName("min_deposit_amount") @Json(name = "min_deposit_amount") val minDepositAmount: Double = 500.0,
    @SerialName("is_enabled") @Json(name = "is_enabled") val isEnabled: Boolean = true
)

@Serializable
@JsonClass(generateAdapter = true)
data class TransactionDto(
    @SerialName("id") val id: String = "",
    @SerialName("user_id") @Json(name = "user_id") val userId: String = "",
    @SerialName("user_name") @Json(name = "user_name") val userName: String = "",
    @SerialName("type") val type: String = "DEPOSIT",
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("currency") val currency: String = "PKR",
    @SerialName("gateway_name") @Json(name = "gateway_name") val gatewayName: String = "",
    @SerialName("account_title") @Json(name = "account_title") val accountTitle: String = "",
    @SerialName("account_number") @Json(name = "account_number") val accountNumber: String = "",
    @SerialName("sender_name") @Json(name = "sender_name") val senderName: String = "",
    @SerialName("transaction_ref") @Json(name = "transaction_ref") val transactionRef: String = "",
    @SerialName("screenshot_url") @Json(name = "screenshot_url") val screenshotUrl: String? = null,
    @SerialName("status") val status: String = "PENDING",
    @SerialName("created_at") @Json(name = "created_at") val createdAt: String? = null
)

@Serializable
@JsonClass(generateAdapter = true)
data class SystemSettingsDto(
    @SerialName("id") val id: Int = 1,
    @SerialName("admin_password") @Json(name = "admin_password") val adminPassword: String = "Asd1234",
    @SerialName("whatsapp_helpline") @Json(name = "whatsapp_helpline") val whatsappHelpline: String = "+923259550448",
    @SerialName("exchange_website_url") @Json(name = "exchange_website_url") val exchangeWebsiteUrl: String = "https://bpexch.live",
    @SerialName("announcement_title") @Json(name = "announcement_title") val announcementTitle: String = "",
    @SerialName("announcement_message") @Json(name = "announcement_message") val announcementMessage: String = ""
)
