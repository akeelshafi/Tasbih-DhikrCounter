package com.tasbih.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StatsTest {
    private val today = LocalDate.of(2026, 11, 22)

    private fun row(daysAgo: Long, id: String, count: Int) =
        DailyDhikr(today.minusDays(daysAgo).toString(), id, count)

    @Test
    fun empty_history_is_ready_and_zero() {
        val h = computeHistory(emptyList(), today)
        assertTrue(h.ready)
        assertEquals(0, h.today)
        assertEquals(0, h.streak)
        assertEquals(0L, h.allTime)
    }

    @Test
    fun streak_counts_consecutive_days_ending_today() {
        val h = computeHistory(listOf(row(0, "subhanallah", 5), row(1, "subhanallah", 5), row(2, "istighfar", 1), row(4, "durood", 9)), today)
        assertEquals(3, h.streak)
    }

    @Test
    fun streak_is_not_broken_until_today_is_over() {
        val h = computeHistory(listOf(row(1, "subhanallah", 5), row(2, "subhanallah", 5)), today)
        assertEquals(2, h.streak)
    }

    @Test
    fun streak_is_zero_after_a_missed_day() {
        val h = computeHistory(listOf(row(2, "subhanallah", 5)), today)
        assertEquals(0, h.streak)
    }

    @Test
    fun totals_best_day_and_breakdown() {
        val h = computeHistory(
            listOf(row(0, "subhanallah", 33), row(0, "alhamdulillah", 33), row(3, "durood", 100)),
            today,
        )
        assertEquals(66, h.today)
        assertEquals(100, h.bestDay)
        assertEquals(166L, h.allTime)
        assertEquals(2, h.activeDays)
        assertEquals(33, h.day(today).byDhikr["alhamdulillah"])
        assertEquals(0, h.day(today.minusDays(1)).total)
    }

    @Test
    fun a_bad_date_in_the_database_is_ignored_not_a_crash() {
        val h = computeHistory(listOf(DailyDhikr("not-a-date", "subhanallah", 5), row(0, "subhanallah", 2)), today)
        assertEquals(2, h.allTime)
    }
}
