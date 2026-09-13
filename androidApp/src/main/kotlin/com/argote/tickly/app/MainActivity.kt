package com.argote.tickly.app

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.argote.tickly.R
import com.argote.tickly.core.design.colorSchemeForAccent
import com.argote.tickly.features.timer.data.TimerNotifications
import com.argote.tickly.features.timer.domain.TimerEngine
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
        )
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val permissionPrefs = remember { getSharedPreferences("tickly_timer", MODE_PRIVATE) }
            var showNotificationContext by remember { mutableStateOf(false) }
            var notificationUnavailable by remember { mutableStateOf(false) }
            var exactAlarmMissing by remember { mutableStateOf(false) }
            var latestDeadline by remember { mutableLongStateOf(0L) }
            var displayContext by remember { mutableStateOf(localizedContext(context, null)) }
            var accentIndex by remember {
                mutableIntStateOf(
                    TimerEngine(permissionPrefs.getString("snapshot", null)).settings.accentIndex,
                )
            }

            fun refreshAlertAccess() {
                val notificationsEnabled =
                    getSystemService(NotificationManager::class.java)
                        .areNotificationsEnabled()
                notificationUnavailable = !notificationsEnabled ||
                    (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                            PackageManager.PERMISSION_GRANTED
                        )
                exactAlarmMissing = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    !getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
            }

            fun rescheduleLatestTimer() {
                if (latestDeadline > System.currentTimeMillis()) {
                    TimerNotifications.schedule(this@MainActivity, latestDeadline)
                }
            }

            fun requestExactAlarm() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    !getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
                ) {
                    startActivity(
                        Intent(
                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.fromParts("package", packageName, null),
                        ),
                    )
                }
            }

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer =
                    object : DefaultLifecycleObserver {
                        override fun onResume(owner: LifecycleOwner) {
                            refreshAlertAccess()
                            // This also upgrades a pending inexact alarm after the user enables exact alarms.
                            rescheduleLatestTimer()
                        }
                    }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            val notificationPermission =
                rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission(),
                ) {
                    refreshAlertAccess()
                    // Android displays this prompt first; only request exact-alarm access after it closes.
                    requestExactAlarm()
                }

            fun dismissNotificationContext() {
                showNotificationContext = false
                permissionPrefs.edit { putBoolean("asked_alert_permissions", true) }
            }

            MaterialTheme(colorScheme = colorSchemeForAccent(accentIndex)) {
                val showAlertBanner =
                    permissionPrefs.getBoolean("asked_alert_permissions", false) &&
                        (notificationUnavailable || exactAlarmMissing)
                Box(Modifier.fillMaxSize()) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(top = if (showAlertBanner) 120.dp else 0.dp),
                    ) {
                        AndroidTicklyApp(
                            context = this@MainActivity,
                            onFirstStart = {
                                if (!permissionPrefs.getBoolean("asked_alert_permissions", false)) {
                                    showNotificationContext = true
                                }
                            },
                            onSnapshotChanged = { snapshot, deadline, keepScreenOn ->
                                latestDeadline = deadline
                                displayContext = localizedContext(context, snapshot)
                                accentIndex = TimerEngine(snapshot).settings.accentIndex
                                TimerNotifications.schedule(this@MainActivity, deadline)
                                if (keepScreenOn) {
                                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                } else {
                                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                }
                            },
                        )
                    }
                    if (showAlertBanner) {
                        AlertStatusBanner(
                            context = displayContext,
                            notificationUnavailable = notificationUnavailable,
                            exactAlarmMissing = exactAlarmMissing,
                            onOpenSettings = {
                                when {
                                    notificationUnavailable -> {
                                        startActivity(
                                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                                            },
                                        )
                                    }

                                    exactAlarmMissing -> {
                                        requestExactAlarm()
                                    }
                                }
                            },
                        )
                    }
                }

                if (showNotificationContext) {
                    AlertDialog(
                        onDismissRequest = ::dismissNotificationContext,
                        title = { Text(displayContext.getString(R.string.notification_permission_title)) },
                        text = {
                            Text(displayContext.getString(R.string.notification_permission_message))
                        },
                        confirmButton = {
                            Button(onClick = {
                                dismissNotificationContext()
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                                    PackageManager.PERMISSION_GRANTED
                                ) {
                                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    requestExactAlarm()
                                }
                            }) { Text(displayContext.getString(R.string.continue_label)) }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = ::dismissNotificationContext) {
                                Text(displayContext.getString(R.string.not_now))
                            }
                        },
                    )
                }
            }
        }
    }
}

private fun localizedContext(
    context: Context,
    snapshot: String?,
): Context {
    val selectedLanguage = snapshot?.let { TimerEngine(it).settings.language } ?: "system"
    val locale =
        when (selectedLanguage) {
            "es" -> Locale("es")
            "en" -> Locale.ENGLISH
            else -> if (Locale.getDefault().language == "es") Locale("es") else Locale.ENGLISH
        }
    val configuration = Configuration(context.resources.configuration)
    configuration.setLocale(locale)
    return context.createConfigurationContext(configuration)
}
