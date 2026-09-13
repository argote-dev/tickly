package com.argote.tickly.app

import android.content.Context
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.argote.tickly.features.settings.platform.TimerSoundPreview
import com.argote.tickly.features.timer.domain.TimerEngine

/** Android bridge: keeps a KMP snapshot local and lets androidApp schedule notifications. */
@Composable
fun AndroidTicklyApp(
    context: Context,
    onFirstStart: () -> Unit,
    onSnapshotChanged: (String, Long, Boolean) -> Unit,
) {
    val preferences = remember { context.getSharedPreferences("tickly_timer", Context.MODE_PRIVATE) }
    val engine = remember { TimerEngine(preferences.getString("snapshot", null)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var isActive by remember { mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) }
    var changeVersion by remember { mutableIntStateOf(0) }
    var settingsVisible by remember { mutableStateOf(false) }
    var backRequest by remember { mutableIntStateOf(0) }
    var savedSnapshot by remember { mutableStateOf<String?>(null) }
    var reducedMotion by remember {
        mutableStateOf(Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                isActive = true
                reducedMotion = Settings.Global.getFloat(
                    context.contentResolver,
                    Settings.Global.ANIMATOR_DURATION_SCALE,
                    1f,
                ) == 0f
                engine.tick(System.currentTimeMillis())
                onSnapshotChanged(
                    engine.serialize(),
                    engine.deadlineMillis,
                    engine.settings.keepScreenOn && engine.phase.name == "FOCUS" && engine.status.name == "RUNNING",
                )
                changeVersion++
            }
            override fun onStop(owner: LifecycleOwner) { isActive = false }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(changeVersion) {
        val snapshot = engine.serialize()
        if (snapshot != savedSnapshot) {
            savedSnapshot = snapshot
            preferences.edit().putString("snapshot", snapshot).apply()
            onSnapshotChanged(
                snapshot,
                engine.deadlineMillis,
                engine.settings.keepScreenOn && engine.phase.name == "FOCUS" && engine.status.name == "RUNNING",
            )
        }
    }
    BackHandler(enabled = settingsVisible) { backRequest++ }
    TicklyApp(
        engine = engine,
        reducedMotion = reducedMotion,
        systemLanguage = if (java.util.Locale.getDefault().language == "es") "es" else "en",
        onPreviewSound = { TimerSoundPreview.play(context, it) },
        isActive = isActive,
        backRequest = backRequest,
        onSettingsVisibilityChanged = { settingsVisible = it },
        revision = changeVersion,
        onFirstStart = onFirstStart,
        onChanged = { changeVersion++ },
    )
}
