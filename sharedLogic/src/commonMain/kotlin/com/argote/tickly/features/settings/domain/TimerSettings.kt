package com.argote.tickly.features.settings.domain

/**
 * User preferences for a pomodoro cycle. Values are normalised by [validated], so
 * callers may safely pass partially edited values from a settings UI.
 */
data class TimerSettings(
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val blocksUntilLongBreak: Int = 4,
    val accentIndex: Int = 0,
    val soundIndex: Int = 0,
    val vibrationEnabled: Boolean = true,
    val animatedBackground: Boolean = true,
    val keepScreenOn: Boolean = false,
    val language: String = "system",
) {
    fun validated(): TimerSettings = copy(
        focusMinutes = focusMinutes.coerceIn(1, 180),
        shortBreakMinutes = shortBreakMinutes.coerceIn(1, 60),
        longBreakMinutes = longBreakMinutes.coerceIn(1, 60),
        blocksUntilLongBreak = blocksUntilLongBreak.coerceIn(2, 8),
        accentIndex = accentIndex.coerceIn(0, ACCENT_COUNT - 1),
        soundIndex = soundIndex.coerceIn(SILENT_SOUND_INDEX, SOUND_COUNT - 1),
        language = language.normalizedLanguage(),
    )

    companion object {
        const val ACCENT_COUNT = 6
        const val SOUND_COUNT = 3
        const val SILENT_SOUND_INDEX = -1
    }
}

private fun String.normalizedLanguage(): String = when (lowercase()) {
    "system" -> "system"
    "es", "español", "spanish" -> "es"
    "en", "english" -> "en"
    else -> "system"
}
