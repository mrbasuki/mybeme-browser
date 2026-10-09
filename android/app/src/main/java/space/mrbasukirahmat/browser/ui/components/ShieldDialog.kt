package space.mrbasukirahmat.browser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import space.mrbasukirahmat.browser.ui.theme.*

@Composable
fun ShieldDialog(
    url: String,
    blockedCount: Int,
    isShieldEnabled: Boolean,
    onToggleShield: (Boolean) -> Unit,
    onClearSiteData: () -> Unit,
    onDismiss: () -> Unit
) {
    val host = remember(url) {
        try {
            java.net.URI(url).host ?: url
        } catch (_: Exception) {
            url
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ObsidianSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
            modifier = Modifier.fillMaxWidth()
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
                                .background(if (isShieldEnabled) ShieldGreen.copy(alpha = 0.15f) else ObsidianBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Shields",
                                tint = if (isShieldEnabled) ShieldGreen else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Brave Shields",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = host,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Stats Box
                Surface(
                    color = ObsidianSurfaceElevated,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
                    modifier = Modifier.fillMaxWidth()
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
                                text = "$blockedCount",
                                color = MoltenOrange,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Pelacak & Iklan Dicegat",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Switch(
                            checked = isShieldEnabled,
                            onCheckedChange = onToggleShield,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianBlack,
                                checkedTrackColor = ShieldGreen,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = ObsidianBorder
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Feature details
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "HTTPS",
                        tint = DesertGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (url.startsWith("https://")) "Koneksi Aman (HTTPS)" else "Koneksi Tidak Aman (HTTP)",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Clear Site Data Button
                Button(
                    onClick = {
                        onClearSiteData()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear",
                        tint = ErrorRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bersihkan Data & Cache Situs Ini",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
