package com.tasbih.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasbih.app.data.SoundStyle
import com.tasbih.app.data.TasbihState
import com.tasbih.app.data.TasbihViewModel

@Composable
fun SettingsScreen(vm: TasbihViewModel, s: TasbihState, look: Look, feedback: Feedback, bottomPadding: Dp) {
    var editName by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = bottomPadding + 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Settings", fontFamily = FontFamily.Serif, fontSize = 32.sp, color = look.accent)

        SectionCard(look, title = "You") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Your name", fontSize = 13.sp, color = look.muted)
                    Text(s.name, fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 24.sp, color = look.onBg)
                }
                IconButton(onClick = { editName = true }) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Change name", tint = look.accent)
                }
            }
        }

        SectionCard(look, title = "Look") {
            Looks.chunked(2).forEachIndexed { row, pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    pair.forEachIndexed { col, l ->
                        val index = row * 2 + col
                        LookCard(l, selected = index == s.lookIndex, modifier = Modifier.weight(1f)) { vm.setLook(index) }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }

        SectionCard(look, title = "Sound and feel") {
            Text("Bead sound", fontSize = 13.sp, color = look.muted)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(SoundStyle.Soft to "Soft click", SoundStyle.Wood to "Wooden", SoundStyle.Off to "Silent").forEach { (style, label) ->
                    FilterChip(
                        selected = s.sound == style,
                        onClick = { vm.setSound(style); feedback.preview(style, s.volume) },
                        label = { Text(label) },
                        colors = chipColors(look),
                    )
                }
            }
            if (s.sound != SoundStyle.Off) {
                var volume by remember(s.volume) { mutableFloatStateOf(s.volume) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Volume", fontSize = 13.sp, color = look.muted, modifier = Modifier.weight(1f))
                    Text("${(volume * 100).toInt()}%", fontSize = 13.sp, color = look.muted)
                }
                Slider(
                    value = volume,
                    onValueChange = { volume = it },
                    onValueChangeFinished = { vm.setVolume(volume); feedback.preview(s.sound, volume) },
                    colors = SliderDefaults.colors(
                        thumbColor = look.accent,
                        activeTrackColor = look.accent,
                        inactiveTrackColor = look.line,
                    ),
                )
                Text(
                    "Follows your phone’s silent mode, so it stays quiet when your phone is on silent or vibrate.",
                    fontSize = 12.sp,
                    color = look.muted,
                )
            }
            HorizontalDivider(color = look.line)
            SwitchRow(look, "Vibration", "A gentle tick on every bead", s.haptics) { vm.toggleHaptics() }
            HorizontalDivider(color = look.line)
            SwitchRow(look, "Keep screen on", "While you are counting", s.keepAwake) { vm.toggleKeepAwake() }
        }

        SectionCard(look, title = "Daily goal") {
            Text("How many dhikr would you like to say each day?", fontSize = 13.sp, color = look.muted)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(33, 100, 300, 500, 1000).forEach { g ->
                    FilterChip(
                        selected = s.dailyGoal == g,
                        onClick = { vm.setDailyGoal(g) },
                        label = { Text("$g") },
                        colors = chipColors(look),
                    )
                }
            }
        }

        SectionCard(look, title = "About") {
            Text(
                "Tasbih works completely offline. There are no ads, no accounts and no internet access. Your dhikr stays on this phone.",
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = look.onBg,
            )
            Text("Version 1.0", fontSize = 12.sp, color = look.muted)
            HorizontalDivider(color = look.line)
            TextButton(onClick = { vm.showTutorial() }) {
                Text("Show the tutorial again", color = look.accent)
            }
            TextButton(onClick = { confirmClear = true }) {
                Text("Clear all dhikr history", color = look.accent)
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    if (editName) {
        var draft by remember { mutableStateOf(s.name) }
        val save: () -> Unit = { if (draft.isNotBlank()) { vm.setName(draft); editName = false } }
        AlertDialog(
            onDismissRequest = { editName = false },
            containerColor = look.surface,
            title = { Text("Your name") },
            text = { NameField(look, draft, { draft = it }, onDone = { save() }, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                TextButton(onClick = { save() }, enabled = draft.isNotBlank()) { Text("Save", color = look.accent) }
            },
            dismissButton = { TextButton(onClick = { editName = false }) { Text("Cancel", color = look.muted) } },
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            containerColor = look.surface,
            title = { Text("Clear all history?") },
            text = { Text("Your daily totals, consistent days and records will be erased from this phone. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { vm.clearHistory(); confirmClear = false }) { Text("Clear", color = look.accent) }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Keep it", color = look.muted) } },
        )
    }
}

@Composable
private fun chipColors(look: Look) = FilterChipDefaults.filterChipColors(
    selectedContainerColor = look.accent,
    selectedLabelColor = look.onAccent,
    labelColor = look.onBg,
)
