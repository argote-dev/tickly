package com.argote.tickly.core.design

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Dark ambient gradient whose motion is opt-in for accessibility. */
@Composable
fun AmbientBackground(accent: Color, animate: Boolean) {
    val offset = if (animate) {
        val transition = rememberInfiniteTransition(label = "ambient background")
        val shift by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(16_000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "gradient drift",
        )
        shift
    } else .35f
    Canvas(Modifier.fillMaxSize()) {
        drawRect(
            Brush.linearGradient(
                listOf(Color(0xFF111016), accent.copy(alpha = .24f), Color(0xFF101217)),
                Offset(size.width * offset - size.width / 2, 0f),
                Offset(size.width, size.height),
            ),
        )
    }
}
