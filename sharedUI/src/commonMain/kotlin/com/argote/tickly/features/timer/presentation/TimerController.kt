package com.argote.tickly.features.timer.presentation

import com.argote.tickly.features.settings.domain.TimerSettings
import com.argote.tickly.features.timer.domain.TimerEngine
import com.argote.tickly.features.timer.domain.TimerSnapshotRepository
import com.argote.tickly.features.timer.domain.TimerStatus

/**
 * Presentation coordinator for timer interactions. It owns the mutable engine and
 * persists only meaningful snapshot changes through a domain boundary.
 */
class TimerController(private val snapshots: TimerSnapshotRepository) {
    val engine = TimerEngine(snapshots.readSnapshot())
    private var lastSnapshot: String? = null

    init { persistIfChanged() }

    fun tick(nowMillis: Long) = mutate { engine.tick(nowMillis) }
    fun start(nowMillis: Long) = mutate { engine.start(nowMillis) }
    fun pause(nowMillis: Long) = mutate { engine.pause(nowMillis) }
    fun restart(nowMillis: Long) = mutate { engine.restart(nowMillis) }
    fun skip(nowMillis: Long) = mutate { engine.skip(nowMillis) }
    fun reset(nowMillis: Long) = mutate { engine.reset(nowMillis) }
    fun updateSettings(settings: TimerSettings, nowMillis: Long) = mutate { engine.updateSettings(settings, nowMillis) }

    fun isRunning(): Boolean = engine.status == TimerStatus.RUNNING
    fun persistIfChanged(): Boolean {
        val snapshot = engine.serialize()
        if (snapshot == lastSnapshot) return false
        snapshots.writeSnapshot(snapshot)
        lastSnapshot = snapshot
        return true
    }

    private inline fun mutate(action: () -> Unit) {
        action()
        persistIfChanged()
    }
}
