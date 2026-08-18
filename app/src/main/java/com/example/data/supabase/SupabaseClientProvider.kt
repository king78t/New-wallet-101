package com.example.data.supabase

import com.example.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClientProvider {
    val baseUrl: String
        get() {
            val url = try { BuildConfig.SUPABASE_URL } catch (e: Throwable) { "" }
            return if (url.isNullOrBlank() || url.contains("your-project")) "" else url.trimEnd('/')
        }

    val anonKey: String
        get() {
            val key = try { BuildConfig.SUPABASE_ANON_KEY } catch (e: Throwable) { "" }
            return if (key.isNullOrBlank() || key.contains("your-supabase-anon-key")) "" else key.trim()
        }

    fun isConfigured(): Boolean {
        val url = baseUrl
        val key = anonKey
        return url.startsWith("https://") && key.length > 20
    }

    fun getSupabaseUrl(): String = baseUrl

    val client: SupabaseClient? by lazy {
        if (!isConfigured()) null
        else {
            createSupabaseClient(
                supabaseUrl = baseUrl,
                supabaseKey = anonKey
            ) {
                install(Auth)
                install(Postgrest)
            }
        }
    }
}
