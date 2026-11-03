package com.vynora.youtube

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage

@Composable
fun YouTubeHomeScreen(
    state: YouTubeHomeState,
    onSearchVideos: (String) -> Unit,
    onSearchPlaylists: (String) -> Unit,
    onOpen: (YouTubeItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var showPlaylists by remember { mutableStateOf(false) }
    val categories = listOf(
        "Latest Hindi" to "latest Hindi songs",
        "Bollywood" to "new Bollywood Hindi songs",
        "Arijit Singh" to "latest Arijit Singh songs",
        "Punjabi" to "latest Punjabi songs",
        "Romantic" to "new Hindi romantic songs",
        "Bangla" to "latest Bangla songs"
    )
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column(Modifier.padding(top = 14.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    placeholder = { Text("What do you want to play?") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) }
                )
                Spacer(Modifier.height(12.dp))
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { (label, search) ->
                        AssistChip(onClick = { showPlaylists = false; onSearchVideos(search) }, label = { Text(label) })
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Indian Music", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                        Text("Fresh Hindi releases & popular picks", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { showPlaylists = false; onSearchVideos("latest Hindi songs") }) { Text("Refresh") }
                }
                when {
                    !state.apiKeyConfigured -> SetupCard()
                    state.isLoading -> Box(Modifier.fillMaxWidth().height(190.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
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
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Made for your mood", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Explore more music and collections", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { showPlaylists = true; onSearchPlaylists("Hindi Bollywood music playlists") }) {
                        Text("See all")
                    }
                }
                if (showPlaylists && state.isLoading) {
                    Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else if (state.playlists.isNotEmpty()) {
                    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(state.playlists, key = { it.id }) { item ->
                            MusicArtworkCard(item = item, onClick = { onOpen(item) }, playlist = true)
                        }
                    }
                } else {
                    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(categories.take(4)) { (label, search) ->
                            MoodCard(label = label, onClick = { showPlaylists = false; onSearchVideos(search) })
                        }
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Latest releases", Modifier.padding(horizontal = 20.dp),
                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                if (state.videos.isNotEmpty()) {
                    state.videos.take(8).forEach { item ->
                        CompactTrackRow(item = item, onClick = { onOpen(item) })
                    }
                }
            }
        }
        item {
            Text("Playback is provided by the official YouTube player. Local music remains available offline.",
                Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MusicArtworkCard(item: YouTubeItem, onClick: () -> Unit, playlist: Boolean = false) {
    Column(Modifier.width(210.dp).clickable(onClick = onClick), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(22.dp))) {
            AsyncImage(model = item.thumbnailUrl, contentDescription = item.title,
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .62f)))))
            FilledIconButton(
                onClick = onClick,
                modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                shape = CircleShape
            ) { Icon(Icons.Rounded.PlayArrow, contentDescription = "Play") }
            Surface(
                modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
                color = Color.Black.copy(alpha = .55f), shape = RoundedCornerShape(50)
            ) {
                Text(if (playlist || item.isPlaylist) "PLAYLIST" else "NEW MUSIC",
                    Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
        Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
            maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(item.channelTitle, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun MoodCard(label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(150.dp).height(112.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(Modifier.fillMaxSize().background(
            Brush.linearGradient(listOf(Color(0xFF39295D), Color(0xFF24505A)))
        ).padding(16.dp), contentAlignment = Alignment.BottomStart) {
            Text(label, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CompactTrackRow(item: YouTubeItem, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(model = item.thumbnailUrl, contentDescription = null,
            modifier = Modifier.size(58.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(item.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
            Text(item.channelTitle, maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun SetupCard() {
    Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Text("Connect YouTube API to discover music.", Modifier.padding(18.dp))
    }
}

@Composable
private fun ErrorCard(message: String, retry: () -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        Text(message, color = MaterialTheme.colorScheme.error)
        TextButton(onClick = retry) { Text("Try again") }
    }
}

@Composable
private fun EmptyCard() {
    Text("No music found. Try refreshing or searching.", Modifier.padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeWatchScreen(item: YouTubeItem, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val playerWebView = remember { mutableStateOf<WebView?>(null) }
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
    Column(modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            FilledTonalButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = null)
                Spacer(Modifier.size(6.dp))
                Text("Back")
            }
            Spacer(Modifier.size(12.dp))
            Text("NOW PLAYING", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        AndroidView(
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(24.dp)).background(Color.Black),
            factory = { context ->
                WebView(context).apply {
                    playerWebView.value = this
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = true
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    settings.loadsImagesAutomatically = true
                    settings.javaScriptCanOpenWindowsAutomatically = false
                    webChromeClient = WebChromeClient()
                    webViewClient = WebViewClient()
                    setBackgroundColor(android.graphics.Color.BLACK)
                    val videoId = item.id
                    val source = if (item.isPlaylist) {
                        "https://www.youtube.com/embed/videoseries?list=$videoId&playsinline=1&enablejsapi=1&origin=https%3A%2F%2Fappassets.androidplatform.net"
                    } else {
                        "https://www.youtube.com/embed/$videoId?playsinline=1&enablejsapi=1&origin=https%3A%2F%2Fappassets.androidplatform.net"
                    }
                    val html = """
                        <!doctype html><html><head>
                        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
                        <meta name="referrer" content="strict-origin-when-cross-origin">
                        <style>html,body{margin:0;padding:0;background:#000;width:100%;height:100%;overflow:hidden}iframe{border:0;width:100%;height:100%}</style>
                        </head><body>
                        <iframe src="$source" title="YouTube player" referrerpolicy="strict-origin-when-cross-origin"
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                        allowfullscreen></iframe>
                        </body></html>
                    """.trimIndent()
                    // appassets origin provides a stable HTTPS referrer for YouTube's embedded-player client checks.
                    loadDataWithBaseURL("https://appassets.androidplatform.net/", html, "text/html", "UTF-8", null)
                }
            }
        )
        Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("NOW PLAYING", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(item.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(item.channelTitle, style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Official YouTube playback", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
