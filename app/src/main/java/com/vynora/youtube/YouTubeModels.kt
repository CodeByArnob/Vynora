package com.vynora.youtube

data class YouTubeItem(
    val id: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val description: String,
    val isPlaylist: Boolean = false
)
