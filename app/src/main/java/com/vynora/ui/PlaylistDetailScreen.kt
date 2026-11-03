package com.vynora.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vynora.library.AudioTrack

data class PlaylistUiModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val coverUri: String,
    val tracks: List<AudioTrack>,
    val curator: String = "YouTube Music"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlist: PlaylistUiModel,
    currentTrackUri: String?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    onTrackSelected: (Int) -> Unit,
    onToggleFavorite: ((AudioTrack) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000)),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 110.dp)
    ) {
        // 1. Top Bar with Back Arrow and Cast Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                IconButton(onClick = { /* Samples switch */ }) {
                    SamplesChevronIcon(
                        size = 22.dp,
                        tint = Color.White
                    )
                }
            }
        }

        // 2. Centered Square Cover Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.68f)
                        .aspectRatio(1f)
                        .shadow(elevation = 24.dp, shape = RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF181818)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = playlist.coverUri,
                        contentDescription = playlist.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Small translucent play circle in top-left (Matches Image 1!)
                    Surface(
                        modifier = Modifier
                            .padding(10.dp)
                            .size(28.dp)
                            .align(Alignment.TopStart),
                        shape = CircleShape,
                        color = Color(0x66000000)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Title (Big Bold)
                Text(
                    text = playlist.title,
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                // Subtitle / Curator
                Text(
                    text = playlist.curator,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = Color(0xFFAAAAAA),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                // Description with hashtags
                Text(
                    text = playlist.description,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = Color(0xFF888888),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(20.dp))

                // 3. Circular Action Bar (Download, Bookmark, Center White Play, Share, More)
                Row(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Download Button
                    ActionCircleButton(
                        icon = Icons.Rounded.FileDownload,
                        contentDescription = "Download",
                        onClick = { /* Download offline */ }
                    )

                    Spacer(Modifier.width(14.dp))

                    // Bookmark / Save to Library Button
                    ActionCircleButton(
                        icon = Icons.Rounded.BookmarkBorder,
                        contentDescription = "Save to library",
                        onClick = { /* Add to library */ }
                    )

                    Spacer(Modifier.width(14.dp))

                    // Center Solid White Play Button
                    Surface(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(elevation = 8.dp, shape = CircleShape)
                            .clickable(onClick = onPlayAll),
                        shape = CircleShape,
                        color = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.PlayArrow,
                                contentDescription = "Play all",
                                tint = Color.Black,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    // Share Button
                    ActionCircleButton(
                        icon = Icons.Rounded.Share,
                        contentDescription = "Share",
                        onClick = { /* Share link */ }
                    )

                    Spacer(Modifier.width(14.dp))

                    // More Options Button
                    ActionCircleButton(
                        icon = Icons.Rounded.MoreVert,
                        contentDescription = "More",
                        onClick = { /* More menu */ }
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }

        // 4. Songs List
        itemsIndexed(playlist.tracks) { index, track ->
            val isCurrent = track.uri == currentTrackUri
            var showMenu by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTrackSelected(index) }
                    .padding(horizontal = 16.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Track Artwork Thumbnail
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF222222)),
                    contentAlignment = Alignment.Center
                ) {
                    val art = track.artworkUri ?: playlist.coverUri
                    AsyncImage(
                        model = art,
                        contentDescription = track.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (isCurrent && isPlaying) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x99000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.GraphicEq,
                                contentDescription = null,
                                tint = Color(0xFF1DB954),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.width(14.dp))

                // Title + Artist & Duration
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isCurrent) Color(0xFF1DB954) else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    val subtitleText = if (track.artist.contains("Irs...") || track.durationMs <= 0L) {
                        track.artist
                    } else {
                        "${track.artist} • ${track.durationLabel}"
                    }
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = Color(0xFFAAAAAA),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 3-dots more menu
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Rounded.MoreVert,
                            contentDescription = "Options",
                            tint = Color(0xFFAAAAAA),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF242424))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Play next", color = Color.White) },
                            onClick = {
                                showMenu = false
                                onTrackSelected(index)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (track.isFavorite) "Remove from favorites" else "Add to favorites", color = Color.White) },
                            onClick = {
                                showMenu = false
                                onToggleFavorite?.invoke(track)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(44.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = Color(0xFF222222)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
