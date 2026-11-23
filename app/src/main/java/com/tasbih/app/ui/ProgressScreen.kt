package com.tasbih.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasbih.app.data.Hijri
import com.tasbih.app.data.History
import com.tasbih.app.data.TasbihState
import com.tasbih.app.data.Virtue
import com.tasbih.app.data.Virtues
import com.tasbih.app.data.dhikrById
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

private fun fmt(n: Number): String = NumberFormat.getInstance().format(n)

private fun dayLabel(date: LocalDate, today: LocalDate): String = when (date) {
    today -> "Today"
    today.minusDays(1) -> "Yesterday"
    else -> date.format(DateTimeFormatter.ofPattern("EEEE, d MMM", Locale.getDefault()))
}

@Composable
fun ProgressScreen(s: TasbihState, look: Look, history: History, todayLive: Int, bottomPadding: Dp) {
    val realToday = LocalDate.now()
    var sel by rememberSaveable { mutableStateOf(realToday.toEpochDay()) }
    val selDate = LocalDate.ofEpochDay(sel)
    val isToday = selDate == realToday
    val dayData = history.day(selDate)
    val total = if (isToday) max(dayData.total, todayLive) else dayData.total
    val goal = s.dailyGoal

    fun totalOn(d: LocalDate): Int = if (d == realToday) max(history.day(d).total, todayLive) else history.day(d).total

    LazyColumn(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = bottomPadding + 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ---- header ----
        item {
            Column {
                Text("Your dhikr", fontFamily = FontFamily.Serif, fontSize = 32.sp, color = look.accent)
                Text(
                    "${dayLabel(selDate, realToday)} · ${Hijri.format(selDate)}",
                    fontSize = 13.sp,
                    color = look.muted,
                )
            }
        }

        // ---- the last two weeks ----
        item {
            val days = remember(realToday) { (13 downTo 0).map { realToday.minusDays(it.toLong()) } }
            val listState = rememberLazyListState()
            LaunchedEffect(Unit) { listState.scrollToItem(max(0, days.lastIndex - 4)) }
            LazyRow(state = listState, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(days) { d ->
                    DayChip(
                        look = look,
                        date = d,
                        selected = d == selDate,
                        progress = if (goal > 0) totalOn(d) / goal.toFloat() else 0f,
                    ) { sel = d.toEpochDay() }
                }
            }
        }

        // ---- the day at a glance ----
        item {
            SectionCard(look) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    ProgressRing(
                        progress = if (goal > 0) total / goal.toFloat() else 0f,
                        diameter = 116.dp,
                        stroke = 10.dp,
                        color = look.accent,
                        track = look.line,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(fmt(total), fontFamily = FontFamily.Serif, fontSize = 30.sp, color = look.onBg)
                            Text("of ${fmt(goal)}", fontSize = 12.sp, color = look.muted)
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            when {
                                total == 0 && isToday -> "Begin with Bismillah"
                                total == 0 -> "Nothing counted this day"
                                goal in 1..total -> "Alhamdulillah, goal reached"
                                else -> "${fmt(goal - total)} to reach your goal"
                            },
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            color = look.onBg,
                        )
                        Text(
                            if (total == 0) "Even one SubhanAllah is a beginning." else "Dhikr said on this day.",
                            fontSize = 13.sp,
                            color = look.muted,
                        )
                    }
                }
            }
        }

        // ---- three numbers ----
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(look, "Consistent days", fmt(history.streak), Modifier.weight(1f))
                StatCard(look, "Best day", fmt(history.bestDay), Modifier.weight(1f))
                StatCard(look, "All time", fmt(history.allTime), Modifier.weight(1f))
            }
        }

        // ---- what was recited ----
        item {
            SectionCard(look, title = "Recited") {
                val rows = dayData.byDhikr.entries.filter { it.value > 0 }.sortedByDescending { it.value }
                if (rows.isEmpty()) {
                    Text("Your dhikr for this day will appear here.", fontSize = 14.sp, color = look.muted)
                } else {
                    val top = rows.first().value.coerceAtLeast(1)
                    rows.forEach { (id, n) ->
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(dhikrById(id).transliteration, Modifier.weight(1f), fontSize = 15.sp, color = look.onBg)
                                Text(fmt(n), fontFamily = FontFamily.Serif, fontSize = 18.sp, color = look.accent)
                            }
                            LinearProgressIndicator(
                                progress = { n / top.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp),
                                color = look.accent,
                                trackColor = look.line,
                                strokeCap = StrokeCap.Round,
                                gapSize = 0.dp,
                                drawStopIndicator = {},
                            )
                        }
                    }
                }
            }
        }

        // ---- glad tidings ----
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Glad tidings from the Sunnah", fontFamily = FontFamily.Serif, fontSize = 22.sp, color = look.accent)
                Text(
                    "Reported in authentic hadith. Your progress for this day is shown on each one.",
                    fontSize = 13.sp,
                    color = look.muted,
                )
            }
        }
        items(Virtues) { v ->
            val count = v.dhikrIds.sumOf { id -> dayData.byDhikr[id] ?: 0 }
            VirtueCard(look, v, count)
        }

        // ---- the last five weeks ----
        item {
            SectionCard(look, title = "The last weeks") {
                val first = remember { WeekFields.of(Locale.getDefault()).firstDayOfWeek }
                val start = remember(realToday) { realToday.minusDays(28).with(TemporalAdjusters.previousOrSame(first)) }
                val weeks = ((ChronoUnit.DAYS.between(start, realToday) + 7) / 7).toInt()
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (0..6).forEach { i ->
                        Text(
                            first.plus(i.toLong()).getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                            Modifier.weight(1f),
                            fontSize = 11.sp,
                            color = look.muted,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                for (w in 0 until weeks) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (d in 0..6) {
                            val date = start.plusDays((w * 7 + d).toLong())
                            if (date.isAfter(realToday)) {
                                Spacer(Modifier.weight(1f))
                            } else {
                                val t = totalOn(date)
                                val strength = if (t == 0) 0f else min(1f, 0.3f + 0.7f * (if (goal > 0) t / goal.toFloat() else 1f))
                                val shape = RoundedCornerShape(10.dp)
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(shape)
                                        .background(if (t == 0) look.line.copy(alpha = 0.35f) else look.accent.copy(alpha = strength))
                                        .then(if (date == selDate) Modifier.border(2.dp, look.onBg.copy(alpha = 0.6f), shape) else Modifier)
                                        .clickable { sel = date.toEpochDay() },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        date.dayOfMonth.toString(),
                                        fontSize = 11.sp,
                                        color = if (strength > 0.55f) look.onAccent else look.onBg.copy(alpha = 0.7f),
                                    )
                                }
                            }
                        }
                    }
                }
                Text("Tap a day to see it. Darker means closer to your daily goal.", fontSize = 12.sp, color = look.muted)
            }
        }

        // ---- a gentle reminder ----
        item {
            Text(
                "Only Allah knows the true reward of every dhikr. Let these numbers encourage consistency, never comparison. May Allah accept from you.",
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                color = look.muted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DayChip(look: Look, date: LocalDate, selected: Boolean, progress: Float, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) look.accent else look.card.copy(alpha = 0.6f),
        border = if (selected) null else BorderStroke(1.dp, look.line.copy(alpha = 0.7f)),
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                fontSize = 11.sp,
                color = if (selected) look.onAccent else look.muted,
            )
            Spacer(Modifier.height(6.dp))
            ProgressRing(
                progress = progress,
                diameter = 38.dp,
                stroke = 3.dp,
                color = if (selected) look.onAccent else look.accent,
                track = if (selected) look.onAccent.copy(alpha = 0.3f) else look.line,
            ) {
                Text(date.dayOfMonth.toString(), fontSize = 14.sp, color = if (selected) look.onAccent else look.onBg)
            }
        }
    }
}

