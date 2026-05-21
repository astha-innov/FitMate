package com.fitmate.ai

import com.fitmate.domain.model.AiConfig
import com.fitmate.domain.model.AiProviderMode
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException

class AiClient {
    suspend fun requestStructuredJson(
        config: AiConfig,
        systemPrompt: String,
        userPrompt: String,
    ): JSONObject {
        return when (config.providerMode) {
            AiProviderMode.REMOTE_API -> requestRemote(config, systemPrompt, userPrompt)
            AiProviderMode.LOCAL_LLM -> requestLocal(config, systemPrompt, userPrompt)
        }
    }

    private fun requestRemote(config: AiConfig, systemPrompt: String, userPrompt: String): JSONObject {
        val url = resolveRemoteUrl(config.baseUrl)
        val body = JSONObject()
            .put("model", config.modelName)
            .put("response_format", JSONObject().put("type", "json_object"))
            .put(
                "messages",
                JSONArray()
                    .put(JSONObject().put("role", "system").put("content", systemPrompt))
                    .put(JSONObject().put("role", "user").put("content", userPrompt)),
            )
        val response = postJson(url, body, config.apiKey.takeIf { it.isNotBlank() })
        val content = response
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
        return JSONObject(content)
    }

    private fun requestLocal(config: AiConfig, systemPrompt: String, userPrompt: String): JSONObject {
        val url = resolveLocalUrl(config.localEndpoint)
        val body = JSONObject()
            .put("model", config.localModelName)
            .put("stream", false)
            .put("format", "json")
            .put(
                "messages",
                JSONArray()
                    .put(JSONObject().put("role", "system").put("content", systemPrompt))
                    .put(JSONObject().put("role", "user").put("content", userPrompt)),
            )
        val response = postJson(url, body, null)
        val content = response.getJSONObject("message").getString("content")
        return JSONObject(content)
    }

    private fun postJson(url: String, body: JSONObject, apiKey: String?): JSONObject {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 30_000
            connection.readTimeout = 90_000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            if (!apiKey.isNullOrBlank()) {
                connection.setRequestProperty("Authorization", "Bearer $apiKey")
            }
            connection.outputStream.use { it.write(body.toString().toByteArray()) }
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val text = stream.bufferedReader().use { it.readText() }
            if (connection.responseCode !in 200..299) error(text.ifBlank { "HTTP ${connection.responseCode}" })
            return JSONObject(text)
        } catch (error: SocketTimeoutException) {
            error("Request timed out while calling $url")
        } catch (error: UnknownHostException) {
            error("Could not reach host for $url")
        }
    }

    private fun resolveRemoteUrl(baseUrl: String): String {
        val trimmed = baseUrl.trim().trimEnd('/')
        return when {
            trimmed.endsWith("/chat/completions") -> trimmed
            trimmed.endsWith("/v1") -> "$trimmed/chat/completions"
            else -> "$trimmed/v1/chat/completions"
        }
    }

    private fun resolveLocalUrl(baseUrl: String): String {
        val trimmed = baseUrl.trim().trimEnd('/')
        return if (trimmed.endsWith("/api/chat")) trimmed else "$trimmed/api/chat"
    }
}
