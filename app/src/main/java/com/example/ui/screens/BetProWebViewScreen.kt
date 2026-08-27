package com.example.ui.screens

import android.graphics.Bitmap
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.MainViewModel

@Composable
fun BetProWebViewScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val userSession by viewModel.currentUser.collectAsState()
    val url by viewModel.exchangeUrl.collectAsState()

    var webViewRef: WebView? = null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1812))
    ) {
        // WebView Top Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF040A07))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Column {
                    Text(
                        text = "BetPro Live Exchange Portal",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Active Account: ${userSession?.fullName} (${userSession?.currency})",
                        color = Color(0xFF22C55E),
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(onClick = { webViewRef?.reload() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reload",
                    tint = Color.White
                )
            }
        }

        // Integrated Android WebView (Fully Responsive Fix)
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            // 1. Force Page Background and Full Height (Removes White Space below Login)
                            // 2. Adjust Viewport Width (Fits Table and Content inside Mobile Screen)
                            evaluateJavascript(
                                """
                                (function() {
                                    document.body.style.minHeight = '100vh';
                                    document.body.style.backgroundColor = '#0d1812';
                                    
                                    var meta = document.querySelector('meta[name="viewport"]');
                                    if (!meta) {
                                        meta = document.createElement('meta');
                                        meta.name = 'viewport';
                                        document.getElementsByTagName('head')[0].appendChild(meta);
                                    }
                                    meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=3.0, user-scalable=yes';
                                })();
                                """.trimIndent(), null
                            )
                        }
                    }

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        
                        // Scale settings to fit width
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        
                        // Enable Zoom so user can zoom table if needed
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false

                        databaseEnabled = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }

                    loadUrl(url)
                    webViewRef = this
                }
            },
            update = { view ->
                webViewRef = view
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Ensures top-to-bottom stretch
        )
    }
}
