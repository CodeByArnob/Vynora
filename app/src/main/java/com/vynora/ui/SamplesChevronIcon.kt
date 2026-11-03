package com.vynora.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Music Discovery "Samples" icon (double right play chevron ▷▷),
 * used in both the bottom navigation bar and the playlist screen action bar.
 */
@Composable
fun SamplesChevronIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color.White
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = 1.8.dp.toPx()

        // Triangle 1 (Left)
        val p1 = Path().apply {
            moveTo(w * 0.16f, h * 0.22f)
            lineTo(w * 0.50f, h * 0.50f)
            lineTo(w * 0.16f, h * 0.78f)
            close()
        }
        drawPath(
            p1,
            color = tint,
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round, cap = StrokeCap.Round)
        )

        // Triangle 2 (Right)
        val p2 = Path().apply {
            moveTo(w * 0.50f, h * 0.22f)
            lineTo(w * 0.84f, h * 0.50f)
            lineTo(w * 0.50f, h * 0.78f)
            close()
        }
        drawPath(
            p2,
            color = tint,
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round, cap = StrokeCap.Round)
        )
    }
}
