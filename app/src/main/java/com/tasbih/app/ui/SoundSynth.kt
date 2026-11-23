package com.tasbih.app.ui

import com.tasbih.app.data.BeadEvent
import com.tasbih.app.data.SoundStyle
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Random
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/**
 * Builds the bead sounds in code, so the app needs no audio files and works offline.
 *
 * Design goal: a calm, dry click that never irritates — NOT a musical note.
 *  - 20–45 ms long: just the "tick" of one bead touching the next
 *  - inharmonic, fast-fading resonances (so there is no pitch to hear), a tiny noise burst and a soft low "body"
 *  - quiet (peak 0.5 of full scale) and low-passed, so even a few hundred beads in a row stay gentle
 *  - every variant is slightly different, and playback adds a small random pitch change, like real beads
 *
 * tools/sound_preview.py is a Python mirror of this file, for listening on a computer.
 */
object SoundSynth {
    const val RATE = 44100

    private class Voice(
        val modes: List<Triple<Double, Double, Double>>, // ratio, loudness, decay per second
        val base: Double,
        val bodyHz: Double,
        val bodyGain: Double,
        val bodyDecay: Double,
        val noise: Double,
        val noiseDecay: Double,
        val bandpass: Boolean,
        val lengthMs: Int,
        val soften: Double,
    )

    private class Hit(val atMs: Int, val pitch: Double, val amp: Double)

    private fun voice(style: SoundStyle, rnd: Random): Voice {
        val j = 1.0 + (rnd.nextDouble() * 2 - 1) * 0.05
        return if (style == SoundStyle.Soft) {
            Voice(
                modes = listOf(Triple(1.0, 1.0, 300.0), Triple(1.83, 0.45, 430.0), Triple(2.9, 0.22, 600.0)),
                base = 1850.0 * j,
                bodyHz = 380.0 * j, bodyGain = 0.28, bodyDecay = 150.0,
                noise = 0.55, noiseDecay = 950.0, bandpass = true,
                lengthMs = 46, soften = 0.55,
            )
        } else {
            Voice(
                modes = listOf(Triple(1.0, 1.0, 120.0), Triple(2.15, 0.55, 180.0), Triple(3.6, 0.25, 270.0)),
                base = 760.0 * j,
                bodyHz = 250.0 * j, bodyGain = 0.4, bodyDecay = 95.0,
                noise = 0.45, noiseDecay = 700.0, bandpass = false,
                lengthMs = 85, soften = 0.8,
            )
        }
    }

    fun render(style: SoundStyle, event: BeadEvent, seed: Long): ShortArray {
        val rnd = Random(seed)
        val v = voice(style, rnd)
        val hits = when (event) {
            BeadEvent.TICK -> listOf(Hit(0, 1.0, 1.0))
            BeadEvent.MARKER -> listOf(Hit(0, 0.78, 1.0), Hit(40, 0.78, 0.6))
            BeadEvent.COMPLETE -> listOf(Hit(0, 0.7, 1.0), Hit(95, 0.7, 0.75))
        }
        val peakTarget = if (event == BeadEvent.TICK) 0.5 else 0.6
        val n = RATE * (hits.last().atMs + v.lengthMs) / 1000
        val out = DoubleArray(n)

        for (h in hits) {
            val start = RATE * h.atMs / 1000
            var l1 = 0.0
            var l2 = 0.0
            for (i in 0 until RATE * v.lengthMs / 1000) {
                if (start + i >= n) break
                val t = i / RATE.toDouble()
                var s = 0.0
                for ((ratio, a, decay) in v.modes) {
                    s += a * sin(2 * PI * v.base * h.pitch * ratio * t) * exp(-decay * t)
                }
                s += v.bodyGain * sin(2 * PI * v.bodyHz * h.pitch * t) * exp(-v.bodyDecay * t)
                val r = rnd.nextDouble() * 2 - 1
                val noise = if (v.bandpass) {
                    l1 += 0.7 * (r - l1)
                    l2 += 0.15 * (r - l2)
                    l1 - l2
                } else {
                    l1 += 0.5 * (r - l1)
                    l1
                }
                s += noise * v.noise * exp(-t * v.noiseDecay)
                out[start + i] += s * h.amp * min(1.0, t / 0.0006)
            }
        }

        // soften the top end, then set the level and fade the last 3 ms so it can never click
        var y = 0.0
        var peak = 1e-9
        for (i in 0 until n) {
            y += v.soften * (out[i] - y)
            out[i] = y
            if (kotlin.math.abs(y) > peak) peak = kotlin.math.abs(y)
        }
        val gain = peakTarget / peak
        val fade = RATE * 3 / 1000
        return ShortArray(n) { i ->
            var x = out[i] * gain
            if (i >= n - fade) x *= (n - 1 - i).toDouble() / fade
            (x.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
    }

    /** 16-bit mono PCM wrapped in a minimal WAV header. */
    fun toWav(samples: ShortArray): ByteArray {
        val dataSize = samples.size * 2
        val buf = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)
        buf.put("RIFF".toByteArray()).putInt(36 + dataSize).put("WAVE".toByteArray())
        buf.put("fmt ".toByteArray()).putInt(16).putShort(1).putShort(1).putInt(RATE).putInt(RATE * 2).putShort(2).putShort(16)
        buf.put("data".toByteArray()).putInt(dataSize)
        for (s in samples) buf.putShort(s)
        return buf.array()
    }
}
