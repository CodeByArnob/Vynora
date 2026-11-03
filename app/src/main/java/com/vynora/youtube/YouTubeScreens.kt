package com.vynora.youtube

import android.annotation.SuppressLint
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.vynora.audius.AudiusTrack
import com.vynora.playback.PlaybackController
import com.vynora.playback.PlaybackSnapshot

private val categories = listOf(
    "Latest Hindi" to "latest Hindi songs T-Series Sony Music Zee Music official",
    "Bollywood" to "new Bollywood songs official video T-Series YRF Zee Music",
    "Punjabi" to "latest Punjabi songs Speed Records White Hill Music official",
    "Arijit Singh" to "Arijit Singh latest Hindi songs official video",
    "Romantic" to "Hindi romantic songs official music video",
    "Bangla" to "latest Bangla songs SVF Music Coke Studio Bangla official",
    "Trending" to "trending Indian music videos official",
    "Indian Lo-Fi" to "Bollywood Hindi lofi chill mix"
)

@Composable
fun YouTubeHomeScreen(
    state: YouTubeHomeState,
    onSearchVideos: (String) -> Unit,
    onSearchPlaylists: (String) -> Unit,
    onOpen: (YouTubeItem) -> Unit,
    onSelectCategory: ((String, String) -> Unit)? = null,
    onFilterLocalInstant: ((String) -> Unit)? = null,
    onPlayAudiusTrack: ((AudiusTrack) -> Unit)? = null,
    onSearchAudius: ((String) -> Unit)? = null,
    onToggleFavoriteVideo: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var selectedChip by remember { mutableStateOf(categories.first().first) }
    var showSearchField by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(Color(0xFF07090E)),
        contentPadding = PaddingValues(bottom = 132.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF112C1F), Color(0xFF07090E))
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Category Filter Pills (Spotify-style top bar)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            Surface(
                                modifier = Modifier.clickable { showSearchField = !showSearchField },
                                shape = RoundedCornerShape(50),
                                color = if (showSearchField) Color(0xFF1DB954) else Color(0xFF18202C)
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Search,
                                        contentDescription = "Search",
                                        tint = if (showSearchField) Color(0xFF07090E) else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "Search",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (showSearchField) Color(0xFF07090E) else Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        items(categories) { (label, searchQuery) ->
                            val isSelected = selectedChip == label
                            Surface(
                                modifier = Modifier.clickable {
                                    selectedChip = label
                                    if (onSelectCategory != null) {
                                        onSelectCategory(label, searchQuery)
                                    } else {
                                        onSearchVideos(searchQuery)
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) Color(0xFF1DB954) else Color(0xFF18202C)
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color(0xFF07090E) else Color(0xFFE2E8F0),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            onFilterLocalInstant?.invoke(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search any song or artist (typo-smart search)...") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color(0xFF1DB954)) },
                        trailingIcon = {
                            if (query.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(onClick = {
                                        onSearchVideos(query)
                                        onSearchAudius?.invoke(query)
                                    }) {
                                        Text("Search", color = Color(0xFF1DB954), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    )
                    if (state.correctedQuerySuggestion != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val corrected = state.correctedQuerySuggestion
                                    query = corrected
                                    onSearchVideos(corrected)
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF12281E)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.GraphicEq,
                                    contentDescription = null,
                                    tint = Color(0xFF1DB954),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Smart Match: Showing results for "${state.correctedQuerySuggestion}"",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF1DB954),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Spotify-Style 2-Column Quick-Launch Grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.take(6).chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { (label, searchQuery) ->
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(54.dp)
                                            .clickable {
                                                selectedChip = label
                                                onSearchVideos(searchQuery)
                                            },
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF151C28)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(54.dp)
                                                    .background(
                                                        Brush.linearGradient(
                                                            listOf(Color(0xFF1DB954), Color(0xFF0F291E))
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Rounded.GraphicEq,
                                                    contentDescription = null,
                                                    tint = Color(0xFF07090E),
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Text(
                                                text = label,
                                                modifier = Modifier.padding(horizontal = 10.dp),
                                                style = MaterialTheme.typography.labelLarge,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Featured Hero Card for #1 Official Release
        state.videos.firstOrNull()?.let { featured ->
            item {
                Column(Modifier.padding(horizontal = 18.dp)) {
                    Text(
                        "FEATURED INDIAN RELEASE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1DB954),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onOpen(featured) },
                        shape = RoundedCornerShape(22.dp),
                        color = Color(0xFF121824)
                    ) {
                        Box(Modifier.fillMaxWidth().height(210.dp)) {
                            AsyncImage(
                                model = featured.thumbnailUrl,
                                contentDescription = featured.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color(0xCC07090E), Color(0xF507090E))
                                        )
                                    )
                            )
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = featured.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "${featured.channelTitle} · ${featured.releaseVsUploadLabel}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFCBD5E1),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = CircleShape,
                                    color = Color(0xFF1DB954)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Rounded.PlayArrow,
                                            contentDescription = "Play Featured",
                                            tint = Color(0xFF07090E),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.recentlyPlayed.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Recently Played",
                    subtitle = "Jump right back into your recent tracks"
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(state.recentlyPlayed, key = { "recent-${it.id}" }) { item ->
                        MusicArtworkCard(item = item, onClick = { onOpen(item) })
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Latest Hindi & Official Releases",
                subtitle = "Last 90 days · Official Indian labels & verified artists",
                actionLabel = "Refresh",
                onAction = { onSearchVideos(state.activeQuery) }
            )
            Spacer(Modifier.height(10.dp))
            when {
                !state.apiKeyConfigured -> SetupCard()
                state.isLoading -> Box(
                    Modifier.fillMaxWidth().height(210.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF1DB954))
                }
                state.error != null -> ErrorCard(state.error) { onSearchVideos(state.activeQuery) }
                state.videos.isEmpty() -> EmptyCard()
                else -> LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(state.videos, key = { it.id }) { item ->
                        MusicArtworkCard(item = item, onClick = { onOpen(item) })
                    }
                }
            }
        }

        if (state.audiusTracks.isNotEmpty() && onPlayAudiusTrack != null) {
            item {
                SectionHeader(
                    title = "Audius Direct Audio Stream",
                    subtitle = "Background-ready Media3 streaming · Instant playback"
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(state.audiusTracks, key = { "audius-${it.id}" }) { track ->
                        AudiusArtworkCard(track = track, onClick = { onPlayAudiusTrack(track) })
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Made For Your Mood",
                subtitle = "Bollywood romance, Punjabi energy, Arijit classics & Bangla indie",
                actionLabel = "Playlists",
                onAction = { onSearchPlaylists("Hindi Bollywood official music playlists") }
            )
            Spacer(Modifier.height(10.dp))
            if (state.playlists.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(state.playlists, key = { "pl-${it.id}" }) { playlistItem ->
                        MusicArtworkCard(
                            item = playlistItem,
                            onClick = { onOpen(playlistItem) },
                            playlist = true
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categories) { (label, search) ->
                    MoodCollectionCard(
                        label = label,
                        onClick = {
                            selectedChip = label
                            onSearchVideos(search)
                        }
                    )
                }
            }
        }

        if (state.videos.isNotEmpty()) {
            item(key = "quick-picks-header") {
                SectionHeader(
                    title = "Quick Picks Tracklist",
                    subtitle = "Official releases ranked by relevance & recency"
                )
            }
            items(state.videos.take(10), key = { "quickpick-${it.id}" }) { item ->
                CompactTrackRow(
                    item = item,
                    isFavorite = state.favoriteVideoIds.contains(item.id),
                    onToggleFavorite = { onToggleFavoriteVideo?.invoke(item.id) },
                    onClick = { onOpen(item) }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )
        }
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel, color = Color(0xFF1DB954), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun MusicArtworkCard(
    item: YouTubeItem,
    onClick: () -> Unit,
    playlist: Boolean = false
) {
    Surface(
        modifier = Modifier.width(168.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF111622)
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B))
            ) {
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).size(36.dp),
                    shape = CircleShape,
                    color = Color(0xFF1DB954)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (playlist) Icons.Rounded.PlaylistPlay else Icons.Rounded.PlayArrow,
                            contentDescription = "Play",
                            tint = Color(0xFF07090E)
                        )
                    }
                }
            }
            Text(
                item.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                item.channelTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8)
            )
            Text(
                item.releaseVsUploadLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF1DB954)
            )
        }
    }
}

@Composable
private fun AudiusArtworkCard(
    track: AudiusTrack,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.width(164.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF111622)
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B))
            ) {
                AsyncImage(
                    model = track.artworkUrl,
                    contentDescription = track.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp).size(36.dp),
                    shape = CircleShape,
                    color = Color(0xFF1DB954)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.GraphicEq,
                            contentDescription = "Stream Audio",
                            tint = Color(0xFF07090E)
                        )
                    }
                }
            }
            Text(
                track.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                track.artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8)
            )
            Text(
                "Audius · Direct Stream",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF38BDF8)
            )
        }
    }
}

