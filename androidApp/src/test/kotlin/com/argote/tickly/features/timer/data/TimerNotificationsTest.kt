package com.argote.tickly.features.timer.data

import com.argote.tickly.features.timer.domain.TimerStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimerNotificationsTest {
    @Test
    fun `scheduled alarm and notification intents target their declared components`() {
        assertEquals("com.argote.tickly.notifications.TimerAlarmReceiver", TimerNotifications.alarmReceiverComponentName())
        assertEquals("com.argote.tickly.app.MainActivity", TimerNotifications.notificationActivityComponentName())
    }

    @Test
    fun `notification channels are unique for every supported sound and vibration preference`() {
        val ids =
            buildSet {
                for (sound in -1..2) {
                    add(TimerNotifications.channelId(sound, vibrationEnabled = false))
                    add(TimerNotifications.channelId(sound, vibrationEnabled = true))
                }
            }

        assertEquals(8, ids.size)
        assertTrue("timer_finished_s-1_v0" in ids)
        assertTrue("timer_finished_s0_v1" in ids)
        assertTrue("timer_finished_s2_v0" in ids)
    }

    @Test
    fun `unsupported sound values map to the silent channel without changing vibration`() {
        assertEquals(
            TimerNotifications.channelId(-1, vibrationEnabled = false),
            TimerNotifications.channelId(-99, vibrationEnabled = false),
        )
        assertEquals(
            TimerNotifications.channelId(2, vibrationEnabled = true),
            TimerNotifications.channelId(99, vibrationEnabled = true),
        )
    }
}
