package com.argote.tickly.features.timer.domain

import com.argote.tickly.features.settings.domain.TimerSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class TimerEngineTest {
    @Test
    fun pauseAndResumeUseAbsoluteClockRatherThanTickCount() {
        val timer = TimerEngine()
        timer.start(1_000)
        timer.pause(61_000)

        assertEquals(TimerStatus.PAUSED, timer.status)
        assertEquals(24 * MINUTE, timer.remainingMillis(100_000))
        assertEquals(0, timer.deadlineMillis)

        timer.start(100_000)
        assertEquals(1_540_000, timer.deadlineMillis)
        assertEquals(24 * MINUTE, timer.remainingMillis(100_000))
    }

    @Test
    fun finishCountsFocusOnlyOnceAndWaitsForManualStart() {
        val timer = TimerEngine()
        timer.start(0)
        timer.tick(25 * MINUTE)
        timer.tick(50 * MINUTE)

        assertEquals(TimerStatus.FINISHED, timer.status)
        assertEquals(TimerPhase.FOCUS, timer.phase)
        assertEquals(1, timer.completedFocusBlocks)
        assertEquals(0, timer.remainingMillis(50 * MINUTE))

        timer.start(50 * MINUTE)
        assertEquals(TimerPhase.SHORT_BREAK, timer.phase)
        assertEquals(TimerStatus.RUNNING, timer.status)
        assertEquals(5 * MINUTE, timer.remainingMillis(50 * MINUTE))
    }

    @Test
    fun lateReturnFinishesOnlyCurrentInterval() {
        val timer = TimerEngine()
        timer.start(0)
        timer.tick(3 * 60 * MINUTE)

        assertEquals(TimerStatus.FINISHED, timer.status)
        assertEquals(TimerPhase.FOCUS, timer.phase)
        assertEquals(1, timer.completedFocusBlocks)
        timer.start(3 * 60 * MINUTE)
        assertEquals(TimerPhase.SHORT_BREAK, timer.phase)
        assertEquals(5 * MINUTE, timer.remainingMillis(3 * 60 * MINUTE))
    }

    @Test
    fun longBreakIsSelectedAtFocusCompletionAndSurvivesThresholdEdit() {
        val timer = TimerEngine()
        timer.updateSettings(TimerSettings(blocksUntilLongBreak = 2), 0)
        completeFocus(timer, 0)
        completeBreak(timer, 25 * MINUTE)
        completeFocus(timer, 30 * MINUTE)
        assertEquals(2, timer.completedFocusBlocks)
        assertEquals(TimerStatus.FINISHED, timer.status)
        assertEquals(TimerPhase.FOCUS, timer.phase)

        timer.updateSettings(TimerSettings(blocksUntilLongBreak = 8, longBreakMinutes = 30), 55 * MINUTE)
        timer.start(55 * MINUTE)

        assertEquals(TimerPhase.LONG_BREAK, timer.phase)
        assertEquals(30 * MINUTE, timer.intervalDurationMillis)
    }

    @Test
    fun thresholdEditBeforeNextFocusCompletionIsEvaluatedOnCompletion() {
        val timer = TimerEngine()
        timer.updateSettings(TimerSettings(blocksUntilLongBreak = 4), 0)
        completeFocus(timer, 0)
        completeBreak(timer, 25 * MINUTE)
        timer.updateSettings(TimerSettings(blocksUntilLongBreak = 2), 30 * MINUTE)
        timer.start(30 * MINUTE)
        timer.tick(55 * MINUTE)

        timer.start(55 * MINUTE)
        assertEquals(TimerPhase.LONG_BREAK, timer.phase)
    }

    @Test
    fun skipsFollowCycleRulesWithoutCountingAbandonedFocus() {
        val timer = TimerEngine()
        timer.start(0)
        timer.skip(1)
        assertEquals(TimerPhase.SHORT_BREAK, timer.phase)
        assertEquals(TimerStatus.READY, timer.status)
        assertEquals(0, timer.completedFocusBlocks)

        timer.skip(2)
        assertEquals(TimerPhase.FOCUS, timer.phase)
        assertEquals(0, timer.completedFocusBlocks)

        timer.updateSettings(TimerSettings(blocksUntilLongBreak = 2), 2)
        completeFocus(timer, 2)
        completeBreak(timer, 25 * MINUTE + 2)
        completeFocus(timer, 30 * MINUTE + 2)
        timer.start(55 * MINUTE + 2)
        assertEquals(TimerPhase.LONG_BREAK, timer.phase)
        timer.skip(55 * MINUTE + 3)
        assertEquals(TimerPhase.FOCUS, timer.phase)
        assertEquals(0, timer.completedFocusBlocks)
    }

    @Test
    fun restartPreservesCapturedDurationAndCycleProgress() {
        val timer = TimerEngine()
        timer.updateSettings(TimerSettings(focusMinutes = 30), 0)
        timer.start(0)
        timer.updateSettings(TimerSettings(focusMinutes = 10), 1)
        timer.restart(2)

        assertEquals(TimerStatus.READY, timer.status)
        assertEquals(30 * MINUTE, timer.intervalDurationMillis)
        assertEquals(30 * MINUTE, timer.remainingMillis(2))
        timer.reset(3)
        assertEquals(TimerPhase.FOCUS, timer.phase)
        assertEquals(10 * MINUTE, timer.intervalDurationMillis)
        assertEquals(0, timer.completedFocusBlocks)
    }

    @Test
    fun snapshotRestoresRunningPausedAndFinishedStateAndRejectsCorruption() {
        val running = TimerEngine().also { it.start(100) }
        val restoredRunning = TimerEngine(running.serialize())
        assertEquals(TimerStatus.RUNNING, restoredRunning.status)
        assertEquals(25 * MINUTE - 900, restoredRunning.remainingMillis(1_000))
        restoredRunning.tick(25 * MINUTE + 100)
        assertEquals(TimerStatus.FINISHED, restoredRunning.status)

        val paused = TimerEngine().also { it.start(0); it.pause(20_000) }
        val restoredPaused = TimerEngine(paused.serialize())
        assertEquals(TimerStatus.PAUSED, restoredPaused.status)
        assertEquals(25 * MINUTE - 20_000, restoredPaused.remainingMillis(900_000))

        val corrupt = TimerEngine("tickly|1|fm=oops")
        assertEquals(TimerStatus.READY, corrupt.status)
        assertEquals(TimerPhase.FOCUS, corrupt.phase)
    }

    @Test
    fun actionsReconcileAnExpiredRunningTimerBeforeActing() {
        val timer = TimerEngine()
        timer.start(0)
        // UI actions need not rely on a ticker having fired at this exact instant.
        timer.pause(25 * MINUTE)
        assertEquals(TimerStatus.FINISHED, timer.status)
        assertEquals(1, timer.completedFocusBlocks)

        timer.start(25 * MINUTE)
        assertEquals(TimerPhase.SHORT_BREAK, timer.phase)
        assertEquals(TimerStatus.RUNNING, timer.status)
    }

    @Test
    fun settingsChangedWhileReadyApplyButSettingsChangedWhilePausedDoNot() {
        val timer = TimerEngine()
        timer.updateSettings(TimerSettings(focusMinutes = 40), 0)
        assertEquals(40 * MINUTE, timer.intervalDurationMillis)
        timer.start(0)
        timer.pause(10)
        timer.updateSettings(TimerSettings(focusMinutes = 5), 11)
        assertEquals(40 * MINUTE, timer.intervalDurationMillis)
        assertEquals(40 * MINUTE - 10, timer.remainingMillis(100))
    }

    private fun completeFocus(timer: TimerEngine, startAt: Long) {
        timer.start(startAt)
        timer.tick(startAt + timer.intervalDurationMillis)
    }

    private fun completeBreak(timer: TimerEngine, startAt: Long) {
        timer.start(startAt)
        timer.tick(startAt + timer.intervalDurationMillis)
    }

    private companion object { const val MINUTE = 60_000L }
}
