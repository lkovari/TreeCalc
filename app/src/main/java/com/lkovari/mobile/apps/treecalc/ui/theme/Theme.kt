package com.lkovari.mobile.apps.treecalc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val LocalTreeCalcPalette = staticCompositionLocalOf { LightPalette }

private val LightColors = lightColorScheme(
    primary = OperatorMist,
    onPrimary = InkRose,
    secondary = FunctionLilac,
    onSecondary = InkRose,
    tertiary = EqualsBlush,
    onTertiary = InkRose,
    background = PetalPaper,
    onBackground = InkRose,
    surface = Porcelain,
    onSurface = InkRose,
    surfaceVariant = Color(0xFFF0E4EA),
    onSurfaceVariant = MutedBlush,
    outline = Color(0xFFD8C4CC),
    error = Color(0xFFB06070),
    onError = Color(0xFFFFF6F7)
)

private val DarkColors = darkColorScheme(
    primary = OperatorMistDark,
    onPrimary = MoonCream,
    secondary = FunctionLilacDark,
    onSecondary = MoonCream,
    tertiary = EqualsBlushDark,
    onTertiary = DuskPlum,
    background = DuskPlum,
    onBackground = MoonCream,
    surface = DuskSurface,
    onSurface = MoonCream,
    surfaceVariant = Color(0xFF423848),
    onSurfaceVariant = MutedMoon,
    outline = Color(0xFF5A4E58),
    error = Color(0xFFE0A0B0),
    onError = Color(0xFF3A2028)
)

fun pastelScreenBrush(palette: TreeCalcPalette): Brush {
    return Brush.verticalGradient(
        colors = listOf(palette.screenWashTop, palette.screenWashBottom)
    )
}

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
