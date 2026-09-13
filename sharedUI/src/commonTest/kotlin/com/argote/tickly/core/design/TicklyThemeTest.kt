package com.argote.tickly.core.design

import androidx.compose.ui.graphics.Color
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TicklyThemeTest {
    @Test
    fun accentSchemesGiveEachSelectionItsOwnTonalContainer() {
        val containers = (0..5).map { colorSchemeForAccent(it).secondaryContainer }

        assertEquals(6, containers.distinct().size)
        assertEquals(Color(0xFF382F4A), containers[0])
        assertEquals(Color(0xFF2B463F), containers[1])
        assertEquals(Color(0xFF30424F), containers[2])
        assertEquals(Color(0xFF4A3040), containers[3])
        assertEquals(Color(0xFF4A3B2D), containers[4])
        assertEquals(Color(0xFF3E482A), containers[5])
    }

    @Test
    fun accentSchemesKeepInteractiveRolePairsLegible() {
        (0..5).forEach { index ->
            val scheme = colorSchemeForAccent(index)

            assertContrastAtLeast(scheme.onPrimary, scheme.primary, 4.5)
            assertContrastAtLeast(scheme.onPrimaryContainer, scheme.primaryContainer, 4.5)
            assertContrastAtLeast(scheme.onSecondary, scheme.secondary, 4.5)
            assertContrastAtLeast(scheme.onSecondaryContainer, scheme.secondaryContainer, 4.5)
            assertContrastAtLeast(scheme.onTertiary, scheme.tertiary, 4.5)
            assertContrastAtLeast(scheme.onTertiaryContainer, scheme.tertiaryContainer, 4.5)
        }
    }

    @Test
    fun invalidAccentIndexFallsBackToTheFirstAccentScheme() {
        val fallback = colorSchemeForAccent(0)
        listOf(-1, 99).forEach { invalidIndex ->
            val scheme = colorSchemeForAccent(invalidIndex)
            assertEquals(fallback.primary, scheme.primary)
            assertEquals(fallback.secondaryContainer, scheme.secondaryContainer)
            assertEquals(fallback.surfaceTint, scheme.surfaceTint)
        }
    }

    private fun assertContrastAtLeast(
        foreground: Color,
        background: Color,
        minimum: Double,
    ) {
        val contrast =
            (foreground.relativeLuminance() + 0.05) /
                (background.relativeLuminance() + 0.05)
        val inverseContrast =
            (background.relativeLuminance() + 0.05) /
                (foreground.relativeLuminance() + 0.05)
        assertTrue(maxOf(contrast, inverseContrast) >= minimum)
    }

    private fun Color.relativeLuminance(): Double = (0.2126 * red.linearized()) + (0.7152 * green.linearized()) + (0.0722 * blue.linearized())

    private fun Float.linearized(): Double = if (this <= 0.04045f) this / 12.92 else ((this + 0.055) / 1.055).pow(2.4)
}
