package com.tasbih.app.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.tasbih.app.data.BeadEvent
import com.tasbih.app.data.SoundStyle
import java.io.File
import java.util.Random
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

/**
 * Bead sound + haptics. Everything here is wrapped so a device quirk can never crash the app —
 * the worst case is that a bead is silent.
 *
 * Sounds are generated once (see [SoundSynth]) and played through one SoundPool: low latency, several taps can
 * overlap, and each play gets a tiny random pitch change. They use the "system sounds" volume, so a phone on
 * silent or vibrate stays quiet — which is what people expect in a mosque.
 */
class Feedback(private val context: Context) {
    private val random = Random()

    private val pool: SoundPool? = runCatching {
        SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }.getOrNull()

    private val samples = ConcurrentHashMap<String, List<Int>>()
    private val loaded: MutableSet<Int> = ConcurrentHashMap.newKeySet()

    @Volatile private var released = false

    private val vibrator: Vibrator? = runCatching {
        if (Build.VERSION.SDK_INT >= 31) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }
    }.getOrNull()

    init {
        pool?.setOnLoadCompleteListener { _, id, status -> if (status == 0) loaded.add(id) }
        // building ~14 tiny sounds takes a few milliseconds, but still keep it off the main thread
        thread(name = "tasbih-sounds", isDaemon = true) { runCatching { buildSamples() } }
    }

    private fun buildSamples() {
        val dir = File(context.cacheDir, "sounds").apply { mkdirs() }
        for (style in listOf(SoundStyle.Soft, SoundStyle.Wood)) {
            for (event in BeadEvent.entries) {
                val variants = if (event == BeadEvent.TICK) 5 else 1
                val ids = (0 until variants).mapNotNull { v ->
                    if (released) return@mapNotNull null
                    val file = File(dir, "${style.name}_${event.name}_$v.wav")
                    file.writeBytes(SoundSynth.toWav(SoundSynth.render(style, event, seed = v + 1L)))
                    pool?.load(file.absolutePath, 1)
                }
                samples["${style.name}_${event.name}"] = ids
            }
        }
    }

    fun play(event: BeadEvent, style: SoundStyle, volume: Float, haptics: Boolean) {
        if (style != SoundStyle.Off && volume > 0.01f) playSound(event, style, volume.coerceIn(0f, 1f))
        if (haptics) vibrate(event)
    }

    fun preview(style: SoundStyle, volume: Float) = play(BeadEvent.TICK, style, volume, haptics = false)

    fun release() {
        released = true
        runCatching { pool?.release() }
    }

    private fun playSound(event: BeadEvent, style: SoundStyle, volume: Float) {
        val p = pool ?: return
        val ready = samples["${style.name}_${event.name}"]?.filter { it in loaded }
        if (ready.isNullOrEmpty()) return
        val id = ready[random.nextInt(ready.size)]
        val rate = 0.96f + random.nextFloat() * 0.08f
        runCatching { p.play(id, volume, volume, 1, 0, rate) }
    }

    private fun vibrate(event: BeadEvent) {
        val v = vibrator ?: return
        runCatching {
            if (!v.hasVibrator()) return
            when (event) {
                BeadEvent.TICK ->
                    if (Build.VERSION.SDK_INT >= 29) v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                    else v.vibrate(VibrationEffect.createOneShot(10, 80))
                BeadEvent.MARKER ->
                    if (Build.VERSION.SDK_INT >= 29) v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    else v.vibrate(VibrationEffect.createOneShot(30, 160))
                BeadEvent.COMPLETE ->
                    v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 80, 60), intArrayOf(0, 150, 0, 220), -1))
            }
        }
    }
}
