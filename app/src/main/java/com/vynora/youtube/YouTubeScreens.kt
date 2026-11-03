package com.vynora.youtube

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(Modifier.padding(horizontal = 20.dp).padding(top = 18.dp)) {
                Text("Discover", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("Fresh music, picked for your mood", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(18.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(22.dp),
                    placeholder = { Text("Search songs, artists or albums") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) }
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showPlaylists = false; onSearchVideos(query.ifBlank { "latest Hindi songs" }) }) {
                        Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Search songs")
                    }
                    FilledTonalButton(onClick = { showPlaylists = true; onSearchPlaylists(query.ifBlank { "latest Hindi music albums" }) }) {
                        Text("Playlists")
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Box(Modifier.fillMaxWidth().background(
                    Brush.linearGradient(listOf(Color(0xFF3D2B70), Color(0xFF754B87), Color(0xFF315B68)))
                ).padding(22.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(color = Color.White.copy(alpha = .16f), shape = CircleShape) {
                            Text("NEW & TRENDING", Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                        Text("Latest Hindi hits", style = MaterialTheme.typography.headlineMedium,
                            color = Color.White, fontWeight = FontWeight.ExtraBold)
                        Text("Discover the newest Bollywood releases and music from India.",
                            color = Color.White.copy(alpha = .88f), style = MaterialTheme.typography.bodyMedium)
                        FilledTonalButton(onClick = { showPlaylists = false; onSearchVideos("latest Hindi songs") }) {
                            Text("Explore new songs")
                        }
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Explore genres", Modifier.padding(horizontal = 20.dp),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { (label, search) ->
                        AssistChip(onClick = { showPlaylists = false; onSearchVideos(search) }, label = { Text(label) })
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (showPlaylists) "Hindi albums & playlists" else "Latest Hindi songs",
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(if (showPlaylists) "Curated collections from YouTube" else "Newest uploads first · India",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = { showPlaylists = false; onSearchVideos("latest Hindi songs") }) { Text("Refresh") }
            }
        }
        if (!state.apiKeyConfigured) {
            item {
                Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Connect YouTube", fontWeight = FontWeight.Bold)
                        Text("Add YOUTUBE_API_KEY in GitHub Actions secrets or local.properties.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else if (state.isLoading) {
            item { Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        } else if (state.error != null) {
            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Text(state.error, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { if (showPlaylists) onSearchPlaylists(state.activeQuery) else onSearchVideos(state.activeQuery) }) {
                        Text("Try again")
                    }
                }
            }
        } else {
            val results = if (showPlaylists) state.playlists else state.videos
            if (results.isEmpty()) {
                item { Text("No results yet. Search for an artist, song or album.", Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(results, key = { "${if (it.isPlaylist) "p" else "v"}-${it.id}" }) { item ->
                    YouTubeResultRow(item = item, onClick = { onOpen(item) })
                }
            }
        }
        item {
            Text("Videos play through YouTube's official player. Your offline music library stays available anytime.",
                Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun YouTubeResultRow(item: YouTubeItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(width = 124.dp, height = 82.dp)) {
                AsyncImage(model = item.thumbnailUrl, contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)))
                Surface(Modifier.align(Alignment.Center), color = Color.Black.copy(alpha = .68f), shape = CircleShape) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", tint = Color.White,
                        modifier = Modifier.padding(5.dp).size(22.dp))
                }
            }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(item.title, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(5.dp))
                Text(item.channelTitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Text(if (item.isPlaylist) "PLAYLIST" else "MUSIC • VIDEO",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold)
            }
        }
    }
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
                Text("Discover")
            }
            Spacer(Modifier.size(12.dp))
            Text("NOW PLAYING", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        AndroidView(
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(20.dp)).background(Color.Black),
            factory = { context ->
                WebView(context).apply {
                    playerWebView.value = this
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = true
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    settings.loadsImagesAutomatically = true
                    webChromeClient = WebChromeClient()
                    val source = if (item.isPlaylist) {
                        "https://www.youtube.com/embed/videoseries?list=${item.id}&playsinline=1"
                    } else {
                        "https://www.youtube.com/embed/${item.id}?playsinline=1"
                    }
                    val html = """
                        <!doctype html><html><head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
                        <style>html,body{margin:0;padding:0;background:#000;width:100%;height:100%;overflow:hidden}iframe{border:0;width:100%;height:100%}</style>
                        </head><body>
                        <iframe src="$source" title="YouTube player" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>
                        </body></html>
                    """.trimIndent()
                    loadDataWithBaseURL("https://www.youtube.com/", html, "text/html", "UTF-8", null)
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
