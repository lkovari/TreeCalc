package com.lkovari.mobile.apps.treecalc.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.lkovari.mobile.apps.treecalc.R
import com.lkovari.mobile.apps.treecalc.engine.CalculatorKey
import com.lkovari.mobile.apps.treecalc.engine.NumericBase
import com.lkovari.mobile.apps.treecalc.ui.AdaptiveMetrics
import com.lkovari.mobile.apps.treecalc.ui.rememberAdaptiveMetrics
import com.lkovari.mobile.apps.treecalc.ui.theme.LocalTreeCalcPalette
import com.lkovari.mobile.apps.treecalc.ui.theme.TreeCalcPalette

private const val COLUMN_COUNT = 5

enum class KeyVisual {
    NUMBER,
    FUNCTION,
    OPERATOR,
    LOGIC,
    EQUALS,
    ACTION
}

data class KeySpec(
    val key: CalculatorKey,
    val label: String,
    val visual: KeyVisual
)

private data class KeypadRow(
    val slots: List<KeySpec?>
)

@Composable
fun CalculatorKeypad(
    onKey: (CalculatorKey) -> Unit,
    onBase: (NumericBase) -> Unit,
    base: NumericBase,
    modifier: Modifier = Modifier,
    metrics: AdaptiveMetrics = rememberAdaptiveMetrics()
) {
    val compactRows = compactKeypadRows()
    val squareRows = squareKeypadRows()
    val compactRowCount = 1 + compactRows.size
    val squareRowCount = squareRows.size

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val spacing = metrics.keySpacing
        val compactBlockHeight =
            metrics.compactKeyHeight * compactRowCount + spacing * (compactRowCount - 1)
        val squareGaps = spacing * (squareRowCount - 1)
        val widthLimitedKey = (maxWidth - spacing * (COLUMN_COUNT - 1)) / COLUMN_COUNT
        val heightLimitedKey =
            ((maxHeight - compactBlockHeight - spacing - squareGaps) / squareRowCount)
                .coerceAtLeast(0.dp)
        val squareKeySize = minOf(widthLimitedKey, heightLimitedKey)
        val squareGridWidth = squareKeySize * COLUMN_COUNT + spacing * (COLUMN_COUNT - 1)

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(metrics.compactKeyHeight),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                BaseKeyButton(
                    numericBase = NumericBase.BINARY,
                    label = stringResource(R.string.base_binary),
                    selected = base,
                    onBase = onBase,
                    metrics = metrics
                )
                BaseKeyButton(
                    numericBase = NumericBase.OCTAL,
                    label = stringResource(R.string.base_octal),
                    selected = base,
                    onBase = onBase,
                    metrics = metrics
                )
                BaseKeyButton(
                    numericBase = NumericBase.DECIMAL,
                    label = stringResource(R.string.base_decimal),
                    selected = base,
                    onBase = onBase,
                    metrics = metrics
                )
                BaseKeyButton(
                    numericBase = NumericBase.HEXADECIMAL,
                    label = stringResource(R.string.base_hexadecimal),
                    selected = base,
                    onBase = onBase,
                    metrics = metrics
                )
            }
            compactRows.forEach { row ->
                CompactKeyRow(
                    row = row,
                    base = base,
                    onKey = onKey,
                    metrics = metrics,
                    rowHeight = metrics.compactKeyHeight,
                    spacing = spacing
                )
            }
            if (squareKeySize > 0.dp) {
                squareRows.forEach { row ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        SquareKeyRow(
                            row = row,
                            base = base,
                            onKey = onKey,
                            metrics = metrics,
                            keySize = squareKeySize,
                            spacing = spacing,
                            gridWidth = squareGridWidth
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactKeyRow(
    row: KeypadRow,
    base: NumericBase,
    onKey: (CalculatorKey) -> Unit,
    metrics: AdaptiveMetrics,
    rowHeight: Dp,
    spacing: Dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        row.slots.forEach { spec ->
            if (spec == null) {
                RowSpacer()
            } else {
                KeyButton(
                    spec = spec,
                    enabled = base.allowsKey(spec.key),
                    onClick = { onKey(spec.key) },
                    metrics = metrics,
                    labelSize = metrics.chipLabelSize,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun SquareKeyRow(
    row: KeypadRow,
    base: NumericBase,
    onKey: (CalculatorKey) -> Unit,
    metrics: AdaptiveMetrics,
    keySize: Dp,
    spacing: Dp,
    gridWidth: Dp
) {
    Row(
        modifier = Modifier
            .width(gridWidth)
            .height(keySize),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        row.slots.forEach { spec ->
            if (spec == null) {
                Spacer(modifier = Modifier.size(keySize))
            } else {
                KeyButton(
                    spec = spec,
                    enabled = base.allowsKey(spec.key),
                    onClick = { onKey(spec.key) },
                    metrics = metrics,
                    modifier = Modifier.size(keySize)
                )
            }
        }
    }
}

@Composable
private fun RowScope.RowSpacer() {
    Spacer(modifier = Modifier.weight(1f))
}

@Composable
private fun RowScope.BaseKeyButton(
    numericBase: NumericBase,
    label: String,
    selected: NumericBase,
    onBase: (NumericBase) -> Unit,
    metrics: AdaptiveMetrics
) {
    val palette = LocalTreeCalcPalette.current
    val selectedNow = numericBase == selected
    val colors = if (selectedNow) {
        KeyPaint(palette.operatorKey, palette.operatorLabel, palette.operatorKeyBorder)
    } else {
        KeyPaint(palette.chipIdle, MaterialTheme.colorScheme.onSurface, palette.chipIdleBorder)
    }
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        Surface(
            onClick = { onBase(numericBase) },
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
                    text = label,
                    color = colors.label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = metrics.chipLabelSize,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun KeyButton(
    spec: KeySpec,
    enabled: Boolean,
    onClick: () -> Unit,
    metrics: AdaptiveMetrics,
    modifier: Modifier,
    labelSize: TextUnit = metrics.keyLabelSize
) {
    val palette = LocalTreeCalcPalette.current
    val colors = keyColors(spec.visual, palette, enabled)
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        Surface(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(metrics.keyCorner),
            color = colors.fill,
            contentColor = colors.label,
            border = BorderStroke(1.dp, colors.border),
            tonalElevation = 0.dp,
            shadowElevation = metrics.keyElevation,
            modifier = modifier
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = spec.label,
                    color = colors.label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = labelSize,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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
        KeyVisual.LOGIC -> KeyPaint(palette.logicKey, palette.keyLabel, palette.logicKeyBorder)
        KeyVisual.EQUALS -> KeyPaint(palette.equalsKey, palette.equalsLabel, palette.equalsKeyBorder)
        KeyVisual.ACTION -> KeyPaint(palette.actionKey, palette.keyLabel, palette.actionKeyBorder)
    }
}

@Composable
private fun compactKeypadRows(): List<KeypadRow> {
    return listOf(
        KeypadRow(
            listOf(
                KeySpec(CalculatorKey.PI, "π", KeyVisual.FUNCTION),
                KeySpec(CalculatorKey.SQRT, "√", KeyVisual.FUNCTION),
                KeySpec(CalculatorKey.SQ, "x²", KeyVisual.FUNCTION),
                KeySpec(CalculatorKey.CUBE, "x³", KeyVisual.FUNCTION),
                KeySpec(CalculatorKey.POW, "xʸ", KeyVisual.FUNCTION)
            )
        ),
        KeypadRow(
            listOf(
                KeySpec(CalculatorKey.SIN, "sin", KeyVisual.FUNCTION),
                KeySpec(CalculatorKey.COS, "cos", KeyVisual.FUNCTION),
                KeySpec(CalculatorKey.TAN, "tan", KeyVisual.FUNCTION),
                KeySpec(CalculatorKey.LN, "ln", KeyVisual.FUNCTION),
                KeySpec(CalculatorKey.LOG, "log", KeyVisual.FUNCTION)
            )
        ),
        KeypadRow(
            listOf(
                KeySpec(CalculatorKey.EXP, "exp", KeyVisual.FUNCTION),
                KeySpec(CalculatorKey.REC, "1/x", KeyVisual.FUNCTION),
                KeySpec(CalculatorKey.FACT, "n!", KeyVisual.FUNCTION),
                KeySpec(CalculatorKey.LEFT_PAREN, "(", KeyVisual.FUNCTION),
                KeySpec(CalculatorKey.RIGHT_PAREN, ")", KeyVisual.FUNCTION)
            )
        ),
        KeypadRow(
            listOf(
                KeySpec(CalculatorKey.MEMORY_CLEAR, stringResource(R.string.key_memory_clear), KeyVisual.ACTION),
                KeySpec(CalculatorKey.MEMORY_RECALL, stringResource(R.string.key_memory_recall), KeyVisual.ACTION),
                KeySpec(CalculatorKey.MEMORY_ADD, stringResource(R.string.key_memory_add), KeyVisual.ACTION),
                KeySpec(CalculatorKey.MEMORY_SUB, stringResource(R.string.key_memory_sub), KeyVisual.ACTION),
                KeySpec(CalculatorKey.TEST, stringResource(R.string.key_test), KeyVisual.ACTION)
            )
        ),
        KeypadRow(
            listOf(
                KeySpec(CalculatorKey.AND, stringResource(R.string.key_and), KeyVisual.LOGIC),
                KeySpec(CalculatorKey.OR, stringResource(R.string.key_or), KeyVisual.LOGIC),
                KeySpec(CalculatorKey.XOR, stringResource(R.string.key_xor), KeyVisual.LOGIC),
                KeySpec(CalculatorKey.NOT, stringResource(R.string.key_not), KeyVisual.LOGIC),
                KeySpec(CalculatorKey.LSH, stringResource(R.string.key_lsh), KeyVisual.LOGIC)
            )
        ),
        KeypadRow(
            listOf(
                KeySpec(CalculatorKey.MOD, stringResource(R.string.key_mod), KeyVisual.LOGIC),
                null,
                null,
                null,
                null
            )
        )
    )
}

@Composable
private fun squareKeypadRows(): List<KeypadRow> {
    return listOf(
        KeypadRow(
            listOf(
                KeySpec(CalculatorKey.DIGIT_F, "F", KeyVisual.NUMBER),
                KeySpec(CalculatorKey.DIGIT_E, "E", KeyVisual.NUMBER),
                KeySpec(CalculatorKey.DIGIT_D, "D", KeyVisual.NUMBER),
                KeySpec(CalculatorKey.DIGIT_C, "C", KeyVisual.NUMBER),
                KeySpec(CalculatorKey.DIGIT_B, "B", KeyVisual.NUMBER)
            )
        ),
        KeypadRow(
            listOf(
                KeySpec(CalculatorKey.DIGIT_A, "A", KeyVisual.NUMBER),
                KeySpec(CalculatorKey.DIGIT_9, "9", KeyVisual.NUMBER),
                KeySpec(CalculatorKey.DIGIT_8, "8", KeyVisual.NUMBER),
                KeySpec(CalculatorKey.DIGIT_7, "7", KeyVisual.NUMBER),
                KeySpec(CalculatorKey.CLEAR, stringResource(R.string.key_clear), KeyVisual.ACTION)
            )
        ),
        KeypadRow(
            listOf(
                KeySpec(CalculatorKey.DIGIT_6, "6", KeyVisual.NUMBER),
                KeySpec(CalculatorKey.DIGIT_5, "5", KeyVisual.NUMBER),
                KeySpec(CalculatorKey.DIGIT_4, "4", KeyVisual.NUMBER),
                KeySpec(CalculatorKey.DIGIT_3, "3", KeyVisual.NUMBER),
                KeySpec(CalculatorKey.BACKSPACE, stringResource(R.string.key_backspace), KeyVisual.ACTION)
            )
        ),
        KeypadRow(
            listOf(
                KeySpec(CalculatorKey.DIGIT_2, "2", KeyVisual.NUMBER),
                KeySpec(CalculatorKey.DIGIT_1, "1", KeyVisual.NUMBER),
                KeySpec(CalculatorKey.DIGIT_0, "0", KeyVisual.NUMBER),
                KeySpec(CalculatorKey.DOT, ".", KeyVisual.NUMBER),
                KeySpec(CalculatorKey.NEGATE, stringResource(R.string.key_negate), KeyVisual.ACTION)
            )
        ),
        KeypadRow(
            listOf(
                KeySpec(CalculatorKey.ADD, "+", KeyVisual.OPERATOR),
                KeySpec(CalculatorKey.SUB, "−", KeyVisual.OPERATOR),
                KeySpec(CalculatorKey.MUL, "×", KeyVisual.OPERATOR),
                KeySpec(CalculatorKey.DIV, "÷", KeyVisual.OPERATOR),
                KeySpec(CalculatorKey.EQUALS, "=", KeyVisual.EQUALS)
            )
        )
    )
}
