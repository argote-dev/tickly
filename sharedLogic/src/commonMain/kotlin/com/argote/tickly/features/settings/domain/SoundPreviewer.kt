package com.argote.tickly.features.settings.domain

/** Native sound output boundary used by settings presentation. */
interface SoundPreviewer {
    fun preview(soundIndex: Int)
}
