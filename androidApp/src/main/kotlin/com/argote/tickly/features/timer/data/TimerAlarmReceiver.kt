package com.argote.tickly.features.timer.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.argote.tickly.features.timer.domain.TimerEngine

class TimerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "com.argote.tickly.TIMER_FINISHED") return
        val token = intent.getLongExtra(TimerNotifications.deadlineExtra, 0L)
        val phase = intent.getStringExtra(TimerNotifications.phaseExtra) ?: return
        val prefs = context.getSharedPreferences(TimerNotifications.preferencesName, Context.MODE_PRIVATE)
        val engine = TimerEngine(prefs.getString(TimerNotifications.snapshotKey, null))
        val now = System.currentTimeMillis()
        if (!isAlarmNotificationEligible(
                token = token,
                phase = phase,
                scheduledToken = prefs.getLong(TimerNotifications.scheduledDeadlineKey, 0L),
                scheduledPhase = prefs.getString(TimerNotifications.scheduledPhaseKey, null),
                lastNotifiedToken = prefs.getLong(TimerNotifications.lastNotifiedDeadlineKey, 0L),
                lastNotifiedPhase = prefs.getString(TimerNotifications.lastNotifiedPhaseKey, null),
                status = engine.status,
                engineDeadline = engine.deadlineMillis,
                enginePhase = engine.phase.name,
                now = now,
            )
        ) return

        engine.tick(now)
        prefs.edit()
            .putString(TimerNotifications.snapshotKey, engine.serialize())
            .putLong(TimerNotifications.lastNotifiedDeadlineKey, token)
            .putString(TimerNotifications.lastNotifiedPhaseKey, phase)
            .remove(TimerNotifications.scheduledDeadlineKey)
            .remove(TimerNotifications.scheduledPhaseKey)
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