@Composable
private fun StatCard(look: Look, label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier,
        shape = RoundedCornerShape(20.dp),
        color = look.card.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, look.line.copy(alpha = 0.6f)),
    ) {
        Column(Modifier.padding(vertical = 14.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontFamily = FontFamily.Serif, fontSize = 24.sp, color = look.accent)
            Text(label, fontSize = 11.sp, color = look.muted, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun VirtueCard(look: Look, v: Virtue, count: Int) {
    val done = v.goal > 0 && count >= v.goal
    SectionCard(look) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(v.title, Modifier.weight(1f), fontFamily = FontFamily.Serif, fontSize = 18.sp, color = look.onBg)
            if (done) {
                Box(
                    Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(look.accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = "Completed", tint = look.onAccent, modifier = Modifier.size(16.dp))
                }
            }
        }
        Text(v.arabic, fontSize = 20.sp, lineHeight = 32.sp, color = look.accent, modifier = Modifier.fillMaxWidth())
        Text(v.text, fontSize = 14.sp, lineHeight = 21.sp, color = look.onBg)
        if (v.goal > 0) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(
                    progress = { min(1f, count / v.goal.toFloat()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = look.accent,
                    trackColor = look.line,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                )
                Text(
                    if (done) "Completed · ${fmt(count)} said" else "${fmt(count)} of ${fmt(v.goal)}",
                    fontSize = 12.sp,
                    color = look.muted,
                )
            }
        } else {
            Text(
                if (count > 0) "${fmt(count)} said on this day" else "Not said on this day",
                fontSize = 12.sp,
                color = look.muted,
            )
        }
        Text(v.source, fontSize = 11.sp, fontStyle = FontStyle.Italic, color = look.muted)
    }
}
