package com.tasbih.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The "what should I call you?" field. A rounded card with a person badge, an elegant serif entry style,
 * a placeholder, a character counter, and a soft glow in the look's accent colour while it is focused.
 */
@Composable
fun NameField(
    look: Look,
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val border by animateColorAsState(if (focused) look.accent else look.line, tween(200), label = "nameBorder")
    val glow by animateDpAsState(if (focused) 12.dp else 2.dp, tween(200), label = "nameGlow")
    val shape = RoundedCornerShape(26.dp)

    Surface(
        modifier = modifier.shadow(
            glow, shape,
            ambientColor = look.accent.copy(alpha = 0.25f),
            spotColor = look.accent.copy(alpha = 0.35f),
        ),
        shape = shape,
        color = look.surface,
        border = BorderStroke(if (focused) 2.dp else 1.dp, border),
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(look.accent.copy(alpha = if (focused) 0.18f else 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Person, contentDescription = null, tint = look.accent)
            }
            Spacer(Modifier.width(14.dp))
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        "Your name",
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 22.sp,
                        color = look.muted.copy(alpha = 0.7f),
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = { if (it.length <= 24) onValueChange(it) },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = FontFamily.Serif, fontSize = 22.sp, color = look.onBg),
                    cursorBrush = SolidColor(look.accent),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { onDone() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Your name" }
                        .onFocusChanged { focused = it.isFocused },
                )
            }
            if (value.isNotEmpty()) {
                Text("${value.length}/24", fontSize = 12.sp, color = look.muted, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
