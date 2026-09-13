package com.argote.tickly.features.settings.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun SettingSlider(
    label: String,
    value: Int,
    range: IntRange,
    suffix: String = " min",
    update: (Int) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .82f),
        ),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    enabled = value > range.first,
                    onClick = { update(value - 1) },
                    modifier = Modifier.semantics { contentDescription = "$label −1" },
                ) { Text("−", fontSize = 24.sp, color = if (value > range.first) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .38f)) }
                Text(
                    "$value$suffix",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                IconButton(
                    enabled = value < range.last,
                    onClick = { update(value + 1) },
                    modifier = Modifier.semantics { contentDescription = "$label +1" },
                ) { Text("+", fontSize = 22.sp, color = if (value < range.last) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .38f)) }
            }
            Slider(
                value = value.toFloat(),
                onValueChange = { update(it.toInt()) },
                valueRange = range.first.toFloat()..range.last.toFloat(),
                // Continuous track: hundreds of discrete marks obscure precise adjustment.
                steps = 0,
                modifier = Modifier.semantics { contentDescription = "$label $value$suffix" },
            )
        }
    }
}
