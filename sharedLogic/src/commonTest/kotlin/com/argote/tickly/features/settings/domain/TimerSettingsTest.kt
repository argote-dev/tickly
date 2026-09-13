package com.argote.tickly.features.settings.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class TimerSettingsTest {
    @Test
    fun settingsAreClampedToProductBounds() {
        val settings =
            TimerSettings(
                focusMinutes = -1,
                shortBreakMinutes = 99,
                longBreakMinutes = 0,
                blocksUntilLongBreak = 99,
                accentIndex = 99,
                soundIndex = -99,
                language = "Portuguese",
            ).validated()

        assertEquals(1, settings.focusMinutes)
        assertEquals(60, settings.shortBreakMinutes)
        assertEquals(1, settings.longBreakMinutes)
        assertEquals(8, settings.blocksUntilLongBreak)
        assertEquals(5, settings.accentIndex)
        assertEquals(-1, settings.soundIndex)
        assertEquals("system", settings.language)
    }
}
