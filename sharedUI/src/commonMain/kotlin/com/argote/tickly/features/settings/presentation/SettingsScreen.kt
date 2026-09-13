package com.argote.tickly.features.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.argote.tickly.core.design.accentForIndex
import com.argote.tickly.core.localization.TimerCopy
import com.argote.tickly.features.settings.domain.TimerSettings

private val accentColorsForSettings = (0..5).map(::accentForIndex)

@Composable
fun SettingsScreen(
    settings: TimerSettings,
    copy: TimerCopy,
    onBack: () -> Unit,
    onSettings: (TimerSettings) -> Unit,
    onPreviewSound: (Int) -> Unit,
) {
    var draft by remember(settings) { mutableStateOf(settings) }

    fun commit(next: TimerSettings) {
        draft = next
        onSettings(next)
    }
    Column(Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(copy.settings, style = MaterialTheme.typography.headlineMedium)
            OutlinedButton(onClick = onBack) { Text(copy.done) }
        }
        Spacer(Modifier.height(20.dp))
        SettingSlider(copy.focusDuration, draft.focusMinutes, 1..180) { commit(draft.copy(focusMinutes = it)) }
        SettingSlider(copy.shortDuration, draft.shortBreakMinutes, 1..60) { commit(draft.copy(shortBreakMinutes = it)) }
        SettingSlider(copy.longDuration, draft.longBreakMinutes, 1..60) { commit(draft.copy(longBreakMinutes = it)) }
        SettingSlider(copy.longAfter, draft.blocksUntilLongBreak, 2..8, suffix = "") { commit(draft.copy(blocksUntilLongBreak = it)) }
        Section(copy.accent)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            accentColorsForSettings.forEachIndexed { index, color ->
                Surface(
                    color = color,
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier =
                    Modifier.size(42.dp).clickable { commit(draft.copy(accentIndex = index)) }.semantics {
                        contentDescription =
                            "${copy.accent} ${index + 1}"
                    },
                ) {
                    if (index ==
                        draft.accentIndex
                    ) {
                        Text(
                            "✓",
                            Modifier.fillMaxSize().wrapContentWidth(Alignment.CenterHorizontally).padding(top = 10.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }
        Section(copy.sound)
        listOf(-1 to copy.silent, 0 to copy.soundOne, 1 to copy.soundTwo, 2 to copy.soundThree).forEach { (value, name) ->
            RadioRow(
                name,
                draft.soundIndex == value,
            ) {
                commit(draft.copy(soundIndex = value))
            }
        }
        SwitchRow(copy.vibration, draft.vibrationEnabled) { commit(draft.copy(vibrationEnabled = it)) }
        SwitchRow(copy.animatedBackground, draft.animatedBackground) { commit(draft.copy(animatedBackground = it)) }
        SwitchRow(copy.keepScreenOn, draft.keepScreenOn) { commit(draft.copy(keepScreenOn = it)) }
        Section(copy.language)
        listOf("system" to copy.system, "es" to "Español", "en" to "English").forEach { (value, name) ->
            RadioRow(
                name,
                draft.language == value,
            ) { commit(draft.copy(language = value)) }
        }
        Spacer(Modifier.height(28.dp))
    }
}
