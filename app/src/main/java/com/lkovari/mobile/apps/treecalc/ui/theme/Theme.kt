package com.lkovari.mobile.apps.treecalc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalTreeCalcPalette = staticCompositionLocalOf { LightPalette }

private val LightColors = lightColorScheme(
    primary = OperatorTeal,
    onPrimary = Color(0xFFFFFFFF),
    secondary = FunctionKeyLight,
    onSecondary = Color(0xFFFFFFFF),
    tertiary = EqualsRose,
    onTertiary = Color(0xFFFFFFFF),
    background = SageMist,
    onBackground = InkGreen,
    surface = WarmPaper,
    onSurface = InkGreen,
    surfaceVariant = NumberKeyLight,
    onSurfaceVariant = MutedSage,
    outline = Color(0xFF8AADA0),
    error = Color(0xFFB4233A),
    onError = Color(0xFFFFF6F7)
)

private val DarkColors = darkColorScheme(
    primary = OperatorTealDark,
    onPrimary = Color(0xFF0E1C16),
    secondary = FunctionKeyDark,
    onSecondary = Color(0xFF0E1C16),
    tertiary = EqualsRoseDark,
    onTertiary = Color(0xFF1A1014),
    background = NightForest,
    onBackground = MoonInk,
    surface = NightSurface,
    onSurface = MoonInk,
    surfaceVariant = NumberKeyDark,
    onSurfaceVariant = MutedMoon,
    outline = Color(0xFF8AA9A2),
    error = Color(0xFFFF9AAB),
    onError = Color(0xFF2A1014)
)

@Composable
fun TreeCalcTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) {
        DarkPalette
    } else {
        LightPalette
    }
    val colors = if (darkTheme) {
        DarkColors
    } else {
        LightColors
    }
    CompositionLocalProvider(LocalTreeCalcPalette provides palette) {
        MaterialTheme(
            colorScheme = colors,
            typography = Typography,
            content = content
        )
    }
}
