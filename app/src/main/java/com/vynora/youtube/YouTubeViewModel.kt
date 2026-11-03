package com.vynora.youtube

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vynora.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class YouTubeHomeState(
    val videos: List<YouTubeItem> = emptyList(),
    val playlists: List<YouTubeItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val apiKeyConfigured: Boolean = BuildConfig.YOUTUBE_API_KEY.isNotBlank(),
    val activeQuery: String = "popular Indian music official music video"
)

class YouTubeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = YouTubeDataRepository(BuildConfig.YOUTUBE_API_KEY)
    private val _state = MutableStateFlow(YouTubeHomeState())
    val state: StateFlow<YouTubeHomeState> = _state.asStateFlow()
    private var hasLoaded = false

    fun loadHome() {
        if (hasLoaded) return
        hasLoaded = true
        searchVideos(_state.value.activeQuery)
    }

    fun searchVideos(query: String) {
        val normalized = query.trim().ifBlank { "popular music official music video" }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, activeQuery = normalized)
            try {
                val videos = repository.searchVideos(normalized)
                _state.value = _state.value.copy(videos = videos, isLoading = false, error = null)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = error.message ?: "Unable to load YouTube results.")
            }
        }
    }

    fun searchPlaylists(query: String) {
        val normalized = query.trim().ifBlank { "Indian music albums official" }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, activeQuery = normalized)
            try {
                val playlists = repository.searchPlaylists(normalized)
                _state.value = _state.value.copy(playlists = playlists, isLoading = false, error = null)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = error.message ?: "Unable to load YouTube playlists.")
            }
        }
    }
}
