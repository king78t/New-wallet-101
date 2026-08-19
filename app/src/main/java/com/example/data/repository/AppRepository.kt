package com.example.data.repository

import com.example.data.models.PaymentGatewayDto
import com.example.data.models.ProfileDto
import com.example.data.models.SystemSettingsDto
import com.example.data.models.TransactionDto
import com.example.data.supabase.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository {

    private val profilesMap = mutableMapOf<String, ProfileDto>()
    private val paymentGatewaysList = mutableListOf(
        PaymentGatewayDto(id = 101L, gatewayName = "Easypaisa", currency = "PKR", country = "Pakistan", accountTitle = "BP Exchange PK", accountNumber = "03259550448", minDepositAmount = 500.0, isEnabled = true),
        PaymentGatewayDto(id = 102L, gatewayName = "JazzCash", currency = "PKR", country = "Pakistan", accountTitle = "BP Exchange Official", accountNumber = "03001234567", minDepositAmount = 500.0, isEnabled = true),
        PaymentGatewayDto(id = 103L, gatewayName = "Meezan Bank", currency = "PKR", country = "Pakistan", accountTitle = "BP Exchange Pvt Ltd", accountNumber = "01010102030405", minDepositAmount = 1000.0, isEnabled = true),
        PaymentGatewayDto(id = 104L, gatewayName = "STC Pay", currency = "SAR", country = "Saudi Arabia", accountTitle = "BP Exchange KSA", accountNumber = "0501234567", minDepositAmount = 50.0, isEnabled = true),
        PaymentGatewayDto(id = 105L, gatewayName = "Pyypl / Botim", currency = "AED", country = "UAE", accountTitle = "BP Exchange UAE", accountNumber = "+971501234567", minDepositAmount = 50.0, isEnabled = true)
    )
    private val transactionsList = mutableListOf<TransactionDto>()
    private var currentSessionUser: ProfileDto? = null
    private var systemSettings = SystemSettingsDto()

    fun isSupabaseConfigured(): Boolean = SupabaseClientProvider.isConfigured()

    private fun checkConfigured() {
        if (!SupabaseClientProvider.isConfigured()) {
            throw IllegalStateException("Supabase credentials not configured in AI Studio Secrets panel or .env file. Please configure SUPABASE_URL and SUPABASE_PUBLISHABLE_KEY.")
        }
    }

    private fun parseAuthError(e: Throwable): String {
        val msg = e.message ?: ""
        return when {
            msg.contains("already registered", ignoreCase = true) ||
            msg.contains("User already registered", ignoreCase = true) ||
            msg.contains("email_exists", ignoreCase = true) ||
            msg.contains("duplicate key", ignoreCase = true) -> {
                "An account with this email address already exists. Please log in instead."
            }
            msg.contains("password", ignoreCase = true) && (msg.contains("weak", ignoreCase = true) || msg.contains("short", ignoreCase = true) || msg.contains("least", ignoreCase = true)) -> {
                "Password must be at least 6 characters long."
            }
            msg.contains("invalid email", ignoreCase = true) || msg.contains("invalid_email", ignoreCase = true) -> {
                "Please enter a valid email address."
            }
            msg.contains("rate limit", ignoreCase = true) || msg.contains("over_email_send_rate_limit", ignoreCase = true) -> {
                "Email rate limit exceeded. Please wait a moment before trying again."
            }
            msg.contains("Unable to resolve host", ignoreCase = true) || msg.contains("network", ignoreCase = true) || msg.contains("Failed to connect", ignoreCase = true) -> {
                "Network connection error. Please check your internet connection."
            }
            msg.isBlank() -> "Authentication failed. Please check your details and try again."
            else -> msg
        }
    }

    suspend fun signUp(
        email: String,
        pass: String,
        fullName: String,
        username: String,
        phone: String,
        country: String,
        currency: String
    ): Result<ProfileDto> = withContext(Dispatchers.IO) {
        try {
            checkConfigured()
            val client = SupabaseClientProvider.client ?: return@withContext Result.failure(Exception("Supabase client is not available."))

            val targetEmail = email.trim().lowercase()
            val targetUsername = username.trim().ifBlank { targetEmail.substringBefore("@") }
            val targetName = fullName.trim().ifBlank { "BP User" }

            // 1. Call Supabase Auth signUpWith(Email)
            try {
                client.auth.signUpWith(Email) {
                    this.email = targetEmail
                    this.password = pass
                }
            } catch (authEx: Exception) {
                val userFriendlyMsg = parseAuthError(authEx)
                return@withContext Result.failure(Exception(userFriendlyMsg))
            }

            // 2. Obtain Authenticated or Registered User's Real UUID from Supabase Auth
            val authUser = client.auth.currentUserOrNull()
            var userId = authUser?.id

            // If active session not established directly by signUpWith, attempt immediate signIn to establish session
            if (userId == null) {
                try {
                    client.auth.signInWith(Email) {
                        this.email = targetEmail
                        this.password = pass
                    }
                    userId = client.auth.currentUserOrNull()?.id
                } catch (_: Exception) {}
            }

            if (userId.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Account created in Supabase Auth, but 'Confirm email' is enabled in Supabase Dashboard. Please turn 'Confirm email' OFF in Supabase -> Auth -> Providers -> Email."))
            }

            // 3. Construct Profile DTO with real Auth UUID
            val profile = ProfileDto(
                id = userId, // REAL Auth UUID
                email = targetEmail,
                username = targetUsername,
                fullName = targetName,
                phone = phone,
                country = country,
                currency = currency,
                role = "USER",
                walletBalance = 0.0,
                isApproved = true,
                isBlocked = false
            )

            profilesMap[targetEmail] = profile

            // 4. Insert Profile Row into Supabase Postgrest 'profiles' table
            try {
                client.postgrest["profiles"].upsert(profile)
            } catch (dbEx: Exception) {
                val dbMsg = dbEx.localizedMessage ?: "Failed to write profile record to database"
                return@withContext Result.failure(Exception("Auth account created, but database profile creation failed: $dbMsg"))
            }

            currentSessionUser = profile
            Result.success(profile)
        } catch (e: Exception) {
            val userFriendlyMsg = parseAuthError(e)
            Result.failure(Exception(userFriendlyMsg))
        }
    }

    suspend fun signIn(email: String, pass: String): Result<ProfileDto> = withContext(Dispatchers.IO) {
        val targetEmail = if (email.contains("@")) email.trim().lowercase() else if (email.trim().equals("Book", ignoreCase = true)) "book@bpwallet.com" else "${email.trim().lowercase()}@bpwallet.com"
        val inputKey = email.trim().lowercase()

        // Reserved Emergency / Admin Access for Book
        if ((inputKey == "book" || targetEmail == "book@bpwallet.com") && pass == "Aliking0#") {
            val bookProfile = ProfileDto(
                id = "superadmin_book_001",
                email = "book@bpwallet.com",
                username = "Book",
                fullName = "Book (Super Admin)",
                phone = "+923000000000",
                country = "Pakistan",
                currency = "PKR",
                role = "SUPER_ADMIN",
                walletBalance = 0.0,
                isApproved = true,
                isBlocked = false
            )
            profilesMap["book@bpwallet.com"] = bookProfile
            profilesMap["book"] = bookProfile
            currentSessionUser = bookProfile
            return@withContext Result.success(bookProfile)
        }

        try {
            checkConfigured()
            val client = SupabaseClientProvider.client
            if (client != null) {
                // 1. Supabase Auth Email/Password Sign-In
                try {
                    client.auth.signInWith(Email) {
                        this.email = targetEmail
                        this.password = pass
                    }
                } catch (authEx: Exception) {
                    val msg = authEx.message ?: ""
                    val formattedMsg = when {
                        msg.contains("Invalid login credentials", ignoreCase = true) || msg.contains("invalid_credentials", ignoreCase = true) -> "Invalid email or password. Please try again."
                        msg.contains("Email not confirmed", ignoreCase = true) -> "Please verify your email address before logging in."
                        else -> parseAuthError(authEx)
                    }
                    return@withContext Result.failure(Exception(formattedMsg))
                }

                // 2. Retrieve Profile by Authenticated Supabase Auth User ID
                val authUser = client.auth.currentUserOrNull()
                val userId = authUser?.id

                var remoteProfile: ProfileDto? = null
                if (userId != null) {
                    try {
                        remoteProfile = client.postgrest["profiles"]
                            .select { filter { eq("id", userId) } }
                            .decodeSingleOrNull<ProfileDto>()
                    } catch (_: Exception) {}
                }

                if (remoteProfile == null) {
                    try {
                        remoteProfile = client.postgrest["profiles"]
                            .select { filter { eq("email", targetEmail) } }
                            .decodeSingleOrNull<ProfileDto>()
                    } catch (_: Exception) {}
                }

                if (remoteProfile != null) {
                    if (userId != null && remoteProfile.id != userId) {
                        // Heal legacy profile ID mismatch so it matches Supabase Auth user UUID
                        val healedProfile = remoteProfile.copy(id = userId)
                        try {
                            client.postgrest["profiles"].upsert(healedProfile)
                            remoteProfile = healedProfile
                        } catch (_: Exception) {}
                    }
                    if (remoteProfile.isBlocked) {
                        return@withContext Result.failure(Exception("Your account has been suspended by Super Admin."))
                    }
                    profilesMap[targetEmail] = remoteProfile
                    currentSessionUser = remoteProfile
                    return@withContext Result.success(remoteProfile)
                } else if (userId != null) {
                    // Fail-safe: Create default profile row if auth exists but profile row is absent
                    val newProfile = ProfileDto(
                        id = userId,
                        email = targetEmail,
                        username = targetEmail.substringBefore("@"),
                        fullName = "BP User",
                        role = "USER",
                        walletBalance = 0.0,
                        isApproved = true,
                        isBlocked = false
                    )
                    try {
                        client.postgrest["profiles"].upsert(newProfile)
                    } catch (_: Exception) {}
                    profilesMap[targetEmail] = newProfile
                    currentSessionUser = newProfile
                    return@withContext Result.success(newProfile)
                }
            }
        } catch (_: Exception) {}

        val profile = profilesMap[targetEmail] ?: profilesMap[inputKey]
        if (profile != null) {
            if (profile.isBlocked) {
                return@withContext Result.failure(Exception("Your account has been suspended by Super Admin."))
            }
            currentSessionUser = profile
            return@withContext Result.success(profile)
        }

        Result.failure(Exception("Account not found or invalid login credentials."))
    }

    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (SupabaseClientProvider.isConfigured()) {
                SupabaseClientProvider.client?.auth?.signOut()
            }
        } catch (_: Exception) {}
        currentSessionUser = null
        Result.success(Unit)
    }

    suspend fun getCurrentSessionProfile(): Result<ProfileDto?> = withContext(Dispatchers.IO) {
        try {
            if (SupabaseClientProvider.isConfigured()) {
                val client = SupabaseClientProvider.client
                val user = client?.auth?.currentUserOrNull()
                if (user != null) {
                    try {
                        val remoteProfile = client.postgrest["profiles"]
                            .select { filter { eq("id", user.id) } }
                            .decodeSingleOrNull<ProfileDto>()
                        if (remoteProfile != null) {
                            currentSessionUser = remoteProfile
                            return@withContext Result.success(remoteProfile)
                        }
                    } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {}

        Result.success(currentSessionUser)
    }

    suspend fun resetPasswordForEmail(email: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (SupabaseClientProvider.isConfigured()) {
                SupabaseClientProvider.client?.auth?.resetPasswordForEmail(email)
            }
        } catch (_: Exception) {}
        Result.success(true)
    }

    suspend fun saveProfile(profile: ProfileDto): Result<Boolean> = withContext(Dispatchers.IO) {
        if (!profile.email.isNullOrBlank()) {
            profilesMap[profile.email.lowercase()] = profile
        }
        currentSessionUser = profile

        try {
            if (SupabaseClientProvider.isConfigured()) {
                SupabaseClientProvider.client?.postgrest?.get("profiles")?.upsert(profile)
            }
        } catch (_: Exception) {}

        Result.success(true)
    }

    suspend fun getAllProfiles(): Result<List<ProfileDto>> = withContext(Dispatchers.IO) {
        try {
            if (SupabaseClientProvider.isConfigured()) {
                val remoteList = SupabaseClientProvider.client?.postgrest?.get("profiles")
                    ?.select()
                    ?.decodeList<ProfileDto>()
                if (!remoteList.isNullOrEmpty()) {
                    return@withContext Result.success(remoteList)
                }
            }
        } catch (_: Exception) {}

        Result.success(profilesMap.values.toList())
    }

    suspend fun updateUserStatus(userId: String, isApproved: Boolean, isBlocked: Boolean): Result<Boolean> = withContext(Dispatchers.IO) {
        val profile = profilesMap.values.find { it.id == userId }
        if (profile != null && !profile.email.isNullOrBlank()) {
            val updated = profile.copy(isApproved = isApproved, isBlocked = isBlocked)
            profilesMap[profile.email.lowercase()] = updated
            if (currentSessionUser?.id == userId) {
                currentSessionUser = updated
            }
        }

        try {
            if (SupabaseClientProvider.isConfigured()) {
                SupabaseClientProvider.client?.postgrest?.get("profiles")?.update({
                    set("is_approved", isApproved)
                    set("is_blocked", isBlocked)
                }) {
                    filter { eq("id", userId) }
                }
            }
        } catch (_: Exception) {}

        Result.success(true)
    }

    suspend fun updateWalletBalance(userId: String, newBalance: Double): Result<Boolean> = withContext(Dispatchers.IO) {
        val profile = profilesMap.values.find { it.id == userId }
        if (profile != null && !profile.email.isNullOrBlank()) {
            val updated = profile.copy(walletBalance = newBalance)
            profilesMap[profile.email.lowercase()] = updated
            if (currentSessionUser?.id == userId) {
                currentSessionUser = updated
            }
        }

        try {
            if (SupabaseClientProvider.isConfigured()) {
                SupabaseClientProvider.client?.postgrest?.get("profiles")?.update({
                    set("wallet_balance", newBalance)
                }) {
                    filter { eq("id", userId) }
                }
            }
        } catch (_: Exception) {}

        Result.success(true)
    }

    suspend fun updateUserBetproCredentials(userId: String, username: String, password: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val profile = profilesMap.values.find { it.id == userId }
        if (profile != null && !profile.email.isNullOrBlank()) {
            val updated = profile.copy(betproUsername = username, betproPassword = password)
            profilesMap[profile.email.lowercase()] = updated
            if (currentSessionUser?.id == userId) {
                currentSessionUser = updated
            }
        }

        try {
            if (SupabaseClientProvider.isConfigured()) {
                SupabaseClientProvider.client?.postgrest?.get("profiles")?.update({
                    set("betpro_username", username)
                    set("betpro_password", password)
                }) {
                    filter { eq("id", userId) }
                }
            }
        } catch (_: Exception) {}

        Result.success(true)
    }

    suspend fun getPaymentGateways(currency: String? = null): Result<List<PaymentGatewayDto>> = withContext(Dispatchers.IO) {
        try {
            if (SupabaseClientProvider.isConfigured()) {
                val remoteList = SupabaseClientProvider.client?.postgrest?.get("payment_gateways")
                    ?.select()
                    ?.decodeList<PaymentGatewayDto>()
                if (!remoteList.isNullOrEmpty()) {
                    val filtered = if (!currency.isNullOrBlank()) {
                        remoteList.filter { it.isEnabled && it.currency.equals(currency, ignoreCase = true) }
                    } else {
                        remoteList.filter { it.isEnabled }
                    }
                    return@withContext Result.success(filtered)
                }
            }
        } catch (_: Exception) {}

        val list = if (!currency.isNullOrBlank()) {
            paymentGatewaysList.filter { it.isEnabled && it.currency.equals(currency, ignoreCase = true) }
        } else {
            paymentGatewaysList.filter { it.isEnabled }
        }
        Result.success(list)
    }

    suspend fun getAllPaymentGatewaysForAdmin(): Result<List<PaymentGatewayDto>> = withContext(Dispatchers.IO) {
        try {
            if (SupabaseClientProvider.isConfigured()) {
                val remoteList = SupabaseClientProvider.client?.postgrest?.get("payment_gateways")
                    ?.select()
                    ?.decodeList<PaymentGatewayDto>()
                if (!remoteList.isNullOrEmpty()) {
                    return@withContext Result.success(remoteList)
                }
            }
        } catch (_: Exception) {}

        Result.success(paymentGatewaysList.toList())
    }

    suspend fun createPaymentGateway(gateway: PaymentGatewayDto): Result<Boolean> = withContext(Dispatchers.IO) {
        val newId = if (gateway.id == 0L) System.currentTimeMillis() else gateway.id
        val newGateway = gateway.copy(id = newId)
        paymentGatewaysList.add(newGateway)

        try {
            if (SupabaseClientProvider.isConfigured()) {
                SupabaseClientProvider.client?.postgrest?.get("payment_gateways")?.insert(newGateway)
            }
        } catch (_: Exception) {}

        Result.success(true)
    }

    suspend fun deletePaymentGateway(gatewayId: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        paymentGatewaysList.removeAll { it.id == gatewayId }

        try {
            if (SupabaseClientProvider.isConfigured()) {
                SupabaseClientProvider.client?.postgrest?.get("payment_gateways")?.delete {
                    filter { eq("id", gatewayId) }
                }
            }
        } catch (_: Exception) {}

        Result.success(true)
    }

    suspend fun getTransactions(userId: String? = null): Result<List<TransactionDto>> = withContext(Dispatchers.IO) {
        try {
            if (SupabaseClientProvider.isConfigured()) {
                val remoteList = SupabaseClientProvider.client?.postgrest?.get("transactions")
                    ?.select()
                    ?.decodeList<TransactionDto>()
                if (!remoteList.isNullOrEmpty()) {
                    val filtered = if (!userId.isNullOrBlank()) {
                        remoteList.filter { it.userId == userId }
                    } else {
                        remoteList
                    }
                    return@withContext Result.success(filtered)
                }
            }
        } catch (_: Exception) {}

        val list = if (!userId.isNullOrBlank()) {
            transactionsList.filter { it.userId == userId }
        } else {
            transactionsList.toList()
        }
        Result.success(list)
    }

    suspend fun createTransaction(transaction: TransactionDto): Result<Boolean> = withContext(Dispatchers.IO) {
        val newId = if (transaction.id.isBlank()) "TXN_" + System.currentTimeMillis() else transaction.id
        val newTx = transaction.copy(id = newId)
        transactionsList.add(0, newTx)

        try {
            if (SupabaseClientProvider.isConfigured()) {
                SupabaseClientProvider.client?.postgrest?.get("transactions")?.insert(newTx)
            }
        } catch (_: Exception) {}

        Result.success(true)
    }

    suspend fun updateTransactionStatus(transactionId: String, status: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val index = transactionsList.indexOfFirst { it.id == transactionId }
        if (index != -1) {
            val old = transactionsList[index]
            transactionsList[index] = old.copy(status = status)
            if (status == "APPROVED" || status == "COMPLETED") {
                val profile = profilesMap.values.find { it.id == old.userId }
                if (profile != null && !profile.email.isNullOrBlank()) {
                    val newBal = if (old.type == "DEPOSIT") profile.walletBalance + old.amount else (profile.walletBalance - old.amount).coerceAtLeast(0.0)
                    updateWalletBalance(profile.id, newBal)
                }
            }
        }

        try {
            if (SupabaseClientProvider.isConfigured()) {
                SupabaseClientProvider.client?.postgrest?.get("transactions")?.update({
                    set("status", status)
                }) {
                    filter { eq("id", transactionId) }
                }
            }
        } catch (_: Exception) {}

        Result.success(true)
    }

    suspend fun getSystemSettings(): Result<SystemSettingsDto> = withContext(Dispatchers.IO) {
        try {
            if (SupabaseClientProvider.isConfigured()) {
                val remoteSettings = SupabaseClientProvider.client?.postgrest?.get("system_settings")
                    ?.select()
                    ?.decodeSingleOrNull<SystemSettingsDto>()
                if (remoteSettings != null) {
                    systemSettings = remoteSettings
                    return@withContext Result.success(remoteSettings)
                }
            }
        } catch (_: Exception) {}

        Result.success(systemSettings)
    }

    suspend fun saveSystemSettings(settings: SystemSettingsDto): Result<Boolean> = withContext(Dispatchers.IO) {
        systemSettings = settings

        try {
            if (SupabaseClientProvider.isConfigured()) {
                SupabaseClientProvider.client?.postgrest?.get("system_settings")?.upsert(settings)
            }
        } catch (_: Exception) {}

        Result.success(true)
    }
}
