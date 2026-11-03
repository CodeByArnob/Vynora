package com.vynora

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.vynora.library.AudioTrack
import com.vynora.library.MediaStoreMusicRepository
import com.vynora.playback.PlaybackController
import com.vynora.playback.PlaybackSnapshot

class MainActivity : ComponentActivity() {
    private lateinit var playbackController: PlaybackController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playbackController = PlaybackController(applicationContext)
        setContent {
            VynoraApp(
                playbackController = playbackController,
                repository = MediaStoreMusicRepository(applicationContext)
            )
        }
    }

    override fun onDestroy() {
        playbackController.release()
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
private fun VynoraApp(
    playbackController: PlaybackController,
    repository: MediaStoreMusicRepository
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var tracks by remember { mutableStateOf<List<AudioTrack>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
                else Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    val snapshot = playbackController.snapshot
    val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            isScanning = true
            tracks = repository.scanAudio()
            isScanning = false
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            isScanning = true
            tracks = repository.scanAudio()
            isScanning = false
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Color(0xFFC8B8FF),
        onPrimary = Color(0xFF24134D),
        secondary = Color(0xFFB8C7FF),
        background = Color(0xFF0D0B14),
        surface = Color(0xFF171421),
        surfaceVariant = Color(0xFF252133)
    )) {
        Scaffold(
            containerColor = Color(0xFF0D0B14),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("VYNORA", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
                            Text("Your music, your space", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            if (hasPermission) {
                                isScanning = true
                                tracks = repository.scanAudio()
                                isScanning = false
                            } else permissionLauncher.launch(permission)
                        }) { Icon(Icons.Rounded.Refresh, contentDescription = "Rescan music") }
                    }
                )
            },
            bottomBar = {
                Column {
                    if (snapshot.title != "Nothing playing" && snapshot.title != "Player unavailable") {
                        MiniPlayer(snapshot, playbackController)
                    }
                    NavigationBar(containerColor = Color(0xFF171421)) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Rounded.LibraryMusic, contentDescription = null) },
                            label = { Text("Library") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Rounded.GraphicEq, contentDescription = null) },
                            label = { Text("Now playing") }
                        )
                    }
                }
            }
        ) { padding ->
            when (selectedTab) {
                0 -> LibraryScreen(
                    modifier = Modifier.padding(padding),
                    tracks = tracks,
                    query = query,
                    onQueryChange = { query = it },
                    isScanning = isScanning,
                    hasPermission = hasPermission,
                    onGrantPermission = { permissionLauncher.launch(permission) },
                    onPlay = { index -> playbackController.playQueue(tracks, index) },
                    onRescan = {
                        isScanning = true
                        tracks = repository.scanAudio()
                        isScanning = false
                    }
                )
                else -> NowPlayingScreen(
                    modifier = Modifier.padding(padding),
                    snapshot = snapshot,
                    controller = playbackController
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun LibraryScreen(
    modifier: Modifier,
    tracks: List<AudioTrack>,
    query: String,
    onQueryChange: (String) -> Unit,
    isScanning: Boolean,
    hasPermission: Boolean,
    onGrantPermission: () -> Unit,
    onPlay: (Int) -> Unit,
    onRescan: () -> Unit
) {
    val filtered = remember(tracks, query) {
        tracks.withIndex().filter { (_, track) ->
            "${track.title} ${track.artist} ${track.album}".contains(query, ignoreCase = true)
        }
    }
    Column(modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(14.dp))
        Text("Your library", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("${tracks.size} songs on this device", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search songs, artists, albums") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(18.dp)
        )
        Spacer(Modifier.height(16.dp))
        if (!hasPermission) {
            EmptyLibrary(
                title = "Let Vynora find your music",
                subtitle = "Audio access is used only to index music stored on this device.",
                action = "Allow audio access",
                onAction = onGrantPermission
            )
        } else if (isScanning) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (tracks.isEmpty()) {
            EmptyLibrary(
                title = "Your library is quiet",
                subtitle = "Add audio files to your device, then rescan your music library.",
                action = "Scan again",
                onAction = onRescan
            )
        } else {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("ALL SONGS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("Tap a song to play", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(filtered, key = { it.value.id }) { indexed ->
                    TrackRow(track = indexed.value, onClick = { onPlay(indexed.index) })
                }
                item { Spacer(Modifier.height(18.dp)) }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun TrackRow(track: AudioTrack, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(48.dp).clip(RoundedCornerShape(13.dp)).background(
                Brush.linearGradient(listOf(Color(0xFF44346A), Color(0xFF24203A)))
            ),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Rounded.Album, contentDescription = null, tint = Color(0xFFD4C7FF)) }
        Column(Modifier.weight(1f).padding(start = 13.dp, end = 8.dp)) {
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
            Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(track.durationLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@androidx.compose.runtime.Composable
private fun EmptyLibrary(title: String, subtitle: String, action: String, onAction: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().weight(1f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(92.dp).clip(CircleShape).background(Color(0xFF211B32)),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Rounded.QueueMusic, contentDescription = null, modifier = Modifier.size(42.dp), tint = Color(0xFFC8B8FF)) }
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(18.dp))
        Button(onClick = onAction) { Text(action) }
    }
}

@androidx.compose.runtime.Composable
private fun MiniPlayer(snapshot: PlaybackSnapshot, controller: PlaybackController) {
    Surface(color = Color(0xFF211B32), tonalElevation = 4.dp) {
        Row(
            Modifier.fillMaxWidth().clickable { }.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Album, contentDescription = null, tint = Color(0xFFC8B8FF), modifier = Modifier.size(34.dp))
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(snapshot.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                Text(snapshot.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = controller::togglePlayback) {
                Icon(if (snapshot.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = "Play or pause")
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun NowPlayingScreen(
    modifier: Modifier,
    snapshot: PlaybackSnapshot,
    controller: PlaybackController
) {
    Column(
        modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(34.dp)).background(
                Brush.linearGradient(listOf(Color(0xFF493A70), Color(0xFF171421), Color(0xFF29203E)))
            ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.GraphicEq, contentDescription = null, modifier = Modifier.size(104.dp), tint = Color(0xFFD4C7FF))
        }
        Spacer(Modifier.height(30.dp))
        Text(snapshot.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(snapshot.artist, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(30.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { controller.seekTo(0L) }) { Icon(Icons.Rounded.SkipPrevious, contentDescription = "Restart track") }
            Spacer(Modifier.size(20.dp))
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                IconButton(onClick = controller::togglePlayback, modifier = Modifier.size(68.dp)) {
                    Icon(
                        if (snapshot.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play or pause",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
            Spacer(Modifier.size(20.dp))
            IconButton(onClick = { }) { Icon(Icons.Rounded.SkipNext, contentDescription = "Next track") }
        }
        Spacer(Modifier.height(18.dp))
        Text("Background playback powered by Android Media3", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
