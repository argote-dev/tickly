package com.argote.tickly.features.timer.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.argote.tickly.core.localization.TimerCopy
import com.argote.tickly.features.timer.domain.TimerPhase
import com.argote.tickly.features.timer.domain.TimerStatus
import kotlinx.coroutines.delay

/** Timer feature UI flow: ticking, user actions, and their confirmations. */
@Composable
fun TimerRoute(
    visible: Boolean,
    controller: TimerController,
    accent: Color,
    copy: TimerCopy,
    isActive: Boolean,
    revision: Int,
    onFirstStart: () -> Unit,
    onChanged: () -> Unit,
    onSettings: () -> Unit,
) {
    val engine = controller.engine
    var now by remember { mutableStateOf(currentTimeMillis()) }
    var dialog by remember { mutableStateOf<TimerAction?>(null) }
    var firstStartRequested by remember { mutableStateOf(false) }

    LaunchedEffect(engine.status, isActive) {
        if (!isActive) return@LaunchedEffect
        while (engine.status == TimerStatus.RUNNING && isActive) {
            now = currentTimeMillis()
            controller.tick(now)
            onChanged()
            delay(250)
        }
    }
    if (visible) {
        TimerScreen(
            engine = engine,
            now = now,
            accent = accent,
            copy = copy,
            revision = revision,
            onStartPause = {
                if (engine.status == TimerStatus.RUNNING) controller.pause(currentTimeMillis())
                else {
                    if (!firstStartRequested) { onFirstStart(); firstStartRequested = true }
                    controller.start(currentTimeMillis())
                }
                onChanged()
            },
            onRestart = { dialog = TimerAction.Restart },
            onSkip = {
                if (engine.phase == TimerPhase.FOCUS) dialog = TimerAction.Skip
                else { controller.skip(currentTimeMillis()); onChanged() }
            },
            onReset = { dialog = TimerAction.Reset },
            onSettings = onSettings,
        )
        dialog?.let { action ->
            ConfirmTimerActionDialog(copy, action, onDismiss = { dialog = null }) {
                when (action) {
                    TimerAction.Restart -> controller.restart(currentTimeMillis())
                    TimerAction.Skip -> controller.skip(currentTimeMillis())
                    TimerAction.Reset -> controller.reset(currentTimeMillis())
                }
                onChanged()
                dialog = null
            }
        }
    }
}

private fun currentTimeMillis() = kotlin.time.Clock.System.now().toEpochMilliseconds()
