package com.vynora.youtube

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap

class YouTubeDataRepository(private val apiKey: String) {
    private val cache = ConcurrentHashMap<String, List<YouTubeItem>>()

    suspend fun searchVideos(query: String, recentWindowDays: Long = 90L): List<YouTubeItem> =
        search(query, "video", recentWindowDays)

    suspend fun searchPlaylists(query: String): List<YouTubeItem> =
        search(query, "playlist", recentWindowDays = 0L)

    private suspend fun search(query: String, type: String, recentWindowDays: Long): List<YouTubeItem> =
        withContext(Dispatchers.IO) {
            require(apiKey.isNotBlank()) {
                "YouTube API key is not configured. Add YOUTUBE_API_KEY to local.properties or your GitHub Actions secret."
            }

            val cleanedQuery = query.trim().ifBlank { "latest Hindi songs T-Series Sony Music Zee Music official video" }
            val cacheKey = "$type|$recentWindowDays|${cleanedQuery.lowercase()}"
            cache[cacheKey]?.takeIf { it.isNotEmpty() }?.let { return@withContext it }

            val encodedQuery = URLEncoder.encode(cleanedQuery, Charsets.UTF_8.name())
            val publishedAfterParam = if (type == "video" && recentWindowDays > 0L) {
                val cutoff = Instant.now().minus(recentWindowDays, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS).toString()
                "&publishedAfter=${URLEncoder.encode(cutoff, Charsets.UTF_8.name())}"
            } else {
                ""
            }

            val videoParams = if (type == "video") {
                "&order=relevance&videoCategoryId=10&videoEmbeddable=true&videoSyndicated=true&safeSearch=none$publishedAfterParam"
            } else {
                "&order=relevance"
            }

            val url = URL(
                "https://www.googleapis.com/youtube/v3/search?part=snippet&type=$type&maxResults=35&regionCode=IN&relevanceLanguage=hi&q=$encodedQuery&key=$apiKey$videoParams"
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
                val parsed = parseSearchResponse(body, type)
                if (parsed.isNotEmpty()) {
                    cache[cacheKey] = parsed
                }
                parsed
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
                val rawTitle = snippet.optString("title", "Untitled")
                val title = YouTubeMusicFilter.cleanHtmlEntities(rawTitle)
                val channelTitle = YouTubeMusicFilter.cleanHtmlEntities(snippet.optString("channelTitle", "YouTube Music"))
                val description = snippet.optString("description", "")

                if (type == "video" && !YouTubeMusicFilter.isGenuineMusicResult(title, description, channelTitle)) continue

                val publishedAt = snippet.optString("publishedAt", "")
                val thumbnails = snippet.optJSONObject("thumbnails")
                val thumbnail = thumbnails?.optJSONObject("high")?.optString("url")
                    ?: thumbnails?.optJSONObject("medium")?.optString("url")
                    ?: thumbnails?.optJSONObject("default")?.optString("url")
                    ?: "https://i.ytimg.com/vi/$id/hqdefault.jpg"

                val official = YouTubeMusicFilter.isOfficialMusicChannel(channelTitle, title)

                add(
                    YouTubeItem(
                        id = id,
                        title = title,
                        channelTitle = channelTitle,
                        thumbnailUrl = thumbnail,
                        description = description,
                        isPlaylist = type == "playlist",
                        publishedAt = publishedAt,
                        releaseYear = YouTubeMusicFilter.extractReleaseYear(title, description, publishedAt),
                        isOfficialChannel = official
                    )
                )
            }
        }

        return parsed.sortedWith(
            compareByDescending<YouTubeItem> { it.isOfficialChannel }
                .thenByDescending { it.publishedAt }
        )
    }
}
