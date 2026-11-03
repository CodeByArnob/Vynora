package com.vynora.youtube

import kotlin.math.min

object SmartMusicSearchEngine {

    // Canonical dictionary of popular Indian artists, movies, songs & genres for instant typo correction
    private val canonicalTerms = mapOf(
        "arijt" to "Arijit Singh",
        "arjit" to "Arijit Singh",
        "arijith" to "Arijit Singh",
        "arijit" to "Arijit Singh",
        "sing" to "Singh",
        "atiff" to "Atif Aslam",
        "atif" to "Atif Aslam",
        "shreya" to "Shreya Ghoshal",
        "sreya" to "Shreya Ghoshal",
        "arman" to "Armaan Malik",
        "armaan" to "Armaan Malik",
        "jubin" to "Jubin Nautiyal",
        "zubin" to "Jubin Nautiyal",
        "darshan" to "Darshan Raval",
        "darsan" to "Darshan Raval",
        "diljit" to "Diljit Dosanjh",
        "diljith" to "Diljit Dosanjh",
        "aujla" to "Karan Aujla",
        "karan" to "Karan Aujla",
        "badsha" to "Badshah",
        "badshah" to "Badshah",
        "ap dhillon" to "AP Dhillon",
        "ap dilon" to "AP Dhillon",
        "pritam" to "Pritam",
        "vishal" to "Vishal Mishra",
        "sachin" to "Sachin-Jigar",
        "anuv" to "Anuv Jain",
        "kesriya" to "Kesariya",
        "kesaria" to "Kesariya",
        "apna bnale" to "Apna Bana Le",
        "apna bana le" to "Apna Bana Le",
        "chalya" to "Chaleya",
        "chaleya" to "Chaleya",
        "tauba" to "Tauba Tauba",
        "toba" to "Tauba Tauba",
        "satrnga" to "Satranga",
        "satranga" to "Satranga",
        "pasuri" to "Pasoori",
        "pasoori" to "Pasoori",
        "afren" to "Afreen Afreen",
        "afreen" to "Afreen Afreen",
        "chann" to "Chann Udta Teer",
        "udta teer" to "Chann Udta Teer",
        "bolywood" to "Bollywood",
        "bollywood" to "Bollywood",
        "punjbi" to "Punjabi",
        "panjabi" to "Punjabi",
        "romantik" to "Romantic Hindi",
        "romantic" to "Romantic Hindi",
        "bangla" to "Bangla Official",
        "bengali" to "Bangla Official",
        "bhojpuri" to "Bhojpuri Official",
        "hndi" to "Hindi",
        "hindi" to "Hindi",
        "lofi" to "Hindi Lofi",
        "sad song" to "Hindi Sad Songs",
        "new song" to "Latest Hindi Songs"
    )

    private val vocabulary = listOf(
        "Arijit Singh", "Shreya Ghoshal", "Jubin Nautiyal", "Atif Aslam", "Armaan Malik",
        "Darshan Raval", "Karan Aujla", "Diljit Dosanjh", "AP Dhillon", "Badshah",
        "Sachin-Jigar", "Pritam", "Vishal Mishra", "Anuv Jain", "Sonu Nigam",
        "Neha Kakkar", "B Praak", "Guru Randhawa", "Sidhu Moose Wala", "Yo Yo Honey Singh",
        "AR Rahman", "Amit Trivedi", "Shankar Ehsaan Loy", "Palak Muchhal", "Tulsi Kumar",
        "Kesariya", "Apna Bana Le", "Chaleya", "Tauba Tauba", "Satranga", "Pasoori",
        "Afreen Afreen", "Tum Hi Ho", "Channa Mereya", "Agar Tum Saath Ho", "Shayad",
        "Heeriye", "O Maahi", "Tere Vaaste", "Phir Aur Kya Chahiye", "Ve Kamleya",
        "Sajni", "Aaj Ki Raat", "Taras", "Akhiyaan Gulaab", "Teri Baaton Mein Aisa Uljha Jiya",
        "Husn", "Baarishein", "Gul", "Chann", "Udta Teer", "T-Series", "Sony Music India",
        "Zee Music Company", "YRF", "Saregama", "Speed Records", "Coke Studio", "Bangla",
        "Bollywood", "Punjabi", "Romantic", "Latest Hindi"
    )

