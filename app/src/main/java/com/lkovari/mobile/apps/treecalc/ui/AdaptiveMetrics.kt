package com.lkovari.mobile.apps.treecalc.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AdaptiveMetrics(
    val compact: Boolean,
    val contentMaxWidth: Dp,
    val resultSize: TextUnit,
    val keyLabelSize: TextUnit,
    val chipLabelSize: TextUnit,
    val titleSize: TextUnit,
    val keySpacing: Dp,
    val keyCorner: Dp,
    val screenPadding: Dp,
    val displayPadding: Dp
)

@Composable
fun rememberAdaptiveMetrics(): AdaptiveMetrics {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    val heightDp = configuration.screenHeightDp
    val tiny = heightDp < 620 || widthDp < 340
    val compact = heightDp < 720 || widthDp < 380
    val tablet = widthDp >= 600
    val keySize = when {
        tiny -> 13.sp
        compact -> 15.sp
        tablet -> 18.sp
        else -> 16.sp
    }
    return AdaptiveMetrics(
        compact = compact,
        contentMaxWidth = if (tablet) 560.dp else widthDp.dp,
        resultSize = when {
            tiny -> 24.sp
            compact -> 28.sp
            tablet -> 40.sp
            else -> 34.sp
        },
        keyLabelSize = keySize,
        chipLabelSize = if (tiny) 12.sp else keySize,
        titleSize = when {
            tiny -> 16.sp
            compact -> 18.sp
            else -> 22.sp
        },
        keySpacing = if (tiny) 2.dp else if (compact) 3.dp else 5.dp,
        keyCorner = if (compact) 10.dp else 14.dp,
        screenPadding = if (tiny) 6.dp else if (compact) 8.dp else 12.dp,
        displayPadding = if (tiny) 8.dp else if (compact) 10.dp else 16.dp
    )
}
