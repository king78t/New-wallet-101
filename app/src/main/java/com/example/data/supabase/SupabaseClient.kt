package com.example.data.supabase

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class SupabaseClient {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    val baseUrl: String
        get() {
            val url = try { BuildConfig.SUPABASE_URL } catch (e: Throwable) { "" }
            return if (url.isNullOrBlank() || url.contains("your-project") || url.contains("demo.supabase.co")) "" else url.trimEnd('/')
        }

    val anonKey: String
        get() {
            val key = try { BuildConfig.SUPABASE_ANON_KEY } catch (e: Throwable) { "" }
            return if (key.isNullOrBlank() || key.contains("your-supabase-anon-key") || key == "demo-anon-key") "" else key.trim()
        }

    fun isConfigured(): Boolean {
        val url = baseUrl
        val key = anonKey
        return url.startsWith("https://") && key.length > 20 && !url.contains("your-project") && !key.contains("your-supabase-anon-key")
    }

    suspend fun pingConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext Result.failure(Exception("Supabase credentials not configured in AI Studio Secrets."))
        }
        try {
            val request = Request.Builder()
                .url("$baseUrl/rest/v1/")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .get()
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 401 || response.code == 404) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("HTTP Error ${response.code}: ${response.message}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------------------------------------
    // AUTHENTICATION APIs
    // ----------------------------------------------------------------

    suspend fun signUp(email: String, pass: String, fullName: String): Result<SupabaseAuthResponse> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext Result.success(
                SupabaseAuthResponse(
                    accessToken = "demo_jwt_token",
                    user = SupabaseUser(id = "USR_" + System.currentTimeMillis() % 10000, email = email)
                )
            )
        }
        try {
            val jsonBody = """
                {
                    "email": "$email",
                    "password": "$pass",
                    "data": { "full_name": "$fullName" }
                }
            """.trimIndent()

            val request = Request.Builder()
                .url("$baseUrl/auth/v1/signup")
                .addHeader("apikey", anonKey)
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toRequestBody(jsonMediaType))
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                val adapter = moshi.adapter(SupabaseAuthResponse::class.java)
                val parsed = try { adapter.fromJson(bodyStr) } catch (e: Exception) { null }

                if (response.isSuccessful && parsed != null) {
                    Result.success(parsed)
                } else {
                    Result.failure(Exception(parsed?.errorDescription ?: parsed?.error ?: "Signup failed (${response.code})"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, pass: String): Result<SupabaseAuthResponse> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext Result.success(
                SupabaseAuthResponse(
                    accessToken = "demo_jwt_access_token",
                    user = SupabaseUser(id = "USR_1001", email = email)
                )
            )
        }
        try {
            val jsonBody = """
                {
                    "email": "$email",
                    "password": "$pass"
                }
            """.trimIndent()

            val request = Request.Builder()
                .url("$baseUrl/auth/v1/token?grant_type=password")
                .addHeader("apikey", anonKey)
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toRequestBody(jsonMediaType))
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                val adapter = moshi.adapter(SupabaseAuthResponse::class.java)
                val parsed = try { adapter.fromJson(bodyStr) } catch (e: Exception) { null }

                if (response.isSuccessful && parsed != null) {
                    Result.success(parsed)
                } else {
                    Result.failure(Exception(parsed?.errorDescription ?: parsed?.error ?: "Invalid credentials or Auth error (${response.code})"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(email: String, token: String, type: String = "signup"): Result<SupabaseAuthResponse> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext Result.success(
                SupabaseAuthResponse(
                    accessToken = "demo_verified_token",
                    user = SupabaseUser(id = "USR_" + (System.currentTimeMillis() % 100000), email = email)
                )
            )
        }
        try {
            val jsonBody = """
                {
                    "type": "$type",
                    "email": "$email",
                    "token": "$token"
                }
            """.trimIndent()

            val request = Request.Builder()
                .url("$baseUrl/auth/v1/verify")
                .addHeader("apikey", anonKey)
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toRequestBody(jsonMediaType))
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                val adapter = moshi.adapter(SupabaseAuthResponse::class.java)
                val parsed = try { adapter.fromJson(bodyStr) } catch (e: Exception) { null }

                if (response.isSuccessful && parsed != null) {
                    Result.success(parsed)
                } else {
                    Result.failure(Exception(parsed?.errorDescription ?: parsed?.error ?: "Invalid OTP code (${response.code})"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resendOtp(email: String, type: String = "signup"): Result<Boolean> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext Result.success(true)
        }
        try {
            val jsonBody = """
                {
                    "type": "$type",
                    "email": "$email"
                }
            """.trimIndent()

            val request = Request.Builder()
                .url("$baseUrl/auth/v1/resend")
                .addHeader("apikey", anonKey)
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toRequestBody(jsonMediaType))
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Resend OTP failed (${response.code})"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------------------------------------
    // POSTGREST DATABASE APIs
    // ----------------------------------------------------------------

    suspend fun fetchUserSession(email: String): Result<SupabaseUserSessionDto?> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext Result.success(null) // Fallback to local session
        }
        try {
            val request = Request.Builder()
                .url("$baseUrl/rest/v1/user_sessions?email=eq.$email&select=*")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .get()
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: "[]"
                    val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, SupabaseUserSessionDto::class.java)
                    val adapter = moshi.adapter<List<SupabaseUserSessionDto>>(listType)
                    val list = adapter.fromJson(bodyStr)
                    Result.success(list?.firstOrNull())
                } else {
                    Result.failure(Exception("Failed to fetch user session (${response.code})"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveUserSession(session: SupabaseUserSessionDto): Result<Boolean> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext Result.success(true)
        }
        try {
            val adapter = moshi.adapter(SupabaseUserSessionDto::class.java)
            val jsonStr = adapter.toJson(session)

            val request = Request.Builder()
                .url("$baseUrl/rest/v1/user_sessions")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(jsonStr.toRequestBody(jsonMediaType))
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 201 || response.code == 204) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Upsert session failed (${response.code})"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExchangeConfig(): Result<SupabaseExchangeConfigDto?> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext Result.success(null)
        }
        try {
            val request = Request.Builder()
                .url("$baseUrl/rest/v1/exchange_config?id=eq.1&select=*")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .get()
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: "[]"
                    val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, SupabaseExchangeConfigDto::class.java)
                    val adapter = moshi.adapter<List<SupabaseExchangeConfigDto>>(listType)
                    val list = adapter.fromJson(bodyStr)
                    Result.success(list?.firstOrNull())
                } else {
                    Result.failure(Exception("Fetch exchange config failed (${response.code})"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveExchangeConfig(config: SupabaseExchangeConfigDto): Result<Boolean> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext Result.success(true)
        }
        try {
            val adapter = moshi.adapter(SupabaseExchangeConfigDto::class.java)
            val jsonStr = adapter.toJson(config)

            val request = Request.Builder()
                .url("$baseUrl/rest/v1/exchange_config")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(jsonStr.toRequestBody(jsonMediaType))
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 201 || response.code == 204) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Save exchange config failed (${response.code})"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
