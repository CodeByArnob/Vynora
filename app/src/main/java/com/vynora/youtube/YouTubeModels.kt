package com.vynora.youtube

data class YouTubeItem(
    val id: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val description: String,
    val isPlaylist: Boolean = false,
    val publishedAt: String = "",
    val releaseYear: String? = null
) {
    val uploadDateFormatted: String
        get() = publishedAt.takeIf { it.length >= 10 }?.substring(0, 10) ?: "Recent upload"

    val releaseVsUploadLabel: String
        get() = if (releaseYear != null) {
            "Release $releaseYear · Uploaded $uploadDateFormatted"
        } else {
            "Uploaded $uploadDateFormatted"
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
        "teaser reaction"
    )

    fun isGenuineMusicResult(title: String, description: String): Boolean {
        val combined = "$title $description".lowercase()
        if (excludedPatterns.any { combined.contains(it) }) return false
        return title.isNotBlank()
    }

    fun extractReleaseYear(title: String, description: String, publishedAt: String): String? {
        val regex = Regex("\\b(202[0-6]|201[5-9])\\b")
        return regex.find(title)?.value
            ?: regex.find(description)?.value
            ?: publishedAt.takeIf { it.length >= 4 }?.substring(0, 4)
    }
}
