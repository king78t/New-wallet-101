package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.screens.admin.AdminDashboardOverviewView
import com.example.ui.screens.admin.AdminDepositsView
import com.example.ui.screens.admin.AdminDeviceManagementView
import com.example.ui.screens.admin.AdminGatewaysView
import com.example.ui.screens.admin.AdminNotificationsCenterView
import com.example.ui.screens.admin.AdminSystemSettingsView
import com.example.ui.screens.admin.AdminTheme
import com.example.ui.screens.admin.AdminUserManagementView
import com.example.ui.screens.admin.AdminWithdrawalsView
import kotlinx.coroutines.launch

@Composable
fun AdminSuperPanelScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    val activeTab by viewModel.activeAdminTab.collectAsState()
    val selectedUserId by viewModel.selectedAdminUserId.collectAsState()
    val selectedDepositTxId by viewModel.selectedAdminDepositTxId.collectAsState()
    val selectedWithdrawalTxId by viewModel.selectedAdminWithdrawalTxId.collectAsState()

    val usersList by viewModel.adminUsersList.collectAsState()
    val transactions by viewModel.allTransactionsForAdmin.collectAsState()
    val paymentGateways by viewModel.paymentGateways.collectAsState()
    val adminDevices by viewModel.adminDevices.collectAsState()
    val adminNotifications by viewModel.adminNotifications.collectAsState()
    val unreadNotifCount by viewModel.unreadAdminNotificationCount.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadAdminDashboardData()
        viewModel.loadAdminDevices()
        viewModel.loadAdminNotifications()
    }

    val pendingDepositsCount = transactions.count { it.type == "DEPOSIT" && it.status == "PENDING" }
    val pendingWithdrawalsCount = transactions.count { it.type == "WITHDRAWAL" && it.status == "PENDING" }

    val navItems = listOf(
        AdminTabItem("OVERVIEW", "Overview", Icons.Default.Dashboard, 0),
        AdminTabItem("DEPOSITS", "Deposits", Icons.Default.Payments, pendingDepositsCount),
        AdminTabItem("WITHDRAWALS", "Withdrawals", Icons.Default.AccountBalanceWallet, pendingWithdrawalsCount),
        AdminTabItem("USERS", "Users", Icons.Default.Group, 0),
        AdminTabItem("GATEWAYS", "Gateways", Icons.Default.Payments, 0),
        AdminTabItem("NOTIFICATIONS", "Alerts", Icons.Default.Notifications, unreadNotifCount),
        AdminTabItem("DEVICES", "Admin Devices", Icons.Default.Devices, 0),
        AdminTabItem("SETTINGS", "Settings", Icons.Default.Settings, 0)
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(AdminTheme.Background)) {
        val isWideScreen = maxWidth >= 720.dp

        if (isWideScreen) {
            // TABLET / EXPANDED DESKTOP VIEW WITH NAVIGATION RAIL
            Row(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                NavigationRail(
                    containerColor = AdminTheme.SurfaceDark,
                    contentColor = Color.White,
                    modifier = Modifier.fillMaxHeight().border(width = 1.dp, color = AdminTheme.BorderSubtle)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(AdminTheme.Amber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = AdminTheme.Amber, modifier = Modifier.size(24.dp))
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    navItems.forEach { item ->
                        NavigationRailItem(
                            selected = activeTab == item.id,
                            onClick = { viewModel.activeAdminTab.value = item.id },
                            icon = {
                                Box {
                                    Icon(imageVector = item.icon, contentDescription = item.title)
                                    if (item.badgeCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(AdminTheme.Amber)
                                        )
                                    }
                                }
                            },
                            label = { Text(item.title, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = AdminTheme.EmeraldDark,
                                unselectedIconColor = AdminTheme.TextSecondary,
                                unselectedTextColor = AdminTheme.TextSecondary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = AdminTheme.Red)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    AdminTopBar(
                        title = navItems.find { it.id == activeTab }?.title ?: "Admin Console",
                        unreadCount = unreadNotifCount,
                        onOpenNotifications = { viewModel.activeAdminTab.value = "NOTIFICATIONS" },
                        onOpenMenu = null,
                        onLogout = {
                            viewModel.logout()
                            onLogout()
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AdminTabContent(
                        activeTab = activeTab,
                        viewModel = viewModel,
                        usersList = usersList,
                        transactions = transactions,
                        paymentGateways = paymentGateways,
                        adminDevices = adminDevices,
                        adminNotifications = adminNotifications,
                        selectedUserId = selectedUserId,
                        selectedDepositTxId = selectedDepositTxId,
                        selectedWithdrawalTxId = selectedWithdrawalTxId,
                        onNavigateTab = { tab -> viewModel.activeAdminTab.value = tab }
                    )
                }
            }
        } else {
            // MOBILE RESPONSIVE DRAWER & SCROLLABLE TAB LAYOUT
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = Color.White,
                        modifier = Modifier.width(300.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
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
                                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = "BP CONTROL CENTER", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 13.sp)
                                        Text(text = "Executive Administration", color = Color(0xFF059669), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }

                                IconButton(onClick = { scope.launch { drawerState.close() } }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            navItems.forEach { item ->
                                val isSelected = activeTab == item.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(0xFFECFDF5) else Color.Transparent)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color(0xFF059669).copy(alpha = 0.3f) else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            viewModel.activeAdminTab.value = item.id
                                            scope.launch { drawerState.close() }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.title,
                                            tint = if (isSelected) Color(0xFF059669) else Color(0xFF64748B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = item.title,
                                            color = if (isSelected) Color(0xFF059669) else Color(0xFF334155),
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                    }

                                    if (item.badgeCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(Color(0xFFF59E0B))
                                                .padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${item.badgeCount}",
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFEE2E2))
                                    .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.logout()
                                        onLogout()
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Log Out Admin", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    AdminTopBar(
                        title = navItems.find { it.id == activeTab }?.title ?: "Admin Console",
                        unreadCount = unreadNotifCount,
                        onOpenNotifications = { viewModel.activeAdminTab.value = "NOTIFICATIONS" },
                        onOpenMenu = { scope.launch { drawerState.open() } },
                        onLogout = {
                            viewModel.logout()
                            onLogout()
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Horizontal Scrolling Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        navItems.forEach { item ->
                            val isSelected = activeTab == item.id
                            Box(
                                modifier = Modifier
                                    .shadow(if (isSelected) 2.dp else 0.dp, RoundedCornerShape(10.dp))
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0xFF059669) else Color.White)
                                    .border(1.dp, if (isSelected) Color(0xFF059669) else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                    .clickable { viewModel.activeAdminTab.value = item.id }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else Color(0xFF64748B),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.title,
                                        color = if (isSelected) Color.White else Color(0xFF334155),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                    )
                                    if (item.badgeCount > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color.White else Color(0xFFF59E0B))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "${item.badgeCount}",
                                                color = if (isSelected) Color(0xFF059669) else Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AdminTabContent(
                        activeTab = activeTab,
                        viewModel = viewModel,
                        usersList = usersList,
                        transactions = transactions,
                        paymentGateways = paymentGateways,
                        adminDevices = adminDevices,
                        adminNotifications = adminNotifications,
                        selectedUserId = selectedUserId,
                        selectedDepositTxId = selectedDepositTxId,
                        selectedWithdrawalTxId = selectedWithdrawalTxId,
                        onNavigateTab = { tab -> viewModel.activeAdminTab.value = tab }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminTopBar(
    title: String,
    unreadCount: Int,
    onOpenNotifications: () -> Unit,
    onOpenMenu: (() -> Unit)?,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onOpenMenu != null) {
                IconButton(
                    onClick = onOpenMenu,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                ) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = Color(0xFF0F172A))
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column {
                Text(
                    text = "BP CONTROL CENTER",
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Administration & Transaction Management",
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.5.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Notification Bell with badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                    .clickable { onOpenNotifications() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = if (unreadCount > 0) Color(0xFFD97706) else Color(0xFF64748B),
                    modifier = Modifier.size(20.dp)
                )
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDC2626))
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Admin Profile Chip
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF059669)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "A",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Admin",
                        color = Color(0xFF0F172A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Logout icon
            IconButton(
                onClick = onLogout,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun AdminTabContent(
    activeTab: String,
    viewModel: MainViewModel,
    usersList: List<com.example.data.models.ProfileDto>,
    transactions: List<com.example.data.models.TransactionDto>,
    paymentGateways: List<com.example.data.models.PaymentGatewayDto>,
    adminDevices: List<com.example.data.models.AdminDeviceDto>,
    adminNotifications: List<com.example.data.models.AdminNotificationDto>,
    selectedUserId: String?,
    selectedDepositTxId: String?,
    selectedWithdrawalTxId: String?,
    onNavigateTab: (String) -> Unit
) {
    when (activeTab) {
        "OVERVIEW" -> AdminDashboardOverviewView(
            users = usersList,
            transactions = transactions,
            devices = adminDevices,
            notifications = adminNotifications,
            onNavigateTab = onNavigateTab
        )
        "DEPOSITS" -> AdminDepositsView(
            viewModel = viewModel,
            transactions = transactions,
            highlightTxId = selectedDepositTxId
        )
        "WITHDRAWALS" -> AdminWithdrawalsView(
            viewModel = viewModel,
            transactions = transactions,
            users = usersList,
            highlightTxId = selectedWithdrawalTxId
        )
        "USERS" -> AdminUserManagementView(
            viewModel = viewModel,
            users = usersList,
            highlightUserId = selectedUserId
        )
        "GATEWAYS" -> AdminGatewaysView(
            viewModel = viewModel,
            gateways = paymentGateways
        )
        "NOTIFICATIONS" -> AdminNotificationsCenterView(
            viewModel = viewModel,
            notifications = adminNotifications,
            onSelectTarget = { target, refId -> viewModel.selectAdminTarget(target, refId) }
        )
        "DEVICES" -> AdminDeviceManagementView(
            viewModel = viewModel,
            devices = adminDevices
        )
        "SETTINGS" -> AdminSystemSettingsView(
            viewModel = viewModel
        )
        else -> AdminDashboardOverviewView(
            users = usersList,
            transactions = transactions,
            devices = adminDevices,
            notifications = adminNotifications,
            onNavigateTab = onNavigateTab
        )
    }
}

private data class AdminTabItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)