@Composable
private fun MoodCollectionCard(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.width(152.dp).height(84.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF151E2E)
    ) {
        Box(Modifier.fillMaxSize().padding(14.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Icon(
                Icons.Rounded.Album,
                contentDescription = null,
                tint = Color(0xFF1DB954),
                modifier = Modifier.align(Alignment.BottomEnd).size(24.dp)
            )
        }
    }
}

@Composable
private fun CompactTrackRow(
    item: YouTubeItem,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF111622)
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = item.title,
                modifier = Modifier.size(54.dp).clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    item.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    item.channelTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    item.releaseVsUploadLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF1DB954)
                )
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFF1DB954) else Color(0xFF94A3B8)
                )
            }
            Icon(
                Icons.Rounded.PlayCircleFilled,
                contentDescription = "Play",
                tint = Color(0xFF1DB954),
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun SetupCard() {
    Card(Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
        Text(
            "Configure YOUTUBE_API_KEY in local.properties or GitHub Actions Secrets to load live music.",
            Modifier.padding(18.dp)
        )
    }
}

@Composable
private fun ErrorCard(message: String, retry: () -> Unit) {
    Column(Modifier.padding(horizontal = 18.dp)) {
        Text(message, color = MaterialTheme.colorScheme.error)
        TextButton(onClick = retry) { Text("Retry") }
    }
}

@Composable
private fun EmptyCard() {
    Text(
        "No matching embeddable music videos found. Try another search or mood filter.",
        Modifier.padding(horizontal = 18.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * Unified YouTube Music / Spotify Now Playing & Video Screen (v0.5.0).
 * - Fixed 60fps layout: WebView is placed in a fixed hardware-accelerated container outside LazyColumn to eliminate scroll/recomposition lag.
 * - Plays 100% REAL music: Uses official YT IFrame API first, and if a label restricts /embed/ (Error 101/150/152/153),
 *   automatically switches to in-app YouTube Mobile Theatre Mode inside the same 16:9 viewport so the actual song always plays.
 * - Real Play/Pause & Seek Bar: Directly controls the active YouTube HTML5 video / YT.Player via JavascriptInterface.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeWatchScreen(
    item: YouTubeItem,
    queue: List<YouTubeItem> = emptyList(),
    snapshot: PlaybackSnapshot? = null,
    controller: PlaybackController? = null,
    onBack: () -> Unit,
    onSelectQueueItem: ((YouTubeItem) -> Unit)? = null,
    onSkipNext: (() -> Unit)? = null,
    onSkipPrevious: (() -> Unit)? = null,
    onAutoFallbackToAudio: (() -> Unit)? = null,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var displayMode by remember { mutableStateOf("video") }
    var usingTheatreFallback by remember(item.id) {
        mutableStateOf(YouTubeMusicFilter.prefersDirectWatchStream(item.channelTitle, item.title))
    }
    var videoHasVisibleFrames by remember(item.id) { mutableStateOf(false) }
    var ytIsPlaying by remember(item.id) { mutableStateOf(true) }
    var ytPositionSec by remember(item.id) { mutableStateOf(0f) }
    var ytDurationSec by remember(item.id) { mutableStateOf(0f) }
    val playerWebView = remember { mutableStateOf<WebView?>(null) }
    val currentItemRef = remember { mutableStateOf(item) }
    currentItemRef.value = item

    val injectTheatreCssJs = """
        (function() {
          try {
            var style = document.getElementById('vynora-theatre-style');
            if (!style) {
              style = document.createElement('style');
              style.id = 'vynora-theatre-style';
              style.innerHTML = 'ytm-mobile-topbar-renderer, #header-bar, ytm-slim-video-metadata-section-renderer, ytm-item-section-renderer, ytm-comment-section-renderer, ytm-pivot-bar-renderer, ytm-companion-slot, .related-chips-slot-wrapper, ytm-MerchandiseShelfRenderer, ytm-engagement-panel-section-list-renderer, .ytp-chrome-top, .ytp-watermark { display: none !important; } body, html { background: transparent !important; overflow: hidden !important; margin: 0 !important; padding: 0 !important; } video { position: fixed !important; top: 0 !important; left: 0 !important; width: 100vw !important; height: 100vh !important; object-fit: cover !important; z-index: 2147483646 !important; background: transparent !important; }';
              document.head.appendChild(style);
            }
            var v = document.querySelector('video');
            if (v) {
              v.muted = false;
              if (v.paused) { v.play().catch(function(){}); }
            }
            if (!window.__vynoraTicker) {
              window.__vynoraTicker = setInterval(function() {
                var vid = document.querySelector('video');
                if (vid && window.VynoraBridge) {
                  var cur = Number(vid.currentTime || 0);
                  var dur = Number(vid.duration || 0);
                  var playing = !vid.paused && !vid.ended;
                  var hasFrames = vid.readyState >= 3 && vid.videoWidth > 0 && cur > 0.3 && playing;
                  window.VynoraBridge.onProgress(cur, dur, playing, hasFrames);
                  if (vid.ended) {
                    window.VynoraBridge.onEnded();
                  }
                }
              }, 800);
            }
          } catch (e) {}
        })();
    """.trimIndent()

    fun loadTrackIntoWebView(webView: WebView, target: YouTubeItem, forceDirectWatch: Boolean) {
        val safeId = target.id.replace("'", "").trim()
        webView.stopLoading()
        if (forceDirectWatch && !target.isPlaylist) {
            webView.loadUrl(
                "https://m.youtube.com/watch?v=$safeId&autoplay=1",
                mapOf("Referer" to "https://m.youtube.com/")
            )
        } else {
            val embedHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                  <meta name="referrer" content="strict-origin-when-cross-origin">
                  <style>
                    html, body { margin: 0; padding: 0; width: 100%; height: 100%; background: transparent; overflow: hidden; }
                    #player { width: 100%; height: 100%; border: 0; }
                  </style>
                </head>
                <body>
                  <div id="player"></div>
                  <script src="https://www.youtube.com/iframe_api"></script>
                  <script>
                    var player;
                    function onYouTubeIframeAPIReady() {
                      player = new YT.Player('player', {
                        width: '100%',
                        height: '100%',
                        ${if (target.isPlaylist) "playerVars: { listType: 'playlist', list: '$safeId', playsinline: 1, autoplay: 1, rel: 0, modestbranding: 1 }," else "videoId: '$safeId', playerVars: { playsinline: 1, autoplay: 1, rel: 0, modestbranding: 1 },"}
                        events: {
                          'onReady': function(e) {
                            try { e.target.playVideo(); } catch (_) {}
                            setInterval(function() {
                              try {
                                if (player && player.getCurrentTime && window.VynoraBridge) {
                                  var cur = Number(player.getCurrentTime() || 0);
                                  var dur = Number(player.getDuration() || 0);
                                  var st = Number(player.getPlayerState() || -1);
                                  var playing = (st === 1 || st === 3);
                                  var hasFrames = (st === 1 && cur > 0.3);
                                  window.VynoraBridge.onProgress(cur, dur, playing, hasFrames);
                                }
                              } catch (_) {}
                            }, 800);
                          },
                          'onStateChange': function(e) {
                            if (e.data === 0 && window.VynoraBridge) {
                              window.VynoraBridge.onEnded();
                            }
                          },
                          'onError': function(e) {
                            if (window.VynoraBridge && window.VynoraBridge.onYouTubeError) {
                              window.VynoraBridge.onYouTubeError(Number(e.data || 150));
                            }
                          }
                        }
                      });
                    }
                  </script>
                </body>
                </html>
            """.trimIndent()

            webView.loadDataWithBaseURL(
                "https://www.youtube-nocookie.com/",
                embedHtml,
                "text/html",
                "UTF-8",
                null
            )
        }
    }

    // Switch tracks inside the single persistent WebView without destroying/recreating it (0ms UI switch!)
    androidx.compose.runtime.LaunchedEffect(item.id) {
        val wv = playerWebView.value ?: return@LaunchedEffect
        val direct = YouTubeMusicFilter.prefersDirectWatchStream(item.channelTitle, item.title)
        usingTheatreFallback = direct
        videoHasVisibleFrames = false
        ytPositionSec = 0f
        ytDurationSec = 0f
        ytIsPlaying = true
        loadTrackIntoWebView(wv, item, direct)
    }

    // Only release WebView when leaving YouTubeWatchScreen altogether
    DisposableEffect(Unit) {
        onDispose {
            playerWebView.value?.apply {
                stopLoading()
                loadUrl("about:blank")
                webChromeClient = null
                destroy()
            }
            playerWebView.value = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF12241B), Color(0xFF07090E), Color(0xFF07090E))
                )
            )
    ) {
        // 1. Fixed Top Bar
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF141C28))
            ) {
                Icon(
                    Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Minimize player",
                    tint = Color.White
                )
            }

            // Segmented Video | Song Switcher
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFF121824),
                modifier = Modifier.border(1.dp, Color(0xFF222E42), RoundedCornerShape(50))
            ) {
                Row(Modifier.padding(3.dp)) {
                    Surface(
                        modifier = Modifier.clickable { displayMode = "video" },
                        shape = RoundedCornerShape(50),
                        color = if (displayMode == "video") Color(0xFF1DB954) else Color.Transparent
                    ) {
                        Row(
                            Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Videocam,
                                contentDescription = null,
                                tint = if (displayMode == "video") Color(0xFF07090E) else Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                "Video",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (displayMode == "video") Color(0xFF07090E) else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier.clickable { displayMode = "song" },
                        shape = RoundedCornerShape(50),
                        color = if (displayMode == "song") Color(0xFF1DB954) else Color.Transparent
                    ) {
                        Row(
                            Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = if (displayMode == "song") Color(0xFF07090E) else Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                "Song",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (displayMode == "song") Color(0xFF07090E) else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = { onToggleFavorite?.invoke() },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF141C28))
            ) {
                Icon(
                    if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFF1DB954) else Color.White
                )
            }
        }

        // 2. Fixed 16:9 Stage with Persistent WebView + Guaranteed High-Res Song Banner Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0B101B))
        ) {
            // Single Persistent WebView (Never destroyed on track switch -> instant switching!)
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        playerWebView.value = this
                        setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                        CookieManager.getInstance().setAcceptCookie(true)
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        settings.loadsImagesAutomatically = true
                        settings.javaScriptCanOpenWindowsAutomatically = false
                        settings.cacheMode = WebSettings.LOAD_DEFAULT
                        settings.userAgentString = settings.userAgentString
                            .replace("; wv", "")
                            .replace("Version/4.0 ", "")
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)

                        addJavascriptInterface(
                            object {
                                @JavascriptInterface
                                fun onYouTubeError(code: Int) {
                                    post {
                                        val activeItem = currentItemRef.value
                                        if (!usingTheatreFallback && !activeItem.isPlaylist) {
                                            usingTheatreFallback = true
                                            val safeId = activeItem.id.replace("'", "").trim()
                                            loadUrl(
                                                "https://m.youtube.com/watch?v=$safeId&autoplay=1",
                                                mapOf("Referer" to "https://m.youtube.com/")
                                            )
                                        }
                                    }
                                }

                                @JavascriptInterface
                                fun onProgress(
                                    currentSec: Double,
                                    durationSec: Double,
                                    playing: Boolean,
                                    hasFrames: Boolean
                                ) {
                                    post {
                                        if (durationSec > 0) {
                                            ytDurationSec = durationSec.toFloat()
                                            ytPositionSec = currentSec.toFloat().coerceIn(0f, ytDurationSec)
                                        }
                                        ytIsPlaying = playing
                                        videoHasVisibleFrames = hasFrames
                                    }
                                }

                                @JavascriptInterface
                                fun onEnded() {
                                    post {
                                        onSkipNext?.invoke()
                                    }
                                }
                            },
                            "VynoraBridge"
                        )

                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean = true
                        }

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val host = request?.url?.host?.lowercase().orEmpty()
                                return !(host.contains("youtube.com") ||
                                    host.contains("youtube-nocookie.com") ||
                                    host.contains("ytimg.com") ||
                                    host.contains("googlevideo.com") ||
                                    host.contains("google.com"))
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                if (url != null && url.contains("m.youtube.com/watch")) {
                                    view?.evaluateJavascript(injectTheatreCssJs, null)
                                }
                            }
                        }

                        val direct = YouTubeMusicFilter.prefersDirectWatchStream(item.channelTitle, item.title)
                        usingTheatreFallback = direct
                        loadTrackIntoWebView(this, item, direct)
                    }
                }
            )

            // High-Resolution Track Banner Image Overlay:
            // Shown in Song mode, during track switching/loading, or when label stream plays audio via direct watch
            val showBannerOverlay = (displayMode == "song") || usingTheatreFallback || !videoHasVisibleFrames
            if (showBannerOverlay) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0B101B))
                ) {
                    // Full-bleed 16:9 HD Track Banner Image
                    AsyncImage(
                        model = item.thumbnailUrl,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Cinematic gradient scrim so banner artwork + foreground card both pop clearly
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x5507090E),
                                        Color(0xAA07090E),
                                        Color(0xEE07090E)
                                    )
                                )
                            )
                    )
                    // Foreground Floating Artwork + Live Playback Status Card
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        AsyncImage(
                            model = item.thumbnailUrl,
                            contentDescription = item.title,
                            modifier = Modifier
                                .size(92.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xCC12291E)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.GraphicEq,
                                        contentDescription = null,
                                        tint = Color(0xFF1DB954),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        if (ytDurationSec > 0f) "NOW PLAYING · OFFICIAL STREAM" else "LOADING STREAM...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF1DB954),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                item.title,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                item.channelTitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }
        }

        // 3. Scrollable Metadata, Real YouTube Seek/Transport Controls & Up Next Queue
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 14.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.channelTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.releaseVsUploadLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF1DB954)
                    )

                    if (ytDurationSec > 1f) {
                        Slider(
                            value = ytPositionSec.coerceIn(0f, ytDurationSec),
                            onValueChange = { targetSec ->
                                ytPositionSec = targetSec
                                playerWebView.value?.evaluateJavascript(
                                    "(function(){ var v=document.querySelector('video'); if(v){v.currentTime=$targetSec;} else if(window.player&&window.player.seekTo){window.player.seekTo($targetSec,true);} })();",
                                    null
                                )
                            },
                            valueRange = 0f..ytDurationSec,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF1DB954),
                                activeTrackColor = Color(0xFF1DB954)
                            )
                        )
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                formatSec(ytPositionSec.toLong()),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                formatSec(ytDurationSec.toLong()),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Primary Transport Controls
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { displayMode = if (displayMode == "video") "song" else "video" }) {
                            Icon(
                                if (displayMode == "video") Icons.Rounded.MusicNote else Icons.Rounded.Videocam,
                                contentDescription = "Toggle Video or Song View",
                                tint = Color(0xFF1DB954)
                            )
                        }
                        IconButton(onClick = { onSkipPrevious?.invoke() }) {
                            Icon(
                                Icons.Rounded.SkipPrevious,
                                contentDescription = "Previous",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .size(64.dp)
                                .clickable {
                                    val nextPlaying = !ytIsPlaying
                                    ytIsPlaying = nextPlaying
                                    playerWebView.value?.evaluateJavascript(
                                        "(function(){ var v=document.querySelector('video'); if(v){ if(v.paused) v.play(); else v.pause(); return; } if(window.player&&window.player.getPlayerState){ if(window.player.getPlayerState()===1) window.player.pauseVideo(); else window.player.playVideo(); } })();",
                                        null
                                    )
                                },
                            shape = CircleShape,
                            color = Color(0xFF1DB954)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (ytIsPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = "Play or Pause",
                                    tint = Color(0xFF07090E),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        IconButton(onClick = { onSkipNext?.invoke() }) {
                            Icon(
                                Icons.Rounded.SkipNext,
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        IconButton(onClick = { onToggleFavorite?.invoke() }) {
                            Icon(
                                if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color(0xFF1DB954) else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            if (queue.isNotEmpty()) {
                item(key = "upnext-header") {
                    Text(
                        "Up Next in Queue",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
                items(
                    items = queue.filterNot { it.id == item.id }.take(12),
                    key = { "upnext-${it.id}" }
                ) { nextTrack ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                            .clickable { onSelectQueueItem?.invoke(nextTrack) },
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF111622)
                    ) {
                        Row(
                            Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = nextTrack.thumbnailUrl,
                                contentDescription = nextTrack.title,
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text(
                                    nextTrack.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    nextTrack.channelTitle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color(0xFF94A3B8),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Icon(
                                Icons.Rounded.PlayArrow,
                                contentDescription = "Play",
                                tint = Color(0xFF1DB954)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatSec(sec: Long): String {
    val s = sec.coerceAtLeast(0L)
    return "%d:%02d".format(s / 60L, s % 60L)
}
