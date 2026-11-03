package com.vynora.ui

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
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vynora.audius.AudiusHomeState
import com.vynora.audius.AudiusTrack
import com.vynora.catalog.AlbumModel
import com.vynora.library.AudioTrack

private val moodPills = listOf(
    "Trending",
    "Electronic",
    "Lo-Fi",
    "Hip-Hop",
    "Chill",
    "Pop",
    "Rock",
    "Acoustic",
    "Ambient"
)

private const val SAFE_PLAYABLE_STREAM = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDiscoveryScreen(
    state: AudiusHomeState,
    onPlaylistSelected: (PlaylistUiModel) -> Unit,
    onTrackSelected: (AudioTrack) -> Unit,
    onPlayAudiusTrack: (AudiusTrack) -> Unit,
    onGenreSelected: (String) -> Unit,
    onSearch: (String) -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val curatedPlaylists = remember(state.albums) {
        state.albums.map { album ->
            PlaylistUiModel(
                id = album.id,
                title = album.title,
                subtitle = album.artist,
                description = album.description,
                coverUri = album.coverArtUri,
                curator = album.curator,
                providerLabel = album.provider.displayName,
                tracks = album.tracks.map { ut ->
                    AudioTrack(
                        uri = ut.streamUrl,
                        id = ut.id.hashCode().toLong(),
                        title = ut.title,
                        artist = ut.artist,
                        album = ut.album,
                        durationMs = ut.durationMs,
                        albumId = 1L,
                        artistId = 1L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = ut.provider.displayName
                    ).apply {
                        if (!ut.artworkUrl.isNullOrBlank()) {
                            customArtworkUri = ut.artworkUrl
                        }
                    }
                },
                unifiedTracks = album.tracks
            )
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000)),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // 1. Top Brand Bar with Ambient Glow & Audius Badge
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF1B2B20),
                                Color(0xFF131F17),
                                Color(0xFF0D1410),
                                Color(0xFF000000)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Green Vynora Badge + Brand Typography
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = Color(0xFF1DB954)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "Vynora",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFCC3399),
                            modifier = Modifier.padding(start = 2.dp)
                        ) {
                            Text(
                                text = "AUDIUS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Right: Notification Bell, Search, User Avatar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(onClick = { /* Notifications */ }) {
                            Icon(
                                Icons.Rounded.NotificationsNone,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(onClick = { showSearchBar = !showSearchBar }) {
                            Icon(
                                Icons.Rounded.Search,
                                contentDescription = "Search",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Surface(
                            modifier = Modifier.size(30.dp),
                            shape = CircleShape,
                            color = Color(0xFF1DB954)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.Person,
                                    contentDescription = "Account",
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                if (showSearchBar) {
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            onSearch(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search Audius songs, artists, genres...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.White) }
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Mood / Genre Filter Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(moodPills) { genre ->
                        val isSelected = state.activeGenre.equals(genre, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onGenreSelected(genre) },
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) Color(0xFF1DB954) else Color(0x38FFFFFF),
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(0.8.dp, Color(0x22FFFFFF))
                        ) {
                            Text(
                                text = genre,
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // Live Audius Search Results (When typing a search query)
        if (searchQuery.isNotBlank()) {
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Text(
                        text = "Audius Search Results (${state.searchResults.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    state.correctedQuerySuggestion?.let { suggestion ->
                        Text(
                            text = "Did you mean: $suggestion",
                            color = Color(0xFF1DB954),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            if (state.isSearching) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF1DB954))
                    }
                }
            } else if (state.searchResults.isEmpty()) {
                item {
                    Text(
                        text = "No matching Audius tracks found. Try searching for electronic, lofi, or a popular artist.",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                items(state.searchResults.take(8)) { audiusTrack ->
                    AudiusTrackRow(
                        track = audiusTrack,
                        onPlay = { onPlayAudiusTrack(audiusTrack) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 2. Curated Albums & Playlists (Indian Music & Audius Highlights)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Curated Albums & Playlists",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(curatedPlaylists) { pl ->
                        PlaylistCuratedCard(
                            playlist = pl,
                            onClick = { onPlaylistSelected(pl) }
                        )
                    }
                }
            }
        }

        // 3. Trending Now on Audius
        if (state.trendingTracks.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Trending on Audius (${state.activeGenre})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Decentralized streaming · High-fidelity audio",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF888888)
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(state.trendingTracks.take(12)) { track ->
                            AudiusTrendingCard(
                                track = track,
                                onPlay = { onPlayAudiusTrack(track) }
                            )
                        }
                    }
                }
            }
        }

        // 4. "Get started" Quick Discovery Stage
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Get started",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    item {
                        GetStartedCard(
                            title = "My Mix 01",
                            subtitle = "Try our perfect playlist for you",
                            imageUri = "file:///android_asset/card_my_mix_01.jpg",
                            containerColor = Color(0xFF203142),
                            onClick = {
                                curatedPlaylists.firstOrNull()?.let { onPlaylistSelected(it) }
                            }
                        )
                    }
                    item {
                        GetStartedCard(
                            title = "Discovery Swipe",
                            subtitle = "Find new music with a swipe",
                            imageUri = "file:///android_asset/card_find_new_swipe.jpg",
                            containerColor = Color(0xFF48212C),
                            onClick = {
                                state.trendingTracks.firstOrNull()?.let { onPlayAudiusTrack(it) }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudiusTrackRow(
    track: AudiusTrack,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onPlay)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF242424)
        ) {
            if (!track.artworkUrl.isNullOrBlank()) {
                AsyncImage(
                    model = track.artworkUrl,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Album, contentDescription = null, tint = Color.Gray)
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${track.artist} · ${track.genre}",
                color = Color(0xFFAAAAAA),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onPlay) {
            Icon(
                Icons.Rounded.PlayArrow,
                contentDescription = "Play",
                tint = Color(0xFF1DB954)
            )
        }
    }
}

@Composable
private fun AudiusTrendingCard(
    track: AudiusTrack,
    onPlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(154.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onPlay)
    ) {
        Box(
            modifier = Modifier
                .size(154.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF222222))
        ) {
            if (!track.artworkUrl.isNullOrBlank()) {
                AsyncImage(
                    model = track.artworkUrl,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Rounded.Album, contentDescription = null, tint = Color(0xFF666666), modifier = Modifier.size(48.dp))
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(34.dp),
                shape = CircleShape,
                color = Color(0xCC000000)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", tint = Color(0xFF1DB954), modifier = Modifier.size(20.dp))
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = track.artist,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFAAAAAA),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PlaylistCuratedCard(
    playlist: PlaylistUiModel,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF222222))
        ) {
            AsyncImage(
                model = playlist.coverUri,
                contentDescription = playlist.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(26.dp),
                shape = CircleShape,
                color = Color(0x99000000)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = playlist.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = playlist.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFAAAAAA),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GetStartedCard(
    title: String,
    subtitle: String,
    imageUri: String,
    containerColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(160.dp)
            .height(220.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x33000000))
            ) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xCCFFFFFF),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
