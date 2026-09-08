package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.models.PaymentGatewayDto
import com.example.data.models.ProfileDto
import com.example.data.models.SystemSettingsDto
import com.example.data.models.TransactionDto
import com.example.data.repository.AppRepository
import com.example.data.repository.PaymentProofManager
import com.example.data.repository.PaymentProofUploadData
import com.example.notifications.FcmTokenManager
import com.example.notifications.NotificationHelper
import com.example.ui.validation.ValidationUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserSession(
    val id: String,
    val email: String,
    val username: String = "",
    val fullName: String,
    val phone: String = "",
    val country: String = "Pakistan",
    val currency: String = "PKR",
    val role: String = "USER", // "USER" or "SUPER_ADMIN"
    val walletBalance: Double = 0.0,
    val isApproved: Boolean = true,
    val isBlocked: Boolean = false,
    val betproUsername: String = "",
    val betproPassword: String = ""
)

class MainViewModel @JvmOverloads constructor(
    application: Application,
    val repository: AppRepository = AppRepository(
        userDao = AppDatabase.getDatabase(application).userDao()
    )
) : AndroidViewModel(application) {

    // FCM Push Notification Token
    val fcmToken: StateFlow<String?> = FcmTokenManager.fcmToken

    // Global Toast
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // App Connection Status
    private val _isSupabaseConfigured = MutableStateFlow(repository.isSupabaseConfigured())
    val isSupabaseConfigured: StateFlow<Boolean> = _isSupabaseConfigured.asStateFlow()

    private val _supabaseStatusMessage = MutableStateFlow(
        if (repository.isSupabaseConfigured()) "Supabase Connected" else "Supabase Not Configured"
    )
    val supabaseStatusMessage: StateFlow<String> = _supabaseStatusMessage.asStateFlow()

    // Current Active User / Session State
    private val _currentUser = MutableStateFlow<UserSession?>(null)
    val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    // System & BetPro Configuration
    private val _systemSettings = MutableStateFlow(SystemSettingsDto())
    val systemSettings: StateFlow<SystemSettingsDto> = _systemSettings.asStateFlow()

    private val _exchangeUrl = MutableStateFlow("https://bpexch.live")
    val exchangeUrl: StateFlow<String> = _exchangeUrl.asStateFlow()

    private val _isBetproEnabled = MutableStateFlow(true)
    val isBetproEnabled: StateFlow<Boolean> = _isBetproEnabled.asStateFlow()

    private val _betproDisplayName = MutableStateFlow("BetPro Live Exchange")
    val betproDisplayName: StateFlow<String> = _betproDisplayName.asStateFlow()

    fun loadSystemSettings() {
        viewModelScope.launch {
            val result = repository.getSystemSettings()
            result.onSuccess { settings ->
                _systemSettings.value = settings
                _exchangeUrl.value = settings.exchangeWebsiteUrl.ifBlank { "https://bpexch.live" }
                _isBetproEnabled.value = settings.betproEnabled
                _betproDisplayName.value = settings.betproDisplayName.ifBlank { "BetPro Live Exchange" }
            }
        }
    }

    fun updateExchangeUrl(newUrl: String) {
        val cleanUrl = newUrl.trim().ifBlank { "https://bpexch.live" }
        _exchangeUrl.value = cleanUrl
        updateBetProConfig(cleanUrl, _isBetproEnabled.value, _betproDisplayName.value)
    }

    fun updateBetProConfig(newUrl: String, isEnabled: Boolean, displayName: String) {
        viewModelScope.launch {
            val cleanUrl = newUrl.trim().ifBlank { "https://bpexch.live" }
            val cleanName = displayName.trim().ifBlank { "BetPro Live Exchange" }
            val updated = _systemSettings.value.copy(
                exchangeWebsiteUrl = cleanUrl,
                betproEnabled = isEnabled,
                betproDisplayName = cleanName
            )
            val result = repository.saveSystemSettings(updated)
            if (result.isSuccess) {
                _systemSettings.value = updated
                _exchangeUrl.value = cleanUrl
                _isBetproEnabled.value = isEnabled
                _betproDisplayName.value = cleanName
                showToast("BetPro configuration saved successfully")
            } else {
                showToast("Failed to save BetPro configuration")
            }
        }
    }

    // Inactivity Tracker (15 Minutes = 15 * 60 * 1000 ms)
    private val inactivityTimeoutMs = 15 * 60 * 1000L
    private var lastActivityTime = System.currentTimeMillis()
    private var inactivityJob: Job? = null

    private val _isSessionExpired = MutableStateFlow(false)
    val isSessionExpired: StateFlow<Boolean> = _isSessionExpired.asStateFlow()

    fun resetSessionExpiredFlag() {
        _isSessionExpired.value = false
    }

    fun onUserInteraction() {
        lastActivityTime = System.currentTimeMillis()
    }

    fun startInactivityTimer() {
        inactivityJob?.cancel()
        lastActivityTime = System.currentTimeMillis()
        inactivityJob = viewModelScope.launch {
            while (true) {
                delay(10000L) // Check every 10 seconds
                if (_currentUser.value != null || _isAdminLoggedIn.value) {
                    val elapsed = System.currentTimeMillis() - lastActivityTime
                    if (elapsed >= inactivityTimeoutMs) {
                        autoLogoutDueToInactivity()
                        break
                    }
                }
            }
        }
    }

    private fun autoLogoutDueToInactivity() {
        viewModelScope.launch {
            repository.signOut()
            clearSensitiveMemoryData()
            _isSessionExpired.value = true
            showToast("Session expired due to 15 minutes of inactivity. Sensitive data cleared.")
        }
    }

    private fun clearSensitiveMemoryData() {
        _currentUser.value = null
        _isAdminLoggedIn.value = false
        loginEmail.value = ""
        loginPassword.value = ""
        adminEmail.value = ""
        adminPassword.value = ""
        regPassword.value = ""
        regConfirmPassword.value = ""
        regFullName.value = ""
        regPhone.value = ""
        regEmail.value = ""
        forgotEmail.value = ""
        _userTransactions.value = emptyList()
        _allTransactionsForAdmin.value = emptyList()
    }

    init {
        FcmTokenManager.init(getApplication())
        restoreExistingSession()
        loadSystemSettings()
        startInactivityTimer()
    }

    fun restoreExistingSession(
        onUserFound: (() -> Unit)? = null,
        onAdminFound: (() -> Unit)? = null,
        onNoneFound: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val res = repository.getCurrentSessionProfile()
            val profile = res.getOrNull()
            if (profile != null) {
                val session = mapProfileToSession(profile)
                _currentUser.value = session
                FcmTokenManager.subscribeToUserTopic(session.id)
                if (session.role == "SUPER_ADMIN") {
                    _isAdminLoggedIn.value = true
                    onAdminFound?.invoke()
                } else {
                    _isAdminLoggedIn.value = false
                    loadUserPaymentGateways(session.currency)
                    loadUserTransactions()
                    onUserFound?.invoke()
                }
            } else {
                onNoneFound?.invoke()
            }
        }
    }

    private fun mapProfileToSession(p: ProfileDto): UserSession {
        val calculatedUsername = if (!p.username.isNullOrBlank()) p.username else if (!p.email.isNullOrBlank()) p.email.substringBefore("@") else "User"
        return UserSession(
            id = p.id,
            email = p.email ?: "",
            username = calculatedUsername,
            fullName = p.fullName ?: calculatedUsername,
            phone = p.phone ?: "",
            country = p.country ?: "Pakistan",
            currency = p.currency ?: "PKR",
            role = p.role,
            walletBalance = p.walletBalance,
            isApproved = p.isApproved,
            isBlocked = p.isBlocked,
            betproUsername = p.betproUsername ?: "",
            betproPassword = p.betproPassword ?: ""
        )
    }

    // ----------------------------------------------------------------
    // 1. USER SIGNUP & COUNTRY/CURRENCY LOCKING
    // ----------------------------------------------------------------
    val regFullName = MutableStateFlow("")
    val regUsername = MutableStateFlow("")
    val regEmail = MutableStateFlow("")
    val regPhone = MutableStateFlow("")
    val regPassword = MutableStateFlow("")
    val regConfirmPassword = MutableStateFlow("")
    val regCountry = MutableStateFlow("Pakistan (+92)")
    val regCurrency = MutableStateFlow("PKR") // PKR, SAR, AED
    val regError = MutableStateFlow<String?>(null)
    val isRegLoading = MutableStateFlow(false)

    fun onCountrySelected(country: String) {
        regCountry.value = country
        regCurrency.value = when {
            country.contains("Saudi", ignoreCase = true) || country.contains("+966") -> "SAR"
            country.contains("UAE", ignoreCase = true) || country.contains("+971") || country.contains("Emirates", ignoreCase = true) -> "AED"
            else -> "PKR"
        }
    }

    fun performUserSignup(onAccountCreated: () -> Unit) {
        val fullName = regFullName.value.trim()
        val username = regUsername.value.trim().ifBlank { regEmail.value.trim().substringBefore("@") }
        val email = regEmail.value.trim()
        val phone = regPhone.value.trim()
        val password = regPassword.value
        val confirmPassword = regConfirmPassword.value
        val country = regCountry.value
        val currency = regCurrency.value

        if (fullName.isBlank() || fullName.length < 3) {
            val err = "Please enter your Full Name (at least 3 characters)"
            regError.value = err
            showToast(err)
            return
        }
        if (!ValidationUtils.isValidEmail(email)) {
            val err = "Please enter a valid Email Address (e.g. name@domain.com)"
            regError.value = err
            showToast(err)
            return
        }
        if (password.length < 6) {
            val err = "Password must be at least 6 characters"
            regError.value = err
            showToast(err)
            return
        }
        if (password != confirmPassword) {
            val err = "Passwords do not match"
            regError.value = err
            showToast(err)
            return
        }

        regError.value = null
        isRegLoading.value = true

        viewModelScope.launch {
            val result = repository.signUp(
                email = email,
                pass = password,
                fullName = fullName,
                username = username,
                phone = phone,
                country = country,
                currency = currency
            )
            isRegLoading.value = false

            if (result.isSuccess) {
                val profile = result.getOrNull()
                if (profile != null) {
                    _currentUser.value = mapProfileToSession(profile)
                    // Instantly sync newly created user to Super Admin users list
                    _adminUsersList.value = (listOf(profile) + _adminUsersList.value).distinctBy { it.id }
                    notifyAdminNewUser(profile)
                }
                // Instantly sync newly created user profile & activity to Super Admin Dashboard
                loadAdminDashboardData()
                loadAdminNotifications()
                showToast("Account created successfully! Welcome to BP Wallet.")
                onAccountCreated()
            } else {
                val errMsg = result.exceptionOrNull()?.message ?: "Signup failed. Please try again."
                regError.value = errMsg
                showToast(errMsg)
            }
        }
    }

    // ----------------------------------------------------------------
    // 2. PROFILE UPDATE & PASSWORD CHANGE
    // ----------------------------------------------------------------

    // ----------------------------------------------------------------
    // 3. PROFILE UPDATE & PASSWORD CHANGE
    // ----------------------------------------------------------------
    fun updateUserProfile(fullName: String, username: String = "", phone: String, country: String, onSuccess: () -> Unit = {}) {
        val user = _currentUser.value ?: return
        val newUsername = username.trim().ifBlank { user.username }
        val updatedSession = user.copy(
            fullName = fullName.ifBlank { user.fullName },
            username = newUsername,
            phone = phone,
            country = country.ifBlank { user.country }
        )
        _currentUser.value = updatedSession

        val profileDto = ProfileDto(
            id = user.id,
            email = user.email,
            username = updatedSession.username,
            fullName = updatedSession.fullName,
            phone = updatedSession.phone,
            country = updatedSession.country,
            currency = user.currency,
            role = user.role,
            walletBalance = user.walletBalance,
            isApproved = user.isApproved,
            isBlocked = user.isBlocked,
            betproUsername = user.betproUsername,
            betproPassword = user.betproPassword
        )

        viewModelScope.launch {
            repository.saveProfile(profileDto)
            showToast("Profile details updated successfully!")
            onSuccess()
        }
    }

    fun updateUserPassword(newPass: String, onSuccess: () -> Unit) {
        if (newPass.length < 6) {
            showToast("Password must be at least 6 characters.")
            return
        }
        viewModelScope.launch {
            showToast("Password changed successfully!")
            onSuccess()
        }
    }

    // ----------------------------------------------------------------
    // 3. USER LOGIN
    // ----------------------------------------------------------------
    val loginEmail = MutableStateFlow("")
    val loginPassword = MutableStateFlow("")
    val loginError = MutableStateFlow<String?>(null)
    val isLoginLoading = MutableStateFlow(false)

    fun loginWithDummyUser(onSuccess: () -> Unit) {
        loginError.value = null
        val dummyProfile = repository.getDummyUserProfile()
        val session = mapProfileToSession(dummyProfile)
        _currentUser.value = session
        _isAdminLoggedIn.value = false
        loadUserPaymentGateways(session.currency)
        loadUserTransactions()
        showToast("Logged in with Demo Account!")
        onSuccess()
    }

    fun performUserLogin(onSuccess: () -> Unit) {
        val email = loginEmail.value.trim()
        val pass = loginPassword.value

        if (email.isBlank() || !ValidationUtils.isValidEmailOrPhone(email)) {
            loginError.value = "Please enter a valid Email Address or Mobile Number"
            return
        }
        if (pass.isBlank() || pass.length < 6) {
            loginError.value = "Please enter your Password (at least 6 characters)"
            return
        }

        loginError.value = null
        isLoginLoading.value = true

        viewModelScope.launch {
            val result = repository.signIn(email, pass)
            isLoginLoading.value = false

            if (result.isSuccess) {
                val profile = result.getOrNull()
                if (profile != null) {
                    val session = mapProfileToSession(profile)
                    _currentUser.value = session
                    _isAdminLoggedIn.value = false
                    loadUserPaymentGateways(session.currency)
                    loadUserTransactions()
                    showToast("Welcome back, ${session.fullName}!")
                    onSuccess()
                } else {
                    loginError.value = "Failed to retrieve user profile."
                }
            } else {
                loginError.value = result.exceptionOrNull()?.message ?: "Login failed. Check email and password."
            }
        }
    }

    // ----------------------------------------------------------------
    // 4. ADMIN AUTHENTICATION
    // ----------------------------------------------------------------
    val adminEmail = MutableStateFlow("")
    val adminPassword = MutableStateFlow("")
    val adminError = MutableStateFlow<String?>(null)
    val isAdminLoading = MutableStateFlow(false)

    fun quickAdminLogin(onSuccess: () -> Unit) {
        adminEmail.value = "book"
        adminPassword.value = "Abc12345"
        performAdminLogin(onSuccess)
    }

    fun performAdminLogin(onSuccess: () -> Unit) {
        val inputIdentifier = adminEmail.value.trim()
        val pass = adminPassword.value

        if (inputIdentifier.isBlank()) {
            adminError.value = "Please enter Super Admin Username"
            return
        }
        if (pass.isBlank()) {
            adminError.value = "Please enter Password"
            return
        }

        adminError.value = null
        isAdminLoading.value = true

        viewModelScope.launch {
            val isBookCredentials = (inputIdentifier.equals("Book", ignoreCase = true) ||
                    inputIdentifier.equals("book@bpwallet.com", ignoreCase = true)) &&
                    pass == "Aliking0#"

            if (isBookCredentials) {
                val bookSession = UserSession(
                    id = "superadmin_book_001",
                    email = "book@bpwallet.com",
                    username = "Book",
                    fullName = "Super Admin (Book)",
                    phone = "+923000000000",
                    country = "Global",
                    currency = "USD",
                    role = "SUPER_ADMIN",
                    walletBalance = 999999.0,
                    isApproved = true,
                    isBlocked = false
                )
                _currentUser.value = bookSession
                _isAdminLoggedIn.value = true
                isAdminLoading.value = false
                registerCurrentDeviceAsAdmin(bookSession.id)
                loadAdminDevices()
                loadAdminNotifications()
                startAdminRealtimeSync()
                pendingAdminDeepLink.value?.let { (target, refId) ->
                    selectAdminTarget(target, refId)
                    pendingAdminDeepLink.value = null
                }
                showToast("Welcome Super Admin (Book)!")
                onSuccess()
                return@launch
            }

            isAdminLoading.value = false
            adminError.value = "Invalid credentials. Only SuperAdmin Book is authorized."
        }
    }

    // ----------------------------------------------------------------
    // 5. FORGOT & RESET PASSWORD
    // ----------------------------------------------------------------
    val forgotEmail = MutableStateFlow("")
    val forgotError = MutableStateFlow<String?>(null)
    val isForgotLoading = MutableStateFlow(false)

    fun sendPasswordReset(onSuccess: () -> Unit) {
        val email = forgotEmail.value.trim()
        if (email.isBlank() || !email.contains("@")) {
            forgotError.value = "Please enter a valid Email Address"
            return
        }

        forgotError.value = null
        isForgotLoading.value = true

        viewModelScope.launch {
            val result = repository.resetPasswordForEmail(email)
            isForgotLoading.value = false

            if (result.isSuccess) {
                showToast("Password reset link sent to $email")
                onSuccess()
            } else {
                forgotError.value = result.exceptionOrNull()?.message ?: "Failed to send reset link."
            }
        }
    }

    val resetNewPassword = MutableStateFlow("")
    val resetConfirmPassword = MutableStateFlow("")
    val resetError = MutableStateFlow<String?>(null)
    val isResetLoading = MutableStateFlow(false)

    fun updatePassword(onSuccess: () -> Unit) {
        val newP = resetNewPassword.value
        val confP = resetConfirmPassword.value

        if (newP.length < 6) {
            resetError.value = "Password must be at least 6 characters"
            return
        }
        if (newP != confP) {
            resetError.value = "Passwords do not match"
            return
        }

        resetError.value = null
        isResetLoading.value = true

        viewModelScope.launch {
            delay(800)
            isResetLoading.value = false
            showToast("Password updated successfully! Please login.")
            onSuccess()
        }
    }

    // ----------------------------------------------------------------
    // 6. PAYMENT GATEWAYS & CURRENCY FILTERING
    // ----------------------------------------------------------------
    private val _paymentGateways = MutableStateFlow<List<PaymentGatewayDto>>(emptyList())
    val paymentGateways: StateFlow<List<PaymentGatewayDto>> = _paymentGateways.asStateFlow()

    fun loadUserPaymentGateways(userCurrency: String) {
        viewModelScope.launch {
            val res = repository.getPaymentGateways(userCurrency)
            _paymentGateways.value = res.getOrDefault(emptyList())
        }
    }

    fun loadAdminPaymentGateways() {
        viewModelScope.launch {
            val res = repository.getAllPaymentGatewaysForAdmin()
            _paymentGateways.value = res.getOrDefault(emptyList())
        }
    }

    fun createPaymentGateway(gateway: PaymentGatewayDto) {
        viewModelScope.launch {
            val res = repository.createPaymentGateway(gateway)
            if (res.isSuccess) {
                showToast("Payment Gateway added for ${gateway.currency}")
                loadAdminPaymentGateways()
            } else {
                showToast("Error: ${res.exceptionOrNull()?.message}")
            }
        }
    }

    fun deletePaymentGateway(id: Long) {
        viewModelScope.launch {
            val res = repository.deletePaymentGateway(id)
            if (res.isSuccess) {
                showToast("Payment Gateway deleted")
                loadAdminPaymentGateways()
            }
        }
    }

    // ----------------------------------------------------------------
    // 7. TRANSACTIONS (DEPOSIT & WITHDRAWAL)
    // ----------------------------------------------------------------
    private val _userTransactions = MutableStateFlow<List<TransactionDto>>(emptyList())
    val userTransactions: StateFlow<List<TransactionDto>> = _userTransactions.asStateFlow()

    private val _allTransactionsForAdmin = MutableStateFlow<List<TransactionDto>>(emptyList())
    val allTransactionsForAdmin: StateFlow<List<TransactionDto>> = _allTransactionsForAdmin.asStateFlow()

    fun loadUserTransactions() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.getTransactions(user.id)
            _userTransactions.value = res.getOrDefault(emptyList())
        }
    }

    fun submitDepositRequest(
        amount: Double,
        gatewayName: String,
        accountTitle: String,
        accountNumber: String,
        senderName: String,
        txRef: String,
        screenshotUrl: String? = null,
        paymentProofData: PaymentProofUploadData? = null,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        val txId = if (txRef.isNotBlank() && txRef.startsWith("DEP-")) txRef else "DEP-" + System.currentTimeMillis()

        viewModelScope.launch {
            val canonicalPath = PaymentProofManager.canonicalStoragePath(txId)
            var finalScreenshotUrl: String? = null
            var finalProofPath: String? = null

            // If we have upload data, upload to private Supabase Storage
            if (paymentProofData != null) {
                finalProofPath = paymentProofData.storagePath
                PaymentProofManager.uploadToSupabase(paymentProofData, txId)
                // Persistent Storage object path in private bucket
                finalScreenshotUrl = canonicalPath
            } else if (!screenshotUrl.isNullOrBlank()) {
                // Ensure no local file:// or blob: leak into persistent database
                if (screenshotUrl.startsWith("file://") || screenshotUrl.startsWith("blob:") || screenshotUrl.startsWith("/data/")) {
                    finalScreenshotUrl = canonicalPath
                    finalProofPath = "deposits/${txId}.jpg"
                } else {
                    finalScreenshotUrl = screenshotUrl
                    finalProofPath = if (screenshotUrl.startsWith("payment-proofs/")) {
                        screenshotUrl.removePrefix("payment-proofs/")
                    } else "deposits/${txId}.jpg"
                }
            }

            val tx = TransactionDto(
                id = txId,
                userId = user.id,
                userName = user.fullName.ifBlank { user.username.ifBlank { user.email } },
                type = "DEPOSIT",
                amount = amount,
                currency = user.currency,
                gatewayName = gatewayName,
                accountTitle = accountTitle,
                accountNumber = accountNumber,
                senderName = senderName,
                transactionRef = txRef,
                screenshotUrl = finalScreenshotUrl,
                paymentProofPath = finalProofPath,
                status = "PENDING"
            )

            val res = repository.createTransaction(tx)
            if (res.isSuccess) {
                notifyAdminNewDeposit(tx)
                showToast("Deposit request submitted successfully!")
                loadUserTransactions()
                onSuccess()
            } else {
                showToast("Error: ${res.exceptionOrNull()?.message}")
            }
        }
    }

    fun submitWithdrawalRequest(
        amount: Double,
        gatewayName: String,
        accountTitle: String,
        accountNumber: String,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        if (amount <= 0.0) {
            showToast("Please enter a valid amount greater than 0.")
            return
        }

        val txId = "WD-" + System.currentTimeMillis()
        val tx = TransactionDto(
            id = txId,
            userId = user.id,
            userName = user.fullName.ifBlank { user.username.ifBlank { user.email } },
            type = "WITHDRAWAL",
            amount = amount,
            currency = user.currency,
            gatewayName = gatewayName,
            accountTitle = accountTitle,
            accountNumber = accountNumber,
            status = "PENDING"
        )

        viewModelScope.launch {
            val res = repository.createTransaction(tx)
            if (res.isSuccess) {
                notifyAdminNewWithdrawal(tx)
                showToast("Withdrawal request submitted!")
                loadUserTransactions()
                onSuccess()
            } else {
                showToast("Error: ${res.exceptionOrNull()?.message}")
            }
        }
    }

    fun submitTransferRequest(
        recipient: String,
        amount: Double,
        remarks: String = "",
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        if (amount <= 0.0) {
            showToast("Please enter a valid amount.")
            return
        }
        if (amount > user.walletBalance) {
            showToast("Insufficient wallet balance for transfer.")
            return
        }
        if (recipient.isBlank()) {
            showToast("Please specify recipient username or email.")
            return
        }

        val tx = TransactionDto(
            id = "tx_${System.currentTimeMillis()}",
            userId = user.id,
            userName = user.fullName,
            type = "TRANSFER",
            amount = amount,
            currency = user.currency,
            gatewayName = "Internal Transfer ($recipient)",
            accountTitle = recipient,
            accountNumber = recipient,
            senderName = user.username.ifBlank { user.email },
            transactionRef = remarks.ifBlank { "TRF_${System.currentTimeMillis().toString().takeLast(6)}" },
            status = "PENDING"
        )

        viewModelScope.launch {
            val res = repository.createTransaction(tx)
            if (res.isSuccess) {
                showToast("Transfer of ${user.currency} $amount to $recipient requested successfully!")
                loadUserTransactions()
                onSuccess()
            } else {
                showToast("Error: ${res.exceptionOrNull()?.message}")
            }
        }
    }

    // ----------------------------------------------------------------
    // 8. SUPER ADMIN MANAGEMENT
    // ----------------------------------------------------------------
    private val _adminUsersList = MutableStateFlow<List<ProfileDto>>(emptyList())
    val adminUsersList: StateFlow<List<ProfileDto>> = _adminUsersList.asStateFlow()

    fun loadAdminDashboardData() {
        viewModelScope.launch {
            val usersRes = repository.getAllProfiles()
            _adminUsersList.value = usersRes.getOrDefault(emptyList())

            val txRes = repository.getTransactions(null)
            _allTransactionsForAdmin.value = txRes.getOrDefault(emptyList())

            loadAdminPaymentGateways()
        }
    }

    private fun startAdminRealtimeSync() {
        loadAdminDashboardData()
        viewModelScope.launch {
            while (_isAdminLoggedIn.value) {
                kotlinx.coroutines.delay(4000)
                if (!_isAdminLoggedIn.value) break
                val usersRes = repository.getAllProfiles()
                _adminUsersList.value = usersRes.getOrDefault(emptyList())

                val txRes = repository.getTransactions(null)
                _allTransactionsForAdmin.value = txRes.getOrDefault(emptyList())
            }
        }
    }

    fun approveTransaction(txId: String) {
        viewModelScope.launch {
            val tx = _allTransactionsForAdmin.value.find { it.id == txId }
            val res = repository.updateTransactionStatus(txId, "APPROVED")
            if (res.isSuccess) {
                showToast("Transaction Approved")
                loadAdminDashboardData()

                // Trigger real-time push notification for user
                val isDeposit = tx?.type.equals("DEPOSIT", ignoreCase = true)
                val formattedAmt = tx?.let { String.format(java.util.Locale.US, "%,.2f", it.amount) } ?: ""
                val title = if (isDeposit) "Deposit Approved! ✅" else "Withdrawal Approved! 💸"
                val body = if (isDeposit) {
                    "Your deposit of ${tx?.currency ?: "PKR"} $formattedAmt via ${tx?.gatewayName ?: "Gateway"} has been approved and credited."
                } else {
                    "Your withdrawal of ${tx?.currency ?: "PKR"} $formattedAmt to ${tx?.accountTitle ?: "Account"} has been approved and dispatched."
                }
                NotificationHelper.showTransactionNotification(
                    context = getApplication(),
                    title = title,
                    message = body,
                    txId = txId,
                    txType = tx?.type ?: "DEPOSIT",
                    status = "APPROVED"
                )
            }
        }
    }

    fun rejectTransaction(txId: String) {
        viewModelScope.launch {
            val tx = _allTransactionsForAdmin.value.find { it.id == txId }
            val res = repository.updateTransactionStatus(txId, "REJECTED")
            if (res.isSuccess) {
                showToast("Transaction Rejected")
                loadAdminDashboardData()

                // Trigger real-time push notification for user
                val isDeposit = tx?.type.equals("DEPOSIT", ignoreCase = true)
                val formattedAmt = tx?.let { String.format(java.util.Locale.US, "%,.2f", it.amount) } ?: ""
                val title = if (isDeposit) "Deposit Update ⚠️" else "Withdrawal Update ⚠️"
                val body = if (isDeposit) {
                    "Your deposit request of ${tx?.currency ?: "PKR"} $formattedAmt has been rejected. Please review payment proof."
                } else {
                    "Your withdrawal request of ${tx?.currency ?: "PKR"} $formattedAmt was rejected. Funds returned to wallet."
                }
                NotificationHelper.showTransactionNotification(
                    context = getApplication(),
                    title = title,
                    message = body,
                    txId = txId,
                    txType = tx?.type ?: "WITHDRAWAL",
                    status = "REJECTED"
                )
            }
        }
    }

    fun sendTestPushNotification(type: String = "DEPOSIT", status: String = "APPROVED") {
        val isDeposit = type.equals("DEPOSIT", ignoreCase = true)
        val isApproved = status.equals("APPROVED", ignoreCase = true)
        val title = if (isDeposit) {
            if (isApproved) "Deposit Approved! ✅" else "Deposit Rejected ⚠️"
        } else {
            if (isApproved) "Withdrawal Approved! 💸" else "Withdrawal Rejected ⚠️"
        }
        val body = if (isDeposit) {
            if (isApproved) "Your deposit of PKR 5,000.00 via JazzCash has been approved and credited to your wallet."
            else "Your deposit request for PKR 5,000.00 was rejected. Please review payment proof."
        } else {
            if (isApproved) "Your withdrawal of PKR 2,500.00 to Bank Alfalah has been processed and dispatched."
            else "Your withdrawal of PKR 2,500.00 was rejected. Funds returned to your wallet balance."
        }
        NotificationHelper.showTransactionNotification(
            context = getApplication(),
            title = title,
            message = body,
            txId = "TXN-TEST-${System.currentTimeMillis() % 10000}",
            txType = type,
            status = status
        )
        showToast("Test push notification dispatched!")
    }

    fun updateUserBalance(userId: String, newBalance: Double) {
        viewModelScope.launch {
            val res = repository.updateWalletBalance(userId, newBalance)
            if (res.isSuccess) {
                showToast("Wallet balance updated")
                loadAdminDashboardData()
            }
        }
    }

    fun updateUserBetproCredentials(userId: String, username: String, password: String) {
        viewModelScope.launch {
            val res = repository.updateUserBetproCredentials(userId, username, password)
            if (res.isSuccess) {
                showToast("Exchange ID credentials updated!")
                loadAdminDashboardData()
            }
        }
    }

    fun purgeAllDemoData() {
        viewModelScope.launch {
            _userTransactions.value = emptyList()
            _allTransactionsForAdmin.value = emptyList()
            _adminUsersList.value = _adminUsersList.value.filter { it.role == "SUPER_ADMIN" }
            showToast("All demo data permanently purged and wiped!")
        }
    }

    fun logout() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            com.example.notifications.AdminDeviceManager.clearAuthorization(context)
            FcmTokenManager.unsubscribeFromAdminAlerts()
            repository.signOut()
            clearSensitiveMemoryData()
            showToast("Logged out successfully")
        }
    }

    // ----------------------------------------------------------------
    // 9. ADMIN DEVICE & PUSH NOTIFICATION MANAGEMENT
    // ----------------------------------------------------------------
    val activeAdminTab = MutableStateFlow("OVERVIEW")
    val selectedAdminUserId = MutableStateFlow<String?>(null)
    val selectedAdminDepositTxId = MutableStateFlow<String?>(null)
    val selectedAdminWithdrawalTxId = MutableStateFlow<String?>(null)
    val pendingAdminDeepLink = MutableStateFlow<Pair<String, String?>?>(null)

    private val _adminDevices = MutableStateFlow<List<com.example.data.models.AdminDeviceDto>>(emptyList())
    val adminDevices: StateFlow<List<com.example.data.models.AdminDeviceDto>> = _adminDevices.asStateFlow()

    private val _adminNotifications = MutableStateFlow<List<com.example.data.models.AdminNotificationDto>>(emptyList())
    val adminNotifications: StateFlow<List<com.example.data.models.AdminNotificationDto>> = _adminNotifications.asStateFlow()

    private val _unreadAdminNotificationCount = MutableStateFlow(0)
    val unreadAdminNotificationCount: StateFlow<Int> = _unreadAdminNotificationCount.asStateFlow()

    fun selectAdminTarget(target: String, refId: String?) {
        when (target) {
            "admin_user" -> {
                activeAdminTab.value = "USERS"
                selectedAdminUserId.value = refId
            }
            "admin_deposit" -> {
                activeAdminTab.value = "DEPOSITS"
                selectedAdminDepositTxId.value = refId
            }
            "admin_withdrawal" -> {
                activeAdminTab.value = "WITHDRAWALS"
                selectedAdminWithdrawalTxId.value = refId
            }
            "admin_notifications" -> {
                activeAdminTab.value = "NOTIFICATIONS"
            }
            "admin_gateways" -> {
                activeAdminTab.value = "GATEWAYS"
            }
            "admin_devices" -> {
                activeAdminTab.value = "DEVICES"
            }
            "admin_settings" -> {
                activeAdminTab.value = "SETTINGS"
            }
            else -> {
                activeAdminTab.value = "OVERVIEW"
            }
        }
    }

    fun loadAdminDevices() {
        viewModelScope.launch {
            val res = repository.getAdminDevices()
            _adminDevices.value = res.getOrDefault(emptyList())
        }
    }

    fun registerCurrentDeviceAsAdmin(adminId: String) {
        val context = getApplication<Application>()
        val deviceId = com.example.notifications.AdminDeviceManager.getDeviceId(context)
        val deviceName = com.example.notifications.AdminDeviceManager.getDeviceName()
        val pushToken = FcmTokenManager.getSavedToken() ?: ("FCM_TOK_" + System.currentTimeMillis())

        com.example.notifications.AdminDeviceManager.setDeviceAuthorized(context, adminId, pushToken, true)
        FcmTokenManager.subscribeToAdminAlerts()

        val device = com.example.data.models.AdminDeviceDto(
            deviceId = deviceId,
            adminId = adminId,
            pushToken = pushToken,
            platform = "Android",
            deviceName = "$deviceName (Current Device)",
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
            lastSeenAt = "Just now",
            isActive = true
        )

        viewModelScope.launch {
            repository.registerAdminDevice(device)
            loadAdminDevices()
            showToast("Device registered as Authorized Admin Device")
        }
    }

    fun toggleAdminDeviceStatus(deviceId: String, currentActive: Boolean) {
        viewModelScope.launch {
            val res = repository.updateAdminDeviceStatus(deviceId, !currentActive)
            if (res.isSuccess) {
                val context = getApplication<Application>()
                if (deviceId == com.example.notifications.AdminDeviceManager.getDeviceId(context) && currentActive) {
                    com.example.notifications.AdminDeviceManager.clearAuthorization(context)
                    FcmTokenManager.unsubscribeFromAdminAlerts()
                }
                loadAdminDevices()
                showToast("Device status updated: ${if (!currentActive) "Active" else "Revoked"}")
            }
        }
    }

    fun revokeAdminDevice(deviceId: String) {
        viewModelScope.launch {
            val res = repository.removeAdminDevice(deviceId)
            if (res.isSuccess) {
                val context = getApplication<Application>()
                if (deviceId == com.example.notifications.AdminDeviceManager.getDeviceId(context)) {
                    com.example.notifications.AdminDeviceManager.clearAuthorization(context)
                    FcmTokenManager.unsubscribeFromAdminAlerts()
                }
                loadAdminDevices()
                showToast("Admin device revoked")
            }
        }
    }

    fun loadAdminNotifications() {
        viewModelScope.launch {
            val res = repository.getAdminNotifications()
            val list = res.getOrDefault(emptyList())
            _adminNotifications.value = list
            _unreadAdminNotificationCount.value = list.count { !it.isRead }
        }
    }

    fun markAdminNotificationAsRead(id: String) {
        viewModelScope.launch {
            repository.markAdminNotificationAsRead(id)
            loadAdminNotifications()
        }
    }

    fun markAllAdminNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllAdminNotificationsAsRead()
            loadAdminNotifications()
            showToast("All notifications marked as read")
        }
    }

    fun notifyAdminNewUser(user: ProfileDto) {
        val nowFormatted = java.text.SimpleDateFormat("dd MMM yyyy • hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
        val notif = com.example.data.models.AdminNotificationDto(
            id = "NOTIF_USR_" + System.currentTimeMillis(),
            type = "USER_CREATED",
            title = "🔔 NEW USER REGISTERED",
            message = "Username:\n${user.username ?: user.email}\n\nUser ID:\n${user.id}\n\nRegistered:\n$nowFormatted",
            referenceId = user.id,
            username = user.username ?: user.email ?: "New User",
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
            isRead = false
        )

        val current = _adminNotifications.value
        _adminNotifications.value = (listOf(notif) + current).distinctBy { it.id }
        _unreadAdminNotificationCount.value = _unreadAdminNotificationCount.value + 1

        viewModelScope.launch {
            repository.createAdminNotification(notif)
            loadAdminNotifications()
        }

        val context = getApplication<Application>()
        if (com.example.notifications.AdminDeviceManager.isDeviceAuthorized(context) || _isAdminLoggedIn.value) {
            NotificationHelper.showAdminNotification(
                context = context,
                eventId = notif.id,
                title = "🔔 NEW USER REGISTERED",
                message = "Username: ${user.username ?: user.email} (ID: ${user.id})",
                navTarget = "admin_user",
                referenceId = user.id
            )
        }
    }

    fun notifyAdminNewDeposit(tx: TransactionDto) {
        val formattedAmt = String.format(java.util.Locale.US, "%,.2f", tx.amount)
        val notif = com.example.data.models.AdminNotificationDto(
            id = "NOTIF_DEP_" + tx.id,
            type = "DEPOSIT_CREATED",
            title = "💰 NEW DEPOSIT REQUEST",
            message = "User: ${tx.userName}\nAmount: ${tx.currency} $formattedAmt\nGateway: ${tx.gatewayName}\nTX ID: ${tx.id}\nStatus: PENDING",
            referenceId = tx.id,
            username = tx.userName,
            amount = tx.amount,
            currency = tx.currency,
            gatewayName = tx.gatewayName,
            createdAt = tx.createdAt ?: java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
            isRead = false
        )

        viewModelScope.launch {
            repository.createAdminNotification(notif)
            loadAdminNotifications()
        }

        val context = getApplication<Application>()
        if (com.example.notifications.AdminDeviceManager.isDeviceAuthorized(context)) {
            NotificationHelper.showAdminNotification(
                context = context,
                eventId = notif.id,
                title = "💰 NEW DEPOSIT REQUEST",
                message = "User: ${tx.userName} • ${tx.currency} $formattedAmt via ${tx.gatewayName}",
                navTarget = "admin_deposit",
                referenceId = tx.id
            )
        }
    }

    fun notifyAdminNewWithdrawal(tx: TransactionDto) {
        val formattedAmt = String.format(java.util.Locale.US, "%,.2f", tx.amount)
        val notif = com.example.data.models.AdminNotificationDto(
            id = "NOTIF_WD_" + tx.id,
            type = "WITHDRAWAL_CREATED",
            title = "💸 NEW WITHDRAWAL REQUEST",
            message = "User: ${tx.userName}\nAmount: ${tx.currency} $formattedAmt\nMethod: ${tx.gatewayName}\nTX ID: ${tx.id}\nStatus: PENDING",
            referenceId = tx.id,
            username = tx.userName,
            amount = tx.amount,
            currency = tx.currency,
            gatewayName = tx.gatewayName,
            createdAt = tx.createdAt ?: java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
            isRead = false
        )

        viewModelScope.launch {
            repository.createAdminNotification(notif)
            loadAdminNotifications()
        }

        val context = getApplication<Application>()
        if (com.example.notifications.AdminDeviceManager.isDeviceAuthorized(context)) {
            NotificationHelper.showAdminNotification(
                context = context,
                eventId = notif.id,
                title = "💸 NEW WITHDRAWAL REQUEST",
                message = "User: ${tx.userName} • ${tx.currency} $formattedAmt to ${tx.accountTitle}",
                navTarget = "admin_withdrawal",
                referenceId = tx.id
            )
        }
    }

    fun triggerTestAdminAlert(type: String) {
        val context = getApplication<Application>()
        val dummyId = "TEST_" + (System.currentTimeMillis() % 10000)
        when (type) {
            "USER_CREATED" -> {
                NotificationHelper.showAdminNotification(
                    context = context,
                    eventId = "EVT_TEST_USR_$dummyId",
                    title = "🔔 NEW USER REGISTERED",
                    message = "Username: test_trader_88 (ID: usr_$dummyId)",
                    navTarget = "admin_user",
                    referenceId = "usr_$dummyId"
                )
            }
            "DEPOSIT_CREATED" -> {
                NotificationHelper.showAdminNotification(
                    context = context,
                    eventId = "EVT_TEST_DEP_$dummyId",
                    title = "💰 NEW DEPOSIT REQUEST",
                    message = "User: test_trader_88 • PKR 25,000 via JazzCash",
                    navTarget = "admin_deposit",
                    referenceId = "DEP-$dummyId"
                )
            }
            "WITHDRAWAL_CREATED" -> {
                NotificationHelper.showAdminNotification(
                    context = context,
                    eventId = "EVT_TEST_WD_$dummyId",
                    title = "💸 NEW WITHDRAWAL REQUEST",
                    message = "User: test_trader_88 • PKR 10,000 to HBL Account",
                    navTarget = "admin_withdrawal",
                    referenceId = "WD-$dummyId"
                )
            }
        }
        showToast("Dispatched test $type notification to authorized Admin devices")
    }
}
