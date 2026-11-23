package com.tasbih.app.data

import android.app.Application
import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

// A damaged settings file is replaced by defaults instead of crashing the app.
private val Context.dataStore by preferencesDataStore(
    name = "tasbih",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
)

enum class BeadEvent { TICK, MARKER, COMPLETE }

/** Order matters only for what is stored on the phone. Soft is the default. */
enum class SoundStyle { Soft, Wood, Off }

data class BeadResult(val event: BeadEvent, val goalReached: Boolean)

data class TasbihState(
    val loaded: Boolean = false,
    val name: String = "",
    val dhikrId: String = Dhikrs[0].id,
    val count: Int = 0,
    /** 0 = free counting, no target. */
    val target: Int = 100,
    val dailyGoal: Int = 100,
    val lookIndex: Int = 0,
    val sound: SoundStyle = SoundStyle.Soft,
    val volume: Float = 0.7f,
    val haptics: Boolean = true,
    val keepAwake: Boolean = true,
    val tutorialDone: Boolean = false,
    /** Bumps whenever the count is changed from outside the beads (reset, new dhikr...). */
    val epoch: Int = 0,
) {
    val dhikr: Dhikr get() = dhikrById(dhikrId)
}

private object K {
    val name = stringPreferencesKey("name")
    val dhikr = stringPreferencesKey("dhikr")
    val count = intPreferencesKey("count")
    val target = intPreferencesKey("target")
    val goal = intPreferencesKey("goal")
    val look = intPreferencesKey("look")
    val sound = intPreferencesKey("sound")
    val volume = floatPreferencesKey("volume")
    val haptics = booleanPreferencesKey("haptics")
    val awake = booleanPreferencesKey("awake")
    val tutorial = booleanPreferencesKey("tutorial")
}

