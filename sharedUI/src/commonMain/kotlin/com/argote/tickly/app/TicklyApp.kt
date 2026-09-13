package com.argote.tickly.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import com.argote.tickly.core.design.AmbientBackground
import com.argote.tickly.core.design.accentForIndex
import com.argote.tickly.core.design.colorSchemeForAccent
import com.argote.tickly.core.localization.TimerCopy
import com.argote.tickly.features.settings.presentation.SettingsScreen
import com.argote.tickly.features.timer.presentation.TimerController
import com.argote.tickly.features.timer.presentation.TimerRoute

/** Native hosts own persistence and platform notifications; this composable owns shared screen wiring. */
@Composable
fun TicklyApp(
    controller: TimerController,
    onChanged: () -> Unit = {},
    onFirstStart: () -> Unit = {},
    reducedMotion: Boolean = false,
    systemLanguage: String = "en",
    onPreviewSound: (Int) -> Unit = {},
    isActive: Boolean = true,
    backRequest: Int = 0,
    onSettingsVisibilityChanged: (Boolean) -> Unit = {},
    revision: Int = 0,
) {
    val engine = controller.engine
    var settingsOpen by remember { mutableStateOf(false) }
    LaunchedEffect(backRequest) {
        if (settingsOpen) {
            settingsOpen = false
            onSettingsVisibilityChanged(false)
        }
    }
    val accent = accentForIndex(engine.settings.accentIndex)
    val copy = TimerCopy.forLanguage(
        if (engine.settings.language == "system") systemLanguage else engine.settings.language,
    )
    MaterialTheme(colorScheme = colorSchemeForAccent(engine.settings.accentIndex)) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                AmbientBackground(accent, engine.settings.animatedBackground && !reducedMotion && isActive)
                TimerRoute(
                    visible = !settingsOpen,
                    controller = controller,
                    accent = accent,
                    copy = copy,
                    isActive = isActive,
                    revision = revision,
                    onFirstStart = onFirstStart,
                    onChanged = onChanged,
                    onSettings = { settingsOpen = true; onSettingsVisibilityChanged(true) },
                )
                if (settingsOpen) {
                    SettingsScreen(
                        settings = engine.settings,
                        copy = copy,
                        onBack = { settingsOpen = false; onSettingsVisibilityChanged(false) },
                        onSettings = { new -> controller.updateSettings(new, currentTimeMillis()); onChanged() },
                        onPreviewSound = onPreviewSound,
                    )
                }
            }
        }
    }
}


private fun currentTimeMillis() = kotlin.time.Clock.System.now().toEpochMilliseconds()
