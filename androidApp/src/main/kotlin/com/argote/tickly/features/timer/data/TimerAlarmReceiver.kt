package com.argote.tickly.features.timer.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.argote.tickly.features.timer.domain.TimerEngine

class TimerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != "com.argote.tickly.TIMER_FINISHED") return
        val token = intent.getLongExtra(TimerNotifications.DEADLINE_EXTRA, 0L)
        val phase = intent.getStringExtra(TimerNotifications.PHASE_EXTRA) ?: return
        val prefs = context.getSharedPreferences(TimerNotifications.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val engine = TimerEngine(prefs.getString(TimerNotifications.SNAPSHOT_KEY, null))
        val now = System.currentTimeMillis()
        if (!isAlarmNotificationEligible(
                token = token,
                phase = phase,
                scheduledToken = prefs.getLong(TimerNotifications.SCHEDULED_DEADLINE_KEY, 0L),
                scheduledPhase = prefs.getString(TimerNotifications.SCHEDULED_PHASE_KEY, null),
                lastNotifiedToken = prefs.getLong(TimerNotifications.LAST_NOTIFIED_DEADLINE_KEY, 0L),
                lastNotifiedPhase = prefs.getString(TimerNotifications.LAST_NOTIFIED_PHASE_KEY, null),
                status = engine.status,
                engineDeadline = engine.deadlineMillis,
                enginePhase = engine.phase.name,
                now = now,
            )
        ) {
            return
        }

        engine.tick(now)
        prefs
            .edit()
            .putString(TimerNotifications.SNAPSHOT_KEY, engine.serialize())
            .putLong(TimerNotifications.LAST_NOTIFIED_DEADLINE_KEY, token)
            .putString(TimerNotifications.LAST_NOTIFIED_PHASE_KEY, phase)
            .remove(TimerNotifications.SCHEDULED_DEADLINE_KEY)
            .remove(TimerNotifications.SCHEDULED_PHASE_KEY)
            .apply()
        TimerNotifications.postFinished(
            context,
            engine.phase,
            engine.settings.language,
            engine.settings.soundIndex,
            engine.settings.vibrationEnabled,
        )
    }
}
