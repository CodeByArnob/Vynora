package com.vynora.youtube

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vynora.BuildConfig
import com.vynora.audius.AudiusRepository
import com.vynora.audius.AudiusTrack
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class YouTubeHomeState(
    val videos: List<YouTubeItem> = emptyList(),
    val allCachedVideos: List<YouTubeItem> = emptyList(),
    val playlists: List<YouTubeItem> = emptyList(),
    val audiusTracks: List<AudiusTrack> = emptyList(),
    val recentlyPlayed: List<YouTubeItem> = emptyList(),
    val favoriteVideoIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isAudiusLoading: Boolean = false,
    val isCatalogSynced: Boolean = false,
    val correctedQuerySuggestion: String? = null,
    val error: String? = null,
    val audiusError: String? = null,
    val apiKeyConfigured: Boolean = BuildConfig.YOUTUBE_API_KEY.isNotBlank(),
    val activeQuery: String = "Latest Hindi"
)

class YouTubeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = YouTubeDataRepository(BuildConfig.YOUTUBE_API_KEY)
    private val audiusRepository = AudiusRepository(BuildConfig.AUDIUS_API_KEY)
    private val catalogCache = YouTubeCatalogCache(application.applicationContext)

    private val _state = MutableStateFlow(YouTubeHomeState())
    val state: StateFlow<YouTubeHomeState> = _state.asStateFlow()

    private var hasSyncedStartupCatalog = false

    init {
        // Immediately load saved catalog from phone database (0ms startup, no waiting!)
        val cached = catalogCache.loadAllTracks()
        _state.value = _state.value.copy(
            videos = cached,
            allCachedVideos = cached,
            isLoading = false
        )
    }

    fun loadHome() {
        if (hasSyncedStartupCatalog) return
        hasSyncedStartupCatalog = true

        // Ensure local catalog is displayed immediately
        val localTracks = catalogCache.loadAllTracks()
        if (_state.value.videos.isEmpty()) {
            _state.value = _state.value.copy(
                videos = localTracks,
                allCachedVideos = localTracks,
                isLoading = false
            )
        }

        // Single startup sync: fetch latest collection in background and save to phone DB
        if (BuildConfig.YOUTUBE_API_KEY.isNotBlank()) {
            viewModelScope.launch {
                try {
                    val queries = listOf(
                        "latest Hindi songs T-Series Sony Music Zee Music official video",
                        "Arijit Singh latest Hindi songs official video",
                        "latest Punjabi songs Speed Records Karan Aujla Diljit official",
                        "latest Bangla songs Coke Studio Bangla SVF Music official"
                    )
                    val deferred = queries.map { q ->
                        async {
                            runCatching { repository.searchVideos(q, recentWindowDays = 120L) }
                                .getOrDefault(emptyList())
                        }
                    }
                    val fetched = deferred.awaitAll().flatten()
                    if (fetched.isNotEmpty()) {
                        catalogCache.saveTracks(fetched)
                        val updatedCatalog = catalogCache.loadAllTracks()
                        _state.value = _state.value.copy(
                            videos = catalogCache.filterByCategory(_state.value.activeQuery, updatedCatalog),
                            allCachedVideos = updatedCatalog,
                            isCatalogSynced = true,
                            isLoading = false,
                            error = null
                        )
                    }
                } catch (_: Exception) {
                    // Keep local phone database catalog active without disrupting user
                }
            }
        }

        loadAudiusTrending()
    }

    /**
     * Switches categories in 0ms using the phone's saved catalog database without re-fetching.
     */
    fun selectCategory(categoryLabel: String, fallbackSearchQuery: String) {
        val allTracks = _state.value.allCachedVideos.ifEmpty { catalogCache.loadAllTracks() }
        val filtered = catalogCache.filterByCategory(categoryLabel, allTracks)
        _state.value = _state.value.copy(
            videos = filtered,
            activeQuery = categoryLabel,
            correctedQuerySuggestion = null,
            isLoading = false,
            error = null
        )
    }

    /**
     * Instant typo-tolerant search:
     * 1. Immediately filters local phone DB using phonetic + Levenshtein fuzzy matching.
     * 2. Auto-corrects misspelled artist/song names and fetches fresh matches in background, saving them to phone DB.
     */
    fun searchVideos(rawQuery: String) {
        val trimmed = rawQuery.trim()
        val allTracks = _state.value.allCachedVideos.ifEmpty { catalogCache.loadAllTracks() }

        if (trimmed.isBlank()) {
            _state.value = _state.value.copy(
                videos = allTracks,
                activeQuery = "Latest Hindi",
                correctedQuerySuggestion = null,
                isLoading = false,
                error = null
            )
            return
        }

        // Check if this is one of the preset category queries
        val isPresetCategory = trimmed.contains("T-Series") || trimmed.contains("Speed Records") || trimmed.contains("SVF Music")
        if (isPresetCategory) {
            val filtered = SmartMusicSearchEngine.searchLocalCatalog(trimmed, allTracks).ifEmpty { allTracks }
            _state.value = _state.value.copy(
                videos = filtered,
                activeQuery = trimmed,
                correctedQuerySuggestion = null,
                isLoading = false,
                error = null
            )
            return
        }

        val (correctedQuery, suggestion) = SmartMusicSearchEngine.smartCorrectQuery(trimmed)
        val localMatches = SmartMusicSearchEngine.searchLocalCatalog(trimmed, allTracks)

        // Show local fuzzy matches immediately (0ms) so user doesn't wait!
        _state.value = _state.value.copy(
            videos = localMatches.ifEmpty { allTracks.take(12) },
            activeQuery = trimmed,
            correctedQuerySuggestion = suggestion,
            isLoading = localMatches.isEmpty(),
            error = null
        )

        viewModelScope.launch {
            // Search Audius cloud tracks concurrently
            launch {
                val audiusResults = runCatching {
                    audiusRepository.searchTracks(trimmed, limit = 20)
                }.getOrDefault(emptyList())
                if (audiusResults.isNotEmpty()) {
                    _state.value = _state.value.copy(audiusTracks = audiusResults)
                }
            }

            if (BuildConfig.YOUTUBE_API_KEY.isNotBlank()) {
                try {
                    val apiQuery = "$correctedQuery official song"
                    val remoteResults = repository.searchVideos(apiQuery, recentWindowDays = 0L)
                    if (remoteResults.isNotEmpty()) {
                        catalogCache.saveTracks(remoteResults)
                        val updatedAll = catalogCache.loadAllTracks()
                        val combinedResults = (remoteResults + localMatches).distinctBy { it.id }
                        _state.value = _state.value.copy(
                            videos = combinedResults,
                            allCachedVideos = updatedAll,
                            isLoading = false,
                            error = null
                        )
                    } else {
                        _state.value = _state.value.copy(isLoading = false)
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    _state.value = _state.value.copy(isLoading = false, error = null)
                }
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun filterLocalInstant(rawQuery: String) {
        val trimmed = rawQuery.trim()
        val allTracks = _state.value.allCachedVideos.ifEmpty { catalogCache.loadAllTracks() }
        if (trimmed.isBlank()) {
            _state.value = _state.value.copy(
                videos = allTracks,
                correctedQuerySuggestion = null
            )
            return
        }
        val (_, suggestion) = SmartMusicSearchEngine.smartCorrectQuery(trimmed)
        val matches = SmartMusicSearchEngine.searchLocalCatalog(trimmed, allTracks)
        if (matches.isNotEmpty()) {
            _state.value = _state.value.copy(
                videos = matches,
                correctedQuerySuggestion = suggestion
            )
        } else if (suggestion != null) {
            _state.value = _state.value.copy(correctedQuerySuggestion = suggestion)
        }
    }

    fun searchPlaylists(query: String) {
        val normalized = query.trim().ifBlank { "Hindi Bollywood official music playlists" }
        viewModelScope.launch {
            try {
                val playlists = repository.searchPlaylists(normalized)
                _state.value = _state.value.copy(playlists = playlists, isLoading = false, error = null)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun loadAudiusTrending() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isAudiusLoading = true, audiusError = null)
            try {
                val tracks = audiusRepository.getTrendingTracks(limit = 20)
                _state.value = _state.value.copy(audiusTracks = tracks, isAudiusLoading = false)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    isAudiusLoading = false,
                    audiusError = error.message ?: "Unable to load Audius trending stream."
                )
            }
        }
    }

    fun searchAudius(query: String) {
        val (corrected, _) = SmartMusicSearchEngine.smartCorrectQuery(query.trim().ifBlank { "Hindi indie lofi" })
        viewModelScope.launch {
            _state.value = _state.value.copy(isAudiusLoading = true, audiusError = null)
            try {
                val tracks = audiusRepository.searchTracks(corrected, limit = 24)
                if (tracks.isNotEmpty()) {
                    _state.value = _state.value.copy(audiusTracks = tracks, isAudiusLoading = false)
                } else {
                    loadAudiusTrending()
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    isAudiusLoading = false,
                    audiusError = error.message ?: "Unable to search Audius tracks."
                )
            }
        }
    }

    fun audiusStreamUrl(trackId: String): String = audiusRepository.streamUrl(trackId)

    fun recordPlayed(item: YouTubeItem) {
        val updated = listOf(item) + _state.value.recentlyPlayed.filterNot { it.id == item.id }
        _state.value = _state.value.copy(recentlyPlayed = updated.take(12))
    }

    fun toggleFavoriteVideo(id: String) {
        val current = _state.value.favoriteVideoIds.toMutableSet()
        if (!current.add(id)) current.remove(id)
        _state.value = _state.value.copy(favoriteVideoIds = current)
    }
}
