package space.mrbasukirahmat.browser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.mrbasukirahmat.browser.ui.theme.*

@Composable
fun BottomNavigationBar(
    currentUrl: String,
    blockedCount: Int,
    tabCount: Int,
    onNavigate: (String) -> Unit,
    onShieldClick: () -> Unit,
    onMybemeClick: () -> Unit,
    onTabClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianSurface,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = ObsidianBorder, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brave Shield Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onShieldClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Brave Shields",
                    tint = if (blockedCount > 0) ShieldGreen else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
                if (blockedCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(MoltenOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (blockedCount > 99) "99+" else "$blockedCount",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ObsidianBlack
                        )
                    }
                }
            }

            // Omnibox
            Omnibox(
                currentUrl = currentUrl,
                onNavigate = onNavigate,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp)
            )

            // Mybeme Co-Pilot Button (Flame Accent)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ObsidianSurfaceElevated)
                    .border(1.dp, MoltenOrange, CircleShape)
                    .clickable { onMybemeClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Mybeme Co-Pilot",
                    tint = DesertGold,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Tab Switcher Box
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.5.dp, TextSecondary, RoundedCornerShape(6.dp))
                    .clickable { onTabClick() }
            ) {
                Text(
                    text = "$tabCount",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Menu Overflow
            IconButton(
                onClick = { onMenuClick() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
