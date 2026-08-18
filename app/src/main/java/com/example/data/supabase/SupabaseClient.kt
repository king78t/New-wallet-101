package com.example.data.supabase

import io.github.jan.supabase.SupabaseClient

object SupabaseClient {
    val instance: SupabaseClient?
        get() = SupabaseClientProvider.client

    fun isConfigured(): Boolean = SupabaseClientProvider.isConfigured()
}
