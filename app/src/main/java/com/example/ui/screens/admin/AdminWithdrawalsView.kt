package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ProfileDto
import com.example.data.models.TransactionDto
import com.example.ui.MainViewModel

@Composable
fun AdminWithdrawalsView(
    viewModel: MainViewModel,
    transactions: List<TransactionDto>,
    users: List<ProfileDto>,
    highlightTxId: String? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("ALL") }

    val withdrawals = transactions.filter { it.type == "WITHDRAWAL" }
    val filteredWithdrawals = withdrawals.filter { tx ->
        val matchesStatus = when (filterStatus) {
            "PENDING" -> tx.status == "PENDING"
            "APPROVED" -> tx.status == "APPROVED"
            "REJECTED" -> tx.status == "REJECTED"
            else -> true
        }
        val matchesQuery = searchQuery.isBlank() ||
                tx.userName.contains(searchQuery, ignoreCase = true) ||
                tx.accountTitle.contains(searchQuery, ignoreCase = true) ||
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
            placeholder = { Text("Search by user, account title, or number...", color = AdminTheme.TextMuted, fontSize = 13.sp) },
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
                focusedBorderColor = AdminTheme.Amber,
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
                    "PENDING" -> withdrawals.count { it.status == "PENDING" }
                    "APPROVED" -> withdrawals.count { it.status == "APPROVED" }
                    "REJECTED" -> withdrawals.count { it.status == "REJECTED" }
                    else -> withdrawals.size
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AdminTheme.Amber else AdminTheme.SurfaceDark)
                        .border(1.dp, if (isSelected) AdminTheme.Amber else AdminTheme.BorderSubtle, RoundedCornerShape(8.dp))
                        .clickable { filterStatus = status }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$status ($count)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else AdminTheme.TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredWithdrawals.isEmpty()) {
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
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = AdminTheme.TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No withdrawal requests found",
                        color = AdminTheme.TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredWithdrawals.forEach { tx ->
                    val isHighlighted = highlightTxId != null && tx.id == highlightTxId
                    val userProfile = users.find { it.id == tx.userId }
                    val currentWalletBalance = userProfile?.walletBalance ?: 0.0

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = AdminTheme.SurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isHighlighted) 2.dp else 1.dp,
                            color = if (isHighlighted) AdminTheme.Amber else AdminTheme.BorderSubtle
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header Row
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
                                            .background(AdminTheme.Amber.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tx.userName.take(1).uppercase(),
                                            color = AdminTheme.Amber,
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
                                        color = AdminTheme.Amber,
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

                            // Payout Details
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AdminTheme.CardBg)
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Destination Method:",
                                        color = AdminTheme.TextSecondary,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = tx.gatewayName,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Account Title:",
                                        color = AdminTheme.TextSecondary,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = tx.accountTitle.ifBlank { "N/A" },
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Account / IBAN:",
                                        color = AdminTheme.TextSecondary,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = tx.accountNumber.ifBlank { "N/A" },
                                        color = AdminTheme.Cyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Balance Verification Check
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "User Current Balance:",
                                        color = AdminTheme.TextMuted,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "${tx.currency} ${String.format(java.util.Locale.US, "%,.0f", currentWalletBalance)}",
                                        color = if (currentWalletBalance >= tx.amount) AdminTheme.Emerald else AdminTheme.Red,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Quick Action Buttons for PENDING
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
                                        Text("Approve & Dispatch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                        Text("Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
