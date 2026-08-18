package com.example.data.supabase

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseRepository {

    fun isSupabaseConfigured(): Boolean = SupabaseClientProvider.isConfigured()

    fun getSupabaseUrl(): String = SupabaseClientProvider.getSupabaseUrl()

    private fun requireClient() = SupabaseClientProvider.client
        ?: throw IllegalStateException("Supabase credentials not configured in AI Studio Secrets panel.")

    // ----------------------------------------------------------------
    // AUTHENTICATION APIs
    // ----------------------------------------------------------------

    suspend fun signUp(
        email: String,
        pass: String,
        fullName: String,
        username: String,
        phone: String,
        country: String,
        currency: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val client = requireClient()
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = pass
                this.data = buildJsonObject {
                    put("full_name", fullName)
                    put("username", username)
                    put("phone", phone)
                    put("country", country)
                    put("currency", currency)
                    put("role", "USER")
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(
        email: String,
        token: String,
        isSignup: Boolean = true
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val client = requireClient()
            val otpType = if (isSignup) OtpType.Email.SIGNUP else OtpType.Email.RECOVERY
            client.auth.verifyEmailOtp(
                type = otpType,
                email = email,
                token = token
            )
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, pass: String): Result<ProfileDto> = withContext(Dispatchers.IO) {
        try {
            val client = requireClient()
            client.auth.signInWith(Email) {
                this.email = email
                this.password = pass
            }
            val user = client.auth.currentUserOrNull()
                ?: return@withContext Result.failure(Exception("Authentication succeeded but session user is null."))

            val profiles = client.postgrest["profiles"].select {
                filter {
                    eq("id", user.id)
                }
            }.decodeList<ProfileDto>()

            val profile = profiles.firstOrNull() ?: ProfileDto(
                id = user.id,
                email = user.email ?: email,
                role = "USER"
            )

            if (profile.isBlocked) {
                client.auth.signOut()
                return@withContext Result.failure(Exception("Your account has been suspended by Super Admin."))
            }

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val client = requireClient()
            client.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentSessionProfile(): Result<ProfileDto?> = withContext(Dispatchers.IO) {
        try {
            val client = requireClient()
            val session = client.auth.currentSessionOrNull() ?: return@withContext Result.success(null)
            val user = session.user ?: return@withContext Result.success(null)

            val profiles = client.postgrest["profiles"].select {
                filter {
                    eq("id", user.id)
                }
            }.decodeList<ProfileDto>()

            val profile = profiles.firstOrNull()
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPasswordForEmail(email: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val client = requireClient()
            client.auth.resetPasswordForEmail(email)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------------------------------------
    // PROFILE MANAGEMENT
    // ----------------------------------------------------------------

    suspend fun saveProfile(profile: ProfileDto): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val client = requireClient()
            client.postgrest["profiles"].upsert(profile)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllProfiles(): Result<List<ProfileDto>> = withContext(Dispatchers.IO) {
        try {
            val client = requireClient()
            val list = client.postgrest["profiles"].select().decodeList<ProfileDto>()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserStatus(userId: String, isApproved: Boolean, isBlocked: Boolean): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val client = requireClient()
            client.postgrest["profiles"].update(
                buildJsonObject {
                    put("is_approved", isApproved)
                    put("is_blocked", isBlocked)
                }
            ) {
                filter {
                    eq("id", userId)
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateWalletBalance(userId: String, newBalance: Double): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val client = requireClient()
            client.postgrest["profiles"].update(
                buildJsonObject {
                    put("wallet_balance", newBalance)
                }
            ) {
                filter {
                    eq("id", userId)
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------------------------------------
    // PAYMENT GATEWAYS (WITH CURRENCY FILTERING)
    // ----------------------------------------------------------------

    suspend fun getPaymentGateways(currency: String? = null): Result<List<PaymentGatewayDto>> = withContext(Dispatchers.IO) {
        try {
            val client = requireClient()
            val list = client.postgrest["payment_gateways"].select {
                filter {
                    eq("is_enabled", true)
                    if (!currency.isNullOrBlank()) {
                        eq("currency", currency)
                    }
                }
            }.decodeList<PaymentGatewayDto>()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllPaymentGatewaysForAdmin(): Result<List<PaymentGatewayDto>> = withContext(Dispatchers.IO) {
        try {
            val client = requireClient()
            val list = client.postgrest["payment_gateways"].select().decodeList<PaymentGatewayDto>()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPaymentGateway(gateway: PaymentGatewayDto): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val client = requireClient()
            client.postgrest["payment_gateways"].insert(gateway)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePaymentGateway(gatewayId: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val client = requireClient()
            client.postgrest["payment_gateways"].delete {
                filter {
                    eq("id", gatewayId)
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------------------------------------
    // TRANSACTIONS
    // ----------------------------------------------------------------

    suspend fun getTransactions(userId: String? = null): Result<List<TransactionDto>> = withContext(Dispatchers.IO) {
        try {
            val client = requireClient()
            val list = if (!userId.isNullOrBlank()) {
                client.postgrest["transactions"].select {
                    filter {
                        eq("user_id", userId)
                    }
                }.decodeList<TransactionDto>()
            } else {
                client.postgrest["transactions"].select().decodeList<TransactionDto>()
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTransaction(transaction: TransactionDto): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val client = requireClient()
            client.postgrest["transactions"].insert(transaction)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTransactionStatus(transactionId: String, status: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val client = requireClient()
            client.postgrest["transactions"].update(
                buildJsonObject {
                    put("status", status)
                }
            ) {
                filter {
                    eq("id", transactionId)
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------------------------------------
    // SYSTEM SETTINGS & CONFIG
    // ----------------------------------------------------------------

    suspend fun getSystemSettings(): Result<SystemSettingsDto> = withContext(Dispatchers.IO) {
        try {
            val client = requireClient()
            val list = client.postgrest["system_settings"].select().decodeList<SystemSettingsDto>()
            Result.success(list.firstOrNull() ?: SystemSettingsDto())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveSystemSettings(settings: SystemSettingsDto): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val client = requireClient()
            client.postgrest["system_settings"].upsert(settings)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
