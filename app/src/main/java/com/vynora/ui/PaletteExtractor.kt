package com.vynora.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ExtractedColors(
    val dominant: Color = Color(0xFF1DB954),
    val secondary: Color = Color(0xFF38BDF8),
    val backgroundGradient: List<Color> = listOf(
        Color(0xFF132A1F),
        Color(0xFF07090E),
        Color(0xFF07090E)
    )
)

object PaletteExtractor {
    suspend fun extractFromUri(context: Context, uri: String?): ExtractedColors {
        if (uri.isNullOrBlank()) return ExtractedColors()

        return withContext(Dispatchers.IO) {
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(uri)
                    .allowHardware(false)
                    .size(128, 128)
                    .build()
                val result = (loader.execute(request) as? SuccessResult)?.drawable
                val bitmap = (result as? BitmapDrawable)?.bitmap ?: return@withContext ExtractedColors()

                val palette = Palette.from(bitmap).generate()

                val dominantInt = palette.getVibrantColor(
                    palette.getDominantColor(0xFF1DB954.toInt())
                )
                val secondaryInt = palette.getMutedColor(
                    palette.getLightVibrantColor(0xFF38BDF8.toInt())
                )

                val domColor = Color(dominantInt)
                val secColor = Color(secondaryInt)

                // Dark ambient gradient for Now Playing background
                val darkAmbient = domColor.copy(alpha = 0.45f)
                val midAmbient = secColor.copy(alpha = 0.20f)
                val deepSlate = Color(0xFF07090E)

                ExtractedColors(
                    dominant = domColor,
                    secondary = secColor,
                    backgroundGradient = listOf(darkAmbient, midAmbient, deepSlate)
                )
            } catch (_: Exception) {
                ExtractedColors()
            }
        }
    }
}
