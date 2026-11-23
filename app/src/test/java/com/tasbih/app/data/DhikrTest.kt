package com.tasbih.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DhikrTest {
    @Test
    fun after_prayer_changes_words_every_33_and_ends_with_tahlil() {
        val d = dhikrById("after_prayer")
        assertEquals("subhanallah", d.at(0).id)
        assertEquals("subhanallah", d.at(32).id)
        assertEquals("alhamdulillah", d.at(33).id)
        assertEquals("allahuakbar", d.at(66).id)
        assertEquals("tahlil", d.at(99).id)
        assertEquals(100, d.target)
    }

    @Test
    fun before_sleep_is_33_33_34() {
        val d = dhikrById("before_sleep")
        assertEquals("allahuakbar", d.at(66).id)
        assertEquals("allahuakbar", d.at(99).id)
    }

    @Test
    fun a_plain_dhikr_does_not_change() {
        val d = dhikrById("istighfar")
        assertEquals(d, d.at(50))
    }

    @Test
    fun unknown_id_falls_back_to_the_first_dhikr() {
        assertEquals(Dhikrs[0], dhikrById("does-not-exist"))
    }

    @Test
    fun every_virtue_points_to_a_real_dhikr_and_has_a_source() {
        val ids = Dhikrs.map { it.id }.toSet()
        for (v in Virtues) {
            assertTrue("virtue ${v.id} has an unknown dhikr", v.dhikrIds.all { it in ids })
            assertTrue("virtue ${v.id} needs a source", v.source.isNotBlank())
        }
    }

    @Test
    fun dhikr_ids_are_unique() {
        assertEquals(Dhikrs.size, Dhikrs.map { it.id }.toSet().size)
    }

    @Test
    fun every_dhikr_has_a_category_and_a_sensible_target() {
        for (d in Dhikrs) {
            assertTrue("${d.id} needs a category", d.category.isNotBlank())
            assertTrue("${d.id} needs a positive target", d.target > 0)
            assertTrue("${d.id} needs Arabic text", d.arabic.isNotBlank())
        }
    }
}
