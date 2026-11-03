package com.vynora.youtube

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class YouTubeDataRepository(private val apiKey: String) {
    suspend fun searchVideos(query: String, preferRecent: Boolean = true): List<YouTubeItem> =
        search(query, "video", preferRecent)

    suspend fun searchPlaylists(query: String): List<YouTubeItem> =
        search(query, "playlist", preferRecent = false)

    private suspend fun search(query: String, type: String, preferRecent: Boolean): List<YouTubeItem> =
        withContext(Dispatchers.IO) {
            require(apiKey.isNotBlank()) {
                "YouTube API key is not configured. Add YOUTUBE_API_KEY to local.properties or your GitHub Actions secret."
            }
            val cleanedQuery = query.trim().ifBlank { "latest Hindi songs 2026 official music video" }
            val encodedQuery = URLEncoder.encode(cleanedQuery, Charsets.UTF_8.name())
            val sort = if (type == "video" && preferRecent) "&order=date" else "&order=relevance"
            val videoParams = if (type == "video") {
                "&videoCategoryId=10&videoEmbeddable=true&videoSyndicated=true&safeSearch=none"
            } else {
                ""
            }
            val url = URL(
                "https://www.googleapis.com/youtube/v3/search?part=snippet&type=$type&maxResults=30&regionCode=IN&relevanceLanguage=hi&q=$encodedQuery&key=$apiKey$sort$videoParams"
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
                val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (status !in 200..299) {
                    val message = runCatching { JSONObject(body).getJSONObject("error").getString("message") }
                        .getOrDefault("YouTube request failed (HTTP $status).")
                    throw IllegalStateException(message)
                }
                parseSearchResponse(body, type)
            } finally {
                connection.disconnect()
            }
        }

    internal fun parseSearchResponse(body: String, type: String): List<YouTubeItem> {
        val items = JSONObject(body).optJSONArray("items") ?: return emptyList()
        val parsed = buildList {
            for (index in 0 until items.length()) {
                val item = items.optJSONObject(index) ?: continue
                val idObject = item.optJSONObject("id") ?: continue
                val id = if (type == "playlist") idObject.optString("playlistId") else idObject.optString("videoId")
                if (id.isBlank()) continue
                val snippet = item.optJSONObject("snippet") ?: continue
                val title = snippet.optString("title", "Untitled")
                val description = snippet.optString("description", "")
                if (type == "video" && !YouTubeMusicFilter.isGenuineMusicResult(title, description)) continue
                val publishedAt = snippet.optString("publishedAt", "")
                val thumbnails = snippet.optJSONObject("thumbnails")
                val thumbnail = thumbnails?.optJSONObject("high")?.optString("url")
                    ?: thumbnails?.optJSONObject("medium")?.optString("url")
                    ?: thumbnails?.optJSONObject("default")?.optString("url")
                    ?: "https://i.ytimg.com/vi/$id/hqdefault.jpg"
                add(
                    YouTubeItem(
                        id = id,
                        title = title,
                        channelTitle = snippet.optString("channelTitle", "YouTube Music"),
                        thumbnailUrl = thumbnail,
                        description = description,
                        isPlaylist = type == "playlist",
                        publishedAt = publishedAt,
                        releaseYear = YouTubeMusicFilter.extractReleaseYear(title, description, publishedAt)
                    )
                )
            }
        }
        return parsed.sortedByDescending { it.publishedAt }
    }
}
