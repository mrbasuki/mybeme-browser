package space.mrbasukirahmat.browser.util

import android.webkit.WebView

object ReadabilityExtractor {

    // JavaScript to extract readable text, stripping scripts, styles, and ads
    const val EXTRACTION_SCRIPT = """
        (function() {
            try {
                const clone = document.body.cloneNode(true);
                const scripts = clone.querySelectorAll('script, style, noscript, iframe, svg, header, footer, nav');
                scripts.forEach(s => s.remove());
                const text = clone.innerText || clone.textContent || '';
                return text.replace(/\s+/g, ' ').trim().substring(0, 8000);
            } catch(e) {
                return '';
            }
        })();
    """

    fun extractCleanText(webView: WebView, onResult: (String) -> Unit) {
        webView.evaluateJavascript(EXTRACTION_SCRIPT) { value ->
            val clean = value?.trim('"', '\'')
                ?.replace("\\n", "\n")
                ?.replace("\\\"", "\"") ?: ""
            onResult(clean)
        }
    }
}
