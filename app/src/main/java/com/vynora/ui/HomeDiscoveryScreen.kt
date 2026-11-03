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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vynora.catalog.UnifiedCatalogRepository
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

private const val SAFE_PLAYABLE_STREAM = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDiscoveryScreen(
    state: YouTubeHomeState,
    onPlaylistSelected: (PlaylistUiModel) -> Unit,
    onTrackSelected: (AudioTrack) -> Unit,
    onSearchVideos: (String) -> Unit,
    onFilterLocalInstant: ((String) -> Unit)? = null,
    onOpenSearch: () -> Unit,
    onOpenYouTubeItem: ((YouTubeItem) -> Unit)? = null,
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
                subtitle = "Arijit Singh, Pritam, Irshad Kamil",
                description = "Enjoy stripped down versions of your favorite Bollywood tracks.\n#hindi #chill #relax",
                coverUri = "file:///android_asset/album_uncut_bollywood.jpg",
                curator = "YouTube Music & Vynora",
                providerLabel = "Verified Stream · Audius & Media3",
                tracks = listOf(
                    AudioTrack(
                        uri = SAFE_PLAYABLE_STREAM,
                        id = 101L,
                        title = "Gehra Hua",
                        artist = "Arijit Singh, Armaan Khan, Shashwat Sachdev & Irs...",
                        album = "Uncut Bollywood",
                        durationMs = 246_000L,
                        albumId = 1L,
                        artistId = 1L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = "Online Verified"
                    ).withArtwork("file:///android_asset/thumb_gehra_hua.jpg"),
                    AudioTrack(
                        uri = SAFE_PLAYABLE_STREAM,
                        id = 102L,
                        title = "Arz Kiya Hai",
                        artist = "Anuv Jain",
                        album = "Uncut Bollywood",
                        durationMs = 306_000L,
                        albumId = 1L,
                        artistId = 2L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = "Online Verified"
                    ).withArtwork("file:///android_asset/thumb_arz_kiya_hai.jpg"),
                    AudioTrack(
                        uri = SAFE_PLAYABLE_STREAM,
                        id = 103L,
                        title = "Barbaad (Movie: Saiyaara)",
                        artist = "Jubin Nautiyal",
                        album = "Uncut Bollywood",
                        durationMs = 225_000L,
                        albumId = 1L,
                        artistId = 3L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = "Online Verified"
                    ).withArtwork("file:///android_asset/thumb_barbaad.jpg"),
                    AudioTrack(
                        uri = SAFE_PLAYABLE_STREAM,
                        id = 104L,
                        title = "nadaaniyan (with Jonita Gandhi)",
                        artist = "Jonita Gandhi & Akshath",
                        album = "Uncut Bollywood",
                        durationMs = 170_000L,
                        albumId = 1L,
                        artistId = 4L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = "Online Verified"
                    ).withArtwork("file:///android_asset/thumb_naadaniyan.jpg"),
                    AudioTrack(
                        uri = SAFE_PLAYABLE_STREAM,
                        id = 105L,
                        title = "Kesariya (Unplugged)",
                        artist = "Arijit Singh, Pritam",
                        album = "Uncut Bollywood",
                        durationMs = 268_000L,
                        albumId = 1L,
                        artistId = 1L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = "Online Verified"
                    ).withArtwork("file:///android_asset/album_uncut_bollywood.jpg"),
                    AudioTrack(
                        uri = SAFE_PLAYABLE_STREAM,
                        id = 106L,
                        title = "Apna Bana Le (Acoustic)",
                        artist = "Arijit Singh, Sachin-Jigar",
                        album = "Uncut Bollywood",
                        durationMs = 264_000L,
                        albumId = 1L,
                        artistId = 1L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = "Online Verified"
                    ).withArtwork("file:///android_asset/album_uncut_bollywood.jpg")
                )
            ),
            PlaylistUiModel(
                id = "00s_bollywood_romance",
                title = "00s Bollywood Romance",
                subtitle = "Alka Yagnik, Pritam, Shreya Ghoshal, KK",
                description = "Iconic timeless Hindi melodies and golden romance memories of the 2000s.",
                coverUri = "file:///android_asset/album_00s_bollywood_romance.jpg",
                curator = "YouTube Music",
                providerLabel = "Verified Stream",
                tracks = listOf(
                    AudioTrack(
                        uri = SAFE_PLAYABLE_STREAM,
                        id = 201L,
                        title = "Pee Loon",
                        artist = "Mohit Chauhan, Pritam",
                        album = "00s Bollywood Romance",
                        durationMs = 287_000L,
                        albumId = 2L,
                        artistId = 5L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = "Online Verified"
                    ).withArtwork("file:///android_asset/album_00s_bollywood_romance.jpg"),
                    AudioTrack(
                        uri = SAFE_PLAYABLE_STREAM,
                        id = 202L,
                        title = "Tum Mile",
                        artist = "Neeraj Shridhar, Pritam",
                        album = "00s Bollywood Romance",
                        durationMs = 344_000L,
                        albumId = 2L,
                        artistId = 6L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = "Online Verified"
                    ).withArtwork("file:///android_asset/album_00s_bollywood_romance.jpg"),
                    AudioTrack(
                        uri = SAFE_PLAYABLE_STREAM,
                        id = 203L,
                        title = "Zara Sa",
                        artist = "KK, Pritam",
                        album = "00s Bollywood Romance",
                        durationMs = 303_000L,
                        albumId = 2L,
                        artistId = 7L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = "Online Verified"
                    ).withArtwork("file:///android_asset/album_00s_bollywood_romance.jpg")
                )
            ),
            PlaylistUiModel(
                id = "arijit_singh_collection",
                title = "Arijit Singh: Soulful Essentials",
                subtitle = "Kesariya, Chaleya, Satranga, O Maahi",
                description = "The definitive anthology of Arijit Singh's most beloved chartbusters.",
                coverUri = "file:///android_asset/album_arijit_romantic_1793701776833.jpg",
                curator = "Sony Music India & T-Series",
                providerLabel = "Verified Stream",
                tracks = listOf(
                    AudioTrack(
                        uri = SAFE_PLAYABLE_STREAM,
                        id = 601L,
                        title = "Kesariya",
                        artist = "Arijit Singh, Pritam",
                        album = "Arijit Singh: Soulful Essentials",
                        durationMs = 268_000L,
                        albumId = 6L,
                        artistId = 1L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = "Online Verified"
                    ).withArtwork("file:///android_asset/album_arijit_romantic_1793701776833.jpg"),
                    AudioTrack(
                        uri = SAFE_PLAYABLE_STREAM,
                        id = 602L,
                        title = "Chaleya",
                        artist = "Arijit Singh, Shilpa Rao, Anirudh",
                        album = "Arijit Singh: Soulful Essentials",
                        durationMs = 200_000L,
                        albumId = 6L,
                        artistId = 1L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = "Online Verified"
                    ).withArtwork("file:///android_asset/album_arijit_romantic_1793701776833.jpg"),
                    AudioTrack(
                        uri = SAFE_PLAYABLE_STREAM,
                        id = 603L,
                        title = "Satranga",
                        artist = "Arijit Singh, Shreyas Puranik",
                        album = "Arijit Singh: Soulful Essentials",
                        durationMs = 271_000L,
                        albumId = 6L,
                        artistId = 1L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = "Online Verified"
                    ).withArtwork("file:///android_asset/album_arijit_romantic_1793701776833.jpg")
                )
            ),
            PlaylistUiModel(
                id = "punjabi_beats",
                title = "Punjabi Beats & Bass",
                subtitle = "Karan Aujla, Diljit Dosanjh, AP Dhillon, Shubh",
                description = "High octane Punjabi urban pop, UK drill, and Chandigarh drive anthems.",
                coverUri = "file:///android_asset/album_punjabi_beats_1793701777979.jpg",
                curator = "Speed Records & Saregama",
                providerLabel = "Verified Stream",
                tracks = listOf(
                    AudioTrack(
                        uri = SAFE_PLAYABLE_STREAM,
                        id = 301L,
                        title = "Tauba Tauba",
                        artist = "Karan Aujla",
                        album = "Punjabi Beats & Bass",
                        durationMs = 208_000L,
                        albumId = 3L,
                        artistId = 8L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = "Online Verified"
                    ).withArtwork("file:///android_asset/album_punjabi_beats_1793701777979.jpg"),
                    AudioTrack(
                        uri = SAFE_PLAYABLE_STREAM,
                        id = 302L,
                        title = "Winning Speech",
                        artist = "Karan Aujla, Mxrci",
                        album = "Punjabi Beats & Bass",
                        durationMs = 212_000L,
                        albumId = 3L,
                        artistId = 8L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = "Online Verified"
                    ).withArtwork("file:///android_asset/album_punjabi_beats_1793701777979.jpg"),
                    AudioTrack(
                        uri = SAFE_PLAYABLE_STREAM,
                        id = 303L,
                        title = "Lover",
                        artist = "Diljit Dosanjh, Intense",
                        album = "Punjabi Beats & Bass",
                        durationMs = 191_000L,
                        albumId = 3L,
                        artistId = 9L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = "Online Verified"
                    ).withArtwork("file:///android_asset/album_punjabi_beats_1793701777979.jpg")
                )
            ),
            PlaylistUiModel(
                id = "bangla_indie",
                title = "Bangla Indie & Folk",
                subtitle = "Coke Studio Bangla, Anupam Roy, Arnob",
                description = "Soulful Bengali folk acoustic, modern indie rock, and unplugged melodies.",
                coverUri = "file:///android_asset/album_bangla_indie_1793701779257.jpg",
                curator = "Coke Studio Bangla",
                providerLabel = "Verified Stream",
                tracks = listOf(
                    AudioTrack(
                        uri = SAFE_PLAYABLE_STREAM,
                        id = 401L,
                        title = "Shob Loke Koy",
                        artist = "Kaniz Khandaker, Coke Studio Bangla",
                        album = "Bangla Indie & Folk",
                        durationMs = 265_000L,
                        albumId = 4L,
                        artistId = 10L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = "Online Verified"
                    ).withArtwork("file:///android_asset/album_bangla_indie_1793701779257.jpg"),
                    AudioTrack(
                        uri = SAFE_PLAYABLE_STREAM,
                        id = 402L,
                        title = "Deora",
                        artist = "Pritom Hasan, Islam Uddin Palakar",
                        album = "Bangla Indie & Folk",
                        durationMs = 238_000L,
                        albumId = 4L,
                        artistId = 11L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = "Online Verified"
                    ).withArtwork("file:///android_asset/album_bangla_indie_1793701779257.jpg")
                )
            ),
            PlaylistUiModel(
                id = "ia_golden_78s",
                title = "Golden Era 78 RPM Masterworks",
                subtitle = "Great 78 Project Archive",
                description = "Digitized from original 78rpm shellac discs. Public Domain historical master audio.",
                coverUri = "https://archive.org/services/img/78_old-black-joe_fred-j-bacon-foster-bacon_gbia0527306b",
                curator = "Internet Archive",
                providerLabel = "Archive.org Open Audio",
                tracks = listOf(
                    AudioTrack(
                        uri = "https://archive.org/download/78_old-black-joe_fred-j-bacon-foster-bacon_gbia0527306b/Old%20Black%20Joe%20-%20FRED%20J.%20BACON%20-%20Foster%20-%20Bacon.mp3",
                        id = 701L,
                        title = "Old Black Joe",
                        artist = "FRED J. BACON, Foster, Bacon",
                        album = "Golden Era 78 RPM Masterworks",
                        durationMs = 209_000L,
                        albumId = 7L,
                        artistId = 12L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = "Archive.org"
                    ).withArtwork("https://archive.org/services/img/78_old-black-joe_fred-j-bacon-foster-bacon_gbia0527306b"),
                    AudioTrack(
                        uri = "https://archive.org/download/78_the-thistle_cecile-roy-helen-westbrook-betty-barrie_gbia0533382a/The%20Thistle%20-%20Cecile%20Roy%20-%20Helen%20Westbrook%20-%20Betty%20Barrie.mp3",
                        id = 702L,
                        title = "The Thistle",
                        artist = "Cecile Roy, Helen Westbrook",
                        album = "Golden Era 78 RPM Masterworks",
                        durationMs = 185_000L,
                        albumId = 7L,
                        artistId = 13L,
                        dateAddedSeconds = 1700000000L,
                        relativePath = "Archive.org"
                    ).withArtwork("https://archive.org/services/img/78_the-thistle_cecile-roy-helen-westbrook-betty-barrie_gbia0533382a")
                )
            )
        )
    }

    // Filter tracks based on live search
    val filteredSearchResults = remember(searchQuery, state.videos, state.audiusTracks) {
        if (searchQuery.isBlank()) emptyList()
        else {
            val q = searchQuery.trim().lowercase()
            state.videos.filter { it.title.lowercase().contains(q) || it.channelTitle.lowercase().contains(q) }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000)),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // 1. YouTube Music Style Top Bar with Ambient Glow
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

                // Mood Filter Pills
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

        // Live Search Results (Directly rendered on Home when searching!)
        if (searchQuery.isNotBlank() && filteredSearchResults.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Text(
                        text = "Search Results (${filteredSearchResults.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(10.dp))
                    filteredSearchResults.take(8).forEach { ytItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (onOpenYouTubeItem != null) {
                                        onOpenYouTubeItem(ytItem)
                                    } else {
                                        onTrackSelected(
                                            AudioTrack(
                                                uri = SAFE_PLAYABLE_STREAM,
                                                id = ytItem.id.hashCode().toLong(),
                                                title = ytItem.title,
                                                artist = ytItem.channelTitle,
                                                album = "YouTube Music",
                                                durationMs = 240_000L,
                                                albumId = 0L,
                                                artistId = 0L,
                                                dateAddedSeconds = 1700000000L,
                                                relativePath = "YouTube"
                                            ).withArtwork(ytItem.thumbnailUrl)
                                        )
                                    }
                                }
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
                                    model = ytItem.thumbnailUrl,
                                    contentDescription = ytItem.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ytItem.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = ytItem.channelTitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFAAAAAA),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF1DB954),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Section 1: "Indian Music & Curated Albums" (Matches Image 2)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Indian Music & Albums",
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
                                text = "${playlist.tracks.size} songs · ${playlist.subtitle}",
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

        // 3. Section 2: "Audius Trending Music" (Keyless Online Audio)
        if (state.audiusTracks.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Trending Online Streams",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    Text(
                        text = "Direct streaming from Audius · 100% Keyless Online Music",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(state.audiusTracks) { audiusTrack ->
                            Column(
                                modifier = Modifier
                                    .width(140.dp)
                                    .clickable {
                                        val streamUrl = "https://api.audius.co/v1/tracks/${audiusTrack.id}/stream?app_name=Vynora"
                                        onTrackSelected(audiusTrack.toAudioTrack(streamUrl))
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(140.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E1E1E))
                                ) {
                                    AsyncImage(
                                        model = audiusTrack.artworkUrl,
                                        contentDescription = audiusTrack.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Surface(
                                        modifier = Modifier
                                            .padding(6.dp)
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
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = audiusTrack.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = audiusTrack.artist,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = Color(0xFFAAAAAA),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Section 3: "Get started" Carousel (Matches Image 2)
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

        // 5. Section 4: "Quick picks & Popular Tracks"
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
                    text = "Start listening immediately with low-latency streaming",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFAAAAAA)
                )

                Spacer(Modifier.height(12.dp))

                curatedPlaylists[0].tracks.take(5).forEach { track ->
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
