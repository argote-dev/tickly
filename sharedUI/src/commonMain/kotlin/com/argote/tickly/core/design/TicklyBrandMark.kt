package com.argote.tickly.core.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope

val TicklyBrandLavender = Color(0xFFB8A1FF)

/** The canonical Tickly T, drawn from the same 48-unit geometry as the launcher icon. */
@Composable
fun TicklyBrandMark(modifier: Modifier = Modifier, color: Color = TicklyBrandLavender) {
    Canvas(modifier.fillMaxSize()) { drawTicklyBrandMark(color) }
}

private fun DrawScope.drawTicklyBrandMark(color: Color) {
    val scaleX = size.width / 48f
    val scaleY = size.height / 48f
    fun x(value: Float) = value * scaleX
    fun y(value: Float) = value * scaleY

    drawPath(
        Path().apply {
            moveTo(x(3f), y(0f))
            quadraticTo(x(0f), y(0f), x(0f), y(3f))
            lineTo(x(0f), y(9f))
            quadraticTo(x(0f), y(12f), x(3f), y(12f))
            lineTo(x(17f), y(12f))
            lineTo(x(17f), y(45f))
            quadraticTo(x(17f), y(48f), x(20f), y(48f))
            lineTo(x(27f), y(48f))
            quadraticTo(x(30f), y(48f), x(30f), y(45f))
            lineTo(x(30f), y(12f))
            lineTo(x(37f), y(0f))
            close()
        },
        color,
    )
    drawPath(
        Path().apply {
            moveTo(x(40f), y(0f))
            lineTo(x(45f), y(0f))
            quadraticTo(x(48f), y(0f), x(48f), y(3f))
            lineTo(x(48f), y(9f))
            quadraticTo(x(48f), y(12f), x(45f), y(12f))
            lineTo(x(33f), y(12f))
            close()
        },
        color,
    )
}
