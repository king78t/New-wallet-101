package com.example.data.supabase

import com.example.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseClientProvider {

    val baseUrl: String
        get() {
            val url = try { BuildConfig.SUPABASE_URL } catch (e: Throwable) { "" }
            return if (url.isBlank() || url.contains("your-project") || url.contains("demo.supabase.co")) "" else url.trimEnd('/')
        }

    val publishableKey: String
        get() {
            val key = try {
                val pub = BuildConfig.SUPABASE_PUBLISHABLE_KEY
                if (pub.isNotBlank()) pub else BuildConfig.SUPABASE_ANON_KEY
            } catch (e: Throwable) { "" }
            return if (key.isBlank() || key.contains("your-supabase") || key == "demo-anon-key") "" else key.trim()
        }

    fun isConfigured(): Boolean {
        val url = baseUrl
        val key = publishableKey
        return url.startsWith("https://") && key.length > 15
    }

    val client: SupabaseClient? by lazy {
        if (!isConfigured()) null
        else {
            createSupabaseClient(
                supabaseUrl = baseUrl,
                supabaseKey = publishableKey
            ) {
                install(Auth)
                install(Postgrest)
                install(Realtime)
            }
        }
    }
}
