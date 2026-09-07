package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.data.models.TransactionDto
import com.example.data.supabase.SupabaseClientProvider
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.minutes

data class PaymentProofUploadData(
    val localFile: File,
    val bytes: ByteArray,
    val base64Thumbnail: String,
    val localUri: String,
    val storagePath: String
)

data class CachedSignedUrl(
    val url: String,
    val expiresAtMillis: Long
)

object PaymentProofManager {
    private const val TAG = "PaymentProofManager"
    const val BUCKET_NAME = "payment-proofs"
    const val STORAGE_FOLDER = "deposits"
    private const val MAX_DIMENSION = 1920
    private const val JPEG_QUALITY = 85

    // In-memory cache for temporary signed URLs (never persisted in DB)
    private val signedUrlCache = ConcurrentHashMap<String, CachedSignedUrl>()

    fun getProofDirectory(context: Context): File {
        val dir = File(context.filesDir, "payment_proofs")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getLocalProofFile(context: Context, txId: String): File {
        return File(getProofDirectory(context), "${txId}.jpg")
    }

    fun canonicalStoragePath(txId: String): String {
        return "$BUCKET_NAME/$STORAGE_FOLDER/${txId}.jpg"
    }

    fun cleanObjectPath(ref: String): String {
        return ref.removePrefix("$BUCKET_NAME/")
            .removePrefix("/$BUCKET_NAME/")
            .removePrefix("/")
            .trim()
    }

    suspend fun processAndSaveProof(
        context: Context,
        uri: Uri,
        txId: String
    ): PaymentProofUploadData? = withContext(Dispatchers.IO) {
        try {
            // 1. Decode bounds first to calculate inSampleSize
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return@withContext null

            var sampleSize = 1
            var w = options.outWidth
            var h = options.outHeight
            while (w > MAX_DIMENSION || h > MAX_DIMENSION) {
                sampleSize *= 2
                w /= 2
                h /= 2
            }

            // 2. Decode scaled bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }

            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from uri: $uri")
                return@withContext null
            }

            // 3. Compress to JPEG bytes
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, bos)
            val compressedBytes = bos.toByteArray()
            bitmap.recycle()

            // 4. Save to persistent local storage for local cache & offline resilience
            val localFile = getLocalProofFile(context, txId)
            localFile.writeBytes(compressedBytes)
            Log.d(TAG, "Saved local proof to: ${localFile.absolutePath} (${compressedBytes.size} bytes)")

            // 5. Generate compact Base64 thumbnail string for instant UI feedback
            val base64String = "data:image/jpeg;base64," + Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
            val storagePath = "$STORAGE_FOLDER/${txId}.jpg"

            PaymentProofUploadData(
                localFile = localFile,
                bytes = compressedBytes,
                base64Thumbnail = base64String,
                localUri = "file://${localFile.absolutePath}",
                storagePath = storagePath
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error processing payment proof: ${e.message}", e)
            null
        }
    }

