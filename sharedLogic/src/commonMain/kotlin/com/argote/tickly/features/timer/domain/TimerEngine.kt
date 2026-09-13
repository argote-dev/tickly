package com.argote.tickly.features.timer.domain

import com.argote.tickly.features.settings.domain.TimerSettings

/** The kind of interval currently displayed by [TimerEngine]. */
enum class TimerPhase { FOCUS, SHORT_BREAK, LONG_BREAK }

/** The lifecycle of the interval currently displayed by [TimerEngine]. */
enum class TimerStatus { READY, RUNNING, PAUSED, FINISHED }

/**
 * A deterministic pomodoro state machine. Time is always supplied by the caller
 * in milliseconds, which keeps this type independent of platform clocks and easy
 * to restore after the process has been killed.
 */
class TimerEngine(snapshot: String? = null) {
    var settings: TimerSettings = TimerSettings().validated()
        private set
    var phase: TimerPhase = TimerPhase.FOCUS
        private set
    var status: TimerStatus = TimerStatus.READY
        private set
    var completedFocusBlocks: Int = 0
        private set
    var intervalDurationMillis: Long = settings.durationFor(phase)
        private set
    var deadlineMillis: Long = 0L
        private set

    private var remainingAtRestMillis: Long = intervalDurationMillis
    // Deliberately stored at focus completion: an edit cannot replace a break
    // type which is already waiting for confirmation (its duration may update).
    private var nextPhase: TimerPhase = TimerPhase.FOCUS
    init {
        if (snapshot != null) restore(snapshot)
    }

    fun remainingMillis(nowMillis: Long): Long = when (status) {
        TimerStatus.RUNNING -> (deadlineMillis - nowMillis).coerceIn(0L, intervalDurationMillis)
        else -> remainingAtRestMillis.coerceIn(0L, intervalDurationMillis)
    }

    /** Records a completed interval when its absolute deadline has passed. */
    fun tick(nowMillis: Long) {
        if (status != TimerStatus.RUNNING || nowMillis < deadlineMillis) return
        remainingAtRestMillis = 0L
        deadlineMillis = 0L
        status = TimerStatus.FINISHED
        nextPhase = when (phase) {
            TimerPhase.FOCUS -> {
                completedFocusBlocks += 1
                val breakPhase = if (completedFocusBlocks >= settings.blocksUntilLongBreak) {
                    TimerPhase.LONG_BREAK
                } else {
                    TimerPhase.SHORT_BREAK
                }
                breakPhase
            }
            TimerPhase.SHORT_BREAK -> TimerPhase.FOCUS
            TimerPhase.LONG_BREAK -> {
                completedFocusBlocks = 0
                TimerPhase.FOCUS
            }
        }
    }

    /** Starts a ready timer, resumes a paused one, or starts the next interval after completion. */
    fun start(nowMillis: Long) {
        tick(nowMillis)
        when (status) {
            TimerStatus.RUNNING -> return
            TimerStatus.FINISHED -> prepare(nextPhase)
            TimerStatus.READY, TimerStatus.PAUSED -> Unit
        }
        deadlineMillis = safeAdd(nowMillis, remainingAtRestMillis)
        status = TimerStatus.RUNNING
    }

    fun pause(nowMillis: Long) {
        tick(nowMillis)
        if (status != TimerStatus.RUNNING) return
        remainingAtRestMillis = remainingMillis(nowMillis)
        deadlineMillis = 0L
        status = TimerStatus.PAUSED
    }

    /** Returns the same interval to its originally captured duration without changing cycle progress. */
    fun restart(nowMillis: Long) {
        deadlineMillis = 0L
        remainingAtRestMillis = intervalDurationMillis
        status = TimerStatus.READY
    }

    /** Abandons the current interval without counting a focus interval. */
    fun skip(nowMillis: Long) {
        tick(nowMillis)
        if (status == TimerStatus.FINISHED) {
            prepare(nextPhase)
            return
        }
        when (phase) {
            TimerPhase.FOCUS -> prepare(TimerPhase.SHORT_BREAK)
            TimerPhase.SHORT_BREAK -> prepare(TimerPhase.FOCUS)
            TimerPhase.LONG_BREAK -> {
                completedFocusBlocks = 0
                prepare(TimerPhase.FOCUS)
            }
        }
    }

    /** Clears cycle progress and returns to a manual, ready focus interval. */
    fun reset(nowMillis: Long) {
        completedFocusBlocks = 0
        prepare(TimerPhase.FOCUS)
    }

