package com.vynora.audius

import com.vynora.BuildConfig
import com.vynora.catalog.AlbumModel
import com.vynora.catalog.AudioSourceProvider
import com.vynora.catalog.UnifiedTrack
import com.vynora.library.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Official Audius V1 API client for online audio discovery and Media3 streaming.
 * Uses only the public client-safe AUDIUS_API_KEY (optional; keyless read-only mode supported).
 * Never requires Google Cloud or YouTube credentials.
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
    ).apply {
        if (!artworkUrl.isNullOrBlank()) {
            customArtworkUri = artworkUrl
        }
    }

    fun toUnifiedTrack(streamUrl: String): UnifiedTrack = UnifiedTrack(
        id = "audius_$id",
        title = title,
        artist = artist,
        album = album,
        durationMs = durationSeconds.coerceAtLeast(0) * 1_000L,
        streamUrl = streamUrl,
        artworkUrl = artworkUrl,
        provider = AudioSourceProvider.AUDIUS,
        genre = genre,
        releaseYear = releaseDate?.take(4),
        license = "Audius Free Streamable"
    )
}

class AudiusRepository(
    private val apiKey: String = BuildConfig.AUDIUS_API_KEY
) {
    suspend fun searchTracks(query: String, limit: Int = 24): List<AudiusTrack> {
        val cleaned = query.trim().ifBlank { "lofi chill" }
        return requestTracks("/tracks/search?query=${encode(cleaned)}&limit=${limit.coerceIn(1, 50)}")
    }

    suspend fun getTrendingTracks(time: String = "week", limit: Int = 24): List<AudiusTrack> =
        requestTracks("/tracks/trending?time=${encode(time)}&limit=${limit.coerceIn(1, 50)}")

    fun streamUrl(trackId: String): String {
        require(trackId.isNotBlank()) { "Track ID cannot be blank" }
        val keyParam = apiKey.takeIf { it.isNotBlank() }?.let { "&api_key=${encode(it)}" }.orEmpty()
        return "https://api.audius.co/v1/tracks/${encode(trackId)}/stream?app_name=Vynora$keyParam"
    }

    /**
     * Curated, verified Audius albums with real streamable tracks.
     */
    suspend fun getCuratedAlbums(): List<AlbumModel> = withContext(Dispatchers.IO) {
        val trending = getTrendingTracks("month", 24)
        if (trending.isEmpty()) return@withContext emptyList()

        val electronic = trending.filter { it.genre.contains("electronic", true) || it.genre.contains("edm", true) || it.genre.contains("dance", true) }.take(6)
        val lofiAndChill = trending.filter { it.genre.contains("lo-fi", true) || it.genre.contains("chill", true) || it.genre.contains("ambient", true) || it.genre.contains("pop", true) }.take(6)
        val hiphop = trending.filter { it.genre.contains("hip-hop", true) || it.genre.contains("rap", true) || it.genre.contains("trap", true) }.take(6)

        buildList {
            if (electronic.isNotEmpty()) {
                add(
                    AlbumModel(
                        id = "audius_album_edm",
                        title = "Audius Bass & Electronic Anthems",
                        artist = "Audius Community Producers",
                        coverArtUri = electronic.firstOrNull()?.artworkUrl ?: "file:///android_asset/album_punjabi_beats_1793701777979.jpg",
                        releaseYear = "2026",
                        genre = "Electronic / Bass",
                        provider = AudioSourceProvider.AUDIUS,
                        description = "High energy dubstep, future bass, and dance anthems directly streamed from independent creators.",
                        curator = "Audius Trending Charts",
                        tracks = electronic.map { it.toUnifiedTrack(streamUrl(it.id)) }
                    )
                )
            }
            if (lofiAndChill.isNotEmpty()) {
                add(
                    AlbumModel(
                        id = "audius_album_lofi",
                        title = "Audius Chillwave & Midnight Lo-Fi",
                        artist = "Global Indie Creators",
                        coverArtUri = lofiAndChill.firstOrNull()?.artworkUrl ?: "file:///android_asset/album_bollywood_lofi_1793701777433.jpg",
                        releaseYear = "2026",
                        genre = "Lo-Fi / Chillhop",
                        provider = AudioSourceProvider.AUDIUS,
                        description = "Cozy beats, mellow guitars, and late night coffeehouse chill sessions.",
                        curator = "Audius Chill Curators",
                        tracks = lofiAndChill.map { it.toUnifiedTrack(streamUrl(it.id)) }
                    )
                )
            }
            if (hiphop.isNotEmpty()) {
                add(
                    AlbumModel(
                        id = "audius_album_hiphop",
                        title = "Audius Underground Hip-Hop",
                        artist = "Independent Beatmakers",
                        coverArtUri = hiphop.firstOrNull()?.artworkUrl ?: "file:///android_asset/card_my_mix_01.jpg",
                        releaseYear = "2026",
                        genre = "Hip-Hop / Rap",
                        provider = AudioSourceProvider.AUDIUS,
                        description = "Boom bap drums, heavy 808s, and raw lyrical flows.",
                        curator = "Audius Underground",
                        tracks = hiphop.map { it.toUnifiedTrack(streamUrl(it.id)) }
                    )
                )
            }
        }
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
            if (status !in 200..299) return@withContext emptyList()
            parseTracksJson(body)
        } catch (_: Exception) {
            emptyList()
        } finally {
            connection.disconnect()
        }
    }

    internal fun parseTracksJson(body: String): List<AudiusTrack> {
        return runCatching {
            val root = JSONObject(body)
            val data = root.optJSONArray("data") ?: return emptyList()
            buildList {
                for (index in 0 until data.length()) {
                    val item = data.optJSONObject(index) ?: continue
                    if (item.optBoolean("is_delete", false)) continue
                    val id = item.optString("id").takeIf(String::isNotBlank) ?: continue
                    val user = item.optJSONObject("user")
                    val artwork = item.optJSONObject("artwork")
                    val artUrl = artwork?.optString("480x480")?.takeIf(String::isNotBlank)
                        ?: artwork?.optString("150x150")?.takeIf(String::isNotBlank)
                        ?: artwork?.optString("1000x1000")?.takeIf(String::isNotBlank)
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
        }.getOrDefault(emptyList())
    }

    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())
}
