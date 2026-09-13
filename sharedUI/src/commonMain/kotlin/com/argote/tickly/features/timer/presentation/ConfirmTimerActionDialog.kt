package com.argote.tickly.features.timer.presentation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.argote.tickly.core.localization.TimerCopy

@Composable
fun ConfirmTimerActionDialog(
    copy: TimerCopy,
    action: TimerAction,
    onDismiss: () -> Unit,
    confirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when (action) {
                    TimerAction.Restart -> copy.restart
                    TimerAction.Skip -> copy.skipFocus
                    TimerAction.Reset -> copy.startOver
                },
            )
        },
        text = {
            Text(
                when (action) {
                    TimerAction.Restart -> copy.restartMessage
                    TimerAction.Skip -> copy.skipMessage
                    TimerAction.Reset -> copy.resetMessage
                },
            )
        },
        confirmButton = { Button(onClick = confirm) { Text(copy.confirm) } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text(copy.cancel) } },
    )
}
