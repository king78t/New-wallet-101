package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.MainViewModel

private const val MOBILE_OPTIMIZATION_JS = """
(function() {
    // 1. Enforce standard mobile device viewport rules
    var meta = document.querySelector('meta[name="viewport"]');
    var targetViewport = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no, viewport-fit=cover';
    if (!meta) {
        meta = document.createElement('meta');
        meta.name = 'viewport';
        meta.content = targetViewport;
        (document.head || document.documentElement).appendChild(meta);
    } else {
        if (!meta.content || meta.content.indexOf('width=device-width') === -1 || meta.content.indexOf('user-scalable=no') === -1) {
            meta.content = targetViewport;
        }
    }

    // 2. Prevent horizontal overflow & ensure 100% max-width containment
    var styleId = 'bp-mobile-containment-css';
    var style = document.getElementById(styleId);
    if (!style) {
        style = document.createElement('style');
        style.id = styleId;
        style.type = 'text/css';
        style.innerHTML = `
            html, body {
                max-width: 100% !important;
                width: 100% !important;
                overflow-x: hidden !important;
                margin: 0 !important;
                padding: 0 !important;
                box-sizing: border-box !important;
                -webkit-text-size-adjust: 100% !important;
                -webkit-tap-highlight-color: transparent !important;
            }
            *, *:before, *:after {
                box-sizing: border-box !important;
            }
            /* Prevent WebKit auto-zooming on input focus while keeping readability */
            input, select, textarea {
                font-size: 16px !important;
            }
        `;
        (document.head || document.documentElement).appendChild(style);
    }

    // 3. Smooth focus scrolling: Keep focused fields and login buttons visible above virtual keyboard
    function setupFormAutoScroll() {
        var elements = document.querySelectorAll('input, select, textarea');
        for (var i = 0; i < elements.length; i++) {
            var el = elements[i];
            if (!el.getAttribute('data-bp-focus-scroller')) {
                el.setAttribute('data-bp-focus-scroller', 'true');
                el.addEventListener('focus', function(e) {
                    var target = e.target;
                    setTimeout(function() {
                        try {
                            target.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'nearest' });
                        } catch (err) {
                            target.scrollIntoView(false);
                        }
                    }, 280);
                });
            }
        }
    }
    setupFormAutoScroll();

    if (window.MutationObserver && !window._bpAutoScrollObserver) {
        window._bpAutoScrollObserver = true;
        var observer = new MutationObserver(function() {
            setupFormAutoScroll();
        });
        observer.observe(document.body || document.documentElement, { childList: true, subtree: true });
    }
})();
"""

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BetProWebViewScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val url by viewModel.exchangeUrl.collectAsState()
    val displayName by viewModel.betproDisplayName.collectAsState()

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableFloatStateOf(0.1f) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Always fetch freshest BetPro URL from backend/database upon opening screen
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadSystemSettings()
    }

    // Hardware/System back button handling
    BackHandler {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onBack()
        }
    }

    // Clean up WebView on disposal
    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.let { wv ->
                wv.stopLoading()
                wv.destroy()
            }
            webViewInstance = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060B12))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // ================================================================
        // PREMIUM TOP APP BAR
        // ================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF090D16)
                        )
                    )
                )
                .border(
                    width = 0.5.dp,
                    color = Color(0xFF1E293B)
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back button & Title block
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = {
                                if (webViewInstance?.canGoBack() == true) {
                                    webViewInstance?.goBack()
                                } else {
                                    onBack()
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate Back",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // BetPro Brand Shield Icon
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF10B981), Color(0xFF047857))
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Title and Live Status Indicator
                        Column {
                            Text(
                                text = displayName.ifBlank { "BetPro Exchange" },
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 1.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(Color(0xFF22C55E), shape = CircleShape)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Live Exchange",
                                    color = Color(0xFF22C55E),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "256-bit SSL",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Refresh Button
                    IconButton(
                        onClick = {
                            hasError = false
                            webViewInstance?.reload()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload Page",
                            tint = Color.White
                        )
                    }
                }

                // Loading Progress Indicator
                AnimatedVisibility(
                    visible = isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LinearProgressIndicator(
                        progress = { loadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp),
                        color = Color(0xFF10B981),
                        trackColor = Color(0xFF0F172A)
                    )
                }
            }
        }

        // ================================================================
        // FULL-SCREEN CONSTRAINED WEBVIEW CONTENT AREA
        // ================================================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .background(Color(0xFF000000))
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        // Configure standard touch & scrolling constraints
                        overScrollMode = View.OVER_SCROLL_NEVER
                        isHorizontalScrollBarEnabled = false
                        isVerticalScrollBarEnabled = true
                        isFocusable = true
                        isFocusableInTouchMode = true

                        // Modern Cookie Setup (including 3rd-party cookies if required)
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        // Strict Android WebView Settings
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            setSupportZoom(false)
                            builtInZoomControls = false
                            displayZoomControls = false
                            textZoom = 100
                            layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
                            cacheMode = WebSettings.LOAD_DEFAULT
                            mediaPlaybackRequiresUserGesture = false
                            allowFileAccess = false
                            allowContentAccess = false
                        }

                        // Robust WebChromeClient for smooth progress tracking
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                loadProgress = (newProgress.coerceIn(1, 100)) / 100f
                                if (newProgress >= 100) {
                                    isLoading = false
                                }
                                // Inject viewport early during DOM construction
                                if (newProgress in 30..90) {
                                    view?.evaluateJavascript(MOBILE_OPTIMIZATION_JS, null)
                                }
                            }
                        }

                        // Custom WebViewClient with mobile viewport enforcement
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                                hasError = false
                                errorMessage = null
                                view?.evaluateJavascript(MOBILE_OPTIMIZATION_JS, null)
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                CookieManager.getInstance().flush()
                                view?.evaluateJavascript(MOBILE_OPTIMIZATION_JS, null)
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                if (request?.isForMainFrame == true) {
                                    isLoading = false
                                    hasError = true
                                    errorMessage = error?.description?.toString()
                                        ?: "Failed to establish connection to BetPro Exchange."
                                }
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val destinationUri = request?.url ?: return false
                                val scheme = destinationUri.scheme?.lowercase()
                                return if (scheme == "http" || scheme == "https") {
                                    false
                                } else {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, destinationUri)
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                    true
                                }
                            }
                        }

                        loadUrl(url)
                        webViewInstance = this
                    }
                },
                update = { view ->
                    webViewInstance = view
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
            )

            // ============================================================
            // NATIVE ERROR / RETRY SCREEN
            // ============================================================
            if (hasError) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF060B12))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(Color(0xFF7F1D1D).copy(alpha = 0.3f), shape = CircleShape)
                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Connection Error",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = errorMessage ?: "Unable to load BetPro Exchange. Please check your internet connection.",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                hasError = false
                                isLoading = true
                                webViewInstance?.reload()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Try Again",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
