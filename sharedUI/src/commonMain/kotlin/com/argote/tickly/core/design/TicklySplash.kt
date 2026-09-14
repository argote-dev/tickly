package com.argote.tickly.core.design

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private val SplashBackground = androidx.compose.ui.graphics.Color(0xFF101015)

/** Full-screen, non-interactive launch treatment. The host decides when it is shown. */
@Composable
fun TicklySplash(reducedMotion: Boolean, onFinished: () -> Unit) {
    var rowWidth by remember { mutableIntStateOf(0) }
    var markWidth by remember { mutableIntStateOf(0) }
    var reveal by remember { mutableFloatStateOf(0f) }
    val opacity = remember { Animatable(1f) }
    // dp-to-sp conversion preserves the 54dp wordmark size regardless of the user's font scale.
    val wordmarkSize = with(LocalDensity.current) { 54.dp.toSp() }

    LaunchedEffect(reducedMotion) {
        if (reducedMotion) {
            onFinished()
            return@LaunchedEffect
        }
        delay(SplashTimeline.MARK_HOLD_MILLIS.toLong())
        androidx.compose.animation.core.animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(SplashTimeline.REVEAL_MILLIS, easing = FastOutSlowInEasing),
        ) { value, _ -> reveal = value }
        delay(SplashTimeline.NAME_HOLD_MILLIS.toLong())
        opacity.animateTo(0f, tween(SplashTimeline.FADE_OUT_MILLIS, easing = FastOutSlowInEasing))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(opacity.value)
            .background(SplashBackground)
            .clickable(onClick = { })
            // The launch overlay is announced as the product name, never as the hidden timer content.
            .clearAndSetSemantics { contentDescription = "Tickly" },
        contentAlignment = Alignment.Center,
    ) {
        val shift = (rowWidth - markWidth).coerceAtLeast(0) / 2f
        Row(
            modifier = Modifier
                .onSizeChanged { rowWidth = it.width }
                .offset { IntOffset((shift * (1f - reveal)).roundToInt(), 0) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TicklyBrandMark(
                modifier = Modifier
                    .size(60.dp)
                    .onSizeChanged { markWidth = it.width },
            )
            Text(
                text = "ickly",
                modifier = Modifier.diagonalReveal(reveal),
                color = TicklyBrandLavender,
                fontSize = wordmarkSize,
                lineHeight = wordmarkSize,
                letterSpacing = with(LocalDensity.current) { (-2).dp.toSp() },
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

private fun Modifier.diagonalReveal(progress: Float): Modifier = drawWithContent {
    val slice = size.height * .28f
    // Keep the leading edge outside both bounds at the endpoints: no letter flashes early.
    val revealedWidth = SplashTimeline.diagonalSliceEdge(progress, size.width, size.height)
    clipPath(
        androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, 0f)
            lineTo(revealedWidth, 0f)
            lineTo(revealedWidth - slice * 2f, size.height)
            lineTo(0f, size.height)
            close()
        },
    ) { this@drawWithContent.drawContent() }
}
