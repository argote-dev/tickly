package com.argote.tickly.features.settings.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun Section(title: String) { Spacer(Modifier.height(12.dp)); HorizontalDivider(); Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 18.dp, bottom = 6.dp)) }
