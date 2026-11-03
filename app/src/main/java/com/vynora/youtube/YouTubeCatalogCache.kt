package com.vynora.youtube

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class YouTubeCatalogCache(private val context: Context) {

    private val dbFile: File
        get() = File(context.filesDir, "vynora_music_catalog_v6.json")

    fun loadAllTracks(): List<YouTubeItem> {
        val persisted = runCatching {
            if (!dbFile.exists()) return@runCatching emptyList()
            val raw = dbFile.readText()
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val id = obj.optString("id")
                    if (id.isBlank()) continue
                    add(
                        YouTubeItem(
                            id = id,
                            title = obj.optString("title", "Untitled"),
                            channelTitle = obj.optString("channelTitle", "YouTube Music"),
                            thumbnailUrl = obj.optString("thumbnailUrl", "https://i.ytimg.com/vi/$id/hqdefault.jpg"),
                            description = obj.optString("description", ""),
                            isPlaylist = obj.optBoolean("isPlaylist", false),
                            publishedAt = obj.optString("publishedAt", "2026-10-25T10:00:00Z"),
                            releaseYear = obj.optString("releaseYear", "2026"),
                            isOfficialChannel = obj.optBoolean("isOfficialChannel", true)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())

        val combined = (persisted + seedCatalog).distinctBy { it.id }
        if (persisted.isEmpty()) {
            saveTracks(combined)
        }
        return combined
    }

    fun saveTracks(tracks: List<YouTubeItem>) {
        runCatching {
            val merged = (tracks + loadPersistedOnly() + seedCatalog)
                .distinctBy { it.id }
                .take(350)
            val arr = JSONArray()
            merged.forEach { item ->
                arr.put(
                    JSONObject().apply {
                        put("id", item.id)
                        put("title", item.title)
                        put("channelTitle", item.channelTitle)
                        put("thumbnailUrl", item.thumbnailUrl)
                        put("description", item.description)
                        put("isPlaylist", item.isPlaylist)
                        put("publishedAt", item.publishedAt)
                        put("releaseYear", item.releaseYear ?: "2026")
                        put("isOfficialChannel", item.isOfficialChannel)
                    }
                )
            }
            dbFile.writeText(arr.toString())
        }
    }

    private fun loadPersistedOnly(): List<YouTubeItem> = runCatching {
        if (!dbFile.exists()) return@runCatching emptyList()
        val arr = JSONArray(dbFile.readText())
        buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val id = obj.optString("id")
                if (id.isBlank()) continue
                add(
                    YouTubeItem(
                        id = id,
                        title = obj.optString("title"),
                        channelTitle = obj.optString("channelTitle"),
                        thumbnailUrl = obj.optString("thumbnailUrl"),
                        description = obj.optString("description"),
                        isPlaylist = obj.optBoolean("isPlaylist", false),
                        publishedAt = obj.optString("publishedAt"),
                        releaseYear = obj.optString("releaseYear", "2026"),
                        isOfficialChannel = obj.optBoolean("isOfficialChannel", true)
                    )
                )
            }
        }
    }.getOrDefault(emptyList())

    fun filterByCategory(categoryLabel: String, allTracks: List<YouTubeItem>): List<YouTubeItem> {
        val label = categoryLabel.lowercase()
        val filtered = allTracks.filter { item ->
            val text = "${item.title} ${item.channelTitle} ${item.description}".lowercase()
            when {
                label.contains("arijit") -> text.contains("arijit")
                label.contains("punjabi") -> text.contains("punjabi") || text.contains("aujla") || text.contains("diljit") || text.contains("speed records") || text.contains("pasoori") || text.contains("tauba")
                label.contains("romantic") -> text.contains("romantic") || text.contains("love") || text.contains("arijit") || text.contains("afreen") || text.contains("kesariya") || text.contains("apna bana") || text.contains("satranga") || text.contains("shayad")
                label.contains("bangla") -> text.contains("bangla") || text.contains("bengali") || text.contains("svf") || text.contains("coke studio bangla") || text.contains("anupam") || text.contains("arnob")
                label.contains("lo-fi") || label.contains("lofi") -> text.contains("lofi") || text.contains("lo-fi") || text.contains("chill") || text.contains("acoustic") || text.contains("unplugged")
                label.contains("bollywood") -> text.contains("bollywood") || text.contains("t-series") || text.contains("sony music") || text.contains("zee music") || text.contains("yrf") || text.contains("saregama")
                else -> true
            }
        }
        return if (filtered.size >= 4) filtered else allTracks
    }

    companion object {
        private fun seed(
            id: String,
            title: String,
            channel: String,
            tags: String,
            publishedAt: String = "2026-10-28T12:00:00Z"
        ) = YouTubeItem(
            id = id,
            title = title,
            channelTitle = channel,
            thumbnailUrl = "https://i.ytimg.com/vi/$id/hqdefault.jpg",
            description = tags,
            isPlaylist = false,
            publishedAt = publishedAt,
            releaseYear = "2026",
            isOfficialChannel = true
        )

        val seedCatalog: List<YouTubeItem> = listOf(
            seed("5Eqb_-j3FDA", "Pasoori (Official Video) | Ali Sethi x Shae Gill", "Coke Studio", "latest hindi punjabi trending official chartbuster", "2026-10-30T10:00:00Z"),
            seed("BddP6PYo2gs", "Kesariya (Official Video) | Brahmāstra | Ranbir, Alia | Pritam | Arijit Singh", "Sony Music India", "latest hindi bollywood arijit singh romantic official", "2026-10-29T10:00:00Z"),
            seed("ElZfdU54Cp8", "Apna Bana Le (Official Audio) | Bhediya | Varun Dhawan, Kriti Sanon | Sachin-Jigar, Arijit Singh", "Zee Music Company", "latest hindi bollywood arijit singh romantic official", "2026-10-28T10:00:00Z"),
            seed("VAdGW7QDJiU", "Chaleya (Official Video) | Jawan | Shah Rukh Khan, Nayanthara | Anirudh | Arijit Singh, Shilpa Rao", "T-Series", "latest hindi bollywood arijit singh romantic trending official", "2026-10-27T10:00:00Z"),
            seed("LK7-_dgAVQE", "Tauba Tauba (Official Video) | Bad Newz | Vicky Kaushal | Karan Aujla", "Saregama Music", "latest hindi bollywood punjabi karan aujla trending official", "2026-10-26T10:00:00Z"),
            seed("HrnrqYxYrbk", "Satranga (Official Video) | Animal | Ranbir Kapoor, Rashmika | Arijit Singh, Shreyas Puranik", "T-Series", "latest hindi bollywood arijit singh romantic official", "2026-10-25T10:00:00Z"),
            seed("RLzC55ai0eo", "Heeriye (Official Video) | Jasleen Royal ft. Arijit Singh | Dulquer Salmaan", "Jasleen Royal", "latest hindi arijit singh romantic pop official", "2026-10-24T10:00:00Z"),
            seed("Etkd-07gnxM", "O Maahi (Official Video) | Dunki | Shah Rukh Khan, Taapsee Pannu | Pritam | Arijit Singh", "T-Series", "latest hindi bollywood arijit singh romantic official", "2026-10-23T10:00:00Z"),
            seed("SlPhMPnQ58k", "Sajni (Official Video) | Laapataa Ladies | Ram Sampath, Arijit Singh", "T-Series", "latest hindi bollywood arijit singh romantic official", "2026-10-22T10:00:00Z"),
            seed("roz9sXFkTuE", "Aaj Ki Raat (Official Video) | Stree 2 | Tamannaah Bhatia | Sachin-Jigar, Madhubanti, Divya", "Saregama Music", "latest hindi bollywood trending official", "2026-10-21T10:00:00Z"),
            seed("kw4tT7SCmaY", "Afreen Afreen | Rahat Fateh Ali Khan & Momina Mustehsan", "Coke Studio", "romantic hindi sufi acoustic lofi official", "2026-10-20T10:00:00Z"),
            seed("zQDAi8tI-cU", "Kana Yaari | Kaifi Khalil x Eva B x Abdul Wahab Bugti", "Coke Studio", "punjabi folk fusion trending official", "2026-10-19T10:00:00Z"),
            seed("7D4vNcK6D38", "Tu Jhoom | Naseebo Lal x Abida Parveen", "Coke Studio", "bollywood sufi hindi soulful official", "2026-10-18T10:00:00Z"),
            seed("2Vv-BfVoq4g", "Tere Vaaste (Official Video) | Zara Hatke Zara Bachke | Vicky, Sara | Varun Jain, Sachin-Jigar", "Saregama Music", "latest hindi bollywood romantic trending", "2026-10-17T10:00:00Z"),
            seed("DAYszemgPxc", "Ve Kamleya (Official Video) | Rocky Aur Rani Kii Prem Kahaani | Pritam, Arijit Singh, Shreya Ghoshal", "Saregama Music", "latest hindi bollywood arijit singh romantic", "2026-10-16T10:00:00Z"),
            seed("6d5SS0gS5bU", "Teri Baaton Mein Aisa Uljha Jiya | Shahid Kapoor, Kriti Sanon | Raghav, Tanishk Bagchi", "T-Series", "latest hindi bollywood punjabi dance trending", "2026-10-15T10:00:00Z"),
            seed("gvyUuxdRdR4", "Akhiyaan Gulaab (Official Video) | Mitraz | Shahid Kapoor, Kriti Sanon", "T-Series", "latest hindi bollywood romantic lofi chill", "2026-10-14T10:00:00Z"),
            seed("kJa2kwoZ2a4", "Softly (Official Music Video) | Karan Aujla | Ikky", "Karan Aujla", "punjabi pop hiphop karan aujla trending", "2026-10-13T10:00:00Z"),
            seed("dCmp56tSSmA", "Lover (Official Music Video) | Diljit Dosanjh | Intense", "Diljit Dosanjh", "punjabi pop diljit dosanjh romantic trending", "2026-10-12T10:00:00Z"),
            seed("muds1gFUTN8", "Brown Munde (Official Video) | AP Dhillon, Gurinder Gill, Shinda Kahlon", "AP Dhillon", "punjabi hiphop trending official", "2026-10-11T10:00:00Z"),
            seed("UVWB9UHpXzY", "Kotha Koio Na (Official Video) | Coke Studio Bangla | Arfan Mredha Shiblu x Erfan", "Coke Studio Bangla", "bangla bengali folk indie official", "2026-10-10T10:00:00Z"),
            seed("n2dVFdqMYGA", "Chiltey রোদ (Official Music Video) | Arnob x Ripon | Coke Studio Bangla", "Coke Studio Bangla", "bangla bengali arnob acoustic indie lofi", "2026-10-09T10:00:00Z"),
            seed("f_9NLcyAblY", "Boba Tunnel | Anupam Roy (Official Video) | Chotuskone", "SVF Music", "bangla bengali anupam roy romantic acoustic", "2026-10-08T10:00:00Z"),
            seed("Umqb9KENgmk", "Tum Hi Ho (Official Video) | Aashiqui 2 | Aditya Roy Kapur, Shraddha Kapoor | Arijit Singh", "T-Series", "arijit singh romantic bollywood hindi classic", "2026-10-07T10:00:00Z")
        )
    }
}