    /**
     * Normalizes South Asian transliterated text so spelling variations match phonetically.
     */
    fun phoneticNormalize(input: String): String {
        val lower = input.lowercase().trim()
        if (lower.isEmpty()) return ""
        return lower
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace("aa", "a")
            .replace("ee", "i")
            .replace("ii", "i")
            .replace("oo", "u")
            .replace("uu", "u")
            .replace("kh", "k")
            .replace("gh", "g")
            .replace("chh", "ch")
            .replace("jh", "j")
            .replace("th", "t")
            .replace("dh", "d")
            .replace("ph", "f")
            .replace("bh", "b")
            .replace("sh", "s")
            .replace("z", "j")
            .replace("w", "v")
            .replace(Regex("(.)\\1+"), "$1")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Corrects misspelled user search queries into the best canonical music search query.
     */
    fun smartCorrectQuery(rawQuery: String): Pair<String, String?> {
        val trimmed = rawQuery.trim()
        if (trimmed.isEmpty()) return "" to null

        val lower = trimmed.lowercase()
        canonicalTerms[lower]?.let { exact ->
            val didChange = !exact.equals(trimmed, ignoreCase = true)
            return exact to (if (didChange) exact else null)
        }

        val tokens = lower.split(Regex("\\s+")).filter { it.isNotBlank() }
        var correctedAny = false
        val correctedTokens = tokens.map { token ->
            canonicalTerms[token]?.also {
                if (!it.equals(token, ignoreCase = true)) correctedAny = true
            } ?: findClosestVocabularyToken(token)?.also {
                if (!it.equals(token, ignoreCase = true)) correctedAny = true
            } ?: token
        }

        val finalQuery = correctedTokens.joinToString(" ")
        return finalQuery to (if (correctedAny) finalQuery else null)
    }

    private fun findClosestVocabularyToken(token: String): String? {
        if (token.length < 3) return null
        val normToken = phoneticNormalize(token)
        var bestWord: String? = null
        var bestDistance = Int.MAX_VALUE

        for (phrase in vocabulary) {
            for (word in phrase.split(" ", "-")) {
                val cleanWord = word.trim()
                if (cleanWord.length < 3) continue
                val normWord = phoneticNormalize(cleanWord)
                if (normWord == normToken) return cleanWord
                val dist = levenshtein(normToken, normWord)
                val maxAllowed = if (normToken.length <= 4) 1 else 2
                if (dist <= maxAllowed && dist < bestDistance) {
                    bestDistance = dist
                    bestWord = cleanWord
                }
            }
        }
        return bestWord
    }

    /**
     * Ranks and filters local cached tracks with fuzzy typo tolerance.
     */
    fun searchLocalCatalog(query: String, catalog: List<YouTubeItem>): List<YouTubeItem> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return catalog

        val (corrected, _) = smartCorrectQuery(trimmed)
        val rawTokens = trimmed.lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }
        val correctedTokens = corrected.lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }
        val phoneticTokens = (rawTokens + correctedTokens).map { phoneticNormalize(it) }.filter { it.isNotBlank() }.distinct()

        val scored = catalog.mapNotNull { item ->
            val haystack = "${item.title} ${item.channelTitle} ${item.description}".lowercase()
            val normHaystack = phoneticNormalize("${item.title} ${item.channelTitle}")
            val haystackWords = normHaystack.split(" ").filter { it.isNotBlank() }

            var score = 0
            for (token in rawTokens) {
                if (haystack.contains(token)) score += 35
            }
            for (token in correctedTokens) {
                if (haystack.contains(token)) score += 30
            }
            for (pToken in phoneticTokens) {
                if (normHaystack.contains(pToken)) {
                    score += 25
                } else if (pToken.length >= 3) {
                    val fuzzyHit = haystackWords.any { word ->
                        word.startsWith(pToken) || pToken.startsWith(word) ||
                            levenshtein(pToken, word) <= (if (pToken.length <= 4) 1 else 2)
                    }
                    if (fuzzyHit) score += 18
                }
            }

            if (score > 0) {
                if (item.isOfficialChannel) score += 10
                item to score
            } else {
                null
            }
        }

        return scored
            .sortedWith(compareByDescending<Pair<YouTubeItem, Int>> { it.second }.thenByDescending { it.first.publishedAt })
            .map { it.first }
    }

    fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLen = lhs.length
        val rhsLen = rhs.length
        if (lhsLen == 0) return rhsLen
        if (rhsLen == 0) return lhsLen

        var cost = IntArray(lhsLen + 1) { it }
        var newCost = IntArray(lhsLen + 1)

        for (i in 1..rhsLen) {
            newCost[0] = i
            for (j in 1..lhsLen) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1
                newCost[j] = min(min(costInsert, costDelete), costReplace)
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }
        return cost[lhsLen]
    }
}
