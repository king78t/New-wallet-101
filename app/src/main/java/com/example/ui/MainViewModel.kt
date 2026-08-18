package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.supabase.PaymentGatewayDto
import com.example.data.supabase.ProfileDto
import com.example.data.supabase.SupabaseRepository
import com.example.data.supabase.TransactionDto
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
    val isBlocked: Boolean = false
)

class MainViewModel(
    val supabaseRepo: SupabaseRepository = SupabaseRepository()
) : ViewModel() {

    // Global Toast
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // Supabase Status
    private val _isSupabaseConfigured = MutableStateFlow(supabaseRepo.isSupabaseConfigured())
    val isSupabaseConfigured: StateFlow<Boolean> = _isSupabaseConfigured.asStateFlow()

    private val _supabaseStatusMessage = MutableStateFlow(
        if (supabaseRepo.isSupabaseConfigured()) "Supabase Live Connected (${supabaseRepo.getSupabaseUrl()})"
        else "Supabase Auth Credentials Missing in AI Studio Secrets"
    )
    val supabaseStatusMessage: StateFlow<String> = _supabaseStatusMessage.asStateFlow()

    // Current Active User / Session State
    private val _currentUser = MutableStateFlow<UserSession?>(null)
    val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    // Exchange Website URL
    private val _exchangeUrl = MutableStateFlow("https://bpexch.live")
    val exchangeUrl: StateFlow<String> = _exchangeUrl.asStateFlow()

    fun updateExchangeUrl(newUrl: String) {
        _exchangeUrl.value = newUrl
        showToast("Exchange URL updated successfully")
    }

    init {
        restoreExistingSession()
    }

    fun restoreExistingSession(
        onUserFound: (() -> Unit)? = null,
        onAdminFound: (() -> Unit)? = null,
        onNoneFound: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            if (!supabaseRepo.isSupabaseConfigured()) {
                onNoneFound?.invoke()
                return@launch
            }
            val res = supabaseRepo.getCurrentSessionProfile()
            val profile = res.getOrNull()
            if (profile != null) {
                val session = mapProfileToSession(profile)
                _currentUser.value = session
                if (session.role == "SUPER_ADMIN") {
                    _isAdminLoggedIn.value = true
                    onAdminFound?.invoke()
                } else {
                    _isAdminLoggedIn.value = false
                    loadUserPaymentGateways(session.currency)
                    onUserFound?.invoke()
                }
            } else {
                onNoneFound?.invoke()
            }
        }
    }

    private fun mapProfileToSession(p: ProfileDto): UserSession {
        return UserSession(
            id = p.id,
            email = p.email ?: "",
            username = p.username ?: "",
            fullName = p.fullName ?: "BP User",
            phone = p.phone ?: "",
            country = p.country ?: "Pakistan",
            currency = p.currency ?: "PKR",
            role = p.role,
            walletBalance = p.walletBalance,
            isApproved = p.isApproved,
            isBlocked = p.isBlocked
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

    fun performUserSignup(onNavigateToVerifyOtp: () -> Unit) {
        val fullName = regFullName.value.trim()
        val username = regUsername.value.trim()
        val email = regEmail.value.trim()
        val phone = regPhone.value.trim()
        val password = regPassword.value
        val confirmPassword = regConfirmPassword.value
        val country = regCountry.value
        val currency = regCurrency.value

        if (fullName.isBlank()) {
            regError.value = "Please enter your Full Name"
            return
        }
        if (username.isBlank()) {
            regError.value = "Please enter a Username"
            return
        }
        if (email.isBlank() || !email.contains("@")) {
            regError.value = "Please enter a valid Email Address"
            return
        }
        if (password.length < 6) {
            regError.value = "Password must be at least 6 characters"
            return
        }
        if (password != confirmPassword) {
            regError.value = "Passwords do not match"
            return
        }

        if (!supabaseRepo.isSupabaseConfigured()) {
            regError.value = "Supabase API credentials are not configured in AI Studio Secrets panel."
            return
        }

        regError.value = null
        isRegLoading.value = true

        viewModelScope.launch {
            val result = supabaseRepo.signUp(
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
                otpEmail.value = email
                isOtpForSignup.value = true
                startOtpCountdown()
                showToast("Verification code sent to $email")
                onNavigateToVerifyOtp()
            } else {
                regError.value = result.exceptionOrNull()?.message ?: "Signup failed. Please try again."
            }
        }
    }

    // ----------------------------------------------------------------
    // 2. OTP VERIFICATION (SIGNUP & PASSWORD RECOVERY)
    // ----------------------------------------------------------------
    val otpEmail = MutableStateFlow("")
    val otpDigits = MutableStateFlow(listOf("", "", "", "", "", ""))
    val otpError = MutableStateFlow<String?>(null)
    val isOtpLoading = MutableStateFlow(false)
    val isOtpForSignup = MutableStateFlow(true)
    val otpCountdown = MutableStateFlow(60)

    fun updateOtpDigit(index: Int, value: String) {
        if (value.length <= 1) {
            val list = otpDigits.value.toMutableList()
            list[index] = value
            otpDigits.value = list
            otpError.value = null
        } else if (value.length == 6) {
            otpDigits.value = value.map { it.toString() }
            otpError.value = null
        }
    }

    fun startOtpCountdown() {
        viewModelScope.launch {
            otpCountdown.value = 60
            while (otpCountdown.value > 0) {
                delay(1000)
                otpCountdown.value -= 1
            }
        }
    }

    fun resendOtp() {
        val email = otpEmail.value.trim()
        if (email.isBlank()) return

        viewModelScope.launch {
            val result = supabaseRepo.resetPasswordForEmail(email)
            if (result.isSuccess) {
                showToast("Resent OTP verification code to $email")
                startOtpCountdown()
            } else {
                otpError.value = result.exceptionOrNull()?.message ?: "Failed to resend OTP"
            }
        }
    }

    fun verifyOtp(onSuccess: () -> Unit) {
        val token = otpDigits.value.joinToString("")
        val email = otpEmail.value.trim()

        if (token.length < 6) {
            otpError.value = "Please enter the complete 6-digit OTP code"
            return
        }

        if (!supabaseRepo.isSupabaseConfigured()) {
            otpError.value = "Supabase API credentials missing in Secrets."
            return
        }

        otpError.value = null
        isOtpLoading.value = true

        viewModelScope.launch {
            val isSignup = isOtpForSignup.value
            val result = supabaseRepo.verifyOtp(email = email, token = token, isSignup = isSignup)

            if (result.isSuccess) {
                if (isSignup) {
                    // Create/update profile row in Supabase
                    val currentAuthProfile = supabaseRepo.getCurrentSessionProfile().getOrNull()
                    val profileToSave = ProfileDto(
                        id = currentAuthProfile?.id ?: "",
                        email = email,
                        username = regUsername.value.ifBlank { email.substringBefore("@") },
                        fullName = regFullName.value.ifBlank { "BP User" },
                        phone = regPhone.value,
                        country = regCountry.value,
                        currency = regCurrency.value,
                        role = "USER",
                        isApproved = true,
                        isBlocked = false,
                        walletBalance = 0.0
                    )
                    supabaseRepo.saveProfile(profileToSave)
                    _currentUser.value = mapProfileToSession(profileToSave)
                    isOtpLoading.value = false
                    showToast("Email verified! Account created successfully.")
                    onSuccess()
                } else {
                    isOtpLoading.value = false
                    showToast("OTP verified! Set your new password.")
                    onSuccess()
                }
            } else {
                isOtpLoading.value = false
                otpError.value = result.exceptionOrNull()?.message ?: "Invalid or expired OTP code."
            }
        }
    }

    // ----------------------------------------------------------------
    // 3. USER LOGIN
    // ----------------------------------------------------------------
    val loginEmail = MutableStateFlow("")
    val loginPassword = MutableStateFlow("")
    val loginError = MutableStateFlow<String?>(null)
    val isLoginLoading = MutableStateFlow(false)

    fun performUserLogin(onSuccess: () -> Unit) {
        val email = loginEmail.value.trim()
        val pass = loginPassword.value

        if (email.isBlank()) {
            loginError.value = "Please enter your Email Address"
            return
        }
        if (pass.isBlank()) {
            loginError.value = "Please enter your Password"
            return
        }

        if (!supabaseRepo.isSupabaseConfigured()) {
            loginError.value = "Supabase credentials not configured in AI Studio Secrets panel."
            return
        }

        loginError.value = null
        isLoginLoading.value = true

        viewModelScope.launch {
            val result = supabaseRepo.signIn(email, pass)
            isLoginLoading.value = false

            if (result.isSuccess) {
                val profile = result.getOrNull()
                if (profile != null) {
                    val session = mapProfileToSession(profile)
                    _currentUser.value = session
                    _isAdminLoggedIn.value = false
                    loadUserPaymentGateways(session.currency)
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
    // 4. ADMIN AUTHENTICATION (RBAC via SUPABASE AUTH)
    // ----------------------------------------------------------------
    val adminEmail = MutableStateFlow("")
    val adminPassword = MutableStateFlow("")
    val adminError = MutableStateFlow<String?>(null)
    val isAdminLoading = MutableStateFlow(false)

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
            // Check for explicit Super Admin credential (username: Boss, password: Asdf1234)
            val isBossCredentials = (inputIdentifier.equals("Boss", ignoreCase = true) ||
                    inputIdentifier.equals("boss@bpwallet.com", ignoreCase = true) ||
                    inputIdentifier.equals("boss@admin.com", ignoreCase = true)) &&
                    pass == "Asdf1234"

            if (isBossCredentials) {
                val bossSession = UserSession(
                    id = "super_admin_boss",
                    email = "boss@bpwallet.com",
                    username = "Boss",
                    fullName = "Boss (Super Admin)",
                    phone = "+923000000000",
                    country = "Pakistan",
                    currency = "PKR",
                    role = "SUPER_ADMIN",
                    walletBalance = 0.0,
                    isApproved = true,
                    isBlocked = false
                )
                _currentUser.value = bossSession
                _isAdminLoggedIn.value = true
                isAdminLoading.value = false
                loadAdminDashboardData()
                showToast("Welcome Super Admin Boss!")
                onSuccess()
                return@launch
            }

            if (!supabaseRepo.isSupabaseConfigured()) {
                isAdminLoading.value = false
                adminError.value = "Invalid Super Admin credentials."
                return@launch
            }

            val result = supabaseRepo.signIn(inputIdentifier, pass)
            isAdminLoading.value = false

            if (result.isSuccess) {
                val profile = result.getOrNull()
                if (profile != null && profile.role.equals("SUPER_ADMIN", ignoreCase = true)) {
                    val session = mapProfileToSession(profile)
                    _currentUser.value = session
                    _isAdminLoggedIn.value = true
                    loadAdminDashboardData()
                    showToast("Super Admin Logged In Successfully!")
                    onSuccess()
                } else {
                    // Sign out immediately if not SUPER_ADMIN
                    supabaseRepo.signOut()
                    adminError.value = "Access Denied: Account is not authorized as Super Admin."
                }
            } else {
                adminError.value = result.exceptionOrNull()?.message ?: "Admin authentication failed. Check credentials."
            }
        }
    }

    // ----------------------------------------------------------------
    // 5. FORGOT & RESET PASSWORD
    // ----------------------------------------------------------------
    val forgotEmail = MutableStateFlow("")
    val forgotError = MutableStateFlow<String?>(null)
    val isForgotLoading = MutableStateFlow(false)

    fun sendPasswordReset(onProceedToOtp: () -> Unit) {
        val email = forgotEmail.value.trim()
        if (email.isBlank() || !email.contains("@")) {
            forgotError.value = "Please enter a valid Email Address"
            return
        }

        if (!supabaseRepo.isSupabaseConfigured()) {
            forgotError.value = "Supabase credentials missing in Secrets."
            return
        }

        forgotError.value = null
        isForgotLoading.value = true

        viewModelScope.launch {
            val result = supabaseRepo.resetPasswordForEmail(email)
            isForgotLoading.value = false

            if (result.isSuccess) {
                otpEmail.value = email
                isOtpForSignup.value = false
                startOtpCountdown()
                showToast("Password reset code sent to $email")
                onProceedToOtp()
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
    // 6. PAYMENT GATEWAYS & CURRENCY FILTERING (CRITICAL REQUIREMENT)
    // ----------------------------------------------------------------
    private val _paymentGateways = MutableStateFlow<List<PaymentGatewayDto>>(emptyList())
    val paymentGateways: StateFlow<List<PaymentGatewayDto>> = _paymentGateways.asStateFlow()

    fun loadUserPaymentGateways(userCurrency: String) {
        viewModelScope.launch {
            if (supabaseRepo.isSupabaseConfigured()) {
                val res = supabaseRepo.getPaymentGateways(userCurrency)
                _paymentGateways.value = res.getOrDefault(emptyList())
            } else {
                _paymentGateways.value = emptyList()
            }
        }
    }

    fun loadAdminPaymentGateways() {
        viewModelScope.launch {
            if (supabaseRepo.isSupabaseConfigured()) {
                val res = supabaseRepo.getAllPaymentGatewaysForAdmin()
                _paymentGateways.value = res.getOrDefault(emptyList())
            }
        }
    }

    fun createPaymentGateway(gateway: PaymentGatewayDto) {
        viewModelScope.launch {
            if (supabaseRepo.isSupabaseConfigured()) {
                val res = supabaseRepo.createPaymentGateway(gateway)
                if (res.isSuccess) {
                    showToast("Payment Gateway added for ${gateway.currency}")
                    loadAdminPaymentGateways()
                } else {
                    showToast("Error: ${res.exceptionOrNull()?.message}")
                }
            }
        }
    }

    fun deletePaymentGateway(id: Long) {
        viewModelScope.launch {
            if (supabaseRepo.isSupabaseConfigured()) {
                val res = supabaseRepo.deletePaymentGateway(id)
                if (res.isSuccess) {
                    showToast("Payment Gateway deleted")
                    loadAdminPaymentGateways()
                }
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
            if (supabaseRepo.isSupabaseConfigured()) {
                val res = supabaseRepo.getTransactions(user.id)
                _userTransactions.value = res.getOrDefault(emptyList())
            }
        }
    }

    fun submitDepositRequest(
        amount: Double,
        gatewayName: String,
        accountTitle: String,
        accountNumber: String,
        senderName: String,
        txRef: String,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        val tx = TransactionDto(
            id = "TX_" + System.currentTimeMillis(),
            userId = user.id,
            userName = user.fullName,
            type = "DEPOSIT",
            amount = amount,
            currency = user.currency,
            gatewayName = gatewayName,
            accountTitle = accountTitle,
            accountNumber = accountNumber,
            senderName = senderName,
            transactionRef = txRef,
            status = "PENDING"
        )

        viewModelScope.launch {
            if (supabaseRepo.isSupabaseConfigured()) {
                val res = supabaseRepo.createTransaction(tx)
                if (res.isSuccess) {
                    showToast("Deposit request submitted successfully!")
                    loadUserTransactions()
                    onSuccess()
                } else {
                    showToast("Error: ${res.exceptionOrNull()?.message}")
                }
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
        if (amount > user.walletBalance) {
            showToast("Insufficient wallet balance.")
            return
        }

        val tx = TransactionDto(
            id = "TX_" + System.currentTimeMillis(),
            userId = user.id,
            userName = user.fullName,
            type = "WITHDRAWAL",
            amount = amount,
            currency = user.currency,
            gatewayName = gatewayName,
            accountTitle = accountTitle,
            accountNumber = accountNumber,
            status = "PENDING"
        )

        viewModelScope.launch {
            if (supabaseRepo.isSupabaseConfigured()) {
                val res = supabaseRepo.createTransaction(tx)
                if (res.isSuccess) {
                    showToast("Withdrawal request submitted!")
                    loadUserTransactions()
                    onSuccess()
                } else {
                    showToast("Error: ${res.exceptionOrNull()?.message}")
                }
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
            if (supabaseRepo.isSupabaseConfigured()) {
                val usersRes = supabaseRepo.getAllProfiles()
                _adminUsersList.value = usersRes.getOrDefault(emptyList())

                val txRes = supabaseRepo.getTransactions(null)
                _allTransactionsForAdmin.value = txRes.getOrDefault(emptyList())

                loadAdminPaymentGateways()
            }
        }
    }

    fun approveTransaction(txId: String) {
        viewModelScope.launch {
            if (supabaseRepo.isSupabaseConfigured()) {
                val res = supabaseRepo.updateTransactionStatus(txId, "APPROVED")
                if (res.isSuccess) {
                    showToast("Transaction Approved")
                    loadAdminDashboardData()
                }
            }
        }
    }

    fun rejectTransaction(txId: String) {
        viewModelScope.launch {
            if (supabaseRepo.isSupabaseConfigured()) {
                val res = supabaseRepo.updateTransactionStatus(txId, "REJECTED")
                if (res.isSuccess) {
                    showToast("Transaction Rejected")
                    loadAdminDashboardData()
                }
            }
        }
    }

    fun updateUserBalance(userId: String, newBalance: Double) {
        viewModelScope.launch {
            if (supabaseRepo.isSupabaseConfigured()) {
                val res = supabaseRepo.updateWalletBalance(userId, newBalance)
                if (res.isSuccess) {
                    showToast("Wallet balance updated")
                    loadAdminDashboardData()
                }
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
            if (supabaseRepo.isSupabaseConfigured()) {
                supabaseRepo.signOut()
            }
            _currentUser.value = null
            _isAdminLoggedIn.value = false
            loginEmail.value = ""
            loginPassword.value = ""
            adminEmail.value = ""
            adminPassword.value = ""
            showToast("Logged out successfully")
        }
    }
}
