package com.vynora.youtube

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vynora.BuildConfig
import com.vynora.audius.AudiusRepository
import com.vynora.audius.AudiusTrack
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class YouTubeHomeState(
    val videos: List<YouTubeItem> = emptyList(),
    val playlists: List<YouTubeItem> = emptyList(),
    val audiusTracks: List<AudiusTrack> = emptyList(),
    val recentlyPlayed: List<YouTubeItem> = emptyList(),
    val favoriteVideoIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isAudiusLoading: Boolean = false,
    val error: String? = null,
    val audiusError: String? = null,
    val apiKeyConfigured: Boolean = BuildConfig.YOUTUBE_API_KEY.isNotBlank(),
    val activeQuery: String = "latest Hindi songs 2026 official video"
)

class YouTubeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = YouTubeDataRepository(BuildConfig.YOUTUBE_API_KEY)
    private val audiusRepository = AudiusRepository(BuildConfig.AUDIUS_API_KEY)
    private val _state = MutableStateFlow(YouTubeHomeState())
    val state: StateFlow<YouTubeHomeState> = _state.asStateFlow()
    private var hasLoaded = false

    fun loadHome() {
        if (hasLoaded) return
        hasLoaded = true
        searchVideos(_state.value.activeQuery)
        loadAudiusTrending()
    }

    fun searchVideos(query: String) {
        val normalized = query.trim().ifBlank { "latest Hindi songs 2026 official video" }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, activeQuery = normalized)
            try {
                val videos = repository.searchVideos(normalized)
                _state.value = _state.value.copy(videos = videos, isLoading = false, error = null)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = error.message ?: "Unable to load YouTube results."
                )
            }
        }
    }

    fun searchPlaylists(query: String) {
        val normalized = query.trim().ifBlank { "Hindi Bollywood music playlists" }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, activeQuery = normalized)
            try {
                val playlists = repository.searchPlaylists(normalized)
                _state.value = _state.value.copy(playlists = playlists, isLoading = false, error = null)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = error.message ?: "Unable to load YouTube playlists."
                )
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
        val normalized = query.trim().ifBlank { "Hindi indie lofi" }
        viewModelScope.launch {
            _state.value = _state.value.copy(isAudiusLoading = true, audiusError = null)
            try {
                val tracks = audiusRepository.searchTracks(normalized, limit = 24)
                _state.value = _state.value.copy(audiusTracks = tracks, isAudiusLoading = false)
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
