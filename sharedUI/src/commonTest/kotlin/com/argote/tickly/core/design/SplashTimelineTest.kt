package com.argote.tickly.core.design

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SplashTimelineTest {
    @Test
    fun `diagonal reveal starts and finishes outside its word`() {
        assertEquals(0f, SplashTimeline.diagonalSliceEdge(0f, 100f, 50f))
        val halfway = SplashTimeline.diagonalSliceEdge(.5f, 100f, 50f)
        val end = SplashTimeline.diagonalSliceEdge(1f, 100f, 50f)
        assertTrue(halfway > 0f)
        assertTrue(halfway < end)
        assertTrue(end - 2f * (50f * .28f) >= 100f)
    }
}
