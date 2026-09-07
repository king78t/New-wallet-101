package com.example.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.models.TransactionDto
import com.example.data.repository.PaymentProofManager

/**
 * Premium Light Enterprise Payment Proof Inspector.
 * Designed according to corporate financial compliance and banking back-office standards.
 */
@Composable
fun AdminScreenshotViewerDialog(
    transaction: TransactionDto,
    onDismiss: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val context = LocalContext.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var isFullscreen by remember { mutableStateOf(false) }
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
    }

    fun resetTransform() {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A).copy(alpha = 0.55f))
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = if (isFullscreen) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 14.dp)
                },
                shape = if (isFullscreen) RoundedCornerShape(0.dp) else RoundedCornerShape(20.dp),
                color = Color(0xFFFFFFFF),
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFFFFFF))
                ) {
                    // 1. TOP ENTERPRISE HEADER
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFFFFF))
                            .border(
                                width = 1.dp,
                                color = Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(topStart = if (isFullscreen) 0.dp else 20.dp, topEnd = if (isFullscreen) 0.dp else 20.dp)
                            )
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFECFDF5))
                                    .border(1.dp, Color(0xFF059669).copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "PAYMENT PROOF",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F172A),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Payment Verification • TX: ${transaction.id.takeLast(12)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        // Close Button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                                .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // 2. TOOLBAR CONTROLS ROW
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC))
                            .border(width = 1.dp, color = Color(0xFFE2E8F0))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Zoom Out [ - ]
                            Surface(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        scale = (scale - 0.5f).coerceAtLeast(1f)
                                        if (scale == 1f) { offsetX = 0f; offsetY = 0f }
                                    },
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Zoom Out",
                                        tint = Color(0xFF0F172A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // [ 100% ]
                            Surface(
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { resetTransform() }
                                    .padding(horizontal = 2.dp),
                                color = if (scale == 1f) Color(0xFFE2E8F0) else Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                ) {
                                    Text(
                                        text = "${(scale * 100).toInt()}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }

                            // Zoom In [ + ]
                            Surface(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        scale = (scale + 0.5f).coerceAtMost(5f)
                                    },
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Zoom In",
                                        tint = Color(0xFF0F172A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // [ ↻ ROTATE ]
                            Surface(
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        rotationAngle = (rotationAngle + 90f) % 360f
                                    },
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RotateRight,
                                        contentDescription = "Rotate",
                                        tint = Color(0xFF0F172A),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "ROTATE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }

                            // [ FULLSCREEN ]
                            Surface(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { isFullscreen = !isFullscreen },
                                color = if (isFullscreen) Color(0xFF0F172A) else Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                        contentDescription = "Fullscreen",
                                        tint = if (isFullscreen) Color.White else Color(0xFF0F172A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 3. MAIN CENTRED SCREENSHOT AREA
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9))
                            .padding(if (isFullscreen) 0.dp else 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .shadow(8.dp, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .pointerInput(Unit) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            scale = (scale * zoom).coerceIn(1f, 5f)
                                            if (scale > 1f) {
                                                val maxOffsetX = (size.width * (scale - 1)) / 2
                                                val maxOffsetY = (size.height * (scale - 1)) / 2
                                                offsetX = (offsetX + pan.x * scale).coerceIn(-maxOffsetX, maxOffsetX)
                                                offsetY = (offsetY + pan.y * scale).coerceIn(-maxOffsetY, maxOffsetY)
                                            } else {
                                                offsetX = 0f
                                                offsetY = 0f
                                            }
                                        }
                                    }
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onDoubleTap = {
                                                if (scale > 1f) {
                                                    resetTransform()
                                                } else {
                                                    scale = 2.5f
                                                }
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isResolving) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            CircularProgressIndicator(
                                                color = Color(0xFF059669),
                                                strokeWidth = 3.dp,
                                                modifier = Modifier.size(40.dp)
                                            )
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Text(
                                                text = "Verifying payment proof...",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                } else if (resolvedModel == null) {
                                    // STATE 3: Unavailable / Missing
                                    InspectionErrorView(
                                        reason = "Payment proof is unavailable or not attached.",
                                        transaction = transaction,
                                        onRetry = { retryKey++ }
                                    )
                                } else {
                                    val imageRequest = remember(resolvedModel, retryKey, transaction.id) {
                                        ImageRequest.Builder(context)
                                            .data(resolvedModel)
                                            .crossfade(true)
                                            .memoryCacheKey("${transaction.id}_${retryKey}")
                                            .diskCacheKey("${transaction.id}_${retryKey}")
                                            .build()
                                    }

                                    SubcomposeAsyncImage(
                                        model = imageRequest,
                                        contentDescription = "Full Screen Payment Receipt",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer(
                                                scaleX = scale,
                                                scaleY = scale,
                                                translationX = offsetX,
                                                translationY = offsetY,
                                                rotationZ = rotationAngle
                                            ),
                                        contentScale = ContentScale.Fit,
                                        loading = {
                                            // STATE 1: Loading
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    CircularProgressIndicator(
                                                        color = Color(0xFF059669),
                                                        strokeWidth = 3.dp,
                                                        modifier = Modifier.size(40.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(14.dp))
                                                    Text(
                                                        text = "Loading payment proof...",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = Color(0xFF64748B)
                                                    )
                                                }
                                            }
                                        },
                                        error = {
                                            // STATE 3: Failed
                                            InspectionErrorView(
                                                reason = "Unable to retrieve payment proof.",
                                                transaction = transaction,
                                                onRetry = { retryKey++ }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 4. TRANSACTION SUMMARY & ENTERPRISE ACTIONS BAR
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFFFFF))
                            .border(width = 1.dp, color = Color(0xFFE2E8F0))
                            .padding(14.dp)
                    ) {
                        // Summary Information Strip
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${transaction.userName} • ${transaction.gatewayName}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Ref: ${transaction.transactionRef.ifBlank { "N/A" }}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Text(
                                text = "PKR %,.0f".format(transaction.amount),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF059669)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Bottom Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Reject Button (Red)
                            OutlinedButton(
                                onClick = onReject,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFDC2626)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFDC2626))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Reject",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "REJECT",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            // Approve Button (Emerald)
                            Button(
                                onClick = onApprove,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(46.dp)
                                    .shadow(4.dp, RoundedCornerShape(10.dp)),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF059669),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Approve",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "APPROVE DEPOSIT",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InspectionErrorView(
    reason: String,
    transaction: TransactionDto,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(0xFFFEF3C7)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = Color(0xFFD97706),
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "PAYMENT PROOF UNAVAILABLE",
            color = Color(0xFF0F172A),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = reason,
            color = Color(0xFF64748B),
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Ref / UTR: ${transaction.transactionRef.ifBlank { "N/A" }}",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.RestartAlt,
                contentDescription = "Retry",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "RETRY", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
