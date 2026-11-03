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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
    Column(modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Column(Modifier.padding(horizontal = 20.dp).padding(top = 16.dp)) {
                    Text("Discover", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                    Text("Music from YouTube, alongside your offline library", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Search songs, artists, albums") }
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            showPlaylists = false
                            onSearchVideos(query)
                        }) { Text("Search videos") }
                        TextButton(onClick = {
                            showPlaylists = true
                            onSearchPlaylists(query)
                        }) { Text("Albums & playlists") }
                    }
                }
            }
            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF583B83), Color(0xFF1D5262))))
                            .padding(20.dp)
                    ) {
                        Column {
                            Text("YOUR SOUNDTRACK", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelLarge)
                            Text("Find your next favorite", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("Explore music videos, artists and full playlists", color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
            }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Trending music", "Indian music", "Bangla music", "English hits", "Chill music")) { category ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                showPlaylists = false
                                onSearchVideos(
                                    when (category) {
                                        "Indian music" -> "Indian music official video"
                                        "Bangla music" -> "Bangla music official video"
                                        "English hits" -> "English hit songs official music video"
                                        "Chill music" -> "chill relaxing music official"
                                        else -> "trending music official music video"
                                    }
                                )
                            }
                        ) { Text(category, Modifier.padding(horizontal = 16.dp, vertical = 10.dp), fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
            item {
                Text(
                    if (showPlaylists) "Albums & playlists" else "Music videos",
                    Modifier.padding(horizontal = 20.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            if (!state.apiKeyConfigured) {
                item {
                    Surface(
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Text("Connect YouTube", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text("Add a restricted YouTube Data API key as YOUTUBE_API_KEY in local.properties (or the GitHub Actions secret for release builds). The key is intentionally not included in source control.")
                        }
                    }
                }
            } else if (state.isLoading) {
                item { Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            } else if (state.error != null) {
                item {
                    Column(Modifier.padding(horizontal = 20.dp)) {
                        Text(state.error, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = {
                            if (showPlaylists) onSearchPlaylists(state.activeQuery) else onSearchVideos(state.activeQuery)
                        }) { Text("Try again") }
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
                Text(
                    "YouTube content plays in the official embedded player. Offline and background playback remain available for music stored on your device.",
                    Modifier.padding(horizontal = 20.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun YouTubeResultRow(item: YouTubeItem, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onClick).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier.size(width = 132.dp, height = 82.dp).clip(RoundedCornerShape(10.dp))
        )
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(item.title, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(item.channelTitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Text(if (item.isPlaylist) "YouTube playlist" else "YouTube video", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeWatchScreen(item: YouTubeItem, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        TextButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) { Text("← Back to Discover") }
        Box(Modifier.fillMaxWidth().height(240.dp).background(Color.Black), contentAlignment = Alignment.Center) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = true
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
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
                },
                update = { }
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(item.title, Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(item.channelTitle, Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Text(
            "Playback is provided by YouTube. Keep this player visible while watching; YouTube background audio and audio-only extraction are not supported.",
            Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
