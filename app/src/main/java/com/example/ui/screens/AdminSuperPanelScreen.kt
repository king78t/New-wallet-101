package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.models.PaymentGatewayDto
import com.example.data.models.ProfileDto
import com.example.data.models.TransactionDto
import com.example.ui.MainViewModel
import com.example.ui.components.AdminRequest
import kotlinx.coroutines.launch

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults

@Composable
fun AdminSuperPanelScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    var activeTab by remember { mutableStateOf("USERS") } // USERS, TRANSACTIONS, GATEWAYS, SETTINGS
    val usersList by viewModel.adminUsersList.collectAsState()
    val transactions by viewModel.allTransactionsForAdmin.collectAsState()
    val paymentGateways by viewModel.paymentGateways.collectAsState()
    val exchangeUrl by viewModel.exchangeUrl.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadAdminDashboardData()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = Color(0xFF0F172A),
                    contentColor = Color.White,
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF3C7))
                                    .border(1.5.dp, Color(0xFFF59E0B), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier.width(80.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NavigationRailItem(
                                selected = activeTab == "USERS",
                                onClick = { activeTab = "USERS" },
                                icon = { Icon(Icons.Default.Group, contentDescription = "Users") },
                                label = { Text("Users", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color(0xFFF59E0B),
                                    indicatorColor = Color(0xFFD97706),
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8)
                                )
                            )
                            NavigationRailItem(
                                selected = activeTab == "TRANSACTIONS",
                                onClick = { activeTab = "TRANSACTIONS" },
                                icon = { Icon(Icons.Default.Payments, contentDescription = "Transactions") },
                                label = { Text("Tx Logs", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color(0xFFF59E0B),
                                    indicatorColor = Color(0xFFD97706),
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8)
                                )
                            )
                            NavigationRailItem(
                                selected = activeTab == "GATEWAYS",
                                onClick = { activeTab = "GATEWAYS" },
                                icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Gateways") },
                                label = { Text("Gateways", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color(0xFFF59E0B),
                                    indicatorColor = Color(0xFFD97706),
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8)
                                )
                            )
                            NavigationRailItem(
                                selected = activeTab == "SETTINGS",
                                onClick = { activeTab = "SETTINGS" },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text("Settings", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color(0xFFF59E0B),
                                    indicatorColor = Color(0xFFD97706),
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8)
                                )
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.purgeAllDemoData() },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF450A0A))
                                    .border(1.dp, Color(0xFF991B1B), RoundedCornerShape(8.dp))
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteForever,
                                    contentDescription = "Purge Demo Data",
                                    tint = Color(0xFFFCA5A5),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    viewModel.logout()
                                    onLogout()
                                },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Logout",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    AdminDashboardContent(
                        activeTab = activeTab,
                        onTabSelected = { activeTab = it },
                        usersList = usersList,
                        transactions = transactions,
                        paymentGateways = paymentGateways,
                        exchangeUrl = exchangeUrl,
                        viewModel = viewModel,
                        onLogout = onLogout,
                        isWideScreen = true,
                        onOpenDrawer = {}
                    )
                }
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = Color(0xFF0F172A),
                        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                        modifier = Modifier.width(285.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFEF3C7))
                                                .border(1.5.dp, Color(0xFFF59E0B), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AdminPanelSettings,
                                                contentDescription = null,
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "SUPER ADMIN",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Boss Control Center",
                                                fontSize = 11.sp,
                                                color = Color(0xFFF59E0B),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    IconButton(onClick = { scope.launch { drawerState.close() } }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close Sidebar",
                                            tint = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0xFF334155))
                                )
                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = "ADMIN NAVIGATION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                SidebarNavItem(
                                    title = "User Accounts",
                                    subtitle = "${usersList.size} registered users",
                                    icon = Icons.Default.Group,
                                    isSelected = activeTab == "USERS",
                                    badgeText = "${usersList.size}",
                                    badgeColor = Color(0xFF22C55E)
                                ) {
                                    activeTab = "USERS"
                                    scope.launch { drawerState.close() }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                SidebarNavItem(
                                    title = "Transaction Logs",
                                    subtitle = "Deposit & Withdrawal approvals",
                                    icon = Icons.Default.Payments,
                                    isSelected = activeTab == "TRANSACTIONS",
                                    badgeText = "${transactions.count { it.status == "PENDING" }} Pending",
                                    badgeColor = Color(0xFFF59E0B)
                                ) {
                                    activeTab = "TRANSACTIONS"
                                    scope.launch { drawerState.close() }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                SidebarNavItem(
                                    title = "Payment Gateways",
                                    subtitle = "Easypaisa, JazzCash, Bank",
                                    icon = Icons.Default.AccountBalanceWallet,
                                    isSelected = activeTab == "GATEWAYS",
                                    badgeText = "${paymentGateways.size}",
                                    badgeColor = Color(0xFF3B82F6)
                                ) {
                                    activeTab = "GATEWAYS"
                                    scope.launch { drawerState.close() }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                SidebarNavItem(
                                    title = "System Settings",
                                    subtitle = "Usdt rate & exchange URL",
                                    icon = Icons.Default.Settings,
                                    isSelected = activeTab == "SETTINGS",
                                    badgeText = null,
                                    badgeColor = Color.Transparent
                                ) {
                                    activeTab = "SETTINGS"
                                    scope.launch { drawerState.close() }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0xFF334155))
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "DATABASE ACTIONS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF450A0A))
                                        .border(1.dp, Color(0xFF991B1B), RoundedCornerShape(12.dp))
                                        .clickable {
                                            viewModel.purgeAllDemoData()
                                            scope.launch { drawerState.close() }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteForever,
                                            contentDescription = null,
                                            tint = Color(0xFFFCA5A5),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Purge All Demo Data",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFECACA)
                                            )
                                            Text(
                                                text = "Wipe sample records",
                                                fontSize = 10.sp,
                                                color = Color(0xFFF87171)
                                            )
                                        }
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                                    .clickable {
                                        scope.launch { drawerState.close() }
                                        viewModel.logout()
                                        onLogout()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                            contentDescription = null,
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Logout Super Admin",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF87171)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            ) {
                AdminDashboardContent(
                    activeTab = activeTab,
                    onTabSelected = { activeTab = it },
                    usersList = usersList,
                    transactions = transactions,
                    paymentGateways = paymentGateways,
                    exchangeUrl = exchangeUrl,
                    viewModel = viewModel,
                    onLogout = onLogout,
                    isWideScreen = false,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
        }
    }
}

