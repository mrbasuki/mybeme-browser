package space.mrbasukirahmat.browser

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import space.mrbasukirahmat.browser.shield.BraveShieldsInterceptor

class ShieldTest {

    @Test
    fun testBlockTrackerDomain() {
        val rules = setOf(
            "doubleclick.net",
            "google-analytics.com",
            "adservice.google.com",
            "facebook.net/tr"
        )
        val shield = BraveShieldsInterceptor(rules)

        // Ad / Tracker subdomains
        assertTrue(shield.shouldBlock("https://ad.doubleclick.net/ad/123"))
        assertTrue(shield.shouldBlock("https://www.google-analytics.com/analytics.js"))
        assertTrue(shield.shouldBlock("https://connect.facebook.net/tr?id=123"))

        // Legitimate sites
        assertFalse(shield.shouldBlock("https://mrbasukirahmat.space/dashboard"))
        assertFalse(shield.shouldBlock("https://github.com/mrbasuki"))
        assertFalse(shield.shouldBlock("https://en.wikipedia.org/wiki/Brave_(web_browser)"))
    }

    @Test
    fun testHttpsUpgrade() {
        val shield = BraveShieldsInterceptor()
        val upgraded = shield.upgradeUrlToHttps("http://example.com/login")
        assertTrue(upgraded.startsWith("https://"))
    }
}
