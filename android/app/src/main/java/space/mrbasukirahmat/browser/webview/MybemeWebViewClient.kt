package space.mrbasukirahmat.browser.webview

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import space.mrbasukirahmat.browser.shield.BraveShieldsInterceptor
import java.io.ByteArrayInputStream

class MybemeWebViewClient(
    private val shield: BraveShieldsInterceptor,
    private val onPageStartedCallback: (url: String) -> Unit,
    private val onPageFinishedCallback: (url: String) -> Unit,
    private val onTitleReceivedCallback: (title: String) -> Unit
) : WebViewClient() {

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url?.toString() ?: return null
        if (shield.shouldBlock(url)) {
            // Return empty response to drop the ad/tracker request
            return WebResourceResponse(
                "text/plain",
                "UTF-8",
                ByteArrayInputStream(ByteArray(0))
            )
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        url?.let { onPageStartedCallback(it) }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        url?.let { onPageFinishedCallback(it) }
        view?.title?.let { onTitleReceivedCallback(it) }
    }
}
