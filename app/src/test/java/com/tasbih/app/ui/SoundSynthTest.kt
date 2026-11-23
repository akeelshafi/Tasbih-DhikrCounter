package com.tasbih.app.ui

import com.tasbih.app.data.BeadEvent
import com.tasbih.app.data.SoundStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class SoundSynthTest {
    @Test
    fun every_sound_is_short_and_never_loud() {
        for (style in listOf(SoundStyle.Soft, SoundStyle.Wood)) {
            for (event in BeadEvent.entries) {
                val s = SoundSynth.render(style, event, 1)
                assertTrue(s.isNotEmpty())
                val ms = s.size * 1000 / SoundSynth.RATE
                assertTrue("$style $event is too long: $ms ms", ms <= 250)
                val peak = s.maxOf { abs(it.toInt()) } / 32767.0
                assertTrue("$style $event peak $peak", peak in 0.45..0.65)
            }
        }
    }

    @Test
    fun a_plain_bead_tick_is_under_100_ms() {
        for (style in listOf(SoundStyle.Soft, SoundStyle.Wood)) {
            val ms = SoundSynth.render(style, BeadEvent.TICK, 1).size * 1000 / SoundSynth.RATE
            assertTrue(ms < 100)
        }
    }

    @Test
    fun variants_are_not_identical() {
        val a = SoundSynth.render(SoundStyle.Soft, BeadEvent.TICK, 1)
        val b = SoundSynth.render(SoundStyle.Soft, BeadEvent.TICK, 2)
        assertFalse(a.contentEquals(b))
    }

    @Test
    fun wav_header_is_valid() {
        val wav = SoundSynth.toWav(shortArrayOf(1, 2, 3))
        assertEquals("RIFF", String(wav, 0, 4))
        assertEquals("WAVE", String(wav, 8, 4))
        assertEquals(44 + 6, wav.size)
    }
}
