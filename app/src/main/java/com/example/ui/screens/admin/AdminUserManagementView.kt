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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ProfileDto
import com.example.ui.MainViewModel

@Composable
fun AdminUserManagementView(
    viewModel: MainViewModel,
    users: List<ProfileDto>,
    highlightUserId: String? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("ALL") } // ALL, ACTIVE, BLOCKED

    var editingBalanceUser by remember { mutableStateOf<ProfileDto?>(null) }
    var newBalanceText by remember { mutableStateOf("") }

    var editingCredsUser by remember { mutableStateOf<ProfileDto?>(null) }
    var credsUsername by remember { mutableStateOf("") }
    var credsPassword by remember { mutableStateOf("") }

    val filteredUsers = users.filter { u ->
        val matchesStatus = when (filterStatus) {
            "ACTIVE" -> !u.isBlocked
            "BLOCKED" -> u.isBlocked
            else -> true
        }
        val query = searchQuery.trim()
        val matchesQuery = query.isBlank() ||
                (u.fullName?.contains(query, ignoreCase = true) == true) ||
                (u.username?.contains(query, ignoreCase = true) == true) ||
                (u.email?.contains(query, ignoreCase = true) == true) ||
                u.id.contains(query, ignoreCase = true)

        matchesStatus && matchesQuery
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by username, email, name, or ID...", color = AdminTheme.TextMuted, fontSize = 13.sp) },
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
                focusedBorderColor = AdminTheme.Cyan,
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
            listOf("ALL", "ACTIVE", "BLOCKED").forEach { status ->
                val isSelected = filterStatus == status
                val count = when (status) {
                    "ACTIVE" -> users.count { !it.isBlocked }
                    "BLOCKED" -> users.count { it.isBlocked }
                    else -> users.size
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AdminTheme.Cyan else AdminTheme.SurfaceDark)
                        .border(1.dp, if (isSelected) AdminTheme.Cyan else AdminTheme.BorderSubtle, RoundedCornerShape(8.dp))
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

        if (filteredUsers.isEmpty()) {
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
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = AdminTheme.TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No user accounts match your search",
                        color = AdminTheme.TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredUsers.forEach { user ->
                    val isHighlighted = highlightUserId != null && user.id == highlightUserId

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = AdminTheme.SurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isHighlighted) 2.dp else 1.dp,
                            color = if (isHighlighted) AdminTheme.Cyan else AdminTheme.BorderSubtle
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
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(AdminTheme.Cyan.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (user.fullName ?: user.username ?: "U").take(1).uppercase(),
                                            color = AdminTheme.Cyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = user.fullName ?: user.username ?: "Unnamed Trader",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "@${user.username ?: "user"} • ${user.email ?: "No email"}",
                                            color = AdminTheme.TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (user.isBlocked) AdminTheme.RedDark else AdminTheme.EmeraldDark)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (user.isBlocked) "BLOCKED" else "ACTIVE",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Details Panel
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AdminTheme.CardBg)
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Wallet Balance:",
                                        color = AdminTheme.TextSecondary,
                                        fontSize = 12.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${user.currency} ${String.format(java.util.Locale.US, "%,.2f", user.walletBalance)}",
                                            color = AdminTheme.Emerald,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Balance",
                                            tint = AdminTheme.Cyan,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable {
                                                    editingBalanceUser = user
                                                    newBalanceText = user.walletBalance.toInt().toString()
                                                }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Exchange ID:",
                                        color = AdminTheme.TextSecondary,
                                        fontSize = 11.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val bpUser = user.betproUsername ?: ""
                                        Text(
                                            text = bpUser.ifBlank { "Not Assigned" },
                                            color = if (bpUser.isNotBlank()) AdminTheme.Amber else AdminTheme.TextMuted,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Key,
                                            contentDescription = "Edit Exchange Credentials",
                                            tint = AdminTheme.Amber,
                                            modifier = Modifier
                                                .size(15.dp)
                                                .clickable {
                                                    editingCredsUser = user
                                                    credsUsername = user.betproUsername ?: ""
                                                    credsPassword = user.betproPassword ?: ""
                                                }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "User ID: ${user.id}",
                                    color = AdminTheme.TextMuted,
                                    fontSize = 10.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        editingBalanceUser = user
                                        newBalanceText = user.walletBalance.toInt().toString()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AdminTheme.Cyan)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = AdminTheme.Cyan,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Adjust Balance", color = AdminTheme.Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        editingCredsUser = user
                                        credsUsername = user.betproUsername ?: ""
                                        credsPassword = user.betproPassword ?: ""
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AdminTheme.Amber)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = AdminTheme.Amber,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Exchange ID", color = AdminTheme.Amber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // EDIT BALANCE DIALOG
    editingBalanceUser?.let { user ->
        AlertDialog(
            onDismissRequest = { editingBalanceUser = null },
            containerColor = AdminTheme.SurfaceDark,
            title = {
                Text(
                    text = "Update User Balance",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "User: ${user.fullName ?: user.username}",
                        color = AdminTheme.TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newBalanceText,
                        onValueChange = { newBalanceText = it },
                        label = { Text("New Balance (${user.currency})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminTheme.Emerald,
                            unfocusedBorderColor = AdminTheme.BorderSubtle,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Quick Presets:",
                        color = AdminTheme.TextMuted,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(1000, 5000, 10000, 25000).forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AdminTheme.CardBg)
                                    .clickable {
                                        val cur = newBalanceText.toDoubleOrNull() ?: 0.0
                                        newBalanceText = (cur + preset).toInt().toString()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "+${preset / 1000}k",
                                    color = AdminTheme.Emerald,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = newBalanceText.toDoubleOrNull()
                        if (amount != null) {
                            viewModel.updateUserBalance(user.id, amount)
                            editingBalanceUser = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminTheme.Emerald)
                ) {
                    Text("Save Balance", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingBalanceUser = null }) {
                    Text("Cancel", color = AdminTheme.TextSecondary)
                }
            }
        )
    }

    // EDIT BETPRO CREDENTIALS DIALOG
    editingCredsUser?.let { user ->
        AlertDialog(
            onDismissRequest = { editingCredsUser = null },
            containerColor = AdminTheme.SurfaceDark,
            title = {
                Text(
                    text = "Exchange ID Credentials",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Set BetPro exchange login for ${user.fullName ?: user.username}:",
                        color = AdminTheme.TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = credsUsername,
                        onValueChange = { credsUsername = it },
                        label = { Text("Exchange Username") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminTheme.Amber,
                            unfocusedBorderColor = AdminTheme.BorderSubtle,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = credsPassword,
                        onValueChange = { credsPassword = it },
                        label = { Text("Exchange Password") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AdminTheme.Amber,
                            unfocusedBorderColor = AdminTheme.BorderSubtle,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateUserBetproCredentials(user.id, credsUsername.trim(), credsPassword.trim())
                        editingCredsUser = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminTheme.Amber)
                ) {
                    Text("Update Credentials", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCredsUser = null }) {
                    Text("Cancel", color = AdminTheme.TextSecondary)
                }
            }
        )
    }
}
