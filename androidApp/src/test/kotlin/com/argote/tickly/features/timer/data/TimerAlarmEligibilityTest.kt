package com.argote.tickly.features.timer.data

import com.argote.tickly.features.timer.domain.TimerStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun eligible(
    token: Long = 1_000L,
    phase: String = "FOCUS",
    scheduledToken: Long = token,
    scheduledPhase: String? = phase,
    lastNotifiedToken: Long = 0L,
    lastNotifiedPhase: String? = null,
    status: TimerStatus = TimerStatus.RUNNING,
    engineDeadline: Long = token,
    enginePhase: String = phase,
    now: Long = token,
): Boolean = isAlarmNotificationEligible(
    token, phase, scheduledToken, scheduledPhase, lastNotifiedToken, lastNotifiedPhase,
    status, engineDeadline, enginePhase, now,
)

class TimerAlarmEligibilityTest {
    @Test
    fun `rejects stale token`() {
        assertEquals(false, eligible(scheduledToken = 2_000L))
    }

    @Test
    fun `rejects paused and ready timers`() {
        assertEquals(false, eligible(status = TimerStatus.PAUSED, engineDeadline = 0L))
        assertEquals(false, eligible(status = TimerStatus.READY, engineDeadline = 0L))
    }

    @Test
    fun `rejects alarm delivered before its deadline`() {
        assertEquals(false, eligible(now = 999L))
    }

    @Test
    fun `accepts matching interval already finished by the UI`() {
        assertTrue(eligible(
            status = TimerStatus.FINISHED,
            engineDeadline = 0L,
        ))
    }

    @Test
    fun `rejects finished UI state for a different phase`() {
        assertEquals(false, eligible(
            status = TimerStatus.FINISHED,
            engineDeadline = 0L,
            enginePhase = "SHORT_BREAK",
        ))
    }

    @Test
    fun `rejects duplicate delivery`() {
        assertEquals(false, eligible(lastNotifiedToken = 1_000L, lastNotifiedPhase = "FOCUS"))
    }

    @Test
    fun `accepts a normal due running alarm`() {
        assertTrue(eligible())
    }
}
