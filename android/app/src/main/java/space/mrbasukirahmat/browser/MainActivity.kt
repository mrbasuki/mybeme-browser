package space.mrbasukirahmat.browser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import space.mrbasukirahmat.browser.ui.BrowserScreen
import space.mrbasukirahmat.browser.ui.theme.MybemeBrowserTheme
import space.mrbasukirahmat.browser.ui.viewmodel.BrowserViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle intent if opened via deep link or shared URL
        intent?.dataString?.let { sharedUrl ->
            if (sharedUrl.startsWith("http://") || sharedUrl.startsWith("https://")) {
                viewModel.updateUrl(sharedUrl)
            }
        }

        setContent {
            MybemeBrowserTheme {
                BrowserScreen(viewModel = viewModel)
            }
        }
    }
}