    /**
     * Uploads bytes to the PRIVATE Supabase Storage bucket.
     * Returns the persistent canonical Storage object path (e.g. "payment-proofs/deposits/{tx_id}.jpg").
     * Under NO circumstance does it return a temporary signed URL, public URL, or file:// uri.
     */
    suspend fun uploadToSupabase(
        data: PaymentProofUploadData,
        txId: String
    ): String = withContext(Dispatchers.IO) {
        val persistentPath = canonicalStoragePath(txId)

        if (!SupabaseClientProvider.isConfigured()) {
            Log.d(TAG, "Supabase is not configured, persistent Storage path registered: $persistentPath")
            return@withContext persistentPath
        }

        try {
            val client = SupabaseClientProvider.client
            if (client != null) {
                val storage = client.storage
                val bucket = storage.from(BUCKET_NAME)

                // Upload bytes with upsert to private bucket
                bucket.upload(
                    path = data.storagePath,
                    data = data.bytes
                ) {
                    upsert = true
                }
                Log.d(TAG, "Successfully uploaded proof to private Supabase Storage: ${data.storagePath}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Supabase Storage upload error: ${e.message}. Using persistent path and local cache fallback.", e)
        }

        persistentPath
    }

    /**
     * Generates a fresh temporary signed URL for authorized viewing from the PRIVATE bucket.
     * Implements in-memory expiration tracking so expired URLs are automatically regenerated.
     */
    suspend fun getSignedProofUrl(
        ref: String,
        forceRefresh: Boolean = false
    ): String? = withContext(Dispatchers.IO) {
        val cleanPath = cleanObjectPath(ref)
        if (cleanPath.isBlank()) return@withContext null

        val now = System.currentTimeMillis()

        // 1. Check in-memory cache if not forced
        if (!forceRefresh) {
            val cached = signedUrlCache[cleanPath]
            if (cached != null && now < cached.expiresAtMillis) {
                Log.d(TAG, "Using valid cached signed URL for $cleanPath (expires in ${(cached.expiresAtMillis - now) / 1000}s)")
                return@withContext cached.url
            }
        }

        // 2. Request fresh signed URL from private Supabase Storage
        if (!SupabaseClientProvider.isConfigured()) {
            Log.d(TAG, "Supabase not configured; cannot generate remote signed URL for $cleanPath")
            return@withContext null
        }

        try {
            val client = SupabaseClientProvider.client ?: return@withContext null
            val bucket = client.storage.from(BUCKET_NAME)

            // 30 minute TTL for secure viewing session
            val rawSignedUrl = bucket.createSignedUrl(
                path = cleanPath,
                expiresIn = 30.minutes
            )

            val fullSignedUrl = if (rawSignedUrl.startsWith("http://") || rawSignedUrl.startsWith("https://")) {
                rawSignedUrl
            } else {
                "${SupabaseClientProvider.baseUrl}/storage/v1$rawSignedUrl"
            }

            // Cache for 25 minutes (5 min safety margin before expiration)
            val expiresAt = now + 25.minutes.inWholeMilliseconds
            signedUrlCache[cleanPath] = CachedSignedUrl(fullSignedUrl, expiresAt)
            Log.d(TAG, "Generated fresh Supabase signed URL for $cleanPath")

            fullSignedUrl
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create signed URL for $cleanPath: ${e.message}")
            signedUrlCache.remove(cleanPath)
            null
        }
    }

    /**
     * Fast synchronous check for instant UI preview.
     */
    fun resolveFastPreview(
        context: Context,
        tx: TransactionDto
    ): Any? {
        // 1. Local verified file on device
        if (tx.id.isNotBlank()) {
            val localFile = getLocalProofFile(context, tx.id)
            if (localFile.exists() && localFile.length() > 0) {
                return localFile
            }
        }

        val ref = tx.paymentProofPath?.takeIf { it.isNotBlank() }
            ?: tx.screenshotUrl?.takeIf { it.isNotBlank() }
            ?: return null

        // 2. Base64 data URI
        if (ref.startsWith("data:image/") || (!ref.startsWith("http") && !ref.startsWith("payment-proofs") && !ref.startsWith("deposits") && ref.length > 200)) {
            val base64Data = if (ref.contains(",")) ref.substringAfter(",") else ref
            return try {
                Base64.decode(base64Data, Base64.DEFAULT)
            } catch (_: Exception) { null }
        }

        // 3. Check memory signed URL cache
        val cleanPath = cleanObjectPath(ref)
        val cached = signedUrlCache[cleanPath]
        if (cached != null && System.currentTimeMillis() < cached.expiresAtMillis) {
            return cached.url
        }

        return null
    }

    /**
     * Resolves the best displayable model for Coil (File, ByteArray, or fresh Signed URL).
     * For private buckets, never resolves public URLs.
     */
    suspend fun resolveDisplayModelAsync(
        context: Context,
        tx: TransactionDto,
        forceRefresh: Boolean = false
    ): Any? = withContext(Dispatchers.IO) {
        // Priority 1: Check local persistent file
        if (tx.id.isNotBlank()) {
            val localFile = getLocalProofFile(context, tx.id)
            if (localFile.exists() && localFile.length() > 0) {
                return@withContext localFile
            }
        }

        val ref = tx.paymentProofPath?.takeIf { it.isNotBlank() }
            ?: tx.screenshotUrl?.takeIf { it.isNotBlank() }
            ?: return@withContext null

        // Priority 2: Base64 data URI -> decode to ByteArray
        if (ref.startsWith("data:image/") || (!ref.startsWith("http") && !ref.startsWith("payment-proofs") && !ref.startsWith("deposits") && ref.length > 200)) {
            val base64Data = if (ref.contains(",")) ref.substringAfter(",") else ref
            return@withContext try {
                Base64.decode(base64Data, Base64.DEFAULT)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode base64: ${e.message}")
                null
            }
        }

        // Priority 3: Supabase Storage canonical object path -> generate fresh signed URL
        if (ref.startsWith("$BUCKET_NAME/") || ref.startsWith("$STORAGE_FOLDER/") || ref.contains("/$STORAGE_FOLDER/")) {
            val signedUrl = getSignedProofUrl(ref, forceRefresh = forceRefresh)
            if (signedUrl != null) {
                return@withContext signedUrl
            }
        }

        // Priority 4: Direct HTTP/HTTPS signed URL
        if (ref.startsWith("http://") || ref.startsWith("https://")) {
            return@withContext ref
        }

        null
    }
}

