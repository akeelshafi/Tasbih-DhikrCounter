package com.tasbih.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private class TutorialStep(val title: String, val body: String)

private val Steps = listOf(
    TutorialStep(
        "Slide a bead up",
        "Put your thumb on the glowing bead and slide it up — or just tap anywhere on the beads. Every bead is one count.",
    ),
    TutorialStep(
        "Choose your dhikr",
        "Tap the card at the top to pick a dhikr, set how far to count, or follow the after-prayer dhikr.",
    ),
    TutorialStep(
        "Find your way around",
        "Tasbih is where you count. Progress shows your dhikr for every day. Settings changes the look, sound and daily goal.",
    ),
)

/** A short, friendly walkthrough shown the first time the home screen opens. */
@Composable
fun TutorialOverlay(look: Look, tabs: List<NavTab>, onDone: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    val last = Steps.lastIndex

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.66f))
            .pointerInput(Unit) { detectTapGestures { } }
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = look.surface,
            border = BorderStroke(1.dp, look.line),
        ) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Step ${step + 1} of ${Steps.size}", fontSize = 13.sp, color = look.muted)
                Spacer(Modifier.height(12.dp))

                Box(Modifier.height(190.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AnimatedContent(
                        targetState = step,
                        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(120)) },
                        label = "tutorialArt",
                    ) { s ->
                        when (s) {
                            0 -> BeadDemo(look)
                            1 -> DhikrDemo(look)
                            else -> NavDemo(look, tabs)
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                AnimatedContent(
                    targetState = step,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(120)) },
                    label = "tutorialText",
                ) { s ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(Steps[s].title, fontFamily = FontFamily.Serif, fontSize = 26.sp, color = look.accent, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(10.dp))
                        Text(Steps[s].body, fontSize = 16.sp, lineHeight = 24.sp, color = look.onBg, textAlign = TextAlign.Center)
                    }
                }

                Spacer(Modifier.height(22.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Steps.indices.forEach { i ->
                        val w by animateDpAsState(if (i == step) 24.dp else 8.dp, tween(200), label = "dot")
                        Box(
                            Modifier
                                .width(w)
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(if (i == step) look.accent else look.line),
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (step < last) {
                        TextButton(onClick = onDone) { Text("Skip", color = look.muted, fontSize = 15.sp) }
                    }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = { if (step < last) step++ else onDone() },
                        shape = RoundedCornerShape(26.dp),
                        modifier = Modifier.height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = look.accent, contentColor = look.onAccent),
                    ) {
                        Text(if (step < last) "Next" else "Start counting", fontSize = 16.sp, modifier = Modifier.padding(horizontal = 12.dp))
                    }
                }
            }
        }
    }
}

/** Beads sliding up the string with a fingertip following the active bead. */
@Composable
private fun BeadDemo(look: Look) {
    val t by rememberInfiniteTransition(label = "bead").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "t",
    )
    Canvas(Modifier.size(width = 160.dp, height = 190.dp)) {
        val cx = size.width / 2f
        val d = size.width * 0.34f
        val baseY = size.height * 0.72f
        val step = d * 1.06f
        drawLine(look.string, Offset(cx, 0f), Offset(cx, size.height), strokeWidth = d * 0.07f)
        for (k in 3 downTo -1) {
            val pos = k + t
            val y = baseY - pos * step
            val alpha = (1f - (pos - 2f).coerceAtLeast(0f)).coerceIn(0f, 1f) * (1f + pos).coerceIn(0f, 1f)
            if (alpha <= 0f) continue
            val paint = look.beads[Math.floorMod(k, look.beads.size)]
            val c = Offset(cx, y)
            val r = d / 2f
            drawCircle(Color.Black.copy(alpha = 0.2f * alpha), r, c + Offset(r * 0.12f, r * 0.2f))
            drawCircle(
                Brush.radialGradient(listOf(paint.light, paint.base, paint.dark), center = c + Offset(-r * 0.35f, -r * 0.4f), radius = r * 1.5f),
                radius = r, center = c, alpha = alpha,
            )
        }
        // fingertip
        val finger = Offset(cx + d * 0.1f, baseY - t * step + d * 0.5f)
        drawCircle(look.accent.copy(alpha = 0.18f), d * 0.62f, finger)
        drawCircle(Color.White.copy(alpha = 0.92f), d * 0.34f, finger)
        drawCircle(look.accent.copy(alpha = 0.55f), d * 0.34f, finger, style = androidx.compose.ui.graphics.drawscope.Stroke(d * 0.04f))
    }
}

/** A mock of the dhikr card with a pulsing outline. */
@Composable
private fun DhikrDemo(look: Look) {
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse",
    )
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = look.card,
        border = BorderStroke(2.5.dp, look.accent.copy(alpha = pulse)),
    ) {
        Column(Modifier.padding(horizontal = 30.dp, vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("سُبْحَانَ ٱللَّٰهِ", fontSize = 32.sp, color = look.accent)
            Text("SubhanAllah", fontSize = 15.sp, color = look.onBg)
            Text("Tap to change dhikr", fontSize = 12.sp, color = look.muted, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

/** The real navigation bar, cycling through its tabs. */
@Composable
private fun NavDemo(look: Look, tabs: List<NavTab>) {
    var selected by remember { mutableIntStateOf(0) }
    LaunchedEffect(tabs.size) {
        while (true) {
            delay(1100)
            selected = (selected + 1) % tabs.size
        }
    }
    FloatingNav(look, tabs, selected, onSelect = {})
}
