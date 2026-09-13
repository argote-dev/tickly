package com.argote.tickly.features.timer.data

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.argote.tickly.R
import com.argote.tickly.app.MainActivity
import com.argote.tickly.features.timer.domain.TimerEngine
import com.argote.tickly.features.timer.domain.TimerPhase
import com.argote.tickly.features.timer.domain.TimerStatus
import java.util.Locale
import com.argote.tickly.notifications.TimerAlarmReceiver as LegacyTimerAlarmReceiver

object TimerNotifications {
    private const val ALARM_ACTION = "com.argote.tickly.TIMER_FINISHED"
    internal const val PREFERENCES_NAME = "tickly_timer"
    internal const val SNAPSHOT_KEY = "snapshot"
    internal const val SCHEDULED_DEADLINE_KEY = "scheduled_deadline"
    internal const val SCHEDULED_PHASE_KEY = "scheduled_phase"
    internal const val LAST_NOTIFIED_DEADLINE_KEY = "last_notified_deadline"
    internal const val LAST_NOTIFIED_PHASE_KEY = "last_notified_phase"
    internal const val DEADLINE_EXTRA = "deadline"
    internal const val PHASE_EXTRA = "phase"
    private const val ALARM_REQUEST_CODE = 9

    /** Component names are part of persisted PendingIntent identity, not implementation detail. */
    internal fun alarmReceiverComponentName() = LegacyTimerAlarmReceiver::class.java.name

    internal fun notificationActivityComponentName() = MainActivity::class.java.name

