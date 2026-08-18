# ProGuard rules for BP Wallet Android Application

# Keep Supabase & Ktor DTOs and Serializability
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Kotlinx Serialization
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
    @kotlinx.serialization.SerialName *;
    public static final ** Companion;
}
-keepclassmembers class * {
    *** Companion;
}
-keep class kotlinx.serialization.** { *; }

# Supabase SDK Keep Rules
-keep class io.github.jan.supabase.** { *; }
-keep interface io.github.jan.supabase.** { *; }

# Ktor HTTP Client Keep Rules
-keep class io.ktor.** { *; }
-keep interface io.ktor.** { *; }

# App Specific Data Models & DTOs
-keep class com.example.data.supabase.** { *; }
-keep class com.example.data.model.** { *; }
-keep class com.example.ui.UserSession { *; }

# Moshi JSON Converter Rules
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}

# Room Database Keep Rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Jetpack Compose Keep Rules
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
