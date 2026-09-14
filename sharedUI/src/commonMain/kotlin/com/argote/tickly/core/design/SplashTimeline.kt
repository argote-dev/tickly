package com.argote.tickly.core.design

/** The deterministic sequence used by the branded launch overlay. */
object SplashTimeline {
    const val MARK_HOLD_MILLIS = 2_000
    const val REVEAL_MILLIS = 800
    const val NAME_HOLD_MILLIS = 500
    const val FADE_OUT_MILLIS = 300
    const val TOTAL_MILLIS = MARK_HOLD_MILLIS + REVEAL_MILLIS + NAME_HOLD_MILLIS + FADE_OUT_MILLIS

    fun diagonalSliceEdge(progress: Float, width: Float, height: Float): Float {
        val slice = height * .28f
        return (width + slice * 2f) * progress
    }
}
