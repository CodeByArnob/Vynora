package com.vynora.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val database = VynoraDatabase.getInstance(application)
    private val repository = MediaStoreMusicRepository(application.applicationContext)
    private val trackDao = database.trackDao()

    val tracks: StateFlow<List<AudioTrack>> = trackDao.observeAll().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val favorites: StateFlow<List<AudioTrack>> = trackDao.observeFavorites().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

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
                val scanned = repository.scanAudio()
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

    fun createPlaylist(name: String, onCreated: (Long) -> Unit = {}) {
        val normalized = name.trim()
        if (normalized.isEmpty()) return
        viewModelScope.launch {
            onCreated(database.playlistDao().createPlaylist(PlaylistEntity(name = normalized)))
        }
    }

    fun addToPlaylist(playlistId: Long, track: AudioTrack, position: Int) {
        viewModelScope.launch {
            database.playlistDao().addTrack(PlaylistTrackEntity(playlistId, track.uri, position))
        }
    }
}
