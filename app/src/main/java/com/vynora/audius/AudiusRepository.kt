package com.vynora.audius

import com.vynora.BuildConfig
import com.vynora.library.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Official Audius V1 API client for online audio discovery and Media3 streaming.
 * Uses only the public client-safe AUDIUS_API_KEY; never uses or exposes the Audius Bearer Token.
 */
data class AudiusTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "Audius Stream",
    val artworkUrl: String?,
    val durationSeconds: Int,
    val genre: String = "Electronic / Indie",
    val releaseDate: String? = null
) {
    fun toAudioTrack(streamUrl: String): AudioTrack = AudioTrack(
        id = id.hashCode().toLong(),
        title = title,
        artist = artist,
        album = album,
        durationMs = durationSeconds.coerceAtLeast(0) * 1_000L,
        uri = streamUrl,
        albumId = 0L,
        artistId = 0L,
        dateAddedSeconds = 0L,
        relativePath = "Audius",
        isFavorite = false
    )
}

class AudiusRepository(
    private val apiKey: String = BuildConfig.AUDIUS_API_KEY
) {
    suspend fun searchTracks(query: String, limit: Int = 24): List<AudiusTrack> {
        val cleaned = query.trim().ifBlank { "Hindi indie lofi" }
        return requestTracks("/tracks/search?query=${encode(cleaned)}&limit=${limit.coerceIn(1, 50)}")
    }

    suspend fun getTrendingTracks(time: String = "week", limit: Int = 24): List<AudiusTrack> =
        requestTracks("/tracks/trending?time=${encode(time)}&limit=${limit.coerceIn(1, 50)}")

    fun streamUrl(trackId: String): String {
        require(trackId.isNotBlank()) { "Track ID cannot be blank" }
        val keyParam = apiKey.takeIf { it.isNotBlank() }?.let { "&api_key=${encode(it)}" }.orEmpty()
        return "https://api.audius.co/v1/tracks/${encode(trackId)}/stream?app_name=Vynora$keyParam"
    }

    private suspend fun requestTracks(path: String): List<AudiusTrack> = withContext(Dispatchers.IO) {
        val keyParam = apiKey.takeIf { it.isNotBlank() }?.let { "&api_key=${encode(it)}" }.orEmpty()
        val separator = if (path.contains("?")) "&" else "?"
        val connection = (URL("https://api.audius.co/v1$path${separator}app_name=Vynora$keyParam").openConnection() as HttpURLConnection)
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 12_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("Accept", "application/json")
            val status = connection.responseCode
            val body = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            check(status in 200..299) { "Audius request failed (HTTP $status)" }
            parseTracksJson(body)
        } finally {
            connection.disconnect()
        }
    }

    internal fun parseTracksJson(body: String): List<AudiusTrack> {
        val root = JSONObject(body)
        val data = root.optJSONArray("data") ?: return emptyList()
        return buildList {
            for (index in 0 until data.length()) {
                val item = data.optJSONObject(index) ?: continue
                if (item.optBoolean("is_delete", false)) continue
                val id = item.optString("id").takeIf(String::isNotBlank) ?: continue
                val user = item.optJSONObject("user")
                val artwork = item.optJSONObject("artwork")
                val artUrl = artwork?.optString("480x480")?.takeIf(String::isNotBlank)
                    ?: artwork?.optString("150x150")?.takeIf(String::isNotBlank)
                add(
                    AudiusTrack(
                        id = id,
                        title = item.optString("title", "Untitled"),
                        artist = user?.optString("name", "Unknown artist") ?: "Unknown artist",
                        artworkUrl = artUrl,
                        durationSeconds = item.optInt("duration", 0),
                        genre = item.optString("genre", "Music"),
                        releaseDate = item.optString("release_date").takeIf(String::isNotBlank)
                    )
                )
            }
        }
    }

    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())
}
