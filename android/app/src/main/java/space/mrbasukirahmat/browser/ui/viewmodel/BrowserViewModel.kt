package space.mrbasukirahmat.browser.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.mrbasukirahmat.browser.data.db.TabEntity
import space.mrbasukirahmat.browser.shield.BraveShieldsInterceptor
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
    val tabs: List<TabEntity> = listOf(
        TabEntity(
            id = "default-tab",
            url = "https://duckduckgo.com",
            title = "DuckDuckGo",
            source = "LOCAL"
        )
    ),
    val syncStatus: SyncStatus = SyncStatus.DISCONNECTED,
    val isCoPilotSheetOpen: Boolean = false,
    val isTabGridOpen: Boolean = false,
    val isShieldDialogOpen: Boolean = false,
    val isMenuSheetOpen: Boolean = false,
    val isDesktopMode: Boolean = false,
    val isShieldEnabled: Boolean = true
)

class BrowserViewModel(
    val shield: BraveShieldsInterceptor = BraveShieldsInterceptor()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    fun openShieldDialog(open: Boolean) {
        _uiState.value = _uiState.value.copy(isShieldDialogOpen = open)
    }

    fun openMenuSheet(open: Boolean) {
        _uiState.value = _uiState.value.copy(isMenuSheetOpen = open)
    }

    fun toggleShieldEnabled(enabled: Boolean) {
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

    fun setSyncStatus(status: SyncStatus) {
        _uiState.value = _uiState.value.copy(syncStatus = status)
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
        val updatedTabs = _uiState.value.tabs + newTab
        _uiState.value = _uiState.value.copy(
            tabs = updatedTabs,
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
        val currentTabs = _uiState.value.tabs
        if (currentTabs.size <= 1) {
            // Keep at least one tab
            return
        }
        val updatedTabs = currentTabs.filter { it.id != tab.id }
        val newActive = if (tab.id == _uiState.value.activeTabId) {
            updatedTabs.last()
        } else {
            currentTabs.first { it.id == _uiState.value.activeTabId }
        }
        _uiState.value = _uiState.value.copy(
            tabs = updatedTabs,
            activeTabId = newActive.id,
            currentUrl = newActive.url,
            currentTitle = newActive.title
        )
    }
}
