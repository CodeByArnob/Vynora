package com.vynora.library

data class TrackGroup(
    val name: String,
    val tracks: List<IndexedValue<AudioTrack>>
)

object LibraryGrouping {
    fun group(
        indexedTracks: List<IndexedValue<AudioTrack>>,
        mode: String
    ): List<TrackGroup> {
        val grouped = when (mode) {
            "Albums" -> indexedTracks.groupBy { "${it.value.album} • ${it.value.artist}" }
            "Artists" -> indexedTracks.groupBy { it.value.artist }
            "Folders" -> indexedTracks.groupBy {
                it.value.relativePath
                    ?.trimEnd('/')
                    ?.substringAfterLast('/')
                    ?.takeIf(String::isNotBlank) ?: "Unknown folder"
            }
            else -> emptyMap()
        }
        return grouped.toSortedMap(String.CASE_INSENSITIVE_ORDER)
            .map { (name, tracks) -> TrackGroup(name, tracks) }
    }
}
