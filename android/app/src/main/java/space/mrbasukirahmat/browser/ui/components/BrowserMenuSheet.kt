package space.mrbasukirahmat.browser.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.mrbasukirahmat.browser.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserMenuSheet(
    isDesktopMode: Boolean,
    onNewTab: () -> Unit,
    onReload: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    onShare: () -> Unit,
    onBookmark: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        scrimColor = GlassScrim,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Menu Browser",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            MenuItemRow(
                icon = Icons.Default.Add,
                label = "Tab Baru",
                onClick = {
                    onNewTab()
                    onDismiss()
                }
            )

            MenuItemRow(
                icon = Icons.Default.Refresh,
                label = "Muat Ulang Halaman",
                onClick = {
                    onReload()
                    onDismiss()
                }
            )

            MenuItemRow(
                icon = Icons.Default.Computer,
                label = if (isDesktopMode) "Tampilan Mobile" else "Minta Situs Desktop",
                badge = if (isDesktopMode) "AKTIF" else null,
                onClick = {
                    onToggleDesktopMode()
                    onDismiss()
                }
            )

            MenuItemRow(
                icon = Icons.Default.BookmarkBorder,
                label = "Simpan ke Bookmark",
                onClick = {
                    onBookmark()
                    onDismiss()
                }
            )

            MenuItemRow(
                icon = Icons.Default.Share,
                label = "Bagikan Tautan",
                onClick = {
                    onShare()
                    onDismiss()
                }
            )

            MenuItemRow(
                icon = Icons.Default.Hub,
                label = "Jalur Tailscale Mybeme (100.80.80.80)",
                badge = "TERHUBUNG",
                onClick = {
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MenuItemRow(
    icon: ImageVector,
    label: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Surface(
        color = ObsidianSurface,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MoltenOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = label,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            badge?.let {
                Surface(
                    color = ObsidianSurfaceElevated,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Text(
                        text = it,
                        color = DesertGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
