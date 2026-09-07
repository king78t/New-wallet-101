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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AdminDeviceDto
import com.example.data.models.AdminNotificationDto
import com.example.data.models.ProfileDto
import com.example.data.models.TransactionDto

/**
 * Premium Light Enterprise Admin Overview Dashboard.
 * Clean, executive fintech styling with elevated white cards and clear financial indicators.
 */
@Composable
fun AdminDashboardOverviewView(
    users: List<ProfileDto>,
    transactions: List<TransactionDto>,
    devices: List<AdminDeviceDto>,
    notifications: List<AdminNotificationDto>,
    onNavigateTab: (String) -> Unit
) {
    val pendingDeposits = transactions.filter { it.type == "DEPOSIT" && it.status == "PENDING" }
    val pendingWithdrawals = transactions.filter { it.type == "WITHDRAWAL" && it.status == "PENDING" }
    val approvedDeposits = transactions.filter { it.type == "DEPOSIT" && it.status == "APPROVED" }
    val approvedWithdrawals = transactions.filter { it.type == "WITHDRAWAL" && it.status == "APPROVED" }

    val totalDepositsSum = approvedDeposits.sumOf { it.amount }
    val totalWithdrawalsSum = approvedWithdrawals.sumOf { it.amount }

    val activeDevicesCount = devices.count { it.isActive }

    Column(modifier = Modifier.fillMaxWidth()) {
        // SYSTEM HEALTH BAR
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF059669))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FINANCIAL CORE SYSTEM ONLINE",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF059669),
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9))
                        .clickable { onNavigateTab("DEVICES") }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$activeDevicesCount Authorized Device${if (activeDevicesCount != 1) "s" else ""}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // URGENT PENDING ACTIONS BANNER (If pending deposits or withdrawals exist)
        if (pendingDeposits.isNotEmpty() || pendingWithdrawals.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ACTION REQUIRED: PENDING OPERATIONS",
                                color = Color(0xFF92400E),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${pendingDeposits.size + pendingWithdrawals.size} Pending",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (pendingDeposits.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(10.dp))
                                    .clickable { onNavigateTab("DEPOSITS") }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Deposits Queue",
                                            color = Color(0xFF64748B),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${pendingDeposits.size} Pending Approval",
                                            color = Color(0xFF059669),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        if (pendingWithdrawals.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(10.dp))
                                    .clickable { onNavigateTab("WITHDRAWALS") }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Withdrawals Queue",
                                            color = Color(0xFF64748B),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${pendingWithdrawals.size} Pending Review",
                                            color = Color(0xFFD97706),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // 4 KEY ENTERPRISE METRIC CARDS (SECTION I)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. TOTAL USERS
            EnterpriseMetricCard(
                modifier = Modifier.weight(1f),
                title = "TOTAL USERS",
                primaryValue = "${users.size}",
                subValue = "${users.count { !it.isBlocked }} active accounts",
                icon = Icons.Default.Group,
                accentColor = Color(0xFF2563EB),
                onClick = { onNavigateTab("USERS") }
            )

            // 2. PENDING DEPOSITS
            EnterpriseMetricCard(
                modifier = Modifier.weight(1f),
                title = "PENDING DEPOSITS",
                primaryValue = "%02d".format(pendingDeposits.size),
                subValue = "Needs proof verification",
                icon = Icons.Default.Payments,
                accentColor = Color(0xFF059669),
                onClick = { onNavigateTab("DEPOSITS") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 3. PENDING WITHDRAWALS
            EnterpriseMetricCard(
                modifier = Modifier.weight(1f),
                title = "PENDING WITHDRAWALS",
                primaryValue = "%02d".format(pendingWithdrawals.size),
                subValue = "Awaiting payout",
                icon = Icons.Default.AccountBalanceWallet,
                accentColor = Color(0xFFD97706),
                onClick = { onNavigateTab("WITHDRAWALS") }
            )

            // 4. TOTAL TRANSACTIONS
            EnterpriseMetricCard(
                modifier = Modifier.weight(1f),
                title = "TOTAL TRANSACTIONS",
                primaryValue = "${transactions.size}",
                subValue = "${approvedDeposits.size + approvedWithdrawals.size} settled",
                icon = Icons.Default.ReceiptLong,
                accentColor = Color(0xFF7C3AED),
                onClick = { onNavigateTab("DEPOSITS") }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // RECENT NOTIFICATIONS / AUDIT TRAIL FEED
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECENT OPERATIONS & ALERTS",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF475569),
                letterSpacing = 0.5.sp
            )

            Text(
                text = "View All (${notifications.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0284C7),
                modifier = Modifier.clickable { onNavigateTab("NOTIFICATIONS") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (notifications.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No alerts logged yet",
                        color = Color(0xFF334155),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "New user registrations, deposits and withdrawals will appear here.",
                        color = Color(0xFF64748B),
                        fontSize = 11.5.sp
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                notifications.take(5).forEach { notif ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, RoundedCornerShape(12.dp))
                            .clickable {
                                when (notif.type) {
                                    "USER_CREATED" -> onNavigateTab("USERS")
                                    "DEPOSIT_CREATED" -> onNavigateTab("DEPOSITS")
                                    "WITHDRAWAL_CREATED" -> onNavigateTab("WITHDRAWALS")
                                    else -> onNavigateTab("NOTIFICATIONS")
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (!notif.isRead) Color(0xFFF8FAFC) else Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (!notif.isRead) Color(0xFF059669).copy(alpha = 0.4f) else Color(0xFFE2E8F0)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val (badgeColor, icon) = when (notif.type) {
                                "USER_CREATED" -> Pair(Color(0xFF2563EB), Icons.Default.Group)
                                "DEPOSIT_CREATED" -> Pair(Color(0xFF059669), Icons.Default.Payments)
                                "WITHDRAWAL_CREATED" -> Pair(Color(0xFFD97706), Icons.Default.AccountBalanceWallet)
                                else -> Pair(Color(0xFF7C3AED), Icons.Default.NotificationsActive)
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = badgeColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = notif.title,
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    if (!notif.isRead) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF059669))
                                        )
                                    }
                                }
                                Text(
                                    text = notif.message.replace("\n\n", " • ").replace("\n", " "),
                                    color = Color(0xFF64748B),
                                    fontSize = 11.5.sp,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = notif.createdAt.takeLast(5),
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnterpriseMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    primaryValue: String,
    subValue: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color(0xFF64748B),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = primaryValue,
                color = Color(0xFF0F172A),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subValue,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
