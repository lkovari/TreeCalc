package com.lkovari.mobile.apps.treecalc.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.lkovari.mobile.apps.treecalc.ui.theme.GlossBorder
import com.lkovari.mobile.apps.treecalc.ui.theme.GlossLabelDark
import com.lkovari.mobile.apps.treecalc.ui.theme.GlossLabelLight
import com.lkovari.mobile.apps.treecalc.ui.theme.LocalTreeCalcPalette
import com.lkovari.mobile.apps.treecalc.ui.theme.TreeCalcPalette

private val KeyShape = RoundedCornerShape(8.dp)

@Composable
fun GlossKey(
    label: String,
    visual: KeyVisual,
    enabled: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit,
    testTag: String
) {
    val palette = LocalTreeCalcPalette.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val push = if (pressed && enabled) 0.82f else 1f
    val darkFactor = if (palette.dark) 0.62f else 1f
    val endDepth = if (palette.dark) 0.78f else 0.25f
    val base = fillFor(visual, palette)
    val top = base.scaled(darkFactor * push)
    val bottom = base.scaled(darkFactor * endDepth * push)
    val labelColor = if (palette.dark || visual.lightLabel) {
        GlossLabelLight
    } else {
        GlossLabelDark
    }
    val borderColor = if (selected) Color.White else GlossBorder
    val borderWidth = if (selected) 2.5.dp else 1.5.dp
    val highlight = if (pressed) 0.12f else 0.38f
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        Box(
            modifier = modifier
                .alpha(if (enabled) 1f else 0.38f)
                .testTag(testTag)
                .glossShadow(palette.dark)
                .clip(KeyShape)
                .background(Brush.verticalGradient(listOf(top, bottom)))
                .then(
                    if (palette.dark) {
                        Modifier.background(
                            Brush.verticalGradient(
                                0f to Color.White.copy(alpha = highlight),
                                0.5f to Color.Transparent,
                                1f to Color.Transparent
                            )
                        )
                    } else {
                        Modifier
                    }
                )
                .border(borderWidth, borderColor, KeyShape)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = labelColor,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = fontSize,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun Modifier.glossShadow(dark: Boolean): Modifier {
    if (dark) {
        return this
    }
    return drawBehind {
        val paint = Paint()
        val framework = paint.asFrameworkPaint()
        framework.color = Color.Gray.toArgb()
        framework.maskFilter = BlurMaskFilter(3.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
        val left = 3.dp.toPx()
        val top = 3.dp.toPx()
        drawIntoCanvas { canvas ->
            canvas.drawRoundRect(
                left,
                top,
                size.width + left,
                size.height + top,
                8.dp.toPx(),
                8.dp.toPx(),
                paint
            )
        }
    }
}

private fun fillFor(visual: KeyVisual, palette: TreeCalcPalette): Color {
    return when (visual) {
        KeyVisual.NUMBER -> palette.numberKey
        KeyVisual.FUNCTION -> palette.functionKey
        KeyVisual.OPERATOR -> palette.operatorKey
        KeyVisual.LOGIC -> palette.logicKey
        KeyVisual.EQUALS -> palette.equalsKey
        KeyVisual.ACTION -> palette.actionKey
    }
}

private val KeyVisual.lightLabel: Boolean
    get() = this != KeyVisual.OPERATOR && this != KeyVisual.EQUALS

private fun Color.scaled(depth: Float): Color {
    val factor = depth.coerceIn(0f, 1f)
    return Color(red * factor, green * factor, blue * factor, alpha)
}
