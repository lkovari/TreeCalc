package com.lkovari.mobile.apps.treecalc.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lkovari.mobile.apps.treecalc.R
import com.lkovari.mobile.apps.treecalc.ui.theme.LocalTreeCalcPalette
import com.lkovari.mobile.apps.treecalc.ui.theme.TitleMagenta
import com.lkovari.mobile.apps.treecalc.ui.theme.TreeCalcPalette

private val SplashCardShape = RoundedCornerShape(32.dp)

@Composable
fun SplashScreen() {
    val palette = LocalTreeCalcPalette.current
    val pulse = rememberInfiniteTransition(label = "splashPulse")
    val leafScale by pulse.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leafScale"
    )
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        val compactSplash = maxHeight < 640.dp
        val titleSize = if (compactSplash) 32.sp else 40.sp
        val badgeSize = if (compactSplash) 13.sp else 15.sp
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .shadow(
                    elevation = 10.dp,
                    shape = SplashCardShape,
                    ambientColor = Color.Black.copy(alpha = 0.18f),
                    spotColor = Color.Black.copy(alpha = 0.10f)
                )
                .clip(SplashCardShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(palette.splashTop, palette.splashMid, palette.splashBottom)
                    ),
                    shape = SplashCardShape
                )
                .padding(
                    horizontal = if (compactSplash) 24.dp else 32.dp,
                    vertical = if (compactSplash) 28.dp else 36.dp
                )
        ) {
            Text(
                text = "9, 7, 6, ×, +",
                color = palette.splashOn,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                fontSize = badgeSize,
                modifier = Modifier
                    .background(palette.splashBadgeFill, RoundedCornerShape(999.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(if (compactSplash) 16.dp else 28.dp))
            SplashTree(scale = leafScale, compact = compactSplash, palette = palette)
            Spacer(modifier = Modifier.height(if (compactSplash) 20.dp else 32.dp))
            Text(
                text = stringResource(R.string.app_name),
                color = TitleMagenta,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = titleSize,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.splash_tagline),
                color = palette.splashTagline,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun SplashTree(scale: Float, compact: Boolean, palette: TreeCalcPalette) {
    val root = if (compact) 56.dp else 70.dp
    val mid = if (compact) 46.dp else 56.dp
    val leaf = if (compact) 40.dp else 50.dp
    val hGap = if (compact) 28.dp else 36.dp
    val vGap = if (compact) 18.dp else 24.dp
    val leafGap = if (compact) 22.dp else 28.dp
    val rightSubtreeLeft = mid + hGap
    val leafRowWidth = leaf * 2 + leafGap
    val xNine = 0.dp
    val xSeven = rightSubtreeLeft
    val xSix = rightSubtreeLeft + leaf + leafGap
    val xTimes = rightSubtreeLeft + (leafRowWidth - mid) / 2
    val plusCenterX = (xNine + mid / 2 + xTimes + mid / 2) / 2
    val xPlus = plusCenterX - root / 2
    val yPlus = 0.dp
    val yMid = root + vGap
    val yLeaf = yMid + mid + vGap
    val treeWidth = maxOf(xSix + leaf, xPlus + root, xTimes + mid)
    val treeHeight = yLeaf + leaf
    Box(
        modifier = Modifier
            .size(treeWidth, treeHeight)
            .drawBehind {
                val stroke = 3.dp.toPx()
                fun nodeCenter(x: Dp, y: Dp, size: Dp) = Offset(
                    x.toPx() + size.toPx() / 2f,
                    y.toPx() + size.toPx() / 2f
                )
                val plus = nodeCenter(xPlus, yPlus, root)
                val nine = nodeCenter(xNine, yMid, mid)
                val times = nodeCenter(xTimes, yMid, mid)
                val seven = nodeCenter(xSeven, yLeaf, leaf)
                val six = nodeCenter(xSix, yLeaf, leaf)
                drawLine(palette.splashStem, plus, nine, stroke, StrokeCap.Round)
                drawLine(palette.splashStem, plus, times, stroke, StrokeCap.Round)
                drawLine(palette.splashStem, times, seven, stroke, StrokeCap.Round)
                drawLine(palette.splashStem, times, six, stroke, StrokeCap.Round)
            }
    ) {
        SplashNode(
            label = "+",
            color = palette.splashNodeRoot,
            onColor = palette.splashOn,
            size = root,
            scale = 1f,
            modifier = Modifier.offset(xPlus, yPlus)
        )
        SplashNode(
            label = "9",
            color = palette.splashNodeLeft,
            onColor = palette.splashOn,
            size = mid,
            scale = scale,
            modifier = Modifier.offset(xNine, yMid)
        )
        SplashNode(
            label = "×",
            color = palette.splashNodeOp,
            onColor = palette.splashOn,
            size = mid,
            scale = 1f,
            modifier = Modifier.offset(xTimes, yMid)
        )
        SplashNode(
            label = "7",
            color = palette.splashNodeLeft,
            onColor = palette.splashOn,
            size = leaf,
            scale = scale,
            modifier = Modifier.offset(xSeven, yLeaf)
        )
        SplashNode(
            label = "6",
            color = palette.splashNodeRight,
            onColor = palette.splashOn,
            size = leaf,
            scale = scale,
            modifier = Modifier.offset(xSix, yLeaf)
        )
    }
}

@Composable
private fun SplashNode(
    label: String,
    color: Color,
    onColor: Color,
    size: Dp,
    scale: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .shadow(6.dp, CircleShape, ambientColor = color.copy(alpha = 0.35f), spotColor = color.copy(alpha = 0.28f))
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = onColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = (size.value * 0.36f).sp
        )
    }
}
