package com.argote.tickly.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.argote.tickly.features.timer.data.TimerBootReceiver as FeatureTimerBootReceiver

/** Compatibility component for boot broadcasts targeting the pre-slice receiver name. */
class TimerBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = FeatureTimerBootReceiver().onReceive(context, intent)
}
