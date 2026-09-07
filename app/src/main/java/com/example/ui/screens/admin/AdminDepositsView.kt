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
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.TransactionDto
import com.example.ui.MainViewModel
import com.example.ui.components.PaymentProofImage

/**
 * Premium Light Enterprise Deposit Management Screen.
 * High-contrast, clean corporate banking style with live payment proof thumbnails.
 */
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
        // 1. SECTION HEADER
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            Text(
                text = "DEPOSIT MANAGEMENT",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Review and verify incoming payment requests.",
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )
        }

        // 2. SEARCH BAR
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by user, transaction ID, reference or gateway", color = Color(0xFF94A3B8), fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF059669),
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedContainerColor = Color(0xFFFFFFFF),
                unfocusedContainerColor = Color(0xFFFFFFFF),
                focusedTextColor = Color(0xFF0F172A),
                unfocusedTextColor = Color(0xFF0F172A)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. SEGMENTED STATUS FILTER CHIPS
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

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { filterStatus = status },
                    color = if (isSelected) Color(0xFF059669) else Color(0xFFFFFFFF),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) Color(0xFF059669) else Color(0xFFE2E8F0)
                    ),
                    shadowElevation = if (isSelected) 2.dp else 0.dp
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (count > 0) "$status ($count)" else status,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (isSelected) Color.White else Color(0xFF475569)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. DEPOSIT REQUEST CARDS LIST
        if (filteredDeposits.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No deposit records found",
                        color = Color(0xFF0F172A),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try adjusting your search criteria or filter tab",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                filteredDeposits.forEach { tx ->
                    val isHighlighted = highlightTxId != null && (tx.id == highlightTxId || tx.transactionRef == highlightTxId)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(if (isHighlighted) 6.dp else 2.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isHighlighted) 2.dp else 1.dp,
                            color = if (isHighlighted) Color(0xFF059669) else Color(0xFFE2E8F0)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // TOP ROW: USER AVATAR + USERNAME + AMOUNT + STATUS
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFECFDF5))
                                            .border(1.dp, Color(0xFF059669).copy(alpha = 0.3f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tx.userName.take(1).uppercase().ifBlank { "U" },
                                            color = Color(0xFF059669),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = tx.userName.ifBlank { "User ID: " + tx.userId.take(8) },
                                            color = Color(0xFF0F172A),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "TX ID: ${tx.id}",
                                            color = Color(0xFF64748B),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${tx.currency} %,.0f".format(tx.amount),
                                        color = Color(0xFF059669),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    // Status Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                when (tx.status) {
                                                    "APPROVED" -> Color(0xFFDCFCE7)
                                                    "REJECTED" -> Color(0xFFFEE2E2)
                                                    else -> Color(0xFFFEF3C7)
                                                }
                                            )
                                            .border(
                                                1.dp,
                                                when (tx.status) {
                                                    "APPROVED" -> Color(0xFF86EFAC)
                                                    "REJECTED" -> Color(0xFFFCA5A5)
                                                    else -> Color(0xFFFDE68A)
                                                },
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = tx.status,
                                            color = when (tx.status) {
                                                "APPROVED" -> Color(0xFF166534)
                                                "REJECTED" -> Color(0xFF991B1B)
                                                else -> Color(0xFF92400E)
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // TRANSACTION METADATA STRIP
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Gateway: ",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF64748B)
                                        )
                                        Text(
                                            text = tx.gatewayName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                    if (tx.senderName.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Sender: ",
                                                fontSize = 11.5.sp,
                                                color = Color(0xFF64748B)
                                            )
                                            Text(
                                                text = tx.senderName,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF334155)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Ref / UTR: ",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF64748B)
                                        )
                                        Text(
                                            text = tx.transactionRef.ifBlank { "None" },
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // PAYMENT PROOF SECTION (REAL SCREENSHOT THUMBNAIL + VIEW BUTTON)
                            Text(
                                text = "PAYMENT PROOF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Real Thumbnail
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    PaymentProofImage(
                                        transaction = tx,
                                        modifier = Modifier
                                            .size(width = 80.dp, height = 75.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop,
                                        isThumbnail = true,
                                        onImageClick = { viewingScreenshotTx = tx }
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = "Screenshot Attached",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "Tap thumbnail or button to zoom & inspect",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                // [ VIEW PROOF ] Button
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewingScreenshotTx = tx },
                                    color = Color(0xFFFFFFFF),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    shadowElevation = 1.dp
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInFull,
                                            contentDescription = "View Proof",
                                            tint = Color(0xFF0F172A),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "VIEW PROOF",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                }
                            }

                            // ACTION BUTTONS (FOR PENDING DEPOSITS)
                            if (tx.status == "PENDING") {
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Reject Button
                                    OutlinedButton(
                                        onClick = { viewModel.rejectTransaction(tx.id) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp),
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
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "REJECT",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Approve Button
                                    Button(
                                        onClick = { viewModel.approveTransaction(tx.id) },
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .height(42.dp)
                                            .shadow(2.dp, RoundedCornerShape(10.dp)),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF059669),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Approve",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "APPROVE DEPOSIT",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // INSPECTOR DIALOG
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
