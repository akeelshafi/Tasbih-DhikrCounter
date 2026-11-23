package com.tasbih.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** A very quiet pattern of eight-pointed stars (Rub el Hizb) behind everything, plus a soft glow. */
@Composable
fun Backdrop(look: Look, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val c = Offset(size.width / 2f, size.height * 0.52f)
        val base = size.width * 0.34f
        for (i in 0..3) {
            val outer = base * (1f + i * 0.55f)
            val path = Path()
            for (j in 0 until 16) {
                val angle = j * (PI.toFloat() / 8f) - PI.toFloat() / 2f
                val radius = if (j % 2 == 0) outer else outer * 0.765f
                val x = c.x + cos(angle) * radius
                val y = c.y + sin(angle) * radius
                if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, look.accent.copy(alpha = 0.085f - i * 0.016f), style = Stroke(2f))
        }
        drawCircle(
            Brush.radialGradient(listOf(look.accent.copy(alpha = 0.10f), Color.Transparent), center = c, radius = size.width * 0.8f),
            radius = size.width * 0.8f,
            center = c,
        )
    }
}

/** A mihrab-style pointed arch that frames the count and the beads, like a prayer niche. */
@Composable
fun ArchFrame(look: Look, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height

        fun arch(inset: Float, closed: Boolean): Path {
            val l = w * 0.05f + inset
            val r = w * 0.95f - inset
            val cx = w / 2f
            val apex = inset
            val spring = h * 0.30f + inset
            val bottom = h + 80f
            val rise = spring - apex
            return Path().apply {
                moveTo(l, bottom)
                lineTo(l, spring)
                cubicTo(l, spring - rise * 0.55f, cx - (cx - l) * 0.25f, apex + rise * 0.25f, cx, apex)
                cubicTo(cx + (r - cx) * 0.25f, apex + rise * 0.25f, r, spring - rise * 0.55f, r, spring)
                lineTo(r, bottom)
                if (closed) close()
            }
        }

        drawPath(
            arch(0f, closed = true),
            Brush.verticalGradient(listOf(look.accent.copy(alpha = 0.10f), look.accent.copy(alpha = 0.02f))),
        )
        drawPath(arch(0f, closed = false), look.accent.copy(alpha = 0.38f), style = Stroke(2.5.dp.toPx()))
        drawPath(arch(14.dp.toPx(), closed = false), look.accent.copy(alpha = 0.16f), style = Stroke(1.dp.toPx()))
    }
}
