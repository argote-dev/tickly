package com.argote.tickly.features.settings.data

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.argote.tickly.features.settings.domain.SoundPreviewer
import com.argote.tickly.sharedUI.R

/** Plays a short bundled preview of the selected Tickly alert. */
class AndroidSoundPreviewer(
    private val context: Context,
) : SoundPreviewer {
    override fun preview(soundIndex: Int) {
        val resource =
            when (soundIndex) {
                0 -> R.raw.tickly_0
                1 -> R.raw.tickly_1
                2 -> R.raw.tickly_2
                else -> return
            }
        MediaPlayer.create(context, resource)?.apply {
            setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            setOnCompletionListener { player -> player.release() }
            setOnErrorListener { player, _, _ ->
                player.release()
                true
            }
            start()
        }
    }
}
