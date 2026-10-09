package space.mrbasukirahmat.browser.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.mrbasukirahmat.browser.data.db.AppDatabase
import space.mrbasukirahmat.browser.data.db.HistoryEntity
import space.mrbasukirahmat.browser.data.db.TabEntity
import space.mrbasukirahmat.browser.shield.BraveShieldsInterceptor
import space.mrbasukirahmat.browser.sync.TailscaleSyncManager
import java.util.UUID

enum class SyncStatus {
    CONNECTED, RECONNECTING, DISCONNECTED
}

data class BrowserUiState(
    val currentUrl: String = "https://duckduckgo.com",
    val currentTitle: String = "DuckDuckGo",
    val isLoading: Boolean = false,
    val progress: Int = 100,
    val blockedCount: Int = 0,
    val activeTabId: String = "default-tab",
    val tabs: List<TabEntity> = emptyList(),
    val syncStatus: SyncStatus = SyncStatus.DISCONNECTED,
    val isCoPilotSheetOpen: Boolean = false,
    val isTabGridOpen: Boolean = false,
    val isShieldDialogOpen: Boolean = false,
    val isMenuSheetOpen: Boolean = false,
    val isDesktopMode: Boolean = false,
    val isShieldEnabled: Boolean = true,
    val isToolbarAtTop: Boolean = true
)

class BrowserViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val tabDao = db.tabDao()
    private val historyDao = db.historyDao()

    val shield = BraveShieldsInterceptor(context = application)

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    fun toggleToolbarPosition() {
        _uiState.value = _uiState.value.copy(isToolbarAtTop = !_uiState.value.isToolbarAtTop)
    }

    private val syncManager = TailscaleSyncManager(
        host = "100.80.80.80",
        port = 8765,
        token = "mybeme-browser-key-991823",
        onTabReceived = { url, title, note ->
            viewModelScope.launch(Dispatchers.Main) {
                addNewTab(url = url, title = title, source = "MYBEME")
            }
        },
        onSummaryReceived = { _, _ -> },
        onStatusChanged = { connected ->
            viewModelScope.launch(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    syncStatus = if (connected) SyncStatus.CONNECTED else SyncStatus.DISCONNECTED
                )
            }
        }
    )

    init {
        // Start Tailscale WebSocket connection
        syncManager.connect()

        // Observe Room DB Tabs
        viewModelScope.launch {
            tabDao.getAllTabs().collect { storedTabs ->
                if (storedTabs.isEmpty()) {
                    val initialTab = TabEntity(
                        id = "default-tab",
                        url = "https://duckduckgo.com",
                        title = "DuckDuckGo",
                        source = "LOCAL"
                    )
                    tabDao.insertTab(initialTab)
                    _uiState.value = _uiState.value.copy(
                        tabs = listOf(initialTab),
                        activeTabId = initialTab.id,
                        currentUrl = initialTab.url,
                        currentTitle = initialTab.title
                    )
                } else {
                    val currentActiveId = _uiState.value.activeTabId
                    val active = storedTabs.find { it.id == currentActiveId } ?: storedTabs.first()
                    _uiState.value = _uiState.value.copy(
                        tabs = storedTabs,
                        activeTabId = active.id,
                        currentUrl = if (_uiState.value.currentUrl.isEmpty()) active.url else _uiState.value.currentUrl,
                        currentTitle = if (_uiState.value.currentTitle.isEmpty()) active.title else _uiState.value.currentTitle
                    )
                }
            }
        }
    }

    fun openShieldDialog(open: Boolean) {
        _uiState.value = _uiState.value.copy(isShieldDialogOpen = open)
    }

    fun openMenuSheet(open: Boolean) {
        _uiState.value = _uiState.value.copy(isMenuSheetOpen = open)
    }

    fun toggleShieldEnabled(enabled: Boolean) {
        shield.isEnabled = enabled
        _uiState.value = _uiState.value.copy(isShieldEnabled = enabled)
    }

    fun toggleDesktopMode() {
        _uiState.value = _uiState.value.copy(isDesktopMode = !_uiState.value.isDesktopMode)
    }

    fun clearStats() {
        shield.resetStats()
        _uiState.value = _uiState.value.copy(blockedCount = 0)
    }

    fun updateUrl(url: String) {
        val secured = shield.upgradeUrlToHttps(url)
        _uiState.value = _uiState.value.copy(
            currentUrl = secured,
            blockedCount = shield.blockedCount
        )

        // Record visit in Room DB History
        viewModelScope.launch(Dispatchers.IO) {
            historyDao.recordVisit(
                HistoryEntity(
                    url = secured,
                    title = _uiState.value.currentTitle
                )
            )
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(currentTitle = title)
    }

    fun updateProgress(progress: Int) {
        _uiState.value = _uiState.value.copy(
            progress = progress,
            isLoading = progress < 100,
            blockedCount = shield.blockedCount
        )
    }

    fun openCoPilotSheet(open: Boolean) {
        _uiState.value = _uiState.value.copy(isCoPilotSheetOpen = open)
    }

    fun openTabGrid(open: Boolean) {
        _uiState.value = _uiState.value.copy(isTabGridOpen = open)
    }

    fun addNewTab(url: String = "https://duckduckgo.com", title: String = "Halaman Baru", source: String = "LOCAL") {
        val newTab = TabEntity(
            id = UUID.randomUUID().toString().take(8),
            url = url,
            title = title,
            source = source
        )
        viewModelScope.launch(Dispatchers.IO) {
            tabDao.insertTab(newTab)
        }
        _uiState.value = _uiState.value.copy(
            activeTabId = newTab.id,
            currentUrl = newTab.url,
            currentTitle = newTab.title,
            isTabGridOpen = false
        )
    }

    fun selectTab(tab: TabEntity) {
        _uiState.value = _uiState.value.copy(
            activeTabId = tab.id,
            currentUrl = tab.url,
            currentTitle = tab.title,
            isTabGridOpen = false
        )
    }

    fun closeTab(tab: TabEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            tabDao.deleteTab(tab)
        }
        val remaining = _uiState.value.tabs.filter { it.id != tab.id }
        if (remaining.isNotEmpty()) {
            val next = remaining.last()
            _uiState.value = _uiState.value.copy(
                activeTabId = next.id,
                currentUrl = next.url,
                currentTitle = next.title
            )
        }
    }

    // Real Two-Way Handoff Execution
    fun handoffToVps(cleanText: String, onResult: (String) -> Unit) {
        val currentUrl = _uiState.value.currentUrl
        val currentTitle = _uiState.value.currentTitle

        viewModelScope.launch {
            // Send WebSocket notification
            syncManager.sendHandoffWebSocket(currentUrl, currentTitle, cleanText, action = "handoff")

            // Send HTTP POST fallback
            val success = syncManager.sendHandoffHttp(currentUrl, currentTitle, "Tab dioper dari HP Pak Basuki")
            if (success) {
                onResult("✓ Sukses! Tab dan konteks halaman berhasil dioper ke antrean riset Mybeme di VPS.")
            } else {
                onResult("⚠️ Tailscale VPS sedang offline. Sesi telah disimpan di antrean lokal untuk dikirim ulang.")
            }
        }
    }

    // Real AI Summarize via VPS Gateway with Local Fallback
    fun summarizePage(cleanText: String, onResult: (String) -> Unit) {
        val currentUrl = _uiState.value.currentUrl
        val currentTitle = _uiState.value.currentTitle

        viewModelScope.launch {
            val vpsSummary = syncManager.fetchSummaryHttp(currentUrl, currentTitle, cleanText)
            if (!vpsSummary.isNullOrEmpty()) {
                onResult("⚡ Ringkasan Mybeme (via Gateway VPS):\n\n$vpsSummary")
            } else {
                // Local fallback extraction if VPS is temporarily unreachable
                val sentences = cleanText.split(Regex("[.!?]\\s+"))
                    .map { it.trim() }
                    .filter { it.length > 25 }
                    .take(4)

                val localSummary = if (sentences.isNotEmpty()) {
                    "📱 Ringkasan Cepat (Lokal HP):\n\n" + sentences.joinToString("\n") { "• $it." }
                } else {
                    "Halaman ini tidak memiliki cukup teks artikel untuk diringkas."
                }
                onResult(localSummary)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        syncManager.disconnect()
    }
}
