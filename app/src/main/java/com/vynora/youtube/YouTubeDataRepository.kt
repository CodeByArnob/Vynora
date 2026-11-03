package com.vynora.youtube

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class YouTubeDataRepository(private val apiKey: String) {
    suspend fun searchVideos(query: String): List<YouTubeItem> = search(query, "video")
    suspend fun searchPlaylists(query: String): List<YouTubeItem> = search(query, "playlist")

    private suspend fun search(query: String, type: String): List<YouTubeItem> = withContext(Dispatchers.IO) {
        require(apiKey.isNotBlank()) { "YouTube API key is not configured. Add YOUTUBE_API_KEY to local.properties or your CI secret." }
        val encodedQuery = URLEncoder.encode(query.trim(), Charsets.UTF_8.name())
        val url = URL(
            "https://www.googleapis.com/youtube/v3/search?part=snippet&type=$type&maxResults=24&q=$encodedQuery&key=$apiKey" +
                if (type == "video") "&videoEmbeddable=true&videoSyndicated=true" else ""
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
        }
        try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream.bufferedReader().use { it.readText() }
            if (status !in 200..299) {
                val message = runCatching { JSONObject(body).getJSONObject("error").getString("message") }
                    .getOrDefault("YouTube request failed (HTTP $status).")
                throw IllegalStateException(message)
            }
            val items = JSONObject(body).optJSONArray("items") ?: return@withContext emptyList()
            buildList {
                for (index in 0 until items.length()) {
                    val item = items.optJSONObject(index) ?: continue
                    val idObject = item.optJSONObject("id") ?: continue
                    val id = if (type == "playlist") idObject.optString("playlistId") else idObject.optString("videoId")
                    if (id.isBlank()) continue
                    val snippet = item.optJSONObject("snippet") ?: continue
                    val thumbnails = snippet.optJSONObject("thumbnails")
                    val thumbnail = thumbnails?.optJSONObject("high")?.optString("url")
                        ?: thumbnails?.optJSONObject("medium")?.optString("url")
                        ?: thumbnails?.optJSONObject("default")?.optString("url")
                        ?: ""
                    add(
                        YouTubeItem(
                            id = id,
                            title = snippet.optString("title", "Untitled"),
                            channelTitle = snippet.optString("channelTitle", "YouTube"),
                            thumbnailUrl = thumbnail,
                            description = snippet.optString("description", ""),
                            isPlaylist = type == "playlist"
                        )
                    )
                }
            }
        } finally {
            connection.disconnect()
        }
    }
}
