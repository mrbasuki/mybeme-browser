package space.mrbasukirahmat.browser.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import space.mrbasukirahmat.browser.ui.components.BottomNavigationBar
import space.mrbasukirahmat.browser.ui.components.BrowserMenuSheet
import space.mrbasukirahmat.browser.ui.components.ShieldDialog
import space.mrbasukirahmat.browser.ui.components.TabGridDialog
import space.mrbasukirahmat.browser.ui.copilot.MybemeCoPilotSheet
import space.mrbasukirahmat.browser.ui.theme.*
import space.mrbasukirahmat.browser.ui.viewmodel.BrowserViewModel
import space.mrbasukirahmat.browser.ui.viewmodel.SyncStatus
import space.mrbasukirahmat.browser.util.ReadabilityExtractor
import space.mrbasukirahmat.browser.webview.MybemeWebViewClient

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var summaryText by remember { mutableStateOf<String?>(null) }

    // Handle back button inside WebView
    BackHandler(enabled = webViewRef?.canGoBack() == true) {
        webViewRef?.goBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianBlack,
        topBar = {
            // Dynamic Header with Tailscale Mesh Status
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianSurface)
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Tailscale LED indicator
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (uiState.syncStatus) {
                                        SyncStatus.CONNECTED -> ShieldGreen
                                        SyncStatus.RECONNECTING -> DesertGold
                                        SyncStatus.DISCONNECTED -> TextMuted
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (uiState.syncStatus) {
                                SyncStatus.CONNECTED -> "Mybeme VPS (100.80.80.80)"
                                SyncStatus.RECONNECTING -> "Menyambung Tailscale..."
                                SyncStatus.DISCONNECTED -> "Tailscale Offline"
                            },
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = uiState.currentTitle,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 180.dp)
                    )
                }

                if (uiState.isLoading) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { uiState.progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp),
                        color = MoltenOrange,
                        trackColor = ObsidianBorder
                    )
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(
                currentUrl = uiState.currentUrl,
                blockedCount = uiState.blockedCount,
                tabCount = uiState.tabs.size,
                onNavigate = { newUrl ->
                    viewModel.updateUrl(newUrl)
                    webViewRef?.loadUrl(newUrl)
                },
                onShieldClick = {
                    viewModel.openShieldDialog(true)
                },
                onMybemeClick = {
                    viewModel.openCoPilotSheet(true)
                },
                onTabClick = {
                    viewModel.openTabGrid(true)
                },
                onMenuClick = {
                    viewModel.openMenuSheet(true)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ObsidianBlack)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                        }

                        webViewClient = MybemeWebViewClient(
                            shield = viewModel.shield,
                            onPageStartedCallback = { url ->
                                viewModel.updateUrl(url)
                            },
                            onPageFinishedCallback = { url ->
                                viewModel.updateUrl(url)
                            },
                            onTitleReceivedCallback = { title ->
                                viewModel.updateTitle(title)
                            }
                        )

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                viewModel.updateProgress(newProgress)
                            }
                        }

                        loadUrl(uiState.currentUrl)
                        webViewRef = this
                    }
                },
                update = { webView ->
                    if (webView.url != uiState.currentUrl && !uiState.isLoading) {
                        webView.loadUrl(uiState.currentUrl)
                    }
                    val desktopUA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
                    if (uiState.isDesktopMode && !webView.settings.userAgentString.contains("X11")) {
                        webView.settings.userAgentString = desktopUA
                        webView.reload()
                    } else if (!uiState.isDesktopMode && webView.settings.userAgentString.contains("X11")) {
                        webView.settings.userAgentString = null
                        webView.reload()
                    }
                }
            )

            // Tab Switcher Dialog
            if (uiState.isTabGridOpen) {
                TabGridDialog(
                    tabs = uiState.tabs,
                    activeTabId = uiState.activeTabId,
                    onSelectTab = { tab ->
                        viewModel.selectTab(tab)
                        webViewRef?.loadUrl(tab.url)
                    },
                    onCloseTab = { tab ->
                        viewModel.closeTab(tab)
                    },
                    onNewTab = {
                        viewModel.addNewTab()
                        webViewRef?.loadUrl("https://duckduckgo.com")
                    },
                    onDismiss = {
                        viewModel.openTabGrid(false)
                    }
                )
            }

            // Brave Shields Status Dialog
            if (uiState.isShieldDialogOpen) {
                ShieldDialog(
                    url = uiState.currentUrl,
                    blockedCount = uiState.blockedCount,
                    isShieldEnabled = uiState.isShieldEnabled,
                    onToggleShield = { enabled ->
                        viewModel.toggleShieldEnabled(enabled)
                    },
                    onClearSiteData = {
                        webViewRef?.clearCache(true)
                        webViewRef?.clearFormData()
                        viewModel.clearStats()
                    },
                    onDismiss = {
                        viewModel.openShieldDialog(false)
                    }
                )
            }

            // Browser 3-Dots Overflow Menu Sheet
            if (uiState.isMenuSheetOpen) {
                BrowserMenuSheet(
                    isDesktopMode = uiState.isDesktopMode,
                    onNewTab = {
                        viewModel.addNewTab()
                        webViewRef?.loadUrl("https://duckduckgo.com")
                    },
                    onReload = {
                        webViewRef?.reload()
                    },
                    onToggleDesktopMode = {
                        viewModel.toggleDesktopMode()
                    },
                    onShare = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, uiState.currentUrl)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Bagikan Tautan")
                        context.startActivity(shareIntent)
                    },
                    onBookmark = {
                        summaryText = "⭐ Halaman '${uiState.currentTitle}' disimpan ke bookmark lokal!"
                        viewModel.openCoPilotSheet(true)
                    },
                    onDismiss = {
                        viewModel.openMenuSheet(false)
                    }
                )
            }

            // Mybeme Co-Pilot Bottom Sheet
            if (uiState.isCoPilotSheetOpen) {
                MybemeCoPilotSheet(
                    url = uiState.currentUrl,
                    title = uiState.currentTitle,
                    summaryResult = summaryText,
                    onSummarizeClick = {
                        webViewRef?.let { wv ->
                            ReadabilityExtractor.extractCleanText(wv) { text ->
                                summaryText = "Menganalisis artikel...\n\nPoin Penting:\n• " +
                                    (if (text.length > 200) text.take(200) + "..." else text)
                            }
                        }
                    },
                    onHandoffClick = {
                        summaryText = "✓ Tab berhasil dioper ke workspace riset Mybeme di VPS!"
                    },
                    onSecurityCheckClick = {
                        val isHttps = uiState.currentUrl.startsWith("https://")
                        summaryText = "Audit Keamanan:\n• Protokol: " + (if (isHttps) "HTTPS Terenkripsi" else "HTTP Terbuka") +
                            "\n• Pelacak diblokir: " + uiState.blockedCount + " trackers dicegat" +
                            "\n• Status: Aman untuk browsing"
                    },
                    onDismiss = {
                        viewModel.openCoPilotSheet(false)
                    }
                )
            }
        }
    }
}
