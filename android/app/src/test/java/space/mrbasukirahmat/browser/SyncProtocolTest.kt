package space.mrbasukirahmat.browser

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncProtocolTest {

    @Test
    fun testSerializationHandoff() {
        val payload = JSONObject().apply {
            put("url", "https://mrbasukirahmat.space")
            put("title", "Portfolio")
            put("action_requested", "summarize")
        }
        val message = JSONObject().apply {
            put("event", "HANDOFF_TO_AGENT")
            put("client_id", "android_mybeme")
            put("payload", payload)
        }

        val jsonStr = message.toString()
        assertTrue(jsonStr.contains("\"event\":\"HANDOFF_TO_AGENT\""))
        assertTrue(jsonStr.contains("\"url\":\"https://mrbasukirahmat.space\""))
    }

    @Test
    fun testParseInboundPushTab() {
        val rawJson = """
            {
                "event": "OPEN_TAB",
                "source": "mybeme_agent",
                "payload": {
                    "url": "https://target.audit.example.com",
                    "title": "Hasil Audit",
                    "note": "Cek bagian API endpoint",
                    "timestamp": 1728456010
                }
            }
        """.trimIndent()

        val json = JSONObject(rawJson)
        assertEquals("OPEN_TAB", json.getString("event"))
        val payload = json.getJSONObject("payload")
        assertEquals("https://target.audit.example.com", payload.getString("url"))
        assertEquals("Hasil Audit", payload.getString("title"))
    }
}
