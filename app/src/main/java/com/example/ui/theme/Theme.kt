package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class GlassMaterial {
    CLEAR,
    FROSTED,
    DEEP,
    LUMINOUS,
    PEARL
}

@Immutable
data class AlloraGlassColors(
    val isDark: Boolean,
    val clearGlass: Color,
    val frostedGlass: Color,
    val deepGlass: Color,
    val luminousGlass: Color,
    val pearlTop: Color,
    val pearlBottom: Color,
    val pearlInk: Color,
    val primaryGlass: Color,
    val secondaryGlass: Color,
    val elevatedGlass: Color,
    val glassBorder: Color,
    val innerHighlight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val activeNavPill: Color,
    val activeNavContent: Color,
    val inactiveNavContent: Color
)

private val DarkGlassColors = AlloraGlassColors(
    isDark = true,
    clearGlass = ClearGlassDark,
    frostedGlass = FrostedGlassDark,
    deepGlass = DeepGlassDark,
    luminousGlass = LuminousGlassDark,
    pearlTop = PearlGlassTop,
    pearlBottom = PearlGlassBottom,
    pearlInk = MidnightNavy,
    primaryGlass = DarkGlassPrimary,
    secondaryGlass = DarkGlassSecondary,
    elevatedGlass = DarkGlassElevated,
    glassBorder = DarkGlassBorder,
    innerHighlight = DarkGlassHighlight,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textMuted = DarkTextMuted,
    activeNavPill = PearlWhite,
    activeNavContent = MidnightNavy,
    inactiveNavContent = Color(0xFFD2E2FF)
)

private val LightGlassColors = AlloraGlassColors(
    isDark = false,
    clearGlass = ClearGlassLight,
    frostedGlass = FrostedGlassLight,
    deepGlass = DeepGlassLight,
    luminousGlass = LuminousGlassLight,
    pearlTop = Color(0xFFFFFFFF),
    pearlBottom = Color(0xFFE6F0FF),
    pearlInk = MidnightNavy,
    primaryGlass = LightGlassPrimary,
    secondaryGlass = LightGlassSecondary,
    elevatedGlass = Color(0xE8F7FAFF),
    glassBorder = LightGlassBorder,
    innerHighlight = Color(0xFFFFFFFF),
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    activeNavPill = MidnightNavy,
    activeNavContent = PearlWhite,
    inactiveNavContent = Color(0xFF3A4F78)
)

val LocalAlloraGlassColors = staticCompositionLocalOf { DarkGlassColors }

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = PearlWhite,
    primaryContainer = BlueAtmosphere,
    onPrimaryContainer = SoftCyan,
    secondary = Lavender,
    onSecondary = MidnightNavy,
    secondaryContainer = DeepOcean,
    onSecondaryContainer = IridescentPink,
    tertiary = SoftCyan,
    onTertiary = MidnightNavy,
    background = MidnightNavy,
    onBackground = PearlWhite,
    surface = DeepOcean,
    onSurface = PearlWhite,
    surfaceVariant = DarkGlassSecondary,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkGlassBorder,
    error = ErrorCoral,
    onError = MidnightNavy
)

private val LightColorScheme = lightColorScheme(
    primary = BlueAtmosphere,
    onPrimary = PearlWhite,
    primaryContainer = Color(0xFFDCE9FF),
    onPrimaryContainer = MidnightNavy,
    secondary = Violet,
    onSecondary = PearlWhite,
    secondaryContainer = Color(0xFFEDE8FF),
    onSecondaryContainer = MidnightNavy,
    tertiary = Color(0xFF0E7490),
    onTertiary = PearlWhite,
    background = LightAtmosphereBottom,
    onBackground = LightTextPrimary,
    surface = LightGlassPrimary,
    onSurface = LightTextPrimary,
    surfaceVariant = LightGlassSecondary,
    onSurfaceVariant = LightTextSecondary,
    outline = LightGlassBorder,
    error = ErrorCoral,
    onError = PearlWhite
)

@Composable
fun AlloraTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val glassColors = if (darkTheme) DarkGlassColors else LightGlassColors

    CompositionLocalProvider(LocalAlloraGlassColors provides glassColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