    /**
     * Records the alarm identity separately from the UI snapshot. The Compose ticker
     * writes FINISHED with a zero deadline before a due broadcast is necessarily
     * delivered, so that write must not accidentally cancel the pending broadcast.
     */
    fun schedule(
        context: Context,
        deadlineMillis: Long,
    ) {
        val prefs = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val engine = TimerEngine(prefs.getString(SNAPSHOT_KEY, null))
        val manager = context.getSystemService(AlarmManager::class.java)
        val now = System.currentTimeMillis()
        val scheduledDeadline = prefs.getLong(SCHEDULED_DEADLINE_KEY, 0L)
        val scheduledPhase = prefs.getString(SCHEDULED_PHASE_KEY, null)
        val phase = engine.phase.name
        val shouldSchedule = engine.status == TimerStatus.RUNNING && deadlineMillis > now

        if (shouldSchedule) {
            // The bridge already deduplicates render ticks. Re-arm on resume/boot:
            // a persisted identity does not prove the OS still holds the alarm.
            cancel(manager, context, scheduledDeadline, scheduledPhase)
            val pending = alarmPendingIntent(context, deadlineMillis, phase)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !manager.canScheduleExactAlarms()) {
                    manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, deadlineMillis, pending)
                } else {
                    manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, deadlineMillis, pending)
                }
            } catch (_: SecurityException) {
                // Access can be revoked between the capability check and schedule call.
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, deadlineMillis, pending)
            }
            prefs
                .edit()
                .putLong(SCHEDULED_DEADLINE_KEY, deadlineMillis)
                .putString(SCHEDULED_PHASE_KEY, phase)
                .apply()
            return
        }

        // onStart can call this bridge before its new FINISHED snapshot is persisted.
        val isDueIntervalAwaitingDelivery =
            scheduledDeadline > 0L &&
                scheduledDeadline <= now &&
                scheduledPhase == phase &&
                (
                    (engine.status == TimerStatus.FINISHED) ||
                        (engine.status == TimerStatus.RUNNING && engine.deadlineMillis == scheduledDeadline)
                    )
        if (!isDueIntervalAwaitingDelivery) {
            cancel(manager, context, scheduledDeadline, scheduledPhase)
            prefs
                .edit()
                .remove(SCHEDULED_DEADLINE_KEY)
                .remove(SCHEDULED_PHASE_KEY)
                .apply()
        }
    }

    private fun cancel(
        manager: AlarmManager,
        context: Context,
        deadlineMillis: Long,
        phase: String?,
    ) {
        if (deadlineMillis > 0L && phase != null) {
            manager.cancel(alarmPendingIntent(context, deadlineMillis, phase))
        }
    }

    private fun alarmPendingIntent(
        context: Context,
        deadlineMillis: Long,
        phase: String,
    ): PendingIntent = PendingIntent.getBroadcast(
        context,
        ALARM_REQUEST_CODE,
        Intent(context, LegacyTimerAlarmReceiver::class.java)
            .setAction(ALARM_ACTION)
            // Extras are not PendingIntent identity; data makes each timer session distinct.
            .setData(Uri.parse("tickly://timer/$deadlineMillis/$phase"))
            .putExtra(DEADLINE_EXTRA, deadlineMillis)
            .putExtra(PHASE_EXTRA, phase),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    fun postFinished(
        context: Context,
        phase: TimerPhase,
        language: String,
        soundIndex: Int,
        vibrationEnabled: Boolean,
    ) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val displayContext = localizedContext(context, language)
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val channelId = channelId(soundIndex, vibrationEnabled)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                notificationChannel(displayContext, channelId, soundIndex, vibrationEnabled),
            )
        }

        val title =
            when (phase) {
                TimerPhase.FOCUS -> displayContext.getString(R.string.focus_complete)
                TimerPhase.SHORT_BREAK, TimerPhase.LONG_BREAK -> displayContext.getString(R.string.break_complete)
            }
        val launch =
            PendingIntent.getActivity(
                context,
                10,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(context, channelId)
            } else {
                Notification.Builder(context).apply {
                    soundUri(context, soundIndex)?.let(::setSound)
                    if (vibrationEnabled) setVibrate(longArrayOf(0, 80, 70, 100))
                }
            }
        notificationManager.notify(
            19,
            builder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(displayContext.getString(R.string.timer_finished))
                .setAutoCancel(true)
                .setContentIntent(launch)
                .build(),
        )
    }

    private fun localizedContext(
        context: Context,
        language: String,
    ): Context {
        val locale =
            when (language) {
                "es" -> Locale("es")
                "en" -> Locale.ENGLISH
                else -> return context
            }
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun notificationChannel(
        context: Context,
        id: String,
        soundIndex: Int,
        vibrationEnabled: Boolean,
    ): NotificationChannel = NotificationChannel(
        id,
        context.getString(R.string.timer_notifications),
        NotificationManager.IMPORTANCE_DEFAULT,
    ).apply {
        setSound(
            soundUri(context, soundIndex),
            AudioAttributes
                .Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        enableVibration(vibrationEnabled)
        if (vibrationEnabled) vibrationPattern = longArrayOf(0, 80, 70, 100)
        setBypassDnd(false)
    }

    internal fun channelId(
        soundIndex: Int,
        vibrationEnabled: Boolean,
    ): String = "timer_finished_s${soundIndex.coerceIn(-1, 2)}_v${if (vibrationEnabled) 1 else 0}"

    private fun soundUri(
        context: Context,
        soundIndex: Int,
    ): Uri? {
        val name =
            when (soundIndex) {
                0 -> "tickly_0"
                1 -> "tickly_1"
                2 -> "tickly_2"
                else -> return null
            }
        return Uri.parse("android.resource://${context.packageName}/raw/$name")
    }
}

/** Pure gate for alarm delivery; identity, timing, and UI state must all agree. */
internal fun isAlarmNotificationEligible(
    token: Long,
    phase: String,
    scheduledToken: Long,
    scheduledPhase: String?,
    lastNotifiedToken: Long,
    lastNotifiedPhase: String?,
    status: TimerStatus,
    engineDeadline: Long,
    enginePhase: String,
    now: Long,
): Boolean {
    if (token <= 0L || now < token) return false
    if (token != scheduledToken || phase != scheduledPhase) return false
    if (token == lastNotifiedToken && phase == lastNotifiedPhase) return false
    return when (status) {
        TimerStatus.RUNNING -> engineDeadline == token && enginePhase == phase

        // The foreground ticker may finish before a queued system alarm is delivered.
        TimerStatus.FINISHED -> enginePhase == phase

        TimerStatus.PAUSED, TimerStatus.READY -> false
    }
}
