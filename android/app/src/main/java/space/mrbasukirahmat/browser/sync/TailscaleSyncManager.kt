package space.mrbasukirahmat.browser.sync

import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class TailscaleSyncManager(
    private val host: String = "100.80.80.80",
    private val port: Int = 8765,
    private val token: String = "mybeme-browser-key-991823",
    private val onTabReceived: (url: String, title: String, note: String?) -> Unit,
    private val onSummaryReceived: (url: String, summary: String) -> Unit,
    private val onStatusChanged: (Boolean) -> Unit
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    var isConnected = false
        private set

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun connect() {
        val request = Request.Builder()
            .url("ws://$host:$port/ws/sync?token=$token")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                isConnected = true
                onStatusChanged(true)
            }

            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val event = json.optString("event")
                    val payload = json.optJSONObject("payload") ?: JSONObject()

                    when (event) {
                        "OPEN_TAB" -> {
                            val url = payload.optString("url")
                            val title = payload.optString("title", url)
                            val note = payload.optString("note", "")
                            if (url.isNotEmpty()) {
                                onTabReceived(url, title, note)
                            }
                        }
                        "SUMMARY_RESULT" -> {
                            val url = payload.optString("url")
                            val summary = payload.optString("summary")
                            onSummaryReceived(url, summary)
                        }
                    }
                } catch (_: Exception) {
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                onStatusChanged(false)
                scheduleReconnect()
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                isConnected = false
                onStatusChanged(false)
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        coroutineScope.launch {
            delay(5000)
            connect()
        }
    }

    fun sendHandoffWebSocket(url: String, title: String, cleanText: String, action: String = "open") {
        if (!isConnected || webSocket == null) return
        val payload = JSONObject().apply {
            put("url", url)
            put("title", title)
            put("cleaned_text", cleanText)
            put("action_requested", action)
            put("timestamp", System.currentTimeMillis() / 1000)
        }
        val message = JSONObject().apply {
            put("event", "HANDOFF_TO_AGENT")
            put("client_id", "android_mybeme")
            put("payload", payload)
        }
        webSocket?.send(message.toString())
    }

    suspend fun sendHandoffHttp(url: String, title: String, note: String): Boolean = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("url", url)
            put("title", title)
            put("note", note)
            put("action_requested", "open")
        }
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("http://$host:$port/api/v1/handoff")
            .header("Authorization", "Bearer $token")
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun fetchSummaryHttp(url: String, title: String, cleanText: String): String? = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("url", url)
            put("title", title)
            put("cleaned_text", cleanText)
        }
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("http://$host:$port/api/v1/summarize")
            .header("Authorization", "Bearer $token")
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val respBody = response.body?.string() ?: return@withContext null
                    val respJson = JSONObject(respBody)
                    respJson.optString("summary")
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "App closed")
        coroutineScope.cancel()
    }
}
