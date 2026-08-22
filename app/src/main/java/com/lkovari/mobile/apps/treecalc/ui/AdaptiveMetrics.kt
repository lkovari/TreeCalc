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
    val keyElevation: Dp,
    val screenPadding: Dp,
    val displayPadding: Dp,
    val displayCorner: Dp,
    val chipHeight: Dp
)

@Composable
fun rememberAdaptiveMetrics(): AdaptiveMetrics {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    val heightDp = configuration.screenHeightDp
    val tiny = heightDp < 600 || widthDp < 320
    val compact = heightDp < 720 || widthDp < 360
    val tablet = widthDp >= 600
    val largePhone = widthDp >= 411 && heightDp >= 800 && !tablet
    val keySize = when {
        tiny -> 12.sp
        compact -> 14.sp
        tablet -> 17.sp
        largePhone -> 16.sp
        else -> 15.sp
    }
    return AdaptiveMetrics(
        compact = compact,
        contentMaxWidth = when {
            tablet -> 480.dp
            largePhone -> widthDp.dp
            else -> widthDp.dp
        },
        resultSize = when {
            tiny -> 22.sp
            compact -> 26.sp
            tablet -> 36.sp
            largePhone -> 34.sp
            else -> 30.sp
        },
        keyLabelSize = keySize,
        chipLabelSize = if (tiny) 11.sp else if (compact) 12.sp else 13.sp,
        titleSize = when {
            tiny -> 16.sp
            compact -> 18.sp
            tablet -> 24.sp
            else -> 20.sp
        },
        keySpacing = when {
            tiny -> 3.dp
            compact -> 4.dp
            tablet -> 8.dp
            else -> 6.dp
        },
        keyCorner = when {
            tiny -> 10.dp
            compact -> 12.dp
            tablet -> 18.dp
            else -> 14.dp
        },
        keyElevation = if (tiny) 1.dp else 2.dp,
        screenPadding = when {
            tiny -> 8.dp
            compact -> 10.dp
            tablet -> 20.dp
            else -> 14.dp
        },
        displayPadding = when {
            tiny -> 10.dp
            compact -> 12.dp
            tablet -> 20.dp
            else -> 16.dp
        },
        displayCorner = if (compact) 20.dp else 24.dp,
        chipHeight = if (tiny) 30.dp else if (compact) 34.dp else 38.dp
    )
}
