package com.example.ui.components

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.models.TransactionDto
import com.example.data.repository.PaymentProofManager

private const val TAG = "PaymentProofImage"

@Composable
fun PaymentProofImage(
    transaction: TransactionDto,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    isThumbnail: Boolean = false,
    onImageClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var retryKey by remember { mutableIntStateOf(0) }
    var resolvedModel by remember(transaction.id, transaction.screenshotUrl, retryKey) {
        mutableStateOf(PaymentProofManager.resolveFastPreview(context, transaction))
    }
    var isResolving by remember(transaction.id, transaction.screenshotUrl, retryKey) {
        mutableStateOf(resolvedModel == null)
    }

    LaunchedEffect(transaction.id, transaction.screenshotUrl, retryKey) {
        val model = PaymentProofManager.resolveDisplayModelAsync(
            context = context,
            tx = transaction,
            forceRefresh = (retryKey > 0)
        )
        resolvedModel = model
        isResolving = false
        Log.d(TAG, "Resolved model for tx ${transaction.id}: $resolvedModel")
    }

    val shape = RoundedCornerShape(if (isThumbnail) 10.dp else 16.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFFF1F5F9))
            .border(1.dp, Color(0xFFE2E8F0), shape)
            .then(if (onImageClick != null) Modifier.clickable { onImageClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (isResolving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(if (isThumbnail) 20.dp else 36.dp),
                    color = Color(0xFF059669),
                    strokeWidth = if (isThumbnail) 2.dp else 3.dp
                )
            }
        } else if (resolvedModel == null) {
            // STATE 3: Missing / Empty proof
            ProofUnavailableCard(
                isThumbnail = isThumbnail,
                reason = "Payment screenshot unavailable",
                onRetry = { retryKey++ }
            )
        } else {
            val imageRequest = remember(resolvedModel, retryKey, transaction.id) {
                ImageRequest.Builder(context)
                    .data(resolvedModel)
                    .crossfade(true)
                    .memoryCacheKey("${transaction.id}_${retryKey}")
                    .diskCacheKey("${transaction.id}_${retryKey}")
                    .listener(
                        onError = { _, result ->
                            Log.e(TAG, "Image loading error for tx ${transaction.id}: ${result.throwable.message}", result.throwable)
                        },
                        onSuccess = { _, _ ->
                            Log.d(TAG, "Image successfully loaded for tx ${transaction.id}")
                        }
                    )
                    .build()
            }

            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = "Payment Proof Screenshot",
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                loading = {
                    // STATE 1: Loading
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF8FAFC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(if (isThumbnail) 4.dp else 16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(if (isThumbnail) 20.dp else 36.dp),
                                color = Color(0xFF059669),
                                strokeWidth = if (isThumbnail) 2.dp else 3.dp
                            )
                            if (!isThumbnail) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Loading payment proof...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                },
                error = {
                    // STATE 3: Error / Failed to load
                    ProofUnavailableCard(
                        isThumbnail = isThumbnail,
                        reason = "Unable to retrieve payment proof",
                        onRetry = { retryKey++ }
                    )
                }
            )
        }
    }
}

@Composable
private fun ProofUnavailableCard(
    isThumbnail: Boolean,
    reason: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(if (isThumbnail) 4.dp else 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = "Unavailable",
                tint = Color(0xFFD97706),
                modifier = Modifier.size(if (isThumbnail) 20.dp else 36.dp)
            )

            if (!isThumbnail) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "PAYMENT PROOF UNAVAILABLE",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reason,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(text = "RETRY", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text = "Unavailable",
                    fontSize = 9.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
