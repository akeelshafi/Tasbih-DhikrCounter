package com.tasbih.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/**
 * A tasbih string seen in perspective. Beads near the thumb are large; counted beads slide away up the string.
 * Drag up (or tap) to pull one bead past the glowing spot. Every bead = one count.
 *
 * Bead with slot k sits at t = k + pos on the string. id = base - k keeps each bead's material attached to it
 * while it travels, and [base] is bumped in the same frame as pos is reset, so nothing ever flickers.
 */
@Composable
fun BeadString(
    startCount: Int,
    count: Int,
    target: Int,
    epoch: Int,
    look: Look,
    showHint: Boolean,
    onBead: () -> Unit,
    modifier: Modifier = Modifier,
) {
    key(epoch) {
        var base by remember { mutableIntStateOf(startCount) }
        var pos by remember { mutableFloatStateOf(0f) }
        var unit by remember { mutableFloatStateOf(100f) }
        var animating by remember { mutableStateOf(false) }
        var settle by remember { mutableStateOf<Job?>(null) }
        val scope = rememberCoroutineScope()
        val currentOnBead by rememberUpdatedState(onBead)
        val taps = remember { Channel<Unit>(Channel.UNLIMITED) }
        val pulse = remember { Animatable(1f) }
        val bob by rememberInfiniteTransition(label = "hint").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(950, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "bob",
        )

        fun bead() {
            base += 1
            pos -= 1f
            currentOnBead()
            // a soft ripple spreads from the glowing spot every time a bead is counted
            scope.launch {
                pulse.snapTo(0f)
                pulse.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
            }
        }

        // Taps are queued, so a quick flurry of taps still counts every single bead.
        LaunchedEffect(Unit) {
            for (tap in taps) {
                animating = true
                val from = pos.coerceIn(0f, 1f)
                animate(
                    initialValue = from,
                    targetValue = 1f,
                    animationSpec = tween(max(60, ((1f - from) * 130).toInt()), easing = FastOutSlowInEasing),
                ) { v, _ -> pos = v }
                bead()
                animating = false
            }
        }

        Canvas(
            modifier = modifier
                .fillMaxSize()
                .semantics(mergeDescendants = true) {
                    contentDescription = "Tasbih beads. Count $count" + if (target > 0) " of $target" else ""
                    onClick(label = "Count one bead") {
                        taps.trySend(Unit)
                        true
                    }
                }
                .onSizeChanged { unit = min(it.width * 0.24f, it.height * 0.115f) }
                .pointerInput(Unit) { detectTapGestures(onTap = { taps.trySend(Unit) }) }
                .pointerInput(Unit) {
                    val finish: () -> Unit = {
                        settle = scope.launch {
                            if (pos >= 0.5f) {
                                animate(pos, 1f, animationSpec = tween(110, easing = FastOutSlowInEasing)) { v, _ -> pos = v }
                                bead()
                            } else {
                                animate(
                                    pos, 0f,
                                    animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
                                ) { v, _ -> pos = v }
                            }
                        }
                    }
                    detectDragGestures(
                        onDragStart = { settle?.cancel() },
                        onDragEnd = finish,
                        onDragCancel = finish,
                        onDrag = { change, drag ->
                            change.consume()
                            if (!animating) {
                                // Beads follow the thumb 1:1 — drag up pushes beads up the string.
                                pos = max(0f, pos - drag.y / unit)
                                while (pos >= 1f) bead()
                            }
                        },
                    )
                },
        ) {
            val w = size.width
            val h = size.height
            val u = unit
            val cx = w / 2f
            val y0 = h * 0.57f
            val r = 0.88f
            val lnR = ln(r)

            fun yOf(t: Float) = y0 - u * ((r.pow(t) - 1f) / lnR)
            fun xOf(t: Float) = cx + u * r.pow(t) * 0.7f * sin(0.4f * t)
            fun dOf(t: Float) = u * r.pow(t)

            val knotT = -1.9f
            val active = Offset(xOf(0f), yOf(0f))

            // the string
            val string = Path()
            val steps = 48
            for (i in 0..steps) {
                val t = knotT + i * (9.1f / steps)
                if (i == 0) string.moveTo(xOf(t), yOf(t)) else string.lineTo(xOf(t), yOf(t))
            }
            drawPath(string, look.string, style = Stroke(u * 0.045f, cap = StrokeCap.Round))

            // soft glow where the thumb pulls
            drawCircle(
                Brush.radialGradient(
                    listOf(look.accent.copy(alpha = 0.26f), Color.Transparent),
                    center = active,
                    radius = u * 1.5f,
                ),
                radius = u * 1.5f,
                center = active,
            )

            val interval = if (target >= 99) 33 else if (target > 0) target else 0

            // far beads first so near beads overlap them
            for (k in 8 downTo -3) {
                val t = k + pos
                val a = ((t + 1.4f) / 0.5f).coerceIn(0f, 1f) * ((6.8f - t) / 1.6f).coerceIn(0f, 1f)
                if (a <= 0f) continue
                val id = base - k
                val isMarker = interval > 0 && Math.floorMod(id, interval) == 0
                val paint = if (isMarker) look.marker
                else look.beads[look.pattern[Math.floorMod(id, look.pattern.size)]]
                drawBead(
                    center = Offset(xOf(t), yOf(t)),
                    diameter = dOf(t) * if (isMarker) 1.2f else 0.97f,
                    paint = paint,
                    alpha = a,
                    ring = if (isMarker) look.ring else null,
                )
            }

            // ripple from the glowing spot
            val p = pulse.value
            if (p < 1f) {
                drawCircle(
                    look.accent.copy(alpha = (1f - p) * 0.4f),
                    radius = u * (0.55f + p * 0.9f),
                    center = active,
                    style = Stroke(u * 0.035f),
                )
            }

            // knot bead + tassel
            val kc = Offset(xOf(knotT), yOf(knotT))
            val kd = dOf(knotT) * 1.05f
            drawTassel(kc.x, kc.y + kd * 0.42f, u * 0.28f, u * 0.95f, look)
            drawBead(kc, kd, look.marker, 1f, ring = look.ring)

            // gentle first-time hint: two chevrons drifting up beside the glowing bead
            if (showHint) {
                val hx = active.x + u * 1.3f
                for (i in 0..1) {
                    val hy = active.y - bob * u * 0.4f - i * u * 0.24f
                    val chevron = Path().apply {
                        moveTo(hx - u * 0.15f, hy + u * 0.07f)
                        lineTo(hx, hy - u * 0.07f)
                        lineTo(hx + u * 0.15f, hy + u * 0.07f)
                    }
                    drawPath(
                        chevron,
                        look.accent.copy(alpha = (0.9f - i * 0.4f) * (1f - bob * 0.5f)),
                        style = Stroke(u * 0.05f, cap = StrokeCap.Round),
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawBead(center: Offset, diameter: Float, paint: BeadPaint, alpha: Float, ring: Color? = null) {
    val rad = diameter / 2f
    // soft shadow on the string
    drawCircle(Color.Black.copy(alpha = 0.22f * alpha), rad * 1.02f, center + Offset(rad * 0.12f, rad * 0.22f))
    // polished body
    drawCircle(
        brush = Brush.radialGradient(
            listOf(paint.light, paint.base, paint.dark),
            center = center + Offset(-rad * 0.35f, -rad * 0.4f),
            radius = rad * 1.5f,
        ),
        radius = rad,
        center = center,
        alpha = alpha,
    )
    // faint rim light on the lower edge, so the bead feels round and glossy
    clipPath(Path().apply { addOval(Rect(center, rad)) }) {
        drawCircle(
            Brush.radialGradient(
                listOf(Color.White.copy(alpha = 0.22f * alpha), Color.Transparent),
                center = center + Offset(rad * 0.62f, rad * 0.66f),
                radius = rad * 0.5f,
            ),
            radius = rad * 0.5f,
            center = center + Offset(rad * 0.62f, rad * 0.66f),
        )
    }
    drawCircle(paint.dark.copy(alpha = 0.45f * alpha), rad, center, style = Stroke(rad * 0.06f))
    if (ring != null) drawCircle(ring.copy(alpha = 0.9f * alpha), rad * 0.97f, center, style = Stroke(rad * 0.07f))
    // specular highlight
    drawOval(
        Color.White.copy(alpha = 0.6f * alpha),
        topLeft = center + Offset(-rad * 0.55f, -rad * 0.68f),
        size = Size(rad * 0.55f, rad * 0.32f),
    )
}

private fun DrawScope.drawTassel(cx: Float, topY: Float, w: Float, len: Float, look: Look) {
    val body = Path().apply {
        moveTo(cx - w * 0.28f, topY)
        lineTo(cx + w * 0.28f, topY)
        cubicTo(cx + w * 0.5f, topY + len * 0.4f, cx + w * 0.95f, topY + len * 0.8f, cx + w * 1.05f, topY + len)
        for (j in 0..8) {
            val x = cx + w * 1.05f - j * (w * 2.1f / 8f)
            lineTo(x, topY + len - if (j % 2 == 1) len * 0.05f else 0f)
        }
        cubicTo(cx - w * 0.95f, topY + len * 0.8f, cx - w * 0.5f, topY + len * 0.4f, cx - w * 0.28f, topY)
        close()
    }
    drawPath(body, Brush.verticalGradient(look.tassel, startY = topY, endY = topY + len))
    // silk strands
    for (i in 0..9) {
        val f = i / 9f
        drawLine(
            Color.Black.copy(alpha = 0.18f),
            Offset(cx - w * 0.28f + f * w * 0.56f, topY + len * 0.2f),
            Offset(cx - w * 1.0f + f * w * 2.0f, topY + len * 0.98f),
            strokeWidth = w * 0.025f,
        )
    }
    // wrapped band under the knot
    drawRoundRect(
        Brush.verticalGradient(look.band, startY = topY, endY = topY + len * 0.2f),
        topLeft = Offset(cx - w * 0.42f, topY + len * 0.02f),
        size = Size(w * 0.84f, len * 0.2f),
        cornerRadius = CornerRadius(w * 0.1f),
    )
    for (i in 1..3) {
        val y = topY + len * (0.02f + i * 0.045f)
        drawLine(Color.Black.copy(alpha = 0.3f), Offset(cx - w * 0.42f, y), Offset(cx + w * 0.42f, y), strokeWidth = w * 0.03f)
    }
}
