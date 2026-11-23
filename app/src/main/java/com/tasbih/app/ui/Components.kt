package com.tasbih.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SectionCard(
    look: Look,
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = look.card.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, look.line.copy(alpha = 0.6f)),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (title != null) Text(title, fontFamily = FontFamily.Serif, fontSize = 18.sp, color = look.accent)
            content()
        }
    }
}

/** A circular progress ring that animates smoothly to its value. */
@Composable
fun ProgressRing(
    progress: Float,
    diameter: Dp,
    stroke: Dp,
    color: Color,
    track: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val p by animateFloatAsState(progress.coerceIn(0f, 1f), tween(600), label = "ring")
    Box(modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = stroke.toPx()
            val arcSize = Size(this.size.width - sw, this.size.height - sw)
            val topLeft = Offset(sw / 2f, sw / 2f)
            drawArc(track, -90f, 360f, false, topLeft, arcSize, style = Stroke(sw))
            if (p > 0f) drawArc(color, -90f, 360f * p, false, topLeft, arcSize, style = Stroke(sw, cap = StrokeCap.Round))
        }
        content()
    }
}

@Composable
fun LookCard(l: Look, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = l.bgTop,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) l.accent else l.line),
    ) {
        Column(
            Modifier.padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                l.beads.take(3).forEach { p ->
                    Box(
                        Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(p.light, p.base, p.dark))),
                    )
                }
            }
            Text(l.name, fontSize = 12.sp, color = l.onBg, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun SwitchRow(look: Look, title: String, subtitle: String, checked: Boolean, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = look.onBg)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = look.muted)
        }
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedTrackColor = look.accent,
                checkedThumbColor = look.onAccent,
                uncheckedTrackColor = look.line,
                uncheckedThumbColor = look.muted,
                uncheckedBorderColor = look.line,
            ),
        )
    }
}
