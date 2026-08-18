package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.models.TransactionDto
import com.example.ui.MainViewModel

/**
 * AdminRequest UI Component
 * 
 * Fetches pending deposit requests from Supabase and allows Admins to
 * inspect details, view screenshot proofs, and Approve or Reject requests.
 */
@Composable
fun AdminRequest(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allTransactions by viewModel.allTransactionsForAdmin.collectAsState()
    val isSupabaseConnected by viewModel.isSupabaseConfigured.collectAsState()
    val supabaseStatusMsg by viewModel.supabaseStatusMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var filterMode by remember { mutableStateOf("PENDING") } // "PENDING" or "ALL_DEPOSITS"
    var previewScreenshotUrl by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAdminDashboardData()
    }

    // Filter deposits
    val pendingDeposits = remember(allTransactions, searchQuery, filterMode) {
        allTransactions.filter { tx ->
            val isDeposit = tx.type.equals("DEPOSIT", ignoreCase = true)
            val matchesFilter = if (filterMode == "PENDING") {
                tx.status.equals("PENDING", ignoreCase = true)
            } else true

            val matchesQuery = if (searchQuery.isBlank()) true else {
                tx.userName.contains(searchQuery, ignoreCase = true) ||
                tx.transactionRef.contains(searchQuery, ignoreCase = true) ||
                tx.gatewayName.contains(searchQuery, ignoreCase = true) ||
                tx.amount.toString().contains(searchQuery)
            }

            isDeposit && matchesFilter && matchesQuery
        }
    }

    val pendingCount = allTransactions.count { it.type.equals("DEPOSIT", ignoreCase = true) && it.status.equals("PENDING", ignoreCase = true) }
    val totalPendingSum = allTransactions.filter { it.type.equals("DEPOSIT", ignoreCase = true) && it.status.equals("PENDING", ignoreCase = true) }.sumOf { it.amount }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        // Top Header & Supabase Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Pending Deposit Requests",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF3C7))
                            .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$pendingCount PENDING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB45309)
                        )
                    }
                }
                Text(
                    text = "Review payment proofs & update deposit status in Supabase",
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B)
                )
            }

            IconButton(
                onClick = {
                    isRefreshing = true
                    viewModel.loadAdminDashboardData()
                    isRefreshing = false
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Requests",
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Connection Status Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSupabaseConnected) Color(0xFFECFDF5) else Color(0xFFFEF3C7))
                .border(1.dp, if (isSupabaseConnected) Color(0xFFA7F3D0) else Color(0xFFFDE68A), RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isSupabaseConnected) "⚡ Supabase Active" else "🌐 Local Storage Mode",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSupabaseConnected) Color(0xFF047857) else Color(0xFFB45309)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• $supabaseStatusMsg",
                        fontSize = 11.sp,
                        color = Color(0xFF475569)
                    )
                }

                if (pendingCount > 0) {
                    Text(
                        text = "Total: PKR ${totalPendingSum.toInt()}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search & Filter Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search user, amount, TRX ref...", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color(0xFFE2E8F0)
                )
            )

            // Filter Toggle Button
            Box(
                modifier = Modifier
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (filterMode == "PENDING") Color(0xFF10B981) else Color(0xFFF1F5F9))
                    .clickable {
                        filterMode = if (filterMode == "PENDING") "ALL_DEPOSITS" else "PENDING"
                    }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = if (filterMode == "PENDING") Color.White else Color(0xFF0F172A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (filterMode == "PENDING") "Pending" else "All Deposits",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (filterMode == "PENDING") Color.White else Color(0xFF0F172A)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Pending Deposits List
        if (pendingDeposits.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Clear",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (filterMode == "PENDING") "No pending deposit requests found" else "No matching deposit records found",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "New deposit requests submitted by users will appear here automatically.",
                        fontSize = 11.5.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pendingDeposits.forEach { tx ->
                    PendingDepositItemCard(
                        transaction = tx,
                        onViewScreenshot = { url -> previewScreenshotUrl = url },
                        onApprove = { viewModel.approveTransaction(tx.id) },
                        onReject = { viewModel.rejectTransaction(tx.id) }
                    )
                }
            }
        }
    }

    // Modal Receipt Zoom Dialog
    if (previewScreenshotUrl != null) {
        Dialog(onDismissRequest = { previewScreenshotUrl = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Deposit Payment Receipt Proof",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        IconButton(onClick = { previewScreenshotUrl = null }, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9))
                    ) {
                        AsyncImage(
                            model = previewScreenshotUrl,
                            contentDescription = "Deposit Payment Screenshot",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF10B981))
                            .clickable { previewScreenshotUrl = null },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CLOSE RECEIPT VIEW",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingDepositItemCard(
    transaction: TransactionDto,
    onViewScreenshot: (String) -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: User Name & Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFECFDF5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = transaction.userName.ifBlank { "BP User" },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Gateway: ${transaction.gatewayName}",
                            fontSize = 11.5.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${transaction.amount.toInt()} ${transaction.currency}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF10B981)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (transaction.status) {
                                    "APPROVED" -> Color(0xFFDCFCE7)
                                    "REJECTED" -> Color(0xFFFEE2E2)
                                    else -> Color(0xFFFEF3C7)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = transaction.status,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (transaction.status) {
                                "APPROVED" -> Color(0xFF16A34A)
                                "REJECTED" -> Color(0xFFEF4444)
                                else -> Color(0xFFD97706)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Transaction Details Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    if (transaction.senderName.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Sender Title:", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(text = transaction.senderName, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (transaction.transactionRef.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "TRX Reference:", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(text = transaction.transactionRef, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB))
                        }
                    }
                }
            }

            // Payment Proof Screenshot View Trigger
            if (!transaction.screenshotUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFECFDF5))
                        .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(12.dp))
                        .clickable { onViewScreenshot(transaction.screenshotUrl) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Proof Screenshot",
                                tint = Color(0xFF047857),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🖼️ View Payment Screenshot Proof",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF047857)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Inspect Receipt",
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Action Buttons: Approve and Reject
            if (transaction.status.equals("PENDING", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Approve Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .shadow(3.dp, RoundedCornerShape(12.dp), ambientColor = Color(0x3010B981))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF10B981))
                            .clickable { onApprove() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Approve",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "APPROVE",
                                color = Color.White,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Reject Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .shadow(3.dp, RoundedCornerShape(12.dp), ambientColor = Color(0x30EF4444))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEF4444))
                            .clickable { onReject() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ThumbDown,
                                contentDescription = "Reject",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "REJECT",
                                color = Color.White,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}
