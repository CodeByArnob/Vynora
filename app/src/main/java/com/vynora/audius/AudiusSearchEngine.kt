package com.vynora.audius

import com.vynora.catalog.UnifiedTrack
import kotlin.math.min

object AudiusSearchEngine {

    // Canonical dictionary of popular artists, songs & genres for instant typo correction
    private val canonicalTerms = mapOf(
        "arijt sing" to "Arijit Singh",
        "arjit sing" to "Arijit Singh",
        "arijit sing" to "Arijit Singh",
        "arijt singh" to "Arijit Singh",
        "arjit singh" to "Arijit Singh",
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
        "anuv" to "Anuv Jain",
        "lofi" to "Chill Lo-Fi",
        "chillhop" to "Chillhop",
        "electro" to "Electronic",
        "electrnic" to "Electronic",
        "dubstep" to "Dubstep",
        "trap" to "Trap Beat",
        "hiphop" to "Hip-Hop",
        "hip hop" to "Hip-Hop",
        "bolywood" to "Bollywood",
        "bollywood" to "Bollywood",
        "punjbi" to "Punjabi",
        "panjabi" to "Punjabi",
        "romantik" to "Romantic",
        "romantic" to "Romantic"
    )

    private val vocabulary = listOf(
        "Arijit Singh", "Shreya Ghoshal", "Jubin Nautiyal", "Atif Aslam", "Armaan Malik",
        "Darshan Raval", "Karan Aujla", "Diljit Dosanjh", "AP Dhillon", "Badshah",
        "Sachin-Jigar", "Pritam", "Vishal Mishra", "Anuv Jain", "Electronic",
        "Chillwave", "Lo-Fi", "Dubstep", "Future Bass", "Trap", "Hip-Hop",
        "House", "Techno", "Ambient", "Acoustic", "Bollywood", "Punjabi", "Indie"
    )

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

    fun searchUnifiedTracks(query: String, catalog: List<UnifiedTrack>): List<UnifiedTrack> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return catalog

        val (corrected, _) = smartCorrectQuery(trimmed)
        val rawTokens = trimmed.lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }
        val correctedTokens = corrected.lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }
        val phoneticTokens = (rawTokens + correctedTokens).map { phoneticNormalize(it) }.filter { it.isNotBlank() }.distinct()

        val scored = catalog.mapNotNull { item ->
            val haystack = "${item.title} ${item.artist} ${item.album} ${item.genre}".lowercase()
            val normHaystack = phoneticNormalize("${item.title} ${item.artist}")
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

            if (score > 0) item to score else null
        }

        return scored
            .sortedByDescending { it.second }
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
