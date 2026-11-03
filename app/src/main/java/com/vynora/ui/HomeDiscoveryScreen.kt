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
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vynora.library.AudioTrack
import com.vynora.youtube.YouTubeHomeState
import com.vynora.youtube.YouTubeItem

private val moodPills = listOf(
    "Energize",
    "Relax",
    "Feel good",
    "Workout",
    "Party",
    "Commute",
    "Romance",
    "Focus"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDiscoveryScreen(
    state: YouTubeHomeState,
    onPlaylistSelected: (PlaylistUiModel) -> Unit,
    onTrackSelected: (AudioTrack) -> Unit,
    onSearchVideos: (String) -> Unit,
    onFilterLocalInstant: ((String) -> Unit)? = null,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMood by remember { mutableStateOf<String?>(null) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val curatedPlaylists = remember {
        listOf(
            PlaylistUiModel(
                id = "uncut_bollywood",
                title = "Uncut Bollywood",
                subtitle = "Arijit Singh, Pritam, Irshad Ka...",
                description = "Enjoy stripped down versions of your favorite Bollywood tracks.\n#hindi #chill #relax",
                coverUri = "file:///android_asset/album_uncut_bollywood.jpg",
                tracks = listOf(
                    AudioTrack(
                        uri = "https://audius.co/stream/uncut_1",
                        id = 101L,
                        title = "Gehra Hua",
                        artist = "Arijit Singh, Armaan Khan, Shashwat Sachdev & Irs...",
                        album = "Uncut Bollywood",
                        durationMs = 246_000L,
                        albumId = 1L,
                        artistId = 1L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = null
                    ).withArtwork("file:///android_asset/thumb_gehra_hua.jpg"),
                    AudioTrack(
                        uri = "https://audius.co/stream/uncut_2",
                        id = 102L,
                        title = "Arz Kiya Hai",
                        artist = "Anuv Jain",
                        album = "Uncut Bollywood",
                        durationMs = 306_000L,
                        albumId = 1L,
                        artistId = 2L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = null
                    ).withArtwork("file:///android_asset/thumb_arz_kiya_hai.jpg"),
                    AudioTrack(
                        uri = "https://audius.co/stream/uncut_3",
                        id = 103L,
                        title = "Barbaad (Movie: Saiyaara)",
                        artist = "Jubin Nautiyal",
                        album = "Uncut Bollywood",
                        durationMs = 225_000L,
                        albumId = 1L,
                        artistId = 3L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = null
                    ).withArtwork("file:///android_asset/thumb_barbaad.jpg"),
                    AudioTrack(
                        uri = "https://audius.co/stream/uncut_4",
                        id = 104L,
                        title = "nadaaniyan (with Jonita Gandhi)",
                        artist = "Jonita Gandhi & Akshath",
                        album = "Uncut Bollywood",
                        durationMs = 170_000L,
                        albumId = 1L,
                        artistId = 4L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = null
                    ).withArtwork("file:///android_asset/thumb_naadaniyan.jpg"),
                    AudioTrack(
                        uri = "https://audius.co/stream/uncut_5",
                        id = 105L,
                        title = "Kesariya (Unplugged)",
                        artist = "Arijit Singh, Pritam",
                        album = "Uncut Bollywood",
                        durationMs = 268_000L,
                        albumId = 1L,
                        artistId = 1L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = null
                    ).withArtwork("file:///android_asset/album_uncut_bollywood.jpg")
                )
            ),
            PlaylistUiModel(
                id = "00s_bollywood_romance",
                title = "00s Bollywood Romance",
                subtitle = "Alka Yagnik, Pritam, Shreya Gh...",
                description = "Iconic timeless Hindi melodies and golden romance memories of the 2000s.",
                coverUri = "file:///android_asset/album_00s_bollywood_romance.jpg",
                tracks = listOf(
                    AudioTrack(
                        uri = "https://audius.co/stream/romance_1",
                        id = 201L,
                        title = "Pee Loon",
                        artist = "Mohit Chauhan, Pritam",
                        album = "00s Bollywood Romance",
                        durationMs = 287_000L,
                        albumId = 2L,
                        artistId = 5L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = null
                    ),
                    AudioTrack(
                        uri = "https://audius.co/stream/romance_2",
                        id = 202L,
                        title = "Tum Mile",
                        artist = "Neeraj Shridhar, Pritam",
                        album = "00s Bollywood Romance",
                        durationMs = 344_000L,
                        albumId = 2L,
                        artistId = 6L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = null
                    ),
                    AudioTrack(
                        uri = "https://audius.co/stream/romance_3",
                        id = 203L,
                        title = "Zara Sa",
                        artist = "KK, Pritam",
                        album = "00s Bollywood Romance",
                        durationMs = 303_000L,
                        albumId = 2L,
                        artistId = 7L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = null
                    )
                )
            ),
            PlaylistUiModel(
                id = "punjabi_beats",
                title = "Punjabi Beats & Bass",
                subtitle = "Karan Aujla, Diljit Dosanjh, AP Dhillon...",
                description = "High octane Punjabi urban pop, UK drill, and Chandigarh drive anthems.",
                coverUri = "file:///android_asset/album_punjabi_beats_1793701777979.jpg",
                tracks = listOf(
                    AudioTrack(
                        uri = "https://audius.co/stream/punjabi_1",
                        id = 301L,
                        title = "Winning Speech",
                        artist = "Karan Aujla, Mxrci",
                        album = "Punjabi Beats & Bass",
                        durationMs = 212_000L,
                        albumId = 3L,
                        artistId = 8L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = null
                    ),
                    AudioTrack(
                        uri = "https://audius.co/stream/punjabi_2",
                        id = 302L,
                        title = "Lover",
                        artist = "Diljit Dosanjh, Intense",
                        album = "Punjabi Beats & Bass",
                        durationMs = 191_000L,
                        albumId = 3L,
                        artistId = 9L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = null
                    )
                )
            ),
            PlaylistUiModel(
                id = "bangla_indie",
                title = "Bangla Indie & Folk",
                subtitle = "Coke Studio Bangla, Anupam Roy...",
                description = "Soulful Bengali folk acoustic, modern indie rock, and unplugged melodies.",
                coverUri = "file:///android_asset/album_bangla_indie_1793701779257.jpg",
                tracks = listOf(
                    AudioTrack(
                        uri = "https://audius.co/stream/bangla_1",
                        id = 401L,
                        title = "Shob Loke Koy",
                        artist = "Kaniz Khandaker, Coke Studio Bangla",
                        album = "Bangla Indie & Folk",
                        durationMs = 265_000L,
                        albumId = 4L,
                        artistId = 10L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = null
                    )
                )
            ),
            PlaylistUiModel(
                id = "bollywood_lofi",
                title = "Bollywood Lo-Fi Chill",
                subtitle = "Late Night Hindi Lo-Fi & Chillhop",
                description = "Aesthetic late night monsoon chill mixes with tape saturation and vinyl hiss.",
                coverUri = "file:///android_asset/album_bollywood_lofi_1793701777433.jpg",
                tracks = listOf(
                    AudioTrack(
                        uri = "https://audius.co/stream/lofi_1",
                        id = 501L,
                        title = "Raabta (Lo-Fi Rework)",
                        artist = "Arijit Singh, Vynora Beats",
                        album = "Bollywood Lo-Fi Chill",
                        durationMs = 210_000L,
                        albumId = 5L,
                        artistId = 1L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = null
                    )
                )
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000)),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // 1. YouTube Music Style Top Bar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF4C1D27),
                                Color(0xFF261924),
                                Color(0xFF121622),
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
                    // Left: Red Music Icon + Brand Typography
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = Color(0xFFFF0000)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "Music",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Right: Notification Bell, Search, Red User Avatar
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

                        // Red Profile Avatar
                        Surface(
                            modifier = Modifier.size(30.dp),
                            shape = CircleShape,
                            color = Color(0xFFD32F2F)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.Person,
                                    contentDescription = "Account",
                                    tint = Color.White,
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
                            onFilterLocalInstant?.invoke(it)
                            if (it.length > 2) onSearchVideos(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search songs, albums, artists...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.White) }
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Mood Filter Pills (Horizontal Scroll matching Screenshot 2)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(moodPills) { mood ->
                        val isSelected = selectedMood == mood
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    selectedMood = if (isSelected) null else mood
                                    onSearchVideos("$mood songs official")
                                },
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) Color.White else Color(0x38FFFFFF),
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(0.8.dp, Color(0x22FFFFFF))
                        ) {
                            Text(
                                text = mood,
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

        // 2. Section 1: "Indian Music" Carousel (Matches Image 2)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Indian Music",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Spacer(Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(curatedPlaylists) { playlist ->
                        Column(
                            modifier = Modifier
                                .width(156.dp)
                                .clickable { onPlaylistSelected(playlist) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(156.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E1E1E))
                            ) {
                                AsyncImage(
                                    model = playlist.coverUri,
                                    contentDescription = playlist.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Small translucent play circle in top-left
                                Surface(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(24.dp)
                                        .align(Alignment.TopStart),
                                    shape = CircleShape,
                                    color = Color(0x66000000)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Rounded.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = playlist.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(Modifier.height(2.dp))

                            Text(
                                text = playlist.subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = Color(0xFFAAAAAA),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // 3. Section 2: "Get started" Carousel (Matches Image 2)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Get started",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Spacer(Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    item {
                        GetStartedCard(
                            compositeImageUri = "file:///android_asset/card_my_mix_01.jpg",
                            title = "My Mix 01",
                            subtitle = "Try our perfect playlist for you",
                            bgColor = Color(0xFF223547),
                            onClick = { onPlaylistSelected(curatedPlaylists[0]) }
                        )
                    }
                    item {
                        GetStartedCard(
                            compositeImageUri = "file:///android_asset/card_sam_smith_swipe.jpg",
                            title = "Discovery Swipe",
                            subtitle = "Find new music with a swipe",
                            bgColor = Color(0xFF4A252E),
                            onClick = { onPlaylistSelected(curatedPlaylists[1]) }
                        )
                    }
                    item {
                        GetStartedCard(
                            compositeImageUri = null,
                            title = "Indie Beats",
                            subtitle = "Acoustic coffeehouse & late night sessions",
                            imageUri = "file:///android_asset/album_bangla_indie_1793701779257.jpg",
                            bgColor = Color(0xFF28283E),
                            onClick = { onPlaylistSelected(curatedPlaylists[2]) }
                        )
                    }
                }
            }
        }

        // 4. Section 3: "Quick picks" Track List
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Quick picks",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Start radio from a song",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFAAAAAA)
                )

                Spacer(Modifier.height(12.dp))

                curatedPlaylists[0].tracks.take(4).forEach { track ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTrackSelected(track) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF222222))
                        ) {
                            AsyncImage(
                                model = track.artworkUri ?: curatedPlaylists[0].coverUri,
                                contentDescription = track.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${track.artist} • ${track.durationLabel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFAAAAAA),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(onClick = { /* More menu */ }) {
                            Icon(
                                Icons.Rounded.MoreVert,
                                contentDescription = "More",
                                tint = Color(0xFFAAAAAA),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GetStartedCard(
    compositeImageUri: String? = null,
    title: String = "",
    subtitle: String = "",
    imageUri: String = "",
    bgColor: Color = Color(0xFF222222),
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .height(230.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!compositeImageUri.isNullOrBlank()) {
                AsyncImage(
                    model = compositeImageUri,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(Color(0xFF1E1E1E))
                    ) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = Color(0xFFDDDDDD),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Small translucent play circle in top-left (Matches Image 2!)
            Surface(
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp)
                    .align(Alignment.TopStart),
                shape = CircleShape,
                color = Color(0x66000000)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
