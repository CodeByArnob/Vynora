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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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

private val categories = listOf(
    "Latest Hindi" to "latest Hindi songs 2026 official video",
    "Bollywood" to "new Bollywood songs 2026 official video",
    "Punjabi" to "latest Punjabi songs 2026 official video",
    "Arijit Singh" to "Arijit Singh latest Hindi songs official",
    "Romantic" to "Hindi romantic songs new release",
    "Bangla" to "latest Bangla modern songs 2026",
    "Trending" to "trending Indian music videos 2026",
    "Indian Lo-Fi" to "Bollywood Hindi lofi chill mix"
)

@Composable
fun YouTubeHomeScreen(
    state: YouTubeHomeState,
    onSearchVideos: (String) -> Unit,
    onSearchPlaylists: (String) -> Unit,
    onOpen: (YouTubeItem) -> Unit,
    onPlayAudiusTrack: ((AudiusTrack) -> Unit)? = null,
    onSearchAudius: ((String) -> Unit)? = null,
    onToggleFavoriteVideo: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var selectedChip by remember { mutableStateOf(categories.first().first) }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(Color(0xFF0A0D12)),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF143526), Color(0xFF0A0D12))
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Good Evening · Vynora",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "Latest Hindi Releases · Bollywood · Punjabi · Bangla · Audius",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        FilledTonalIconButton(onClick = {
                            onSearchVideos("latest Hindi songs 2026 official video")
                        }) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Refresh latest Hindi songs")
                        }
                    }

                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search Hindi, Bollywood, Arijit Singh, Punjabi, Audius...") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotBlank()) {
                                TextButton(onClick = {
                                    onSearchVideos(query)
                                    onSearchAudius?.invoke(query)
                                }) {
                                    Text("Search")
                                }
                            }
                        }
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { (label, searchQuery) ->
                            FilterChip(
                                selected = selectedChip == label,
                                onClick = {
                                    selectedChip = label
                                    onSearchVideos(searchQuery)
                                },
                                label = { Text(label, maxLines = 1) },
                                leadingIcon = if (selectedChip == label) {
                                    {
                                        Icon(
                                            Icons.Rounded.GraphicEq,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }
            }
        }

        if (state.recentlyPlayed.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Recently Played",
                    subtitle = "Continue watching or listening where you left off"
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
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
                title = "Latest Hindi & Indian Releases",
                subtitle = "Priority India region · Filtered for genuine official music releases",
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
                    contentPadding = PaddingValues(horizontal = 20.dp),
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
                    title = "Audius Online Audio Stream",
                    subtitle = "Direct Media3 background audio streaming powered by Audius API"
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
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
                title = "Browse Indian Music & Moods",
                subtitle = "Bollywood, Punjabi, Arijit Singh, Romantic, Bangla & Playlists",
                actionLabel = "Load Playlists",
                onAction = { onSearchPlaylists("Hindi Bollywood music playlists 2026") }
            )
            Spacer(Modifier.height(10.dp))
            if (state.playlists.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
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
                contentPadding = PaddingValues(horizontal = 20.dp),
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
            item {
                SectionHeader(
                    title = "New Release Tracklist",
                    subtitle = "Showing actual release year alongside channel upload date"
                )
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.videos.take(10).forEach { item ->
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
        Modifier.fillMaxWidth().padding(horizontal = 20.dp),
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
                Text(actionLabel, color = Color(0xFF1DB954))
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
        modifier = Modifier.width(172.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF141923)
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(14.dp))
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
                            tint = Color.Black
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
        modifier = Modifier.width(168.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF141923)
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(14.dp))
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
                            tint = Color.Black
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
                "Audius · Media3 Stream",
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
        modifier = Modifier.width(156.dp).height(88.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF182232)
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
                modifier = Modifier.align(Alignment.BottomEnd).size(26.dp)
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF121722)
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = item.title,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)),
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
                    color = Color(0xFF64748B)
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
    Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Text(
            "Configure YOUTUBE_API_KEY in local.properties or GitHub Actions Secrets to load live music.",
            Modifier.padding(18.dp)
        )
    }
}

@Composable
private fun ErrorCard(message: String, retry: () -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        Text(message, color = MaterialTheme.colorScheme.error)
        TextButton(onClick = retry) { Text("Retry") }
    }
}

