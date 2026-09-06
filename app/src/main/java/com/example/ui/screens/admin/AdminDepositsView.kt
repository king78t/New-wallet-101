package com.example.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.TransactionDto
import com.example.ui.MainViewModel

@Composable
fun AdminDepositsView(
    viewModel: MainViewModel,
    transactions: List<TransactionDto>,
    highlightTxId: String? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("ALL") } // ALL, PENDING, APPROVED, REJECTED
    var viewingScreenshotTx by remember { mutableStateOf<TransactionDto?>(null) }

    val deposits = transactions.filter { it.type == "DEPOSIT" }
    val filteredDeposits = deposits.filter { tx ->
        val matchesStatus = when (filterStatus) {
            "PENDING" -> tx.status == "PENDING"
            "APPROVED" -> tx.status == "APPROVED"
            "REJECTED" -> tx.status == "REJECTED"
            else -> true
        }
        val matchesQuery = searchQuery.isBlank() ||
                tx.userName.contains(searchQuery, ignoreCase = true) ||
                tx.transactionRef.contains(searchQuery, ignoreCase = true) ||
                tx.accountNumber.contains(searchQuery, ignoreCase = true) ||
                tx.gatewayName.contains(searchQuery, ignoreCase = true) ||
                tx.id.contains(searchQuery, ignoreCase = true)

        matchesStatus && matchesQuery
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by user, ref number, or gateway...", color = AdminTheme.TextMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = AdminTheme.TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AdminTheme.Emerald,
                unfocusedBorderColor = AdminTheme.BorderSubtle,
                focusedContainerColor = AdminTheme.SurfaceDark,
                unfocusedContainerColor = AdminTheme.SurfaceDark,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Status Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "PENDING", "APPROVED", "REJECTED").forEach { status ->
                val isSelected = filterStatus == status
                val count = when (status) {
                    "PENDING" -> deposits.count { it.status == "PENDING" }
                    "APPROVED" -> deposits.count { it.status == "APPROVED" }
                    "REJECTED" -> deposits.count { it.status == "REJECTED" }
                    else -> deposits.size
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AdminTheme.Emerald else AdminTheme.SurfaceDark)
                        .border(1.dp, if (isSelected) AdminTheme.Emerald else AdminTheme.BorderSubtle, RoundedCornerShape(8.dp))
                        .clickable { filterStatus = status }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$status ($count)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else AdminTheme.TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredDeposits.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AdminTheme.SurfaceDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = AdminTheme.TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No deposit records found",
                        color = AdminTheme.TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredDeposits.forEach { tx ->
                    val isHighlighted = highlightTxId != null && (tx.id == highlightTxId || tx.transactionRef == highlightTxId)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = AdminTheme.SurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isHighlighted) 2.dp else 1.dp,
                            color = if (isHighlighted) AdminTheme.Emerald else AdminTheme.BorderSubtle
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header Row: User, Status & Amount
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(AdminTheme.Emerald.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tx.userName.take(1).uppercase(),
                                            color = AdminTheme.Emerald,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = tx.userName.ifBlank { "User ID: " + tx.userId.take(8) },
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "TX ID: ${tx.id}",
                                            color = AdminTheme.TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${tx.currency} ${String.format(java.util.Locale.US, "%,.0f", tx.amount)}",
                                        color = AdminTheme.Emerald,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when (tx.status) {
                                                    "APPROVED" -> AdminTheme.EmeraldDark
                                                    "REJECTED" -> AdminTheme.RedDark
                                                    else -> AdminTheme.AmberDark
                                                }
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = tx.status,
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Details Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AdminTheme.CardBg)
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Gateway: ${tx.gatewayName}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (tx.senderName.isNotBlank()) {
                                        Text(
                                            text = "Sender: ${tx.senderName}",
                                            color = AdminTheme.TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        text = "Ref / UTR: ${tx.transactionRef.ifBlank { "None" }}",
                                        color = AdminTheme.TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }

                                // Screenshot Preview Thumbnail (Tapping opens Fullscreen Zoom)
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black)
                                        .border(1.dp, AdminTheme.BorderSubtle, RoundedCornerShape(8.dp))
                                        .clickable { viewingScreenshotTx = tx },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!tx.screenshotUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = tx.screenshotUrl,
                                            contentDescription = "Payment Proof",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.25f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ZoomIn,
                                                contentDescription = "Zoom",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Image,
                                                contentDescription = null,
                                                tint = AdminTheme.TextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "No Proof",
                                                color = AdminTheme.TextMuted,
                                                fontSize = 8.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Quick Action Buttons for PENDING deposits
                            if (tx.status == "PENDING") {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.approveTransaction(tx.id) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AdminTheme.Emerald),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Approve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { viewModel.rejectTransaction(tx.id) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AdminTheme.Red),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Fullscreen Zoomable Screenshot Dialog
    viewingScreenshotTx?.let { tx ->
        AdminScreenshotViewerDialog(
            transaction = tx,
            onDismiss = { viewingScreenshotTx = null },
            onApprove = {
                viewModel.approveTransaction(tx.id)
                viewingScreenshotTx = null
            },
            onReject = {
                viewModel.rejectTransaction(tx.id)
                viewingScreenshotTx = null
            }
        )
    }
}
