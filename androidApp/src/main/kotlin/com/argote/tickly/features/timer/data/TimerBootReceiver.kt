package com.argote.tickly.features.timer.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.argote.tickly.features.timer.domain.TimerEngine

class TimerBootReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val snapshot =
            context
                .getSharedPreferences(TimerNotifications.PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getString(TimerNotifications.SNAPSHOT_KEY, null)
        val engine = TimerEngine(snapshot)
        if (engine.deadlineMillis > System.currentTimeMillis()) {
            TimerNotifications.schedule(context, engine.deadlineMillis)
        }
    }
}
