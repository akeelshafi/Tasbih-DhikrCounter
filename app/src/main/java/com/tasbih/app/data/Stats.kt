package com.tasbih.app.data

import java.time.LocalDate

data class DayTotal(val total: Int, val byDhikr: Map<String, Int>)

data class History(
    val ready: Boolean = false,
    val date: LocalDate = LocalDate.now(),
    val days: Map<LocalDate, DayTotal> = emptyMap(),
    val today: Int = 0,
    /** Days in a row with at least one dhikr (today may still be open). */
    val streak: Int = 0,
    val bestDay: Int = 0,
    val allTime: Long = 0,
    val activeDays: Int = 0,
) {
    fun day(d: LocalDate): DayTotal = days[d] ?: DayTotal(0, emptyMap())
}

fun computeHistory(rows: List<DailyDhikr>, today: LocalDate): History {
    val days = rows.groupBy { it.date }.mapNotNull { (key, list) ->
        val date = runCatching { LocalDate.parse(key) }.getOrNull() ?: return@mapNotNull null
        date to DayTotal(list.sumOf { it.count }, list.associate { it.dhikrId to it.count })
    }.toMap()

    var d = if ((days[today]?.total ?: 0) > 0) today else today.minusDays(1)
    var streak = 0
    while ((days[d]?.total ?: 0) > 0) {
        streak++
        d = d.minusDays(1)
    }

    return History(
        ready = true,
        date = today,
        days = days,
        today = days[today]?.total ?: 0,
        streak = streak,
        bestDay = days.values.maxOfOrNull { it.total } ?: 0,
        allTime = days.values.sumOf { it.total.toLong() },
        activeDays = days.values.count { it.total > 0 },
    )
}
