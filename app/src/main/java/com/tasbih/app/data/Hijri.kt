package com.tasbih.app.data

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

/** Islamic date from the Umm al-Qura calendar. It can differ by a day from your local moon sighting. */
object Hijri {
    private val months = listOf(
        "Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani", "Jumada al-Awwal", "Jumada al-Thani",
        "Rajab", "Shaban", "Ramadan", "Shawwal", "Dhul Qadah", "Dhul Hijjah",
    )

    fun format(date: LocalDate): String = runCatching {
        val h = HijrahDate.from(date)
        "${h.get(ChronoField.DAY_OF_MONTH)} ${months[h.get(ChronoField.MONTH_OF_YEAR) - 1]} ${h.get(ChronoField.YEAR)} AH"
    }.getOrDefault("")
}
