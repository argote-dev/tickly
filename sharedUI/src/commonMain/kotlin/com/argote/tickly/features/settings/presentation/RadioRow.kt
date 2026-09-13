package com.argote.tickly.features.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun RadioRow(
    label: String,
    selected: Boolean,
    select: () -> Unit,
) = Row(Modifier.fillMaxWidth().clickable(onClick = select).padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
    RadioButton(selected = selected, onClick = select)
    Text(label, Modifier.padding(start = 8.dp))
}
