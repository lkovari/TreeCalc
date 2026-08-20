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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lkovari.mobile.apps.treecalc.R
import com.lkovari.mobile.apps.treecalc.ui.theme.SplashForest
import com.lkovari.mobile.apps.treecalc.ui.theme.SplashRose
import com.lkovari.mobile.apps.treecalc.ui.theme.SplashTeal

@Composable
fun SplashScreen() {
    val pulse = rememberInfiniteTransition(label = "splashPulse")
    val leafScale by pulse.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leafScale"
    )
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SplashTeal,
                        Color(0xFF04948A),
                        SplashForest,
                        Color(0xFFCC1A4A),
                        SplashRose
                    )
                )
            )
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        val compactSplash = maxHeight < 640.dp
        val titleSize = if (compactSplash) 32.sp else 40.sp
        val badgeSize = if (compactSplash) 13.sp else 16.sp
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "9, 7, 6, ×, +",
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                fontSize = badgeSize,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(if (compactSplash) 16.dp else 28.dp))
            SplashTree(scale = leafScale, compact = compactSplash)
            Spacer(modifier = Modifier.height(if (compactSplash) 20.dp else 32.dp))
            Text(
                text = stringResource(R.string.app_name),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = titleSize,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.splash_tagline),
                color = Color.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun SplashTree(scale: Float, compact: Boolean) {
    val root = if (compact) 58.dp else 72.dp
    val mid = if (compact) 48.dp else 58.dp
    val leaf = if (compact) 42.dp else 52.dp
    val gap = if (compact) 28.dp else 36.dp
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        SplashNode(label = "+", color = Color(0xFF5A38CC), size = root, scale = 1f)
        SplashStem()
        Row(horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.CenterVertically) {
            SplashNode(label = "9", color = Color(0xFF1E8A48), size = mid, scale = scale)
            SplashNode(label = "×", color = SplashTeal, size = mid, scale = 1f)
        }
        Box(
            modifier = Modifier
                .padding(start = if (compact) 76.dp else 94.dp)
                .width(3.dp)
                .height(if (compact) 14.dp else 18.dp)
                .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(2.dp))
        )
        Row(
            modifier = Modifier.padding(start = if (compact) 76.dp else 94.dp),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 22.dp else 28.dp)
        ) {
            SplashNode(label = "7", color = Color(0xFF1E8A48), size = leaf, scale = scale)
            SplashNode(label = "6", color = SplashRose, size = leaf, scale = scale)
        }
    }
}

@Composable
private fun SplashStem() {
    Box(
        modifier = Modifier
            .width(4.dp)
            .height(22.dp)
            .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(2.dp))
    )
}

@Composable
private fun SplashNode(label: String, color: Color, size: Dp, scale: Float) {
    Box(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .shadow(8.dp, CircleShape)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.38f).sp
        )
    }
}