@Composable
private fun EmptyCard() {
    Text(
        "No matching embeddable music videos found. Try another search or mood filter.",
        Modifier.padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeWatchScreen(
    item: YouTubeItem,
    onBack: () -> Unit,
    onSkipNext: (() -> Unit)? = null,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val playerWebView = remember { mutableStateOf<WebView?>(null) }
    var playerErrorCode by remember(item.id) { mutableStateOf<Int?>(null) }
    var reloadKey by remember(item.id) { mutableStateOf(0) }

    DisposableEffect(item.id) {
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

    Column(modifier.fillMaxSize().background(Color(0xFF0A0D12))) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = null)
                Spacer(Modifier.size(6.dp))
                Text("Back")
            }
            Spacer(Modifier.weight(1f))
            if (onToggleFavorite != null) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFF1DB954) else Color.White
                    )
                }
            }
            if (onSkipNext != null) {
                IconButton(onClick = onSkipNext) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next track", tint = Color.White)
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .aspectRatio(16f / 9f),
            shape = RoundedCornerShape(20.dp),
            color = Color.Black
        ) {
            Box(Modifier.fillMaxSize()) {
                key(item.id, reloadKey) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            WebView(context).apply {
                                playerWebView.value = this
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
                                setBackgroundColor(android.graphics.Color.BLACK)

                                addJavascriptInterface(
                                    object {
                                        @JavascriptInterface
                                        fun onYouTubeError(code: Int) {
                                            post { playerErrorCode = code }
                                        }
                                    },
                                    "VynoraBridge"
                                )

                                webChromeClient = object : WebChromeClient() {
                                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                        return super.onConsoleMessage(consoleMessage)
                                    }
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

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        if (request?.isForMainFrame == true) {
                                            post { playerErrorCode = -1 }
                                        }
                                    }
                                }

                                val safeId = item.id.replace("'", "")
                                val embedHtml = """
                                    <!DOCTYPE html>
                                    <html>
                                    <head>
                                      <meta charset="utf-8">
                                      <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                      <meta name="referrer" content="strict-origin-when-cross-origin">
                                      <style>
                                        html, body { margin: 0; padding: 0; width: 100%; height: 100%; background: #000; overflow: hidden; }
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
                                            ${if (item.isPlaylist) "playerVars: { listType: 'playlist', list: '$safeId', playsinline: 1, autoplay: 1, rel: 0, modestbranding: 1, origin: 'https://www.youtube.com' }," else "videoId: '$safeId', playerVars: { playsinline: 1, autoplay: 1, rel: 0, modestbranding: 1, origin: 'https://www.youtube.com' },"}
                                            events: {
                                              'onReady': function(e) { try { e.target.playVideo(); } catch (_) {} },
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

                                loadDataWithBaseURL(
                                    "https://www.youtube.com/",
                                    embedHtml,
                                    "text/html",
                                    "UTF-8",
                                    null
                                )
                            }
                        }
                    )
                }

                if (playerErrorCode != null) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xEE0F172A)
                    ) {
                        Column(
                            Modifier.fillMaxSize().padding(20.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                errorTitleForCode(playerErrorCode ?: 150),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                errorDetailForCode(playerErrorCode ?: 150),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(onClick = {
                                    playerErrorCode = null
                                    reloadKey++
                                }) {
                                    Text("Retry Video")
                                }
                                if (onSkipNext != null) {
                                    OutlinedButton(onClick = onSkipNext) {
                                        Text("Skip Track")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Column(
            Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                item.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                item.channelTitle,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF94A3B8)
            )
            Text(
                item.releaseVsUploadLabel,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF1DB954)
            )
        }
    }
}

internal fun errorTitleForCode(code: Int): String = when (code) {
    100 -> "Video unavailable or private (Error 100)"
    101, 150 -> "Owner restricted embedded playback (Error $code)"
    153 -> "Player configuration recovered (Error 153)"
    -1 -> "Network connection interrupted"
    else -> "Playback restricted for this video (Error $code)"
}

internal fun errorDetailForCode(code: Int): String = when (code) {
    100 -> "This video was removed or marked private by the uploader."
    101, 150 -> "The music label disabled third-party embedding for this release. Skip to the next track or stream via Audius."
    153 -> "Origin and third-party cookie policies have been reset. Tap Retry Video or Skip Track."
    -1 -> "Check your internet connection and retry."
    else -> "Unable to play this video inside the embedded player."
}
