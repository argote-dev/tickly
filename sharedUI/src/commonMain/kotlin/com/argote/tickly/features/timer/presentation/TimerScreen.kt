package com.argote.tickly.features.timer.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.argote.tickly.core.localization.TimerCopy
import com.argote.tickly.features.timer.domain.TimerEngine
import com.argote.tickly.features.timer.domain.TimerPhase
import com.argote.tickly.features.timer.domain.TimerStatus

@Composable
fun TimerScreen(
    engine: TimerEngine,
    now: Long,
    accent: Color,
    copy: TimerCopy,
    revision: Int,
    onStartPause: () -> Unit,
    onRestart: () -> Unit,
    onSkip: () -> Unit,
    onReset: () -> Unit,
    onSettings: () -> Unit,
) {
    // Engine is deliberately mutable; revision is the observable invalidation signal from the host.
    @Suppress("UNUSED_VARIABLE")
    val renderedRevision = revision
    val remaining = engine.remainingMillis(now)
    val total = engine.intervalDurationMillis.coerceAtLeast(1)
    val progress by animateFloatAsState((1f - remaining.toFloat() / total).coerceIn(0f, 1f), label = "timer progress")
    val phaseName =
        when (engine.phase) {
            TimerPhase.FOCUS -> copy.focus
            TimerPhase.SHORT_BREAK -> copy.shortBreak
            TimerPhase.LONG_BREAK -> copy.longBreak
        }
    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("TICKLY", style = MaterialTheme.typography.labelLarge, letterSpacing = 3.sp, color = accent)
            OutlinedButton(
                onClick = onSettings,
                modifier = Modifier.semantics { contentDescription = copy.settings },
            ) { Text(copy.settings) }
        }
        Spacer(Modifier.height(58.dp))
        Text(
            phaseName.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        Box(Modifier.size(286.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize().semantics { contentDescription = "${copy.remaining}: ${timeLabel(remaining)}" }) {
                val stroke = 11.dp.toPx()
                val inset = stroke / 2
                drawArc(
                    Color.White.copy(alpha = .10f),
                    -90f,
                    360f,
                    false,
                    Offset(inset, inset),
                    Size(
                        size.width - stroke,
                        size.height - stroke,
                    ),
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
                drawArc(
                    accent,
                    -90f,
                    progress * 360f,
                    false,
                    Offset(inset, inset),
                    Size(size.width - stroke, size.height - stroke),
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(timeLabel(remaining), fontSize = 58.sp, fontWeight = FontWeight.Light, letterSpacing = (-2).sp)
                Text(statusLabel(engine, copy), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "${copy.blocks}: ${engine.completedFocusBlocks} / ${engine.settings.blocksUntilLongBreak}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(30.dp))
        Button(
            onClick = onStartPause,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(if (engine.status == TimerStatus.RUNNING) copy.pause else copy.start, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(onClick = onRestart, modifier = Modifier.weight(1f)) { Text(copy.restart) }
            FilledTonalButton(onClick = onSkip, modifier = Modifier.weight(1f)) { Text(copy.skip) }
        }
        Text(
            copy.startOver,
            Modifier.padding(top = 22.dp).clickable(onClick = onReset).semantics {
                contentDescription = copy.startOver
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(28.dp))
    }
}

private fun statusLabel(
    engine: TimerEngine,
    copy: TimerCopy,
): String {
    val spanish = copy.focus == "Enfoque"
    return when (engine.status) {
        TimerStatus.READY -> {
            copy.ready
        }

        TimerStatus.RUNNING -> {
            if (spanish) "En curso" else "Running"
        }

        TimerStatus.PAUSED -> {
            if (spanish) "En pausa" else "Paused"
        }

        TimerStatus.FINISHED -> {
            when (engine.phase) {
                TimerPhase.FOCUS -> {
                    if (spanish) "Finalizado · Iniciar descanso" else "Finished · Start break"
                }

                TimerPhase.SHORT_BREAK, TimerPhase.LONG_BREAK -> {
                    if (spanish) {
                        "Finalizado · Iniciar enfoque"
                    } else {
                        "Finished · Start focus"
                    }
                }
            }
        }
    }
}

private fun timeLabel(millis: Long): String {
    val seconds = (millis.coerceAtLeast(0) + 999) / 1000
    return "${seconds / 60}".padStart(2, '0') + ":" + "${seconds % 60}".padStart(2, '0')
}