@Composable
fun AdminDashboardContent(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    usersList: List<ProfileDto>,
    transactions: List<TransactionDto>,
    paymentGateways: List<PaymentGatewayDto>,
    exchangeUrl: String,
    viewModel: MainViewModel,
    onLogout: () -> Unit,
    isWideScreen: Boolean,
    onOpenDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isWideScreen) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFEF3C7))
                            .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(10.dp))
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Sidebar Menu",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))
                }

                Column {
                    Text(
                        text = "SUPER ADMIN DASHBOARD",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "Role: SUPER_ADMIN | Boss Control Center",
                        fontSize = 11.sp,
                        color = Color(0xFFD97706),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(onClick = {
                viewModel.logout()
                onLogout()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color(0xFFEF4444)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ENTERPRISE METRICS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricBox(title = "Total Users", value = "${usersList.size}", color = Color(0xFF22C55E))
                    MetricBox(
                        title = "Pending Tx",
                        value = "${transactions.count { it.status == "PENDING" }}",
                        color = Color(0xFFF59E0B)
                    )
                    MetricBox(
                        title = "Gateways",
                        value = "${paymentGateways.size}",
                        color = Color(0xFF3B82F6)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AdminTabButton("USERS", activeTab == "USERS", Icons.Default.Group) { onTabSelected("USERS") }
            AdminTabButton("TX LOGS", activeTab == "TRANSACTIONS", Icons.Default.Payments) { onTabSelected("TRANSACTIONS") }
            AdminTabButton("GATEWAYS", activeTab == "GATEWAYS", Icons.Default.AccountBalanceWallet) { onTabSelected("GATEWAYS") }
            AdminTabButton("SETTINGS", activeTab == "SETTINGS", Icons.Default.Settings) { onTabSelected("SETTINGS") }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (activeTab) {
            "USERS" -> AdminUserManagementView(viewModel, usersList)
            "TRANSACTIONS" -> AdminTransactionManagementView(viewModel)
            "GATEWAYS" -> AdminGatewayManagementView(viewModel)
            "SETTINGS" -> AdminSettingsView(viewModel, exchangeUrl)
        }
    }
}

@Composable
fun SidebarNavItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    badgeText: String?,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFD97706) else Color(0xFF1E293B))
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFFF59E0B) else Color(0xFF334155),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color(0xFFE2E8F0)
                    )
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = if (isSelected) Color(0xFFFEF3C7) else Color(0xFF64748B)
                    )
                }
            }

            if (badgeText != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color.White.copy(alpha = 0.25f) else badgeColor.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else badgeColor
                    )
                }
            }
        }
    }
}

