package com.vynora.visualizer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BassReactiveVisualizer(
    frame: VisualizerDataProcessor.Frame,
    modifier: Modifier = Modifier,
    height: Dp = 80.dp,
    accentColor: Color = Color(0xFF1DB954),
    secondaryColor: Color = Color(0xFF38BDF8),
    onRequestPermission: (() -> Unit)? = null
) {
    val bassAnim = remember { Animatable(frame.bassLevel) }
    LaunchedEffect(frame.bassLevel) {
        bassAnim.animateTo(
            targetValue = frame.bassLevel,
            animationSpec = tween(durationMillis = 60, easing = LinearEasing)
        )
    }

    val glowColor = remember(accentColor, bassAnim.value) {
        accentColor.copy(alpha = (0.15f + bassAnim.value * 0.35f).coerceIn(0.15f, 0.5f))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0C111A)),
            contentAlignment = Alignment.Center
        ) {
            // Ambient Bass Glow Behind the Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f + bassAnim.value * 0.15f)
                    .height(height * 0.7f)
                    .blur(20.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(glowColor, Color.Transparent)
                        )
                    )
            )

            // Smooth animated waveform bars Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                val numBars = frame.barHeights.size
                val totalWidth = size.width
                val maxBarHeight = size.height * 0.88f
                val spacing = 3.dp.toPx()
                val totalSpacing = spacing * (numBars - 1)
                val barWidth = ((totalWidth - totalSpacing) / numBars).coerceAtLeast(2.dp.toPx())

                val gradient = Brush.verticalGradient(
                    colors = listOf(
                        secondaryColor,
                        accentColor,
                        accentColor.copy(alpha = 0.45f)
                    ),
                    startY = 0f,
                    endY = size.height
                )

                for (i in 0 until numBars) {
                    val x = i * (barWidth + spacing)
                    val rawNormalized = frame.barHeights[i].coerceIn(0.06f, 1f)

                    // Emphasize bass bars (lower index bars get extra bounce from bassLevel)
                    val bassInfluence = if (i < 8) bassAnim.value * 0.35f else 0f
                    val effectiveHeight = ((rawNormalized + bassInfluence).coerceIn(0.06f, 1f)) * maxBarHeight

                    val top = size.height - effectiveHeight

                    // Main Bar
                    drawRoundRect(
                        brush = gradient,
                        topLeft = Offset(x, top),
                        size = Size(barWidth, effectiveHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )

                    // Peak Dot
                    val dotRadius = barWidth / 2.2f
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        radius = dotRadius,
                        center = Offset(x + barWidth / 2, (top - 3.dp.toPx()).coerceAtLeast(dotRadius))
                    )
                }
            }
        }

        // Live Reactive Indicator Pill
        Surface(
            modifier = Modifier.clickable {
                if (!frame.isHardwareFft) {
                    onRequestPermission?.invoke()
                }
            },
            shape = RoundedCornerShape(50),
            color = Color(0xFF131D28)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    modifier = Modifier.size(8.dp),
                    shape = CircleShape,
                    color = if (frame.isHardwareFft) Color(0xFF1DB954) else Color(0xFF38BDF8)
                ) {}
                Text(
                    text = if (frame.isHardwareFft) "Bass-Reactive (Hardware FFT)"
                    else "Reactive Spectrum (Tap for Mic FFT)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFCBD5E1),
                    fontWeight = FontWeight.Medium
                )
                if (!frame.isHardwareFft) {
                    Icon(
                        Icons.Rounded.Mic,
                        contentDescription = "Enable FFT",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}
