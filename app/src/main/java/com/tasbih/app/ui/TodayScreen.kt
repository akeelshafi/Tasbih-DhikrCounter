package com.tasbih.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasbih.app.data.BeadEvent
import com.tasbih.app.data.Dhikrs
import com.tasbih.app.data.Hijri
import com.tasbih.app.data.TasbihState
import com.tasbih.app.data.TasbihViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    vm: TasbihViewModel,
    s: TasbihState,
    look: Look,
    today: Int,
    hint: Boolean,
    feedback: Feedback,
    bottomPadding: Dp,
    onOpenProgress: () -> Unit,
) {
    // keep the screen on while counting, only on this screen
    val view = LocalView.current
    DisposableEffect(s.keepAwake) {
        view.keepScreenOn = s.keepAwake
        onDispose { view.keepScreenOn = false }
    }

    var showSheet by remember { mutableStateOf(false) }
    var showReset by remember { mutableStateOf(false) }
    var banner by remember { mutableStateOf<String?>(null) }
    var bannerKey by remember { mutableIntStateOf(0) }
    LaunchedEffect(bannerKey) {
        if (bannerKey > 0) {
            delay(2800)
            banner = null
        }
    }

    val phrase = s.dhikr.at(s.count)

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(bottom = bottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // greeting + today's total
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 20.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Assalamu alaikum,", fontSize = 13.sp, color = look.muted)
                Text(
                    s.name,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 26.sp,
                    color = look.accent,
                    maxLines = 1,
                )
                Text(Hijri.format(LocalDate.now()), fontSize = 12.sp, color = look.muted)
            }
            Surface(
                onClick = onOpenProgress,
                shape = RoundedCornerShape(50),
                color = look.card.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, look.line),
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Today", fontSize = 11.sp, color = look.muted)
                    Text(today.toString(), fontFamily = FontFamily.Serif, fontSize = 22.sp, color = look.onBg)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // what she is saying; tap to change
        Surface(
            onClick = { showSheet = true },
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = look.card.copy(alpha = 0.75f),
            border = BorderStroke(1.dp, look.line.copy(alpha = 0.7f)),
        ) {
            Box {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 44.dp, end = 44.dp, top = 12.dp, bottom = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AnimatedContent(
                        targetState = phrase,
                        transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(200)) },
                        label = "phrase",
                    ) { p ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val long = p.arabic.length > 28
                            Text(
                                p.arabic,
                                fontSize = if (long) 22.sp else 32.sp,
                                lineHeight = if (long) 36.sp else 50.sp,
                                color = look.accent,
                                textAlign = TextAlign.Center,
                            )
                            Text(p.transliteration, fontSize = 14.sp, color = look.onBg, textAlign = TextAlign.Center)
                        }
                    }
                    Text("Tap to change dhikr", fontSize = 11.sp, color = look.muted, modifier = Modifier.padding(top = 4.dp))
                }
                IconButton(onClick = { showReset = true }, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Start this count over", tint = look.muted)
                }
            }
        }

        // the niche: count + beads
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            ArchFrame(look, Modifier.fillMaxSize())
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(18.dp))
                AnimatedContent(
                    targetState = s.count,
                    transitionSpec = {
                        (slideInVertically(tween(160)) { it / 3 } + fadeIn(tween(120))) togetherWith
                            (slideOutVertically(tween(160)) { -it / 3 } + fadeOut(tween(100)))
                    },
                    label = "count",
                ) { n ->
                    Text(n.toString(), fontSize = 72.sp, fontFamily = FontFamily.Serif, color = look.onBg)
                }
                val progress by animateFloatAsState(
                    targetValue = if (s.target > 0) s.count / s.target.toFloat() else 0f,
                    animationSpec = tween(260),
                    label = "progress",
                )
                if (s.target > 0) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .width(120.dp)
                            .height(4.dp),
                        color = look.accent,
                        trackColor = look.line,
                        strokeCap = StrokeCap.Round,
                        gapSize = 0.dp,
                        drawStopIndicator = {},
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("of ${s.target}", fontSize = 13.sp, color = look.muted)
                } else {
                    Text("free count", fontSize = 13.sp, color = look.muted)
                }

                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    BeadString(
                        startCount = s.count,
                        count = s.count,
                        target = s.target,
                        epoch = s.epoch,
                        look = look,
                        showHint = hint,
                        onBead = {
                            val result = vm.increment()
                            val now = vm.state.value
                            feedback.play(result.event, now.sound, now.volume, now.haptics)
                            when {
                                result.goalReached -> {
                                    banner = "Alhamdulillah — daily goal reached"
                                    bannerKey++
                                }
                                result.event == BeadEvent.COMPLETE -> {
                                    banner = "Mashallah, ${now.name}"
                                    bannerKey++
                                }
                            }
                        },
                    )
                    // own Column scope: AnimatedVisibility cannot use the outer Column's scope implicitly
                    Column(
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AnimatedVisibility(
                            visible = banner != null,
                            enter = fadeIn() + scaleIn(initialScale = 0.9f),
                            exit = fadeOut(),
                        ) {
                            Surface(shape = RoundedCornerShape(50), color = look.accent) {
                                Text(
                                    banner ?: "",
                                    Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 17.sp,
                                    color = look.onAccent,
                                )
                            }
                        }
                    }
                    if (hint && banner == null) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 6.dp),
                            shape = RoundedCornerShape(50),
                            color = look.card.copy(alpha = 0.92f),
                            border = BorderStroke(1.dp, look.accent.copy(alpha = 0.5f)),
                        ) {
                            Text(
                                "Slide a bead up, or just tap",
                                Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = look.accent,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            containerColor = look.surface,
            title = { Text("Start over?") },
            text = { Text("This count goes back to 0. Your daily totals are kept.") },
            confirmButton = {
                TextButton(onClick = { vm.reset(); showReset = false }) { Text("Start over", color = look.accent) }
            },
            dismissButton = {
                TextButton(onClick = { showReset = false }) { Text("Keep counting", color = look.muted) }
            },
        )
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = look.surface,
            contentColor = look.onBg,
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Count to", style = MaterialTheme.typography.titleMedium)
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(33, 99, 100, 500, 1000, 0).forEach { t ->
                        FilterChip(
                            selected = s.target == t,
                            onClick = { vm.setTarget(t) },
                            label = { Text(if (t == 0) "Free" else "$t") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = look.accent,
                                selectedLabelColor = look.onAccent,
                                labelColor = look.onBg,
                            ),
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("Choose dhikr", style = MaterialTheme.typography.titleMedium)
                Dhikrs.groupBy { it.category }.forEach { (category, items) ->
                    Text(
                        category,
                        style = MaterialTheme.typography.labelLarge,
                        color = look.muted,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    items.forEach { d ->
                        val selected = d.id == s.dhikrId
                        Surface(
                            onClick = { vm.selectDhikr(d.id); showSheet = false },
                            shape = RoundedCornerShape(20.dp),
                            color = if (selected) look.card else look.card.copy(alpha = 0.45f),
                            border = if (selected) BorderStroke(1.5.dp, look.accent) else null,
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        d.arabic,
                                        fontSize = if (d.arabic.length > 28) 18.sp else 24.sp,
                                        lineHeight = if (d.arabic.length > 28) 30.sp else 38.sp,
                                        color = look.accent,
                                    )
                                    Text(d.transliteration, fontSize = 15.sp, color = look.onBg)
                                    Text(d.meaning, fontSize = 13.sp, color = look.muted)
                                }
                                if (selected) {
                                    Icon(Icons.Rounded.Check, contentDescription = "Selected", tint = look.accent)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.size(24.dp))
            }
        }
    }
}
