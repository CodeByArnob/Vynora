package com.vynora.audius

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vynora.BuildConfig
import com.vynora.catalog.AlbumModel
import com.vynora.catalog.UnifiedCatalogRepository
import com.vynora.catalog.UnifiedTrack
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AudiusHomeState(
    val albums: List<AlbumModel> = emptyList(),
    val trendingTracks: List<AudiusTrack> = emptyList(),
    val searchResults: List<AudiusTrack> = emptyList(),
    val recentlyPlayed: List<AudiusTrack> = emptyList(),
    val favoriteTrackIds: Set<String> = emptySet(),
    val activeGenre: String = "Trending",
    val activeQuery: String = "",
    val correctedQuerySuggestion: String? = null,
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val error: String? = null
)

class AudiusViewModel(application: Application) : AndroidViewModel(application) {
    private val audiusRepository = AudiusRepository(BuildConfig.AUDIUS_API_KEY)
    private val catalogRepository = UnifiedCatalogRepository(application.applicationContext, audiusRepository)

    private val _state = MutableStateFlow(AudiusHomeState(albums = catalogRepository.getCuratedAudiusAlbums()))
    val state: StateFlow<AudiusHomeState> = _state.asStateFlow()

    private var hasLoadedInitialHome = false

    fun loadHome() {
        if (hasLoadedInitialHome) return
        hasLoadedInitialHome = true

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val curatedStatic = catalogRepository.getCuratedAudiusAlbums()
                val liveAlbums = runCatching { audiusRepository.getCuratedAlbums() }.getOrDefault(emptyList())
                val trending = runCatching { audiusRepository.getTrendingTracks("week", 24) }.getOrDefault(emptyList())

                val allAlbums = (curatedStatic + liveAlbums).distinctBy { it.id }
                _state.value = _state.value.copy(
                    albums = allAlbums,
                    trendingTracks = trending,
                    isLoading = false,
                    error = null
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to refresh Audius catalog"
                )
            }
        }
    }

    fun selectGenre(genre: String) {
        _state.value = _state.value.copy(activeGenre = genre, isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val tracks = if (genre.equals("Trending", ignoreCase = true)) {
                    audiusRepository.getTrendingTracks("week", 24)
                } else if (genre.equals("Underground", ignoreCase = true)) {
                    audiusRepository.getUndergroundTrending(24)
                } else {
                    audiusRepository.getTrendingTracks("week", 24, genre = genre)
                }
                _state.value = _state.value.copy(
                    trendingTracks = tracks.ifEmpty { _state.value.trendingTracks },
                    isLoading = false
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun searchTracks(rawQuery: String) {
        val trimmed = rawQuery.trim()
        if (trimmed.isBlank()) {
            _state.value = _state.value.copy(
                searchResults = emptyList(),
                activeQuery = "",
                correctedQuerySuggestion = null,
                isSearching = false
            )
            return
        }

        val (corrected, suggestion) = AudiusSearchEngine.smartCorrectQuery(trimmed)
        _state.value = _state.value.copy(
            activeQuery = trimmed,
            correctedQuerySuggestion = suggestion,
            isSearching = true
        )

        viewModelScope.launch {
            try {
                val results = audiusRepository.searchTracks(corrected, limit = 30)
                _state.value = _state.value.copy(
                    searchResults = results,
                    isSearching = false,
                    error = null
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSearching = false,
                    error = e.message ?: "Unable to search Audius tracks"
                )
            }
        }
    }

    fun audiusStreamUrl(trackId: String): String = audiusRepository.streamUrl(trackId)

    fun recordPlayed(track: AudiusTrack) {
        val updated = listOf(track) + _state.value.recentlyPlayed.filterNot { it.id == track.id }
        _state.value = _state.value.copy(recentlyPlayed = updated.take(15))
    }

    fun toggleFavorite(trackId: String) {
        val current = _state.value.favoriteTrackIds.toMutableSet()
        if (!current.add(trackId)) current.remove(trackId)
        _state.value = _state.value.copy(favoriteTrackIds = current)
    }
}
