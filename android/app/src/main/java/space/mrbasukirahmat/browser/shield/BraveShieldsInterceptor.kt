package space.mrbasukirahmat.browser.shield

import java.net.URI

class BraveShieldsInterceptor(
    customBlocklist: Set<String>? = null
) {
    // Default high-impact tracker and ad patterns
    private val blocklist: Set<String> = customBlocklist ?: setOf(
        "doubleclick.net",
        "google-analytics.com",
        "googletagmanager.com",
        "adservice.google.com",
        "pagead2.googlesyndication.com",
        "adroll.com",
        "adnxs.com",
        "criteo.com",
        "outbrain.com",
        "taboola.com",
        "hotjar.com",
        "segment.io",
        "mixpanel.com",
        "facebook.net/tr"
    )

    private var _blockedCount: Int = 0
    val blockedCount: Int get() = _blockedCount

    fun shouldBlock(url: String): Boolean {
        try {
            val lowerUrl = url.lowercase()
            for (pattern in blocklist) {
                if (lowerUrl.contains(pattern)) {
                    _blockedCount++
                    return true
                }
            }
        } catch (_: Exception) {
            return false
        }
        return false
    }

    fun upgradeUrlToHttps(url: String): String {
        if (url.startsWith("http://") && !url.contains("127.0.0.1") && !url.contains("localhost") && !url.contains("100.80.80.80")) {
            return url.replaceFirst("http://", "https://")
        }
        return url
    }

    fun resetStats() {
        _blockedCount = 0
    }
}
