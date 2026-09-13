package com.argote.tickly.features.timer.data

import android.content.Context
import com.argote.tickly.features.timer.domain.TimerSnapshotRepository
import androidx.core.content.edit

/** Android persistence adapter. The existing file and key are a compatibility contract. */
class SharedPreferencesTimerSnapshotRepository(context: Context) : TimerSnapshotRepository {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun readSnapshot(): String? = preferences.getString(SNAPSHOT_KEY, null)
    override fun writeSnapshot(snapshot: String) {
        preferences.edit { putString(SNAPSHOT_KEY, snapshot) }
    }

    companion object {
        const val PREFERENCES_NAME = "tickly_timer"
        const val SNAPSHOT_KEY = "snapshot"
    }
}
