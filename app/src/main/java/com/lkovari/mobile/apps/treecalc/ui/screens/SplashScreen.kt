package com.lkovari.mobile.apps.treecalc.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
    val gap = if (compact) 28.dp else 36.dp
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        SplashNode(label = "+", color = palette.splashNodeRoot, onColor = palette.splashOn, size = root, scale = 1f)
        SplashStem(color = palette.splashStem)
        Row(horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.CenterVertically) {
            SplashNode(label = "9", color = palette.splashNodeLeft, onColor = palette.splashOn, size = mid, scale = scale)
            SplashNode(label = "×", color = palette.splashNodeOp, onColor = palette.splashOn, size = mid, scale = 1f)
        }
        Box(
            modifier = Modifier
                .padding(start = if (compact) 76.dp else 94.dp)
                .width(3.dp)
                .height(if (compact) 14.dp else 18.dp)
                .background(palette.splashStem, RoundedCornerShape(2.dp))
        )
        Row(
            modifier = Modifier.padding(start = if (compact) 76.dp else 94.dp),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 22.dp else 28.dp)
        ) {
            SplashNode(label = "7", color = palette.splashNodeLeft, onColor = palette.splashOn, size = leaf, scale = scale)
            SplashNode(label = "6", color = palette.splashNodeRight, onColor = palette.splashOn, size = leaf, scale = scale)
        }
    }
}

@Composable
private fun SplashStem(color: Color) {
    Box(
        modifier = Modifier
            .width(3.dp)
            .height(22.dp)
            .background(color, RoundedCornerShape(2.dp))
    )
}

@Composable
private fun SplashNode(label: String, color: Color, onColor: Color, size: Dp, scale: Float) {
    Box(
        modifier = Modifier
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
