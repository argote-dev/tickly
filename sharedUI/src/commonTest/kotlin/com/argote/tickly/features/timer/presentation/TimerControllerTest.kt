package com.argote.tickly.features.timer.presentation

import com.argote.tickly.features.settings.domain.TimerSettings
import com.argote.tickly.features.timer.domain.TimerSnapshotRepository
import com.argote.tickly.features.timer.domain.TimerStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimerControllerTest {
    @Test
    fun `loads and persists engine snapshots through injected repository`() {
        val repository = FakeSnapshots()
        val controller = TimerController(repository)

        controller.updateSettings(TimerSettings(focusMinutes = 42), nowMillis = 1)
        controller.start(nowMillis = 10)

        val restored = TimerController(FakeSnapshots(repository.writes.last()))
        assertEquals(42, restored.engine.settings.focusMinutes)
        assertEquals(60_000L * 42 + 10, restored.engine.deadlineMillis)
    }

    @Test
    fun `unchanged ticks do not duplicate persistence writes`() {
        val repository = FakeSnapshots()
        val controller = TimerController(repository)
        val initialWrites = repository.writes.size

        controller.tick(nowMillis = 1)
        controller.tick(nowMillis = 2)

        assertEquals(initialWrites, repository.writes.size)
    }

    @Test
    fun `pause resume completion and settings changes each persist state`() {
        val repository = FakeSnapshots()
        val controller = TimerController(repository)
        controller.updateSettings(TimerSettings(focusMinutes = 1), nowMillis = 0)
        controller.start(nowMillis = 10)
        controller.pause(nowMillis = 110)
        val pausedSnapshot = repository.writes.last()

        controller.start(nowMillis = 200)
        controller.tick(nowMillis = controller.engine.deadlineMillis)

        assertTrue(repository.writes.contains(pausedSnapshot))
        assertEquals(TimerStatus.FINISHED, controller.engine.status)
        assertEquals(controller.engine.serialize(), repository.writes.last())
    }

    @Test
    fun `malformed snapshot safely restores defaults and later settings persist`() {
        val repository = FakeSnapshots("not-a-tickly-snapshot")
        val controller = TimerController(repository)

        assertEquals(25, controller.engine.settings.focusMinutes)
        controller.updateSettings(TimerSettings(soundIndex = 2, language = "es"), nowMillis = 0)

        assertEquals(2, controller.engine.settings.soundIndex)
        assertTrue(repository.writes.last().contains("si=2"))
    }

    private class FakeSnapshots(private var value: String? = null) : TimerSnapshotRepository {
        val writes = mutableListOf<String>()
        override fun readSnapshot() = value
        override fun writeSnapshot(snapshot: String) { value = snapshot; writes += snapshot }
    }
}
