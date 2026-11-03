package com.vynora.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val database = VynoraDatabase.getInstance(application)
    private val repository = MediaStoreMusicRepository(application.applicationContext)
    private val trackDao = database.trackDao()
    private val playlistDao = database.playlistDao()

    val tracks: StateFlow<List<AudioTrack>> = trackDao.observeAll().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val favorites: StateFlow<List<AudioTrack>> = trackDao.observeFavorites().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val playlists: StateFlow<List<PlaylistEntity>> = playlistDao.observePlaylists().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    private val _selectedPlaylistId = MutableStateFlow<Long?>(null)
    val selectedPlaylistId: StateFlow<Long?> = _selectedPlaylistId
    val selectedPlaylistTracks: StateFlow<List<AudioTrack>> = _selectedPlaylistId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else playlistDao.observePlaylistTracks(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning
    private val _scanError = MutableStateFlow<String?>(null)
    val scanError: StateFlow<String?> = _scanError

    fun refreshLibrary() {
        if (_isScanning.value) return
        viewModelScope.launch {
            _isScanning.value = true
            _scanError.value = null
            try {
                val favoriteUris = trackDao.getFavoriteUris().toHashSet()
                val scanned = repository.scanAudio().map { track ->
                    track.copy(isFavorite = track.uri in favoriteUris)
                }
                trackDao.upsertAll(scanned)
                if (scanned.isEmpty()) trackDao.deleteAll()
                else trackDao.deleteMissing(scanned.map(AudioTrack::uri))
            } catch (_: SecurityException) {
                _scanError.value = "Audio permission is required to scan your library."
            } catch (error: Exception) {
                _scanError.value = error.localizedMessage ?: "Unable to scan audio files."
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun setFavorite(track: AudioTrack, favorite: Boolean) {
        viewModelScope.launch { trackDao.setFavorite(track.uri, favorite) }
    }

    fun createPlaylist(name: String) {
        val normalized = name.trim()
        if (normalized.isEmpty()) return
        viewModelScope.launch {
            val id = playlistDao.createPlaylist(PlaylistEntity(name = normalized))
            _selectedPlaylistId.value = id
        }
    }

    fun selectPlaylist(id: Long?) {
        _selectedPlaylistId.value = id
    }

    fun addAllTracksToPlaylist(playlistId: Long) {
        viewModelScope.launch {
            tracks.value.forEachIndexed { index, track ->
                playlistDao.addTrack(PlaylistTrackEntity(playlistId, track.uri, index))
            }
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, track: AudioTrack) {
        viewModelScope.launch { playlistDao.removeTrack(playlistId, track.uri) }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            playlistDao.deletePlaylist(playlistId)
            if (_selectedPlaylistId.value == playlistId) _selectedPlaylistId.value = null
        }
    }
}
