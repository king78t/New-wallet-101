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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AdminDeviceDto
import com.example.data.models.AdminNotificationDto
import com.example.data.models.ProfileDto
import com.example.data.models.TransactionDto

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
    val pendingDepositsSum = pendingDeposits.sumOf { it.amount }
    val pendingWithdrawalsSum = pendingWithdrawals.sumOf { it.amount }

    val activeDevicesCount = devices.count { it.isActive }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Live System Health Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AdminTheme.SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, AdminTheme.BorderSubtle)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(AdminTheme.Emerald)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EXCHANGE CORE: ONLINE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdminTheme.Emerald,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AdminTheme.BorderSubtle.copy(alpha = 0.5f))
                        .clickable { onNavigateTab("DEVICES") }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = null,
                        tint = AdminTheme.Cyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$activeDevicesCount Admin Device${if (activeDevicesCount != 1) "s" else ""}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // URGENT PENDING ACTIONS ALERT (If pending deposits or withdrawals exist)
        if (pendingDeposits.isNotEmpty() || pendingWithdrawals.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A1B0E)
                ),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AdminTheme.Amber)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AdminTheme.Amber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ACTION REQUIRED: PENDING REQUESTS",
                                color = AdminTheme.Amber,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AdminTheme.Amber)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${pendingDeposits.size + pendingWithdrawals.size} Pending",
                                color = Color.Black,
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
                                    .background(AdminTheme.SurfaceDark)
                                    .border(1.dp, AdminTheme.EmeraldDark, RoundedCornerShape(10.dp))
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
                                            color = AdminTheme.TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${pendingDeposits.size} Pending",
                                            color = AdminTheme.Emerald,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = AdminTheme.Emerald,
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
                                    .background(AdminTheme.SurfaceDark)
                                    .border(1.dp, AdminTheme.AmberDark, RoundedCornerShape(10.dp))
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
                                            color = AdminTheme.TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${pendingWithdrawals.size} Pending",
                                            color = AdminTheme.Amber,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = AdminTheme.Amber,
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

        // 2x2 METRICS GRID
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Total Users Card
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "TOTAL USERS",
                primaryValue = "${users.size}",
                subValue = "${users.count { !it.isBlocked }} Active • ${users.count { it.isBlocked }} Blocked",
                icon = Icons.Default.Group,
                accentColor = AdminTheme.Cyan,
                onClick = { onNavigateTab("USERS") }
            )

            // Admin Notifications Card
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "ADMIN ALERTS",
                primaryValue = "${notifications.size}",
                subValue = "${notifications.count { !it.isRead }} Unread alerts",
                icon = Icons.Default.NotificationsActive,
                accentColor = AdminTheme.Purple,
                onClick = { onNavigateTab("NOTIFICATIONS") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Deposits Summary Card
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "DEPOSITS (APPROVED)",
                primaryValue = "PKR ${String.format(java.util.Locale.US, "%,.0f", totalDepositsSum)}",
                subValue = "${approvedDeposits.size} Approved • ${pendingDeposits.size} Pending",
                icon = Icons.Default.Payments,
                accentColor = AdminTheme.Emerald,
                onClick = { onNavigateTab("DEPOSITS") }
            )

            // Withdrawals Summary Card
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "WITHDRAWALS (APPROVED)",
                primaryValue = "PKR ${String.format(java.util.Locale.US, "%,.0f", totalWithdrawalsSum)}",
                subValue = "${approvedWithdrawals.size} Approved • ${pendingWithdrawals.size} Pending",
                icon = Icons.Default.AccountBalanceWallet,
                accentColor = AdminTheme.Amber,
                onClick = { onNavigateTab("WITHDRAWALS") }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // RECENT NOTIFICATION / ACTIVITY FEED
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECENT ADMIN ALERTS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AdminTheme.TextSecondary,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "View All (${notifications.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AdminTheme.Cyan,
                modifier = Modifier.clickable { onNavigateTab("NOTIFICATIONS") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (notifications.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AdminTheme.SurfaceDark)
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
                        tint = AdminTheme.TextMuted,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No alerts logged yet",
                        color = AdminTheme.TextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "New user signups, deposits and withdrawals will appear here.",
                        color = AdminTheme.TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                notifications.take(5).forEach { notif ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
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
                            containerColor = if (!notif.isRead) AdminTheme.CardBg else AdminTheme.SurfaceDark
                        ),
                        border = if (!notif.isRead) androidx.compose.foundation.BorderStroke(1.dp, AdminTheme.BorderGlow) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val (badgeColor, icon) = when (notif.type) {
                                "USER_CREATED" -> Pair(AdminTheme.Cyan, Icons.Default.Group)
                                "DEPOSIT_CREATED" -> Pair(AdminTheme.Emerald, Icons.Default.Payments)
                                "WITHDRAWAL_CREATED" -> Pair(AdminTheme.Amber, Icons.Default.AccountBalanceWallet)
                                else -> Pair(AdminTheme.Purple, Icons.Default.NotificationsActive)
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor.copy(alpha = 0.2f)),
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
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    if (!notif.isRead) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(AdminTheme.Amber)
                                        )
                                    }
                                }
                                Text(
                                    text = notif.message.replace("\n\n", " • ").replace("\n", " "),
                                    color = AdminTheme.TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = notif.createdAt.takeLast(5),
                                color = AdminTheme.TextMuted,
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
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    primaryValue: String,
    subValue: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AdminTheme.SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, AdminTheme.BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = AdminTheme.TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = primaryValue,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subValue,
                color = AdminTheme.TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
