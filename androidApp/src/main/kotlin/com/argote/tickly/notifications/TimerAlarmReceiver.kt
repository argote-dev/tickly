package com.argote.tickly.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.argote.tickly.features.timer.data.TimerAlarmReceiver as FeatureTimerAlarmReceiver

/**
 * Compatibility components for alarms registered before the notifications slice moved.
 * PendingIntent identity includes its explicit receiver component, so these names remain
 * declared in the manifest and delegate to the feature implementation.
 */
class TimerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) = FeatureTimerAlarmReceiver().onReceive(context, intent)
}