    /**
     * Stores new preferences. A live, paused, or completed interval retains its
     * captured duration; a ready interval is the next interval and is refreshed.
     */
    fun updateSettings(settings: TimerSettings, nowMillis: Long) {
        tick(nowMillis)
        this.settings = settings.validated()
        if (status == TimerStatus.READY) prepare(phase)
    }

    /** A small, versioned, dependency-free persistence format. Invalid input restores defaults. */
    fun serialize(): String = listOf(
        "tickly", "1",
        "fm=${settings.focusMinutes}", "sm=${settings.shortBreakMinutes}",
        "lm=${settings.longBreakMinutes}", "lb=${settings.blocksUntilLongBreak}",
        "ai=${settings.accentIndex}", "si=${settings.soundIndex}",
        "v=${if (settings.vibrationEnabled) 1 else 0}",
        "ab=${if (settings.animatedBackground) 1 else 0}",
        "ks=${if (settings.keepScreenOn) 1 else 0}", "lang=${settings.language}",
        "p=${phase.name}", "s=${status.name}", "cb=$completedFocusBlocks",
        "d=$intervalDurationMillis", "r=$remainingAtRestMillis", "dl=$deadlineMillis",
        "n=${nextPhase.name}",
    ).joinToString("|")

    private fun prepare(newPhase: TimerPhase) {
        phase = newPhase
        intervalDurationMillis = settings.durationFor(newPhase)
        remainingAtRestMillis = intervalDurationMillis
        deadlineMillis = 0L
        status = TimerStatus.READY
        nextPhase = TimerPhase.FOCUS
    }

    private fun restore(snapshot: String) {
        val fields = snapshot.split('|')
        if (fields.size < 2 || fields[0] != "tickly" || fields[1] != "1") return
        val map = fields.drop(2).mapNotNull {
            val separator = it.indexOf('=')
            if (separator <= 0) null else it.substring(0, separator) to it.substring(separator + 1)
        }.toMap()
        try {
            val restoredSettings = TimerSettings(
                focusMinutes = map.int("fm"), shortBreakMinutes = map.int("sm"),
                longBreakMinutes = map.int("lm"), blocksUntilLongBreak = map.int("lb"),
                accentIndex = map.int("ai"), soundIndex = map.int("si"),
                vibrationEnabled = map.bool("v"), animatedBackground = map.bool("ab"),
                keepScreenOn = map.bool("ks"), language = map.required("lang"),
            ).validated()
            val restoredPhase = enumValueOf<TimerPhase>(map.required("p"))
            val restoredStatus = enumValueOf<TimerStatus>(map.required("s"))
            val restoredBlocks = map.int("cb")
            val duration = map.long("d")
            val rest = map.long("r")
            val deadline = map.long("dl")
            val pending = enumValueOf<TimerPhase>(map.required("n"))
            if (restoredBlocks !in 0..8 || !duration.isValidDuration(restoredPhase) || rest !in 0..duration ||
                deadline < 0L || (restoredStatus == TimerStatus.RUNNING && deadline == 0L) ||
                (restoredStatus != TimerStatus.RUNNING && deadline != 0L)
            ) return
            settings = restoredSettings
            phase = restoredPhase
            status = restoredStatus
            completedFocusBlocks = restoredBlocks
            intervalDurationMillis = duration
            remainingAtRestMillis = rest
            deadlineMillis = deadline
            nextPhase = pending
        } catch (_: IllegalArgumentException) {
            // A corrupt or obsolete snapshot must never prevent opening the timer.
        }
    }

    private fun Map<String, String>.required(key: String): String = get(key) ?: throw IllegalArgumentException()
    private fun Map<String, String>.int(key: String): Int = required(key).toInt()
    private fun Map<String, String>.long(key: String): Long = required(key).toLong()
    private fun Map<String, String>.bool(key: String): Boolean = when (required(key)) {
        "1" -> true
        "0" -> false
        else -> throw IllegalArgumentException()
    }

    private fun Long.isValidDuration(forPhase: TimerPhase): Boolean = this in 1_000L..forPhase.maxDurationMillis()
    private fun TimerPhase.maxDurationMillis(): Long = if (this == TimerPhase.FOCUS) 180L * MINUTE else 60L * MINUTE
    private fun safeAdd(left: Long, right: Long): Long = if (left > Long.MAX_VALUE - right) Long.MAX_VALUE else left + right

    private companion object { const val MINUTE = 60_000L }
}

private fun TimerSettings.durationFor(phase: TimerPhase): Long = when (phase) {
    TimerPhase.FOCUS -> focusMinutes.toLong() * 60_000L
    TimerPhase.SHORT_BREAK -> shortBreakMinutes.toLong() * 60_000L
    TimerPhase.LONG_BREAK -> longBreakMinutes.toLong() * 60_000L
}
