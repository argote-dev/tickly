package com.argote.tickly.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.argote.tickly.features.timer.domain.TimerEngine
import com.argote.tickly.features.timer.domain.TimerPhase
import com.argote.tickly.features.timer.domain.TimerStatus
import com.argote.tickly.features.timer.presentation.ConfirmTimerActionDialog
import com.argote.tickly.features.timer.presentation.TimerAction
import com.argote.tickly.features.timer.presentation.TimerScreen
import kotlinx.coroutines.delay

/** Native hosts own persistence and platform notifications; this composable owns shared screen wiring. */
@Composable
fun TicklyApp(
    engine: TimerEngine,
    onChanged: () -> Unit,
    onFirstStart: () -> Unit = {},
    reducedMotion: Boolean = false,
    systemLanguage: String = "en",
    onPreviewSound: (Int) -> Unit = {},
    isActive: Boolean = true,
    backRequest: Int = 0,
    onSettingsVisibilityChanged: (Boolean) -> Unit = {},
    revision: Int = 0,
) {
    var now by remember { mutableStateOf(currentTimeMillis()) }
    var settingsOpen by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf<TimerAction?>(null) }
    var firstStartRequested by remember { mutableStateOf(false) }
    LaunchedEffect(backRequest) {
        if (settingsOpen) {
            settingsOpen = false
            onSettingsVisibilityChanged(false)
        }
    }
    LaunchedEffect(engine.status, isActive) {
        if (!isActive) return@LaunchedEffect
        while (engine.status == TimerStatus.RUNNING && isActive) {
            now = currentTimeMillis()
            engine.tick(now)
            onChanged()
            delay(250)
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
                if (settingsOpen) {
                    SettingsScreen(
                        settings = engine.settings,
                        copy = copy,
                        onBack = { settingsOpen = false; onSettingsVisibilityChanged(false) },
                        onSettings = { new -> engine.updateSettings(new, currentTimeMillis()); onChanged() },
                        onPreviewSound = onPreviewSound,
                    )
                } else {
                    TimerScreen(
                        engine = engine,
                        now = now,
                        accent = accent,
                        copy = copy,
                        revision = revision,
                        onStartPause = {
                            if (engine.status == TimerStatus.RUNNING) engine.pause(currentTimeMillis())
                            else {
                                if (!firstStartRequested) { onFirstStart(); firstStartRequested = true }
                                engine.start(currentTimeMillis())
                            }
                            onChanged()
                        },
                        onRestart = { dialog = TimerAction.Restart },
                        onSkip = {
                            if (engine.phase == TimerPhase.FOCUS) dialog = TimerAction.Skip
                            else { engine.skip(currentTimeMillis()); onChanged() }
                        },
                        onReset = { dialog = TimerAction.Reset },
                        onSettings = { settingsOpen = true; onSettingsVisibilityChanged(true) },
                    )
                }
                dialog?.let { action ->
                    ConfirmTimerActionDialog(copy, action, onDismiss = { dialog = null }) {
                        when (action) {
                            TimerAction.Restart -> engine.restart(currentTimeMillis())
                            TimerAction.Skip -> engine.skip(currentTimeMillis())
                            TimerAction.Reset -> engine.reset(currentTimeMillis())
                        }
                        onChanged()
                        dialog = null
                    }
                }
            }
        }
    }
}

private fun currentTimeMillis() = kotlin.time.Clock.System.now().toEpochMilliseconds()
