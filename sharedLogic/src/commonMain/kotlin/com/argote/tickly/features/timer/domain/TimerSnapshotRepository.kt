package com.argote.tickly.features.timer.domain

/** Persistence boundary for the versioned [TimerEngine] snapshot. */
interface TimerSnapshotRepository {
    fun readSnapshot(): String?

    fun writeSnapshot(snapshot: String)
}
