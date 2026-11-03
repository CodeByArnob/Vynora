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
            seed("Umqb9KENgmk", "Tum Hi Ho (Official Video) | Aashiqui 2 | Aditya Roy Kapur, Shraddha Kapoor | Arijit Singh", "T-Series", "arijit singh romantic bollywood hindi classic", "2026-10-07T10:00:00Z"),
            seed("Yba9b2e2_c8", "Jeene Laga Hoon (Official Video) | Ramaiya Vastavaiya | Girish Kumar, Shruti Haasan | Atif Aslam", "Tips Official", "atif aslam romantic bollywood hindi classic", "2026-10-06T10:00:00Z"),
            seed("w_Mv3LM9W3M", "Tera Hone Laga Hoon (Official Video) | Ajab Prem Ki Ghazab Kahani | Atif Aslam, Alisha Chinai", "Tips Official", "atif aslam romantic bollywood hindi classic", "2026-10-05T10:00:00Z"),
            seed("k76B0M6E4rA", "Pee Loon (Official Video) | Once Upon A Time in Mumbaai | Emraan Hashmi, Prachi Desai | Mohit Chauhan", "T-Series", "mohit chauhan pritam romantic hindi bollywood", "2026-10-04T10:00:00Z"),
            seed("8v_4O44sfjM", "Zara Sa (Official Video) | Jannat | Emraan Hashmi, Sonal Chauhan | KK, Pritam", "Sony Music India", "kk pritam romantic hindi classic", "2026-10-03T10:00:00Z"),
            seed("r8K8c2m0j5s", "Tum Mile (Love Reprise) | Emraan Hashmi, Soha Ali Khan | Neeraj Shridhar, Pritam", "Sony Music India", "pritam romantic hindi bollywood classic", "2026-10-02T10:00:00Z"),
            seed("t_b4L0m9W7s", "Kinni Kinni (Official Video) | Diljit Dosanjh | Ghost", "Diljit Dosanjh", "diljit dosanjh punjabi pop trending romantic", "2026-10-01T10:00:00Z"),
            seed("v9M_4b8K2rA", "Winning Speech (Official Music Video) | Karan Aujla | Mxrci", "Karan Aujla", "karan aujla punjabi drill hiphop urban", "2026-09-30T10:00:00Z"),
            seed("7c8B_1M4j5s", "Cheques (Official Music Video) | Shubh", "Shubh", "shubh punjabi hiphop trending official", "2026-09-29T10:00:00Z"),
            seed("p8K_4b2M9rA", "Still Rollin (Official Audio) | Shubh", "Shubh", "shubh punjabi hiphop trending official", "2026-09-28T10:00:00Z"),
            seed("b9M_4c8K2rA", "Baller (Official Music Video) | Shubh", "Shubh", "shubh punjabi hiphop trending official", "2026-09-27T10:00:00Z"),
            seed("q8B_4M1j5sA", "Shob Loke Koy (Official Music Video) | Coke Studio Bangla | Kaniz Khandaker x Murshidabadi", "Coke Studio Bangla", "bangla bengali folk sufi coke studio bangla", "2026-09-26T10:00:00Z"),
            seed("w9M_8b4K2rA", "Deora (Official Music Video) | Coke Studio Bangla | Pritom Hasan x Islam Uddin Palakar", "Coke Studio Bangla", "bangla bengali folk dance coke studio bangla", "2026-09-25T10:00:00Z"),
            seed("c8B_1M4j5sK", "Bulbuli (Official Music Video) | Coke Studio Bangla | Ritu Raj x Nandita", "Coke Studio Bangla", "bangla bengali classic nazrul geeti coke studio bangla", "2026-09-24T10:00:00Z"),
            seed("OPf0YbXqDm0", "Uptown Funk (Official Video) | Mark Ronson ft. Bruno Mars", "Mark Ronson", "bruno mars english pop funk chartbuster", "2026-09-23T10:00:00Z"),
            seed("4NRXx6U8ABQ", "Blinding Lights (Official Video) | The Weeknd", "The Weeknd", "the weeknd synthpop english pop retro 80s", "2026-09-22T10:00:00Z"),
            seed("TUVcZfQe-Kw", "Levitating (Official Music Video) | Dua Lipa", "Dua Lipa", "dua lipa disco pop english dance", "2026-09-21T10:00:00Z"),
            seed("JGwWNGJdvx8", "Shape of You (Official Music Video) | Ed Sheeran", "Ed Sheeran", "ed sheeran pop acoustic chartbuster", "2026-09-20T10:00:00Z"),
            seed("ApXoWvfEYVU", "Sunflower (Spider-Man: Into the Spider-Verse) | Post Malone, Swae Lee", "Post Malone", "post malone hiphop chill english pop", "2026-09-19T10:00:00Z")
        )
    }
}