@Composable
fun AdminTabButton(
    title: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFFFEF3C7) else Color.White)
            .border(
                0.75.dp,
                if (isSelected) Color(0xFFF59E0B) else Color(0xFFE2E8F0),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFFD97706) else Color(0xFF6B7280),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFFD97706) else Color(0xFF374151)
            )
        }
    }
}

@Composable
fun MetricBox(title: String, value: String, color: Color) {
    Column {
        Text(text = title, fontSize = 11.sp, color = Color(0xFF6B7280))
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun AdminUserManagementView(viewModel: MainViewModel, users: List<ProfileDto>) {
    Text(
        text = "USER ACCOUNTS & WALLETS",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF111827),
        modifier = Modifier.padding(bottom = 10.dp)
    )

    if (users.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Text(
                text = "No user profiles registered yet.",
                modifier = Modifier.padding(16.dp),
                color = Color(0xFF6B7280),
                fontSize = 13.sp
            )
        }
    } else {
        users.forEach { profile ->
            AdminUserCard(profile = profile, viewModel = viewModel)
        }
    }
}

@Composable
fun AdminUserCard(profile: ProfileDto, viewModel: MainViewModel) {
    var newBalanceText by remember(profile.id) { mutableStateOf(profile.walletBalance.toString()) }
    var betproUserText by remember(profile.id) { mutableStateOf(profile.betproUsername ?: "") }
    var betproPassText by remember(profile.id) { mutableStateOf(profile.betproPassword ?: "") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = profile.fullName ?: "No Name",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "${profile.email} | ${profile.country ?: "Pakistan"} (${profile.currency ?: "PKR"})",
                        fontSize = 11.5.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (profile.isBlocked) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (profile.isBlocked) "BLOCKED" else "ACTIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (profile.isBlocked) Color(0xFFEF4444) else Color(0xFF16A34A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Wallet Balance Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = newBalanceText,
                    onValueChange = { newBalanceText = it },
                    label = { Text("Balance (${profile.currency})", fontSize = 11.sp) },
                    modifier = Modifier.width(160.dp),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF22C55E),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF22C55E))
                        .clickable {
                            val b = newBalanceText.toDoubleOrNull()
                            if (b != null) {
                                viewModel.updateUserBalance(profile.id, b)
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "UPDATE BAL",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F5F9)))
            Spacer(modifier = Modifier.height(8.dp))

            // BetPro Exchange Credentials Row
            Text(
                text = "🔑 ISSUE EXCHANGE ID CREDENTIALS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = betproUserText,
                    onValueChange = { betproUserText = it },
                    label = { Text("BetPro User", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                OutlinedTextField(
                    value = betproPassText,
                    onValueChange = { betproPassText = it },
                    label = { Text("BetPro Pass", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF059669))
                        .clickable {
                            viewModel.updateUserBetproCredentials(
                                profile.id,
                                betproUserText.trim(),
                                betproPassText.trim()
                            )
                        }
                        .padding(horizontal = 10.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "SAVE ID",
                        color = Color.White,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AdminTransactionManagementView(viewModel: MainViewModel) {
    AdminRequest(viewModel = viewModel)
}

@Composable
fun AdminGatewayManagementView(viewModel: MainViewModel) {
    val gateways by viewModel.paymentGateways.collectAsState()

    var nameInput by remember { mutableStateOf("") }
    var titleInput by remember { mutableStateOf("") }
    var numberInput by remember { mutableStateOf("") }
    var currencyInput by remember { mutableStateOf("PKR") } // PKR, SAR, AED

    Text(
        text = "PAYMENT GATEWAYS & CURRENCIES",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF111827),
        modifier = Modifier.padding(bottom = 10.dp)
    )

    // Add New Payment Gateway Card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Add Payment Gateway",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Gateway Name (e.g. EasyPaisa, STC Pay, EasyBank)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = titleInput,
                onValueChange = { titleInput = it },
                label = { Text("Account Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = numberInput,
                onValueChange = { numberInput = it },
                label = { Text("Account Number / IBAN") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Locked Gateway Currency",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF374151)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("PKR", "SAR", "AED").forEach { curr ->
                    val isSel = currencyInput == curr
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Color(0xFFDCFCE7) else Color(0xFFF1F5F9))
                            .border(
                                1.dp,
                                if (isSel) Color(0xFF22C55E) else Color(0xFFE2E8F0),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { currencyInput = curr },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = curr,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color(0xFF16A34A) else Color(0xFF374151)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF22C55E))
                    .clickable {
                        if (nameInput.isNotBlank() && titleInput.isNotBlank()) {
                            viewModel.createPaymentGateway(
                                PaymentGatewayDto(
                                    gatewayName = nameInput,
                                    currency = currencyInput,
                                    accountTitle = titleInput,
                                    accountNumber = numberInput,
                                    isEnabled = true
                                )
                            )
                            nameInput = ""
                            titleInput = ""
                            numberInput = ""
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SAVE GATEWAY",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Existing Gateways
    gateways.forEach { gw ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${gw.gatewayName} (${gw.currency})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "Title: ${gw.accountTitle} | No: ${gw.accountNumber}",
                        fontSize = 11.5.sp,
                        color = Color(0xFF6B7280)
                    )
                }

                IconButton(onClick = {
                    gw.id?.let { viewModel.deletePaymentGateway(it) }
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}

@Composable
fun AdminSettingsView(viewModel: MainViewModel, exchangeUrl: String) {
    var urlText by remember { mutableStateOf(exchangeUrl) }

    Text(
        text = "EXCHANGE WEBSITE & HELPLINE",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF111827),
        modifier = Modifier.padding(bottom = 10.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = urlText,
                onValueChange = { urlText = it },
                label = { Text("BetPro Exchange URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF22C55E))
                    .clickable {
                        viewModel.updateExchangeUrl(urlText)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "UPDATE EXCHANGE URL",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "DATABASE & DEMO DATA MANAGEMENT",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF111827),
        modifier = Modifier.padding(bottom = 10.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Purge Demo Data",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF991B1B)
            )
            Text(
                text = "Permanently wipe all demo transactions, dummy users, and sample logs.",
                fontSize = 12.sp,
                color = Color(0xFF7F1D1D),
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFDC2626))
                    .clickable {
                        viewModel.purgeAllDemoData()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PURGE ALL DEMO DATA PERMANENTLY",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
