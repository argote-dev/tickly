package com.argote.tickly.app

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.argote.tickly.R

@Composable
internal fun AlertStatusBanner(
    context: Context,
    notificationUnavailable: Boolean,
    exactAlarmMissing: Boolean,
    onOpenSettings: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            if (notificationUnavailable) {
                Text(context.getString(R.string.alerts_disabled))
            }
            if (exactAlarmMissing) {
                Text(context.getString(R.string.alerts_may_be_delayed))
            }
            OutlinedButton(onClick = onOpenSettings) {
                Text(context.getString(R.string.open_settings))
            }
        }
    }
}
