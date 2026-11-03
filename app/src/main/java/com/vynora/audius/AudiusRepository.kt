package com.vynora.audius

import com.vynora.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

/**
 * Read-only Audius catalog client. The API key is intended for client-side use;
 * the separate Audius Bearer Token must never be shipped in an Android app.
 */
data class AudiusTrack(
    val id: String,
    val title: String,
    val artist: String,
    val artworkUrl: String?,
    val durationSeconds: Int
)

class AudiusRepository(
    private val apiKey: String = BuildConfig.AUDIUS_API_KEY
) {
    suspend fun searchTracks(query: String, limit: Int = 20): List<AudiusTrack> =
        requestTracks("/tracks/search?query=${encode(query)}&limit=${limit.coerceIn(1, 50)}")

    suspend fun getTrendingTracks(limit: Int = 20): List<AudiusTrack> =
        requestTracks("/tracks/trending?limit=${limit.coerceIn(1, 50)}")

    fun streamUrl(trackId: String): String {
        require(trackId.isNotBlank()) { "Track ID cannot be blank" }
        val key = apiKey.takeIf { it.isNotBlank() }?.let { "&api_key=${encode(it)}" }.orEmpty()
        return "https://api.audius.co/v1/tracks/${encode(trackId)}/stream?app_name=Vynora$key"
    }

    private suspend fun requestTracks(path: String): List<AudiusTrack> = withContext(Dispatchers.IO) {
        val key = apiKey.takeIf { it.isNotBlank() }?.let { "&api_key=${encode(it)}" }.orEmpty()
        val connection = (URL("https://api.audius.co/v1$path&app_name=Vynora$key").openConnection() as HttpURLConnection)
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15_000
            connection.readTimeout = 20_000
            connection.setRequestProperty("Accept", "application/json")
            val status = connection.responseCode
            val body = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            check(status in 200..299) { "Audius request failed (HTTP $status)" }
            val root = JSONObject(body)
            val data = root.optJSONArray("data") ?: return@withContext emptyList()
            buildList {
                for (index in 0 until data.length()) {
                    val item = data.optJSONObject(index) ?: continue
                    val id = item.optString("id").takeIf(String::isNotBlank) ?: continue
                    val user = item.optJSONObject("user")
                    val artwork = item.optJSONObject("artwork")
                    add(
                        AudiusTrack(
                            id = id,
                            title = item.optString("title", "Untitled"),
                            artist = user?.optString("name", "Unknown artist") ?: "Unknown artist",
                            artworkUrl = artwork?.optString("150x150")?.takeIf(String::isNotBlank),
                            durationSeconds = item.optInt("duration", 0)
                        )
                    )
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())
}
