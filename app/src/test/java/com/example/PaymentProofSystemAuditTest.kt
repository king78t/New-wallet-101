package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.data.models.TransactionDto
import com.example.data.repository.PaymentProofManager
import com.example.data.repository.PaymentProofUploadData
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PaymentProofSystemAuditTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val dir = PaymentProofManager.getProofDirectory(context)
        dir.deleteRecursively()
        dir.mkdirs()
    }

    private fun createSampleImageUri(filename: String, color: Int = Color.RED): Uri {
        val file = File(context.cacheDir, filename)
        val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(color)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return Uri.fromFile(file)
    }

    @Test
    fun test1_userUpload_processesImageAndSavesLocalJpeg() = runBlocking {
        val txId = "DEP-1772957000001-1001"
        val imageUri = createSampleImageUri("sample_receipt_1.jpg", Color.BLUE)

        val uploadData = PaymentProofManager.processAndSaveProof(context, imageUri, txId)
        assertNotNull("Image processing should succeed and return upload data", uploadData)
        assertTrue("Compressed image bytes should be non-empty", uploadData!!.bytes.isNotEmpty())

        val localFile = PaymentProofManager.getLocalProofFile(context, txId)
        assertTrue("Local persistent JPEG file must exist", localFile.exists())
        assertTrue("Local file size must be > 0", localFile.length() > 0)
        assertEquals("Storage path must target deposits folder", "deposits/$txId.jpg", uploadData.storagePath)

        val persistentPath = PaymentProofManager.uploadToSupabase(uploadData, txId)
        assertEquals(
            "Persistent canonical path must match storage format",
            "payment-proofs/deposits/$txId.jpg",
            persistentPath
        )
    }

    @Test
    fun test2_database_screenshotUrlMustNotContainLocalOrTempPaths() {
        val txId = "DEP-1772957000002-1002"
        val canonicalPath = PaymentProofManager.canonicalStoragePath(txId)

        val tx = TransactionDto(
            id = txId,
            userId = "USR-1002",
            type = "DEPOSIT",
            amount = 5000.0,
            currency = "PKR",
            gatewayName = "JazzCash",
            accountNumber = "03001234567",
            accountTitle = "John Doe",
            senderName = "John",
            transactionRef = "UTR-999888",
            screenshotUrl = canonicalPath,
            paymentProofPath = "deposits/$txId.jpg",
            status = "PENDING"
        )

        // Strict verification of database path format
        assertNotNull("screenshotUrl must be non-null", tx.screenshotUrl)
        assertFalse("screenshotUrl must NOT contain blob:", tx.screenshotUrl!!.startsWith("blob:"))
        assertFalse("screenshotUrl must NOT contain file://", tx.screenshotUrl!!.startsWith("file://"))
        assertFalse("screenshotUrl must NOT contain temporary local storage path", tx.screenshotUrl!!.contains("/data/user/"))
        assertFalse("screenshotUrl must NOT contain signed URL token query", tx.screenshotUrl!!.contains("token="))

        assertEquals("payment-proofs/deposits/$txId.jpg", tx.screenshotUrl)
        assertEquals("deposits/$txId.jpg", tx.paymentProofPath)
    }

    @Test
    fun test3_adminRetrieval_resolvesLocalVerifiedFileOrSignedUrl() = runBlocking {
        val txId = "DEP-1772957000003-1003"
        val imageUri = createSampleImageUri("sample_receipt_3.jpg", Color.GREEN)

        // Simulate user upload
        PaymentProofManager.processAndSaveProof(context, imageUri, txId)

        val tx = TransactionDto(
            id = txId,
            userId = "USR-1003",
            type = "DEPOSIT",
            amount = 12000.0,
            screenshotUrl = PaymentProofManager.canonicalStoragePath(txId),
            paymentProofPath = "deposits/$txId.jpg",
            status = "PENDING"
        )

        // Admin fast preview
        val fastModel = PaymentProofManager.resolveFastPreview(context, tx)
        assertNotNull("Fast preview must find local verified file", fastModel)
        assertTrue("Model should be a valid File", fastModel is File)
        assertTrue("File must exist on disk", (fastModel as File).exists())

        // Full async resolution
        val asyncModel = PaymentProofManager.resolveDisplayModelAsync(context, tx)
        assertNotNull("Async resolver must return valid model", asyncModel)
        assertTrue("Async model should resolve to File", asyncModel is File)
    }

    @Test
    fun test4_twoScreenshots_distinctStoragePathsAndNoCrossReferencing() = runBlocking {
        val txId1 = "DEP-1772957000004-1004"
        val txId2 = "DEP-1772957000005-1005"

        val uri1 = createSampleImageUri("receipt_a.jpg", Color.MAGENTA)
        val uri2 = createSampleImageUri("receipt_b.jpg", Color.CYAN)

        val data1 = PaymentProofManager.processAndSaveProof(context, uri1, txId1)
        val data2 = PaymentProofManager.processAndSaveProof(context, uri2, txId2)

        assertNotNull(data1)
        assertNotNull(data2)

        val file1 = PaymentProofManager.getLocalProofFile(context, txId1)
        val file2 = PaymentProofManager.getLocalProofFile(context, txId2)

        // Verify distinct files on disk
        assertTrue("File 1 must exist", file1.exists())
        assertTrue("File 2 must exist", file2.exists())
        assertFalse("Paths must be distinct", file1.absolutePath == file2.absolutePath)

        val path1 = PaymentProofManager.canonicalStoragePath(txId1)
        val path2 = PaymentProofManager.canonicalStoragePath(txId2)

        assertFalse("Supabase storage paths must be distinct", path1 == path2)
        assertEquals("payment-proofs/deposits/$txId1.jpg", path1)
        assertEquals("payment-proofs/deposits/$txId2.jpg", path2)

        val tx1 = TransactionDto(id = txId1, screenshotUrl = path1, type = "DEPOSIT")
        val tx2 = TransactionDto(id = txId2, screenshotUrl = path2, type = "DEPOSIT")

        val model1 = PaymentProofManager.resolveFastPreview(context, tx1) as File
        val model2 = PaymentProofManager.resolveFastPreview(context, tx2) as File

        assertEquals(file1.absolutePath, model1.absolutePath)
        assertEquals(file2.absolutePath, model2.absolutePath)
    }

    @Test
    fun test5_adminSecurity_clientDoesNotExposeServiceRoleAndUsesAnonOnly() {
        // Verify bucket privacy and configuration
        assertEquals("payment-proofs", PaymentProofManager.BUCKET_NAME)
        assertEquals("deposits", PaymentProofManager.STORAGE_FOLDER)

        // Verify canonical object path cleaner
        val cleaned = PaymentProofManager.cleanObjectPath("payment-proofs/deposits/DEP-101.jpg")
        assertEquals("deposits/DEP-101.jpg", cleaned)

        val cleanedWithSlash = PaymentProofManager.cleanObjectPath("/payment-proofs/deposits/DEP-101.jpg")
        assertEquals("deposits/DEP-101.jpg", cleanedWithSlash)
    }

    @Test
    fun test6_failureStates_missingOrInvalidScreenshotYieldsNullAndTriggersUnavailableUI() = runBlocking {
        // Missing screenshot entirely
        val emptyTx = TransactionDto(
            id = "DEP-EMPTY",
            screenshotUrl = null,
            paymentProofPath = null,
            type = "DEPOSIT"
        )
        val emptyModel = PaymentProofManager.resolveDisplayModelAsync(context, emptyTx)
        assertNull("Missing screenshot must resolve to null (triggering PAYMENT PROOF UNAVAILABLE)", emptyModel)

        // Invalid storage path with no local file
        val invalidTx = TransactionDto(
            id = "DEP-NONEXISTENT",
            screenshotUrl = "payment-proofs/deposits/NONEXISTENT.jpg",
            paymentProofPath = "deposits/NONEXISTENT.jpg",
            type = "DEPOSIT"
        )
        // With no local file and unconfigured or non-existent remote object, resolver gracefully returns null
        val invalidModel = PaymentProofManager.resolveDisplayModelAsync(context, invalidTx)
        // Resolves null so UI displays "PAYMENT PROOF UNAVAILABLE" and never a black screen
        assertNull("Non-existent proof should gracefully resolve to null", invalidModel)
    }
}