@OptIn(FlowPreview::class)
class TasbihViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = TasbihDb.get(app).dao()
    private val store = app.applicationContext.dataStore

    private val _state = MutableStateFlow(TasbihState())
    val state: StateFlow<TasbihState> = _state.asStateFlow()

    /** Live count for today. Updates the instant a bead is counted. */
    private val _today = MutableStateFlow(0)
    val today: StateFlow<Int> = _today.asStateFlow()

    // If the database ever cannot be read, the app keeps working with an empty history instead of crashing.
    val history: StateFlow<History> = combine(
        dao.observeAll().catch { emit(emptyList()) },
        dateTicker(),
    ) { rows, date -> computeHistory(rows, date) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, History())

    // every counted bead goes through one queue, so writes stay in order
    private val writes = Channel<Pair<String, String>>(Channel.UNLIMITED)
    private val pending = AtomicInteger(0)
    private var liveDate: LocalDate = LocalDate.now()
    private val saveTick = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        viewModelScope.launch {
            for ((date, id) in writes) {
                try {
                    dao.add(date, id, 1)
                } catch (e: Exception) {
                    // a failed write must never take the app down; the on-screen count is still correct
                } finally {
                    pending.decrementAndGet()
                }
            }
        }
        viewModelScope.launch {
            history.collect { h ->
                if (h.ready && pending.get() == 0) {
                    liveDate = h.date
                    _today.value = h.today
                }
            }
        }
        viewModelScope.launch {
            // if settings cannot be read, start with defaults — never leave the user on a blank splash
            val p: Preferences = try {
                store.data.first()
            } catch (e: Exception) {
                emptyPreferences()
            }
            val dhikr = dhikrById(p[K.dhikr] ?: Dhikrs[0].id)
            val target = (p[K.target] ?: dhikr.target).coerceAtLeast(0)
            val count = (p[K.count] ?: 0).coerceAtLeast(0)
            _state.value = TasbihState(
                loaded = true,
                name = (p[K.name] ?: "").take(24),
                dhikrId = dhikr.id,
                count = if (target > 0 && count >= target) 0 else count,
                target = target,
                dailyGoal = (p[K.goal] ?: 100).coerceIn(1, 100_000),
                lookIndex = p[K.look] ?: 0,
                sound = SoundStyle.entries.getOrElse(p[K.sound] ?: 0) { SoundStyle.Soft },
                volume = (p[K.volume] ?: 0.7f).coerceIn(0f, 1f),
                haptics = p[K.haptics] ?: true,
                keepAwake = p[K.awake] ?: true,
                tutorialDone = p[K.tutorial] ?: false,
            )
        }
        viewModelScope.launch {
            saveTick.debounce(300).collect { persist() }
        }
    }

    private fun dateTicker() = flow {
        while (true) {
            emit(LocalDate.now())
            delay(20_000)
        }
    }.distinctUntilChanged()

    /** Called once for every bead that passes the thumb. */
    fun increment(): BeadResult {
        val now = LocalDate.now()
        if (now != liveDate) {
            liveDate = now
            _today.value = 0
        }
        val before = _state.value
        var event = BeadEvent.TICK
        _state.update { s ->
            val next = s.count + 1
            when {
                s.target > 0 && next >= s.target -> {
                    event = BeadEvent.COMPLETE
                    s.copy(count = 0)
                }
                s.target >= 99 && next % 33 == 0 -> {
                    event = BeadEvent.MARKER
                    s.copy(count = next)
                }
                else -> {
                    event = BeadEvent.TICK
                    s.copy(count = next)
                }
            }
        }
        val total = _today.updateAndGet { it + 1 }
        pending.incrementAndGet()
        writes.trySend(now.toString() to before.dhikrId)
        saveTick.tryEmit(Unit)
        return BeadResult(event, before.dailyGoal > 0 && total == before.dailyGoal)
    }

    fun setName(name: String) {
        val clean = name.trim().take(24)
        if (clean.isNotEmpty()) change { it.copy(name = clean) }
    }

    fun reset() = change { it.copy(count = 0, epoch = it.epoch + 1) }

    fun selectDhikr(id: String) = change {
        val d = dhikrById(id)
        it.copy(dhikrId = d.id, count = 0, target = d.target, epoch = it.epoch + 1)
    }

    fun setTarget(target: Int) = change {
        it.copy(target = target, count = if (target > 0 && it.count >= target) 0 else it.count, epoch = it.epoch + 1)
    }

    fun setDailyGoal(goal: Int) = change { it.copy(dailyGoal = goal) }
    fun setLook(index: Int) = change { it.copy(lookIndex = index) }
    fun setSound(style: SoundStyle) = change { it.copy(sound = style) }
    fun setVolume(volume: Float) = change { it.copy(volume = volume.coerceIn(0f, 1f)) }
    fun toggleHaptics() = change { it.copy(haptics = !it.haptics) }
    fun toggleKeepAwake() = change { it.copy(keepAwake = !it.keepAwake) }
    fun setTutorialDone() = change { it.copy(tutorialDone = true) }
    fun showTutorial() = change { it.copy(tutorialDone = false) }

    fun clearHistory() {
        viewModelScope.launch {
            try {
                dao.clear()
            } catch (e: Exception) {
                // ignore: nothing else to do
            }
            _today.value = 0
        }
    }

    /** Save right now (called when the app goes to the background). */
    fun flush() = persist()

    private fun change(block: (TasbihState) -> TasbihState) {
        _state.update(block)
        persist()
    }

    private fun persist() {
        val s = _state.value
        if (!s.loaded) return
        viewModelScope.launch {
            try {
                store.edit {
                    it[K.name] = s.name
                    it[K.dhikr] = s.dhikrId
                    it[K.count] = s.count
                    it[K.target] = s.target
                    it[K.goal] = s.dailyGoal
                    it[K.look] = s.lookIndex
                    it[K.sound] = s.sound.ordinal
                    it[K.volume] = s.volume
                    it[K.haptics] = s.haptics
                    it[K.awake] = s.keepAwake
                    it[K.tutorial] = s.tutorialDone
                }
            } catch (e: Exception) {
                // disk full or similar: skip this save, the next one will try again
            }
        }
    }
}
