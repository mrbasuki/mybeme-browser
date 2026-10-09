package space.mrbasukirahmat.browser.shield

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI

class BraveShieldsInterceptor(
    context: Context? = null,
    customBlocklist: Set<String>? = null
) {
    constructor(customBlocklist: Set<String>) : this(null, customBlocklist)
    private val blocklist = HashSet<String>()
    var isEnabled: Boolean = true

    init {
        // Fallback core rules
        val defaultRules = setOf(
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
            "facebook.net",
            "appsflyer.com",
            "adjust.com",
            "branch.io",
            "amplitude.com",
            "clarity.ms"
        )
        blocklist.addAll(defaultRules)

        if (customBlocklist != null) {
            blocklist.addAll(customBlocklist)
        }

        // Load asset blocklist if context provided
        context?.let { ctx ->
            try {
                ctx.assets.open("adblock_hosts.txt").use { input ->
                    BufferedReader(InputStreamReader(input)).useLines { lines ->
                        lines.forEach { line ->
                            val trimmed = line.trim()
                            if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                                blocklist.add(trimmed.lowercase())
                            }
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    private var _blockedCount: Int = 0
    val blockedCount: Int get() = _blockedCount

    fun shouldBlock(url: String): Boolean {
        if (!isEnabled) return false
        try {
            val uri = URI(url)
            val host = uri.host?.lowercase() ?: return false

            for (pattern in blocklist) {
                if (pattern.contains("/")) {
                    if (url.lowercase().contains(pattern)) {
                        _blockedCount++
                        return true
                    }
                } else {
                    if (host == pattern || host.endsWith(".$pattern")) {
                        _blockedCount++
                        return true
                    }
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
