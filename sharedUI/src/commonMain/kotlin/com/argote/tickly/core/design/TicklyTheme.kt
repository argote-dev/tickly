package com.argote.tickly.core.design

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

private val accentColors = listOf(
    Color(0xFFB8A1FF), Color(0xFF69D7C3), Color(0xFF8CB8FF),
    Color(0xFFFFA7C1), Color(0xFFFFC980), Color(0xFFC2DC70),
)

private val accentContainers = listOf(
    Color(0xFF382F4A), Color(0xFF2B463F), Color(0xFF30424F),
    Color(0xFF4A3040), Color(0xFF4A3B2D), Color(0xFF3E482A),
)

private val appBackground = Color(0xFF101015)
private val appSurface = Color(0xFF1B1920)
private val appOnSurface = Color(0xFFF0EDF6)
private val appOnSurfaceVariant = Color(0xFFC9C1D0)
private val darkContent = Color(0xFF17141C)

/** The selected accent, with invalid persisted indices falling back to the first option. */
fun accentForIndex(index: Int): Color = accentColors.getOrElse(index) { accentColors.first() }

/**
 * Complete dark Material scheme for a selected Tickly accent.
 *
 * Interactive semantic roles all derive from the accent so components such as tonal and
 * outlined buttons cannot fall back to Material's default purple tokens.
 */
fun colorSchemeForAccent(index: Int): ColorScheme {
    val resolvedIndex = index.takeIf { it in accentColors.indices } ?: 0
    val accent = accentColors[resolvedIndex]
    val container = accentContainers[resolvedIndex]
    val tertiaryContainer = blend(appSurface, accent, 0.12f)
    val outline = blend(appOnSurfaceVariant, accent, 0.26f)
    val outlineVariant = blend(appSurface, accent, 0.22f)
    val inverseSurface = Color(0xFFE9E5ED)
    val inversePrimary = darken(accent, 0.36f)

    return darkColorScheme(
        primary = accent,
        onPrimary = darkContent,
        primaryContainer = container,
        onPrimaryContainer = accent,
        inversePrimary = inversePrimary,
        secondary = accent,
        onSecondary = darkContent,
        secondaryContainer = container,
        onSecondaryContainer = accent,
        tertiary = accent,
        onTertiary = darkContent,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = accent,
        background = appBackground,
        onBackground = appOnSurface,
        surface = appSurface,
        onSurface = appOnSurface,
        surfaceVariant = Color(0xFF2B2930),
        onSurfaceVariant = appOnSurfaceVariant,
        surfaceTint = accent,
        inverseSurface = inverseSurface,
        inverseOnSurface = darkContent,
        outline = outline,
        outlineVariant = outlineVariant,
        surfaceBright = Color(0xFF25232A),
        surfaceDim = Color(0xFF101015),
        surfaceContainer = Color(0xFF1F1D24),
        surfaceContainerHigh = Color(0xFF29272E),
        surfaceContainerHighest = Color(0xFF343139),
        surfaceContainerLow = Color(0xFF19171E),
        surfaceContainerLowest = Color(0xFF0D0C11),
    ).copy(
        primaryFixed = accent,
        primaryFixedDim = accent,
        onPrimaryFixed = darkContent,
        onPrimaryFixedVariant = darkContent,
        secondaryFixed = accent,
        secondaryFixedDim = accent,
        onSecondaryFixed = darkContent,
        onSecondaryFixedVariant = darkContent,
        tertiaryFixed = accent,
        tertiaryFixedDim = accent,
        onTertiaryFixed = darkContent,
        onTertiaryFixedVariant = darkContent,
    )
}

private fun blend(start: Color, end: Color, fraction: Float): Color = Color(
    red = start.red + (end.red - start.red) * fraction,
    green = start.green + (end.green - start.green) * fraction,
    blue = start.blue + (end.blue - start.blue) * fraction,
    alpha = 1f,
)

private fun darken(color: Color, amount: Float): Color = Color(
    red = color.red * amount,
    green = color.green * amount,
    blue = color.blue * amount,
    alpha = 1f,
)
