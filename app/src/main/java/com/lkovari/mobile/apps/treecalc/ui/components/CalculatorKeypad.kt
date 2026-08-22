package com.lkovari.mobile.apps.treecalc.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lkovari.mobile.apps.treecalc.R
import com.lkovari.mobile.apps.treecalc.engine.CalculatorKey
import com.lkovari.mobile.apps.treecalc.engine.NumericBase
import com.lkovari.mobile.apps.treecalc.ui.theme.LocalTreeCalcPalette
import com.lkovari.mobile.apps.treecalc.ui.theme.TreeCalcPalette
import com.lkovari.mobile.apps.treecalc.ui.AdaptiveMetrics
import com.lkovari.mobile.apps.treecalc.ui.rememberAdaptiveMetrics

enum class KeyVisual {
    NUMBER,
    FUNCTION,
    OPERATOR,
    EQUALS,
    ACTION
}

data class KeySpec(
    val key: CalculatorKey,
    val label: String,
    val visual: KeyVisual
)

@Composable
fun CalculatorKeypad(
    onKey: (CalculatorKey) -> Unit,
    base: NumericBase,
    modifier: Modifier = Modifier,
    metrics: AdaptiveMetrics = rememberAdaptiveMetrics()
) {
    val rows = keypadRows()
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(metrics.keySpacing)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(metrics.keySpacing)
            ) {
                row.forEach { spec ->
                    KeyButton(
                        spec = spec,
                        enabled = base.allowsKey(spec.key),
                        onClick = { onKey(spec.key) },
                        metrics = metrics
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.KeyButton(
    spec: KeySpec,
    enabled: Boolean,
    onClick: () -> Unit,
    metrics: AdaptiveMetrics
) {
    val palette = LocalTreeCalcPalette.current
    val colors = keyColors(spec.visual, palette, enabled)
    val fontSize = metrics.keyLabelSize
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(metrics.keyCorner),
        color = colors.fill,
        contentColor = colors.label,
        border = BorderStroke(1.dp, colors.border),
        tonalElevation = 0.dp,
        shadowElevation = metrics.keyElevation,
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = spec.label,
                color = colors.label,
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

private data class KeyPaint(
    val fill: Color,
    val label: Color,
    val border: Color
)

private fun keyColors(
    visual: KeyVisual,
    palette: TreeCalcPalette,
    enabled: Boolean
): KeyPaint {
    if (!enabled) {
        return KeyPaint(palette.disabledKey, palette.disabledLabel, palette.disabledKeyBorder)
    }
    return when (visual) {
        KeyVisual.NUMBER -> KeyPaint(palette.numberKey, palette.keyLabel, palette.numberKeyBorder)
        KeyVisual.FUNCTION -> KeyPaint(palette.functionKey, palette.keyLabel, palette.functionKeyBorder)
        KeyVisual.OPERATOR -> KeyPaint(palette.operatorKey, palette.operatorLabel, palette.operatorKeyBorder)
        KeyVisual.EQUALS -> KeyPaint(palette.equalsKey, palette.equalsLabel, palette.equalsKeyBorder)
        KeyVisual.ACTION -> KeyPaint(palette.actionKey, palette.keyLabel, palette.actionKeyBorder)
    }
}

@Composable
private fun keypadRows(): List<List<KeySpec>> {
    return listOf(
        listOf(
            KeySpec(CalculatorKey.PI, "π", KeyVisual.FUNCTION),
            KeySpec(CalculatorKey.SQRT, "√", KeyVisual.FUNCTION),
            KeySpec(CalculatorKey.SQ, "x²", KeyVisual.FUNCTION),
            KeySpec(CalculatorKey.CUBE, "x³", KeyVisual.FUNCTION),
            KeySpec(CalculatorKey.POW, "xʸ", KeyVisual.FUNCTION)
        ),
        listOf(
            KeySpec(CalculatorKey.SIN, "sin", KeyVisual.FUNCTION),
            KeySpec(CalculatorKey.COS, "cos", KeyVisual.FUNCTION),
            KeySpec(CalculatorKey.TAN, "tan", KeyVisual.FUNCTION),
            KeySpec(CalculatorKey.LN, "ln", KeyVisual.FUNCTION),
            KeySpec(CalculatorKey.LOG, "log", KeyVisual.FUNCTION)
        ),
        listOf(
            KeySpec(CalculatorKey.EXP, "exp", KeyVisual.FUNCTION),
            KeySpec(CalculatorKey.REC, "1/x", KeyVisual.FUNCTION),
            KeySpec(CalculatorKey.FACT, "n!", KeyVisual.FUNCTION),
            KeySpec(CalculatorKey.LEFT_PAREN, "(", KeyVisual.FUNCTION),
            KeySpec(CalculatorKey.RIGHT_PAREN, ")", KeyVisual.FUNCTION)
        ),
        listOf(
            KeySpec(CalculatorKey.MEMORY_CLEAR, stringResource(R.string.key_memory_clear), KeyVisual.ACTION),
            KeySpec(CalculatorKey.MEMORY_RECALL, stringResource(R.string.key_memory_recall), KeyVisual.ACTION),
            KeySpec(CalculatorKey.MEMORY_ADD, stringResource(R.string.key_memory_add), KeyVisual.ACTION),
            KeySpec(CalculatorKey.MEMORY_SUB, stringResource(R.string.key_memory_sub), KeyVisual.ACTION),
            KeySpec(CalculatorKey.NEGATE, stringResource(R.string.key_negate), KeyVisual.ACTION)
        ),
        listOf(
            KeySpec(CalculatorKey.DIGIT_A, "A", KeyVisual.NUMBER),
            KeySpec(CalculatorKey.DIGIT_B, "B", KeyVisual.NUMBER),
            KeySpec(CalculatorKey.DIGIT_C, "C", KeyVisual.NUMBER),
            KeySpec(CalculatorKey.DIGIT_D, "D", KeyVisual.NUMBER),
            KeySpec(CalculatorKey.DIGIT_E, "E", KeyVisual.NUMBER)
        ),
        listOf(
            KeySpec(CalculatorKey.DIGIT_F, "F", KeyVisual.NUMBER),
            KeySpec(CalculatorKey.AND, stringResource(R.string.key_and), KeyVisual.OPERATOR),
            KeySpec(CalculatorKey.OR, stringResource(R.string.key_or), KeyVisual.OPERATOR),
            KeySpec(CalculatorKey.XOR, stringResource(R.string.key_xor), KeyVisual.OPERATOR),
            KeySpec(CalculatorKey.NOT, stringResource(R.string.key_not), KeyVisual.OPERATOR)
        ),
        listOf(
            KeySpec(CalculatorKey.DIGIT_7, "7", KeyVisual.NUMBER),
            KeySpec(CalculatorKey.DIGIT_8, "8", KeyVisual.NUMBER),
            KeySpec(CalculatorKey.DIGIT_9, "9", KeyVisual.NUMBER),
            KeySpec(CalculatorKey.DIV, "÷", KeyVisual.OPERATOR),
            KeySpec(CalculatorKey.LSH, stringResource(R.string.key_lsh), KeyVisual.OPERATOR)
        ),
        listOf(
            KeySpec(CalculatorKey.DIGIT_4, "4", KeyVisual.NUMBER),
            KeySpec(CalculatorKey.DIGIT_5, "5", KeyVisual.NUMBER),
            KeySpec(CalculatorKey.DIGIT_6, "6", KeyVisual.NUMBER),
            KeySpec(CalculatorKey.MUL, "×", KeyVisual.OPERATOR),
            KeySpec(CalculatorKey.MOD, stringResource(R.string.key_mod), KeyVisual.OPERATOR)
        ),
        listOf(
            KeySpec(CalculatorKey.DIGIT_1, "1", KeyVisual.NUMBER),
            KeySpec(CalculatorKey.DIGIT_2, "2", KeyVisual.NUMBER),
            KeySpec(CalculatorKey.DIGIT_3, "3", KeyVisual.NUMBER),
            KeySpec(CalculatorKey.SUB, "−", KeyVisual.OPERATOR),
            KeySpec(CalculatorKey.BACKSPACE, stringResource(R.string.key_backspace), KeyVisual.ACTION)
        ),
        listOf(
            KeySpec(CalculatorKey.DIGIT_0, "0", KeyVisual.NUMBER),
            KeySpec(CalculatorKey.DOT, ".", KeyVisual.NUMBER),
            KeySpec(CalculatorKey.ADD, "+", KeyVisual.OPERATOR),
            KeySpec(CalculatorKey.EQUALS, "=", KeyVisual.EQUALS),
            KeySpec(CalculatorKey.CLEAR, stringResource(R.string.key_clear), KeyVisual.ACTION)
        )
    )
}
