package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.models.PaymentGatewayDto
import com.example.data.models.TransactionDto
import com.example.ui.MainViewModel
import com.example.ui.components.AnimatedGlassBackground

private fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val bytes = inputStream.readBytes()
        inputStream.close()
        "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        null
    }
}

@Composable
fun UserDashboardScreen(
    viewModel: MainViewModel,
    onOpenBetProExchange: () -> Unit,
    onOpenProfile: () -> Unit = {},
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val userSession by viewModel.currentUser.collectAsState()
    val paymentGateways by viewModel.paymentGateways.collectAsState()
    val transactions by viewModel.userTransactions.collectAsState()

    var showDepositModal by remember { mutableStateOf(false) }
    var showWithdrawModal by remember { mutableStateOf(false) }
    var showTransferModal by remember { mutableStateOf(false) }
    var showHistoryModal by remember { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }
    var selectedBottomNavTab by remember { mutableStateOf(0) } // 0: Home, 1: Deposit, 2: BetPro, 3: Withdraw, 4: History

    LaunchedEffect(userSession?.currency) {
        userSession?.currency?.let { curr ->
            viewModel.loadUserPaymentGateways(curr)
        }
        viewModel.loadUserTransactions()
    }

    val currency = userSession?.currency ?: "PKR"
    val userName = userSession?.fullName?.ifBlank { "BP User" } ?: "BP User"
    val userHandle = if (userSession?.username.isNullOrBlank()) "@user" else "@${userSession?.username}"
    val betproUser = userSession?.betproUsername ?: ""
    val betproPass = userSession?.betproPassword ?: ""
    val hasCredentials = betproUser.isNotBlank() && betproPass.isNotBlank()

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedGlassBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 70.dp) // Leave room for bottom bar
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header Bar matching video
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenProfile() }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = userName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userHandle,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF059669)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFDCFCE7))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "⚡ $currency FIXED",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF047857)
                                    )
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onOpenProfile() }) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color(0xFF10B981)
                            )
                        }

                        Box {
                            IconButton(onClick = { showHistoryModal = true }) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color(0xFF10B981)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                                    .align(Alignment.TopEnd)
                            )
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
                }

                Spacer(modifier = Modifier.height(12.dp))

                // TOP QUICK ACTIONS: PROMINENT DEPOSIT & WITHDRAWAL BUTTONS (PREMIUM WALLET STYLE)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // DEPOSIT BUTTON
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x4010B981), spotColor = Color(0x4010B981))
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF10B981), Color(0xFF059669))
                                )
                            )
                            .clickable { showDepositModal = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Deposit",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "DEPOSIT",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = currency,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // WITHDRAWAL BUTTON
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x400F172A), spotColor = Color(0x400F172A))
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                )
                            )
                            .clickable { showWithdrawModal = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Withdrawal",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "WITHDRAWAL",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = currency,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // CARD 1: EXCHANGE ID CREDENTIALS
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "EXCHANGE ID CREDENTIALS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF334155),
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (hasCredentials) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (hasCredentials) "🟢 ACTIVE ID" else "⏳ AWAITING ADMIN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasCredentials) Color(0xFF047857) else Color(0xFFD97706)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Username Box
                        Column {
                            Text(
                                text = "EXCHANGE ID / USERNAME",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (hasCredentials) {
                                    Text(
                                        text = betproUser,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0F172A)
                                    )
                                    IconButton(onClick = {
                                        clipboardManager.setText(AnnotatedString(betproUser))
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Username",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Available Soon",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Password Box
                        Column {
                            Text(
                                text = "EXCHANGE ID PASSWORD",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (hasCredentials) {
                                    Text(
                                        text = betproPass,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0F172A)
                                    )
                                    IconButton(onClick = {
                                        clipboardManager.setText(AnnotatedString(betproPass))
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Password",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Available Soon",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Warning / Instruction Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (hasCredentials) Color(0xFFFEF3C7) else Color(0xFFEFF6FF))
                                .border(1.dp, if (hasCredentials) Color(0xFFFDE68A) else Color(0xFFBFDBFE), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Text(text = if (hasCredentials) "💡" else "⏳", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (hasCredentials)
                                        "Use these credentials on the official exchange website to log in directly."
                                    else
                                        "Your Exchange ID credentials will be assigned by Admin after your deposit approval.",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (hasCredentials) Color(0xFF92400E) else Color(0xFF1E40AF)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Open Exchange Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .shadow(4.dp, RoundedCornerShape(14.dp), ambientColor = Color(0x3010B981))
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF10B981), Color(0xFF059669))
                                    )
                                )
                                .clickable { onOpenBetProExchange() },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "OPEN BETPRO EXCHANGE",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // CARD 2: OFFICIAL INSTRUCTIONS
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDCFCE7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "OFFICIAL INSTRUCTIONS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A),
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Urdu Instruction Text
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFECFDF5))
                                .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "برائے کرم اپنا بی پی ایکسچینج آئی ڈی حاصل کرنے کے لیے سیکیور ڈپازٹ کریں۔ کم از کم PKR 500 کا ڈپازٹ کریں بعد ایڈمن اپ کا ٹی ڈی ایکٹیو کر دے گا۔",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46),
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // English Text
                        Text(
                            text = "Please deposit a minimum of PKR 500 to get your official BP Exchange ID credentials. Once approved, admin will activate your BP username and password.",
                            fontSize = 11.5.sp,
                            color = Color(0xFF475569),
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // WHATSAPP HELPLINE BUTTON
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .shadow(4.dp, RoundedCornerShape(14.dp), ambientColor = Color(0x3025D366))
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF25D366))
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/923259550448"))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WhatsApp Helpline",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // FLOATING ACTION BUTTON (FAB) SPEED DIAL QUICK ACTIONS MENU
        if (isFabExpanded) {
            // Backdrop to close menu when tapping outside
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { isFabExpanded = false }
            )

            // Speed Dial Expanded Items (Deposit, Withdraw, Transfer)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 144.dp, end = 20.dp)
            ) {
                // Option 3: Transfer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.clickable {
                        isFabExpanded = false
                        showTransferModal = true
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Transfer",
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(6.dp, CircleShape, ambientColor = Color(0x406366F1))
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "Transfer",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Option 2: Withdraw
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.clickable {
                        isFabExpanded = false
                        showWithdrawModal = true
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Withdraw",
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(6.dp, CircleShape, ambientColor = Color(0x400F172A))
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(Color(0xFF334155), Color(0xFF0F172A)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Withdraw",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Option 1: Deposit
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.clickable {
                        isFabExpanded = false
                        showDepositModal = true
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Deposit",
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(6.dp, CircleShape, ambientColor = Color(0x4010B981))
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Deposit",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Main Floating Action Button (FAB) Trigger
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 76.dp, end = 20.dp)
                .size(56.dp)
                .shadow(8.dp, CircleShape, ambientColor = Color(0x4010B981), spotColor = Color(0x4010B981))
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = if (isFabExpanded) listOf(Color(0xFFEF4444), Color(0xFFDC2626))
                        else listOf(Color(0xFF10B981), Color(0xFF047857))
                    )
                )
                .clickable { isFabExpanded = !isFabExpanded },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isFabExpanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "Quick Actions FAB Menu",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        // FIXED BOTTOM NAVIGATION BAR WITH 5 TABS (Home, Deposit, BetPro, Withdraw, History)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(64.dp)
                .background(Color.White)
                .border(width = 1.dp, color = Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Home
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { selectedBottomNavTab = 0 }
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = if (selectedBottomNavTab == 0) Color(0xFF10B981) else Color(0xFF94A3B8),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Home",
                        fontSize = 11.sp,
                        fontWeight = if (selectedBottomNavTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedBottomNavTab == 0) Color(0xFF10B981) else Color(0xFF94A3B8)
                    )
                }

                // Tab 2: Deposit
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            selectedBottomNavTab = 1
                            showDepositModal = true
                        }
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Deposit",
                        tint = if (selectedBottomNavTab == 1) Color(0xFF10B981) else Color(0xFF94A3B8),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Deposit",
                        fontSize = 11.sp,
                        fontWeight = if (selectedBottomNavTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedBottomNavTab == 1) Color(0xFF10B981) else Color(0xFF94A3B8)
                    )
                }

                // Tab 3: BetPro Center Button
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .shadow(6.dp, CircleShape, ambientColor = Color(0x4010B981))
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF22C55E), Color(0xFF059669))
                            )
                        )
                        .clickable { onOpenBetProExchange() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "BetPro",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "BetPro",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                // Tab 4: Withdraw
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            selectedBottomNavTab = 3
                            showWithdrawModal = true
                        }
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Withdraw",
                        tint = if (selectedBottomNavTab == 3) Color(0xFF10B981) else Color(0xFF94A3B8),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Withdraw",
                        fontSize = 11.sp,
                        fontWeight = if (selectedBottomNavTab == 3) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedBottomNavTab == 3) Color(0xFF10B981) else Color(0xFF94A3B8)
                    )
                }

                // Tab 5: History
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            selectedBottomNavTab = 4
                            showHistoryModal = true
                        }
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = if (selectedBottomNavTab == 4) Color(0xFF10B981) else Color(0xFF94A3B8),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "History",
                        fontSize = 11.sp,
                        fontWeight = if (selectedBottomNavTab == 4) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedBottomNavTab == 4) Color(0xFF10B981) else Color(0xFF94A3B8)
                    )
                }
            }
        }
    }

    // Modal Deposit Dialog (Matching Image 1)
    if (showDepositModal) {
        DepositDialog(
            userCurrency = currency,
            gateways = paymentGateways,
            userTransactions = transactions,
            onDismiss = { showDepositModal = false },
            onSubmit = { amount, gwName, accTitle, accNum, sender, ref, screenshotUrl ->
                viewModel.submitDepositRequest(amount, gwName, accTitle, accNum, sender, ref, screenshotUrl) {
                    showDepositModal = false
                }
            }
        )
    }

    // Modal Transfer Dialog
    if (showTransferModal) {
        TransferDialog(
            userCurrency = currency,
            userBalance = userSession?.walletBalance ?: 0.0,
            onDismiss = { showTransferModal = false },
            onSubmit = { recipient, amount, remarks ->
                viewModel.submitTransferRequest(recipient, amount, remarks) {
                    showTransferModal = false
                }
            }
        )
    }

    // Modal Withdraw Dialog (Matching Image 2)
    if (showWithdrawModal) {
        WithdrawDialog(
            userCurrency = currency,
            userBalance = userSession?.walletBalance ?: 0.0,
            gateways = paymentGateways,
            userTransactions = transactions,
            onDismiss = { showWithdrawModal = false },
            onSubmit = { amount, gwName, accTitle, accNum ->
                viewModel.submitWithdrawalRequest(amount, gwName, accTitle, accNum) {
                    showWithdrawModal = false
                }
            }
        )
    }

    // Modal Transaction History Dialog
    if (showHistoryModal) {
        Dialog(onDismissRequest = { showHistoryModal = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TRANSACTION HISTORY",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )
                        IconButton(onClick = { showHistoryModal = false }, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (transactions.isEmpty()) {
                        Text(
                            text = "No deposit or withdrawal history found.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        transactions.forEach { tx ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${tx.type} via ${tx.gatewayName}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "Status: ${tx.status}",
                                            fontSize = 11.sp,
                                            color = when (tx.status) {
                                                "APPROVED" -> Color(0xFF16A34A)
                                                "REJECTED" -> Color(0xFFEF4444)
                                                else -> Color(0xFFD97706)
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "${tx.amount} ${tx.currency}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (tx.type == "DEPOSIT") Color(0xFF16A34A) else Color(0xFF0F172A)
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

@Composable
fun DepositDialog(
    userCurrency: String,
    gateways: List<PaymentGatewayDto>,
    userTransactions: List<TransactionDto>,
    onDismiss: () -> Unit,
    onSubmit: (amount: Double, gatewayName: String, accountTitle: String, accountNumber: String, senderName: String, txRef: String, screenshotUrl: String?) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Default gateways if list is empty
    val displayGateways = if (gateways.isNotEmpty()) gateways else listOf(
        PaymentGatewayDto(id = 1L, gatewayName = "Bank Transfer", currency = userCurrency, accountTitle = "BP Wallet Services", accountNumber = "PK36SCBL0000001123456702", isEnabled = true),
        PaymentGatewayDto(id = 2L, gatewayName = "USDT (TRC20)", currency = userCurrency, accountTitle = "USDT Wallet", accountNumber = "TYu78xKPq23M9901xL", isEnabled = true),
        PaymentGatewayDto(id = 3L, gatewayName = "JazzCash", currency = userCurrency, accountTitle = "JazzCash Official", accountNumber = "03259550448", isEnabled = true),
        PaymentGatewayDto(id = 4L, gatewayName = "EasyPaisa", currency = userCurrency, accountTitle = "EasyPaisa Official", accountNumber = "03259550448", isEnabled = true)
    )

    var selectedGateway by remember { mutableStateOf<PaymentGatewayDto>(displayGateways.first()) }
    var amountText by remember { mutableStateOf("") }
    var senderName by remember { mutableStateOf("") }
    var txRef by remember { mutableStateOf("") }
    var screenshotBase64 by remember { mutableStateOf<String?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Image picker launcher for screenshot proof upload
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64 = uriToBase64(context, uri)
            if (base64 != null) {
                screenshotBase64 = base64
                validationError = null
            } else {
                screenshotBase64 = uri.toString()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header (Matching Image 1)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Deposit Funds",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Instant automatic processing ($userCurrency)",
                            fontSize = 11.5.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Gateway Tabs Horizontal Row (Matching Image 1)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    displayGateways.forEach { gw ->
                        val isSel = selectedGateway.id == gw.id || selectedGateway.gatewayName == gw.gatewayName
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) Color(0xFFECFDF5) else Color(0xFFF8FAFC))
                                .border(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) Color(0xFF10B981) else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedGateway = gw
                                    validationError = null
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSel) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = gw.gatewayName,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color(0xFF047857) else Color(0xFF334155)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // "SEND PAYMENT TO" Green Box (Matching Image 1)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFECFDF5))
                        .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "SEND PAYMENT TO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF047857),
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedGateway.accountTitle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = selectedGateway.accountNumber,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF047857)
                                )
                            }

                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString("${selectedGateway.accountTitle}\n${selectedGateway.accountNumber}"))
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD1FAE5))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy details",
                                    tint = Color(0xFF047857),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Meezan Bank / Gateway. Use your Account Title as reference.",
                            fontSize = 10.5.sp,
                            color = Color(0xFF065F46)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Form Inputs
                Text(
                    text = "Deposit Amount ($userCurrency)",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        validationError = null
                    },
                    placeholder = { Text("e.g. 5000") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Sender Account Title",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = senderName,
                    onValueChange = {
                        senderName = it
                        validationError = null
                    },
                    placeholder = { Text("e.g. Ali Khan") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Transaction ID / reference",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = txRef,
                    onValueChange = {
                        txRef = it
                        validationError = null
                    },
                    placeholder = { Text("e.g. TRX8837261") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Payment screenshot field with dashed box (Matching Image 1)
                Text(
                    text = "Payment screenshot (Proof of payment)",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (screenshotBase64 != null) Color(0xFFECFDF5) else Color(0xFFF8FAFC))
                        .border(
                            width = 1.5.dp,
                            color = if (screenshotBase64 != null) Color(0xFF10B981) else Color(0xFFCBD5E1),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            imagePickerLauncher.launch("image/*")
                        }
                        .padding(14.dp)
                ) {
                    if (screenshotBase64 != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                            ) {
                                AsyncImage(
                                    model = screenshotBase64,
                                    contentDescription = "Payment Screenshot Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF047857),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Screenshot Attached! Click to change",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857)
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDCFCE7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Upload Screenshot",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Upload proof of payment",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "PNG or JPG, up to 5 MB",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                if (validationError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = validationError!!,
                        color = Color(0xFFEF4444),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit deposit request Pill Button (Matching Image 1)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .shadow(4.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x3010B981))
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF10B981))
                        .clickable {
                            val amt = amountText.toDoubleOrNull()
                            if (amt == null || amt <= 0.0) {
                                validationError = "Please enter a valid deposit amount > 0"
                            } else if (txRef.isBlank()) {
                                validationError = "Please enter Transaction ID / reference"
                            } else {
                                onSubmit(
                                    amt,
                                    selectedGateway.gatewayName,
                                    selectedGateway.accountTitle,
                                    selectedGateway.accountNumber,
                                    senderName.ifBlank { "User" },
                                    txRef.trim(),
                                    screenshotBase64
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Submit deposit request",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Deposit History List (Matching Image 1)
                Text(
                    text = "Deposit history",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(8.dp))

                val depositHistory = userTransactions.filter { it.type == "DEPOSIT" }
                if (depositHistory.isEmpty()) {
                    Text(
                        text = "No previous deposit requests found.",
                        fontSize = 11.5.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    depositHistory.forEach { tx ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = tx.gatewayName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "TRX: ${tx.transactionRef.ifBlank { tx.id.take(12) }}",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Rs ${tx.amount.toInt()}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0F172A)
                                    )
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
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = tx.status,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (tx.status) {
                                                "APPROVED" -> Color(0xFF16A34A)
                                                "REJECTED" -> Color(0xFFEF4444)
                                                else -> Color(0xFFD97706)
                                            }
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
}

@Composable
fun WithdrawDialog(
    userCurrency: String,
    userBalance: Double,
    gateways: List<PaymentGatewayDto>,
    userTransactions: List<TransactionDto>,
    onDismiss: () -> Unit,
    onSubmit: (amount: Double, gatewayName: String, accountTitle: String, accountNumber: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedGatewayName by remember { mutableStateOf("JazzCash") }
    var accountTitle by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var remarksText by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val gatewayOptions = listOf("JazzCash", "EasyPaisa", "Meezan Bank", "USDT (TRC20)", "Bank Transfer")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header (Matching Image 2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF0F172A)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Withdraw",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Payouts processed daily ($userCurrency)",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Form Inputs (Matching Image 2)
                Text(
                    text = "Amount ($userCurrency)",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        validationError = null
                    },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Bank / wallet",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    gatewayOptions.forEach { opt ->
                        val isSel = selectedGatewayName == opt
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) Color(0xFFECFDF5) else Color(0xFFF8FAFC))
                                .border(
                                    width = if (isSel) 1.5.dp else 1.dp,
                                    color = if (isSel) Color(0xFF10B981) else Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedGatewayName = opt }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = opt,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color(0xFF047857) else Color(0xFF334155)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Account title",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = accountTitle,
                    onValueChange = {
                        accountTitle = it
                        validationError = null
                    },
                    placeholder = { Text("Enter account title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Account number / IBAN",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = {
                        accountNumber = it
                        validationError = null
                    },
                    placeholder = { Text("Enter account number or IBAN") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Remarks (optional)",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = remarksText,
                    onValueChange = { remarksText = it },
                    placeholder = { Text("e.g. Urgent payout") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                if (validationError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = validationError!!,
                        color = Color(0xFFEF4444),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit withdrawal request Pill Button (Matching Image 2)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .shadow(4.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x3010B981))
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF10B981))
                        .clickable {
                            val amt = amountText.toDoubleOrNull()
                            if (amt == null || amt <= 0.0) {
                                validationError = "Please enter a valid withdrawal amount > 0"
                            } else if (accountTitle.isBlank()) {
                                validationError = "Please enter account title"
                            } else if (accountNumber.isBlank()) {
                                validationError = "Please enter account number or IBAN"
                            } else {
                                onSubmit(
                                    amt,
                                    selectedGatewayName,
                                    accountTitle.trim(),
                                    accountNumber.trim()
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Submit withdrawal request",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Withdrawal History List (Matching Image 2)
                Text(
                    text = "Withdrawal history",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(8.dp))

                val withdrawalHistory = userTransactions.filter { it.type == "WITHDRAWAL" }
                if (withdrawalHistory.isEmpty()) {
                    Text(
                        text = "No previous withdrawal requests found.",
                        fontSize = 11.5.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    withdrawalHistory.forEach { tx ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = tx.gatewayName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "To: ${tx.accountTitle} (${tx.accountNumber.take(10)})",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Rs ${tx.amount.toInt()}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0F172A)
                                    )
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
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = tx.status,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (tx.status) {
                                                "APPROVED" -> Color(0xFF16A34A)
                                                "REJECTED" -> Color(0xFFEF4444)
                                                else -> Color(0xFFD97706)
                                            }
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
}

@Composable
fun TransferDialog(
    userCurrency: String,
    userBalance: Double,
    onDismiss: () -> Unit,
    onSubmit: (recipient: String, amount: Double, remarks: String) -> Unit
) {
    var recipientText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var remarksText by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
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
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Transfer Funds",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Instant wallet transfer",
                                fontSize = 11.5.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Available Balance Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Available Balance",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$userCurrency ${String.format("%.2f", userBalance)}",
                            fontSize = 14.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Recipient Input
                Text(
                    text = "Recipient Username or Email",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = recipientText,
                    onValueChange = {
                        recipientText = it
                        validationError = null
                    },
                    placeholder = { Text("e.g. @john_doe or user@email.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Amount Input
                Text(
                    text = "Transfer Amount ($userCurrency)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        validationError = null
                    },
                    placeholder = { Text("Enter amount") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Remarks Input
                Text(
                    text = "Remarks / Note (optional)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = remarksText,
                    onValueChange = { remarksText = it },
                    placeholder = { Text("e.g. Payment for bill") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                if (validationError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = validationError!!,
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Confirm Transfer Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(4.dp, RoundedCornerShape(25.dp), ambientColor = Color(0x306366F1))
                        .clip(RoundedCornerShape(25.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
                            )
                        )
                        .clickable {
                            val amt = amountText.toDoubleOrNull()
                            if (recipientText.isBlank()) {
                                validationError = "Please enter recipient username or email"
                            } else if (amt == null || amt <= 0.0) {
                                validationError = "Please enter a valid amount > 0"
                            } else if (amt > userBalance) {
                                validationError = "Amount exceeds available balance"
                            } else {
                                onSubmit(recipientText.trim(), amt, remarksText.trim())
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Confirm Transfer",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
