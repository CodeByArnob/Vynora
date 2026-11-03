package com.vynora.metadata

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class MusicBrainzRelease(
    val mbid: String,
    val title: String,
    val artistName: String,
    val releaseDate: String?,
    val primaryType: String,
    val coverArtFrontUrl: String?
)

/**
 * Metadata provider using official MusicBrainz and Cover Art Archive APIs.
 * Strictly enforces MusicBrainz non-commercial terms, User-Agent identification,
 * and a 1 request/second rate-limit throttle.
 */
class MusicBrainzRepository {

    private val rateLimitMutex = Mutex()
    private var lastRequestTimeMs = 0L

    companion object {
        private const val USER_AGENT = "Vynora/0.7.0 ( https://github.com/CodeByArnob/Vynora )"
        private const val RATE_LIMIT_INTERVAL_MS = 1_050L
    }

    suspend fun searchReleaseGroups(query: String, limit: Int = 10): List<MusicBrainzRelease> = withContext(Dispatchers.IO) {
        val cleaned = query.trim()
        if (cleaned.isBlank()) return@withContext emptyList()

        throttle()

        val encoded = URLEncoder.encode(cleaned, Charsets.UTF_8.name())
        val urlString = "https://musicbrainz.org/ws/2/release-group?query=$encoded&fmt=json&limit=${limit.coerceIn(1, 20)}"

        val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 12_000
            setRequestProperty("User-Agent", USER_AGENT)
            setRequestProperty("Accept", "application/json")
        }

        try {
            val status = connection.responseCode
            if (status !in 200..299) return@withContext emptyList()
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            parseReleaseGroups(body)
        } catch (_: Exception) {
            emptyList()
        } finally {
            connection.disconnect()
        }
    }

    suspend fun getCoverArtFrontUrl(mbid: String): String? = withContext(Dispatchers.IO) {
        if (mbid.isBlank()) return@withContext null
        throttle()

        val urlString = "https://coverartarchive.org/release-group/$mbid/front-500"
        val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "HEAD"
            instanceFollowRedirects = false
            connectTimeout = 8_000
            readTimeout = 8_000
            setRequestProperty("User-Agent", USER_AGENT)
        }

        try {
            val status = connection.responseCode
            if (status in 200..399) {
                // If 307 redirect or 200 OK, cover art exists!
                urlString
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun throttle() {
        rateLimitMutex.withLock {
            val now = System.currentTimeMillis()
            val elapsed = now - lastRequestTimeMs
            if (elapsed < RATE_LIMIT_INTERVAL_MS) {
                kotlinx.coroutines.delay(RATE_LIMIT_INTERVAL_MS - elapsed)
            }
            lastRequestTimeMs = System.currentTimeMillis()
        }
    }

    private fun parseReleaseGroups(json: String): List<MusicBrainzRelease> {
        return runCatching {
            val root = JSONObject(json)
            val groups = root.optJSONArray("release-groups") ?: return emptyList()
            buildList {
                for (i in 0 until groups.length()) {
                    val group = groups.optJSONObject(i) ?: continue
                    val id = group.optString("id").takeIf(String::isNotBlank) ?: continue
                    val title = group.optString("title", "Untitled")
                    val primaryType = group.optString("primary-type", "Album")
                    val releaseDate = group.optString("first-release-date").takeIf(String::isNotBlank)

                    var artistName = "Various Artists"
                    val artistCredit = group.optJSONArray("artist-credit")
                    if (artistCredit != null && artistCredit.length() > 0) {
                        val firstCredit = artistCredit.optJSONObject(0)
                        artistName = firstCredit?.optString("name") ?: artistName
                    }

                    val coverArtUrl = "https://coverartarchive.org/release-group/$id/front-500"

                    add(
                        MusicBrainzRelease(
                            mbid = id,
                            title = title,
                            artistName = artistName,
                            releaseDate = releaseDate,
                            primaryType = primaryType,
                            coverArtFrontUrl = coverArtUrl
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }
}
