package space.mrbasukirahmat.browser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.mrbasukirahmat.browser.ui.theme.*

@Composable
fun Omnibox(
    currentUrl: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember(currentUrl) { mutableStateOf(currentUrl) }
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(ObsidianSurfaceElevated)
            .border(
                width = 1.dp,
                color = if (isFocused) MoltenOrange else ObsidianBorder,
                shape = RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (currentUrl.startsWith("https://")) Icons.Default.Lock else Icons.Default.Search,
                contentDescription = "Security Status",
                tint = if (currentUrl.startsWith("https://")) DesertGold else TextSecondary,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(MoltenOrange),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(
                    onGo = {
                        val trimmed = text.trim()
                        val destination = when {
                            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
                            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
                            else -> "https://duckduckgo.com/?q=${trimmed.replace(" ", "+")}"
                        }
                        onNavigate(destination)
                    }
                ),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            text = "Ketik URL atau cari...",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            )

            if (text.isNotEmpty() && text != currentUrl) {
                IconButton(
                    onClick = { text = "" },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
