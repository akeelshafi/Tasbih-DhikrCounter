package com.tasbih.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class NavTab(val label: String, val icon: ImageVector)

/**
 * A simple floating bar: three icons with their names, hovering above the content in the look's colours.
 * No expanding animations — the selected tab changes the instant it is tapped.
 */
@Composable
fun FloatingNav(
    look: Look,
    tabs: List<NavTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(30.dp)
    Surface(
        modifier = modifier.shadow(
            elevation = 12.dp,
            shape = shape,
            ambientColor = look.accent.copy(alpha = 0.25f),
            spotColor = look.accent.copy(alpha = 0.35f),
        ),
        shape = shape,
        color = look.surface,
        border = BorderStroke(1.dp, look.line),
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == selectedIndex
                val color = if (selected) look.accent else look.muted
                Column(
                    Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (selected) look.accent.copy(alpha = 0.14f) else Color.Transparent)
                        .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(index) })
                        .padding(horizontal = 22.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(tab.icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                    Text(
                        tab.label,
                        fontSize = 11.sp,
                        color = color,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
