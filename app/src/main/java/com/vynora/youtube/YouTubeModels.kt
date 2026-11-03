package com.vynora.youtube

data class YouTubeItem(
    val id: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val description: String,
    val isPlaylist: Boolean = false,
    val publishedAt: String = "",
    val releaseYear: String? = null,
    val isOfficialChannel: Boolean = false
) {
    val uploadDateFormatted: String
        get() = publishedAt.takeIf { it.length >= 10 }?.substring(0, 10) ?: "Recent"

    val releaseVsUploadLabel: String
        get() = buildString {
            if (isOfficialChannel) append("Official · ")
            if (releaseYear != null) {
                append("Release $releaseYear · $uploadDateFormatted")
            } else {
                append("Released $uploadDateFormatted")
            }
        }
}

object YouTubeMusicFilter {
    private val excludedPatterns = listOf(
        "#shorts",
        "#short",
        "whatsapp status",
        "status video",
        "30 sec status",
        "fullscreen status",
        "ringtone",
        "reaction to",
        "teaser reaction",
        "[artist",
        "[singer",
        "artist/singer",
        "singer name]",
        "artist name]",
        "template",
        "type beat",
        "karaoke with lyrics"
    )

    private val officialIndianChannels = listOf(
        "t-series",
        "sony music india",
        "zee music company",
        "saregama",
        "yrf",
        "tips official",
        "universal music india",
        "indie music label",
        "coke studio",
        "arijit singh",
        "speed records",
        "white hill music",
        "svf music",
        "desi melodies",
        "vyrl originals",
        "panorama music",
        "hitz music",
        "play dmf"
    )

    fun cleanHtmlEntities(raw: String): String = raw
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&apos;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .trim()

    fun isGenuineMusicResult(title: String, description: String, channelTitle: String = ""): Boolean {
        val cleanedTitle = cleanHtmlEntities(title)
        if (cleanedTitle.isBlank()) return false
        val combined = "$cleanedTitle $description $channelTitle".lowercase()
        if (excludedPatterns.any { combined.contains(it) }) return false
        return true
    }

    fun isOfficialMusicChannel(channelTitle: String, title: String): Boolean {
        val lowerChannel = channelTitle.lowercase()
        val lowerTitle = title.lowercase()
        return officialIndianChannels.any { lowerChannel.contains(it) } ||
            lowerChannel.endsWith(" - topic") ||
            lowerChannel.contains("vevo") ||
            (lowerTitle.contains("official") && !lowerTitle.contains("["))
    }

    fun extractReleaseYear(title: String, description: String, publishedAt: String): String? {
        val regex = Regex("\\b(202[0-6]|201[5-9])\\b")
        return regex.find(title)?.value
            ?: regex.find(description)?.value
            ?: publishedAt.takeIf { it.length >= 4 }?.substring(0, 4)
    }

    private val embedRestrictedLabels = listOf(
        "t-series",
        "sony music",
        "zee music",
        "saregama",
        "yrf",
        "yash raj",
        "tips official",
        "tips music",
        "speed records",
        "white hill",
        "universal music",
        "svf music",
        "desi melodies",
        "vyrl",
        "panorama music",
        "hitz music",
        "play dmf",
        "venus"
    )

    /**
     * Major Indian labels set embeddable=true in API v3 but block 3P iframe embeds with Error 150/152.
     * Detecting them upfront avoids a 3-4 second failed /embed/ roundtrip when switching songs.
     */
    fun prefersDirectWatchStream(channelTitle: String, title: String): Boolean {
        val combined = "$channelTitle $title".lowercase()
        return embedRestrictedLabels.any { combined.contains(it) }
    }
}
