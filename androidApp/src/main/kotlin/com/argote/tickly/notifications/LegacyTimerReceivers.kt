package com.argote.tickly.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.argote.tickly.features.timer.notifications.TimerAlarmReceiver as FeatureTimerAlarmReceiver
import com.argote.tickly.features.timer.notifications.TimerBootReceiver as FeatureTimerBootReceiver

/**
 * Compatibility components for alarms registered before the notifications slice moved.
 * PendingIntent identity includes its explicit receiver component, so these names remain
 * declared in the manifest and delegate to the feature implementation.
 */
class TimerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = FeatureTimerAlarmReceiver().onReceive(context, intent)
}

/** Compatibility component for boot broadcasts targeting the pre-slice receiver name. */
class TimerBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = FeatureTimerBootReceiver().onReceive(context, intent)
}
