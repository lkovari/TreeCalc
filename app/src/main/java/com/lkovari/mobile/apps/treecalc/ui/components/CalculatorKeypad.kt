package com.lkovari.mobile.apps.treecalc.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.lkovari.mobile.apps.treecalc.R
import com.lkovari.mobile.apps.treecalc.engine.AngleMode
import com.lkovari.mobile.apps.treecalc.engine.CalculatorKey
import com.lkovari.mobile.apps.treecalc.engine.NumericBase
import com.lkovari.mobile.apps.treecalc.ui.AdaptiveMetrics
import com.lkovari.mobile.apps.treecalc.ui.rememberAdaptiveMetrics

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
    angleMode: AngleMode = AngleMode.DEGREES,
    modifier: Modifier = Modifier,
    metrics: AdaptiveMetrics = rememberAdaptiveMetrics()
) {
    val compactRows = compactKeypadRows(angleMode)
    val squareRows = squareKeypadRows()
    val compactRowCount = 1 + compactRows.size
    val squareRowCount = squareRows.size

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val spacing = metrics.keySpacing
        val columnWidth = (maxWidth - spacing * (COLUMN_COUNT - 1)) / COLUMN_COUNT
        val preferredSquare = columnWidth.coerceAtMost(92.dp)
        val gapCount = compactRowCount + squareRowCount - 1
        val wanted = metrics.compactKeyHeight * compactRowCount + preferredSquare * squareRowCount
        val available = (maxHeight - spacing * gapCount).coerceAtLeast(0.dp)
        val scale = if (wanted > available && wanted > 0.dp) {
            available / wanted
        } else {
            1f
        }
        val compactHeight = (metrics.compactKeyHeight * scale).coerceAtLeast(1.dp)
        val squareHeight = (preferredSquare * scale).coerceAtLeast(1.dp)

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(compactHeight),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                BaseKeyButton(
                    numericBase = NumericBase.BINARY,
                    label = stringResource(R.string.base_binary),
                    selected = base == NumericBase.BINARY,
                    onBase = onBase,
                    metrics = metrics,
                    modifier = Modifier.weight(1f).height(compactHeight)
                )
                BaseKeyButton(
                    numericBase = NumericBase.OCTAL,
                    label = stringResource(R.string.base_octal),
                    selected = base == NumericBase.OCTAL,
                    onBase = onBase,
                    metrics = metrics,
                    modifier = Modifier.weight(1f).height(compactHeight)
                )
                BaseKeyButton(
                    numericBase = NumericBase.DECIMAL,
                    label = stringResource(R.string.base_decimal),
                    selected = base == NumericBase.DECIMAL,
                    onBase = onBase,
                    metrics = metrics,
                    modifier = Modifier.weight(1f).height(compactHeight)
                )
                BaseKeyButton(
                    numericBase = NumericBase.HEXADECIMAL,
                    label = stringResource(R.string.base_hexadecimal),
                    selected = base == NumericBase.HEXADECIMAL,
                    onBase = onBase,
                    metrics = metrics,
                    modifier = Modifier.weight(1f).height(compactHeight)
                )
            }
            compactRows.forEach { row ->
                KeyRow(
                    row = row,
                    base = base,
                    onKey = onKey,
                    keyWidth = columnWidth,
                    rowHeight = compactHeight,
                    spacing = spacing,
                    labelSize = metrics.chipLabelSize
                )
            }
            squareRows.forEach { row ->
                KeyRow(
                    row = row,
                    base = base,
                    onKey = onKey,
                    keyWidth = columnWidth,
                    rowHeight = squareHeight,
                    spacing = spacing,
                    labelSize = metrics.keyLabelSize
                )
            }
        }
    }
}

@Composable
private fun KeyRow(
    row: KeypadRow,
    base: NumericBase,
    onKey: (CalculatorKey) -> Unit,
    keyWidth: Dp,
    rowHeight: Dp,
    spacing: Dp,
    labelSize: TextUnit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        row.slots.forEach { spec ->
            if (spec == null) {
                Spacer(modifier = Modifier.width(keyWidth).height(rowHeight))
            } else {
                GlossKey(
                    label = spec.label,
                    visual = spec.visual,
                    enabled = base.allowsKey(spec.key),
                    selected = false,
                    onClick = { onKey(spec.key) },
                    modifier = Modifier.width(keyWidth).height(rowHeight),
                    fontSize = labelSize,
                    testTag = "calc-key-${spec.key.name}"
                )
            }
        }
    }
}

@Composable
private fun BaseKeyButton(
    numericBase: NumericBase,
    label: String,
    selected: Boolean,
    onBase: (NumericBase) -> Unit,
    metrics: AdaptiveMetrics,
    modifier: Modifier
) {
    GlossKey(
        label = label,
        visual = KeyVisual.LOGIC,
        enabled = true,
        selected = selected,
        onClick = { onBase(numericBase) },
        modifier = modifier,
        fontSize = metrics.chipLabelSize,
        testTag = "calc-base-${numericBase.name}"
    )
}

@Composable
private fun compactKeypadRows(angleMode: AngleMode): List<KeypadRow> {
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
                KeySpec(CalculatorKey.MEMORY_CLEAR, stringResource(R.string.key_memory_clear), KeyVisual.FUNCTION),
                KeySpec(CalculatorKey.MEMORY_RECALL, stringResource(R.string.key_memory_recall), KeyVisual.FUNCTION),
                KeySpec(CalculatorKey.MEMORY_ADD, stringResource(R.string.key_memory_add), KeyVisual.FUNCTION),
                KeySpec(CalculatorKey.MEMORY_SUB, stringResource(R.string.key_memory_sub), KeyVisual.FUNCTION),
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
                KeySpec(
                    CalculatorKey.ANGLE_MODE,
                    if (angleMode == AngleMode.DEGREES) {
                        stringResource(R.string.key_degrees)
                    } else {
                        stringResource(R.string.key_radians)
                    },
                    KeyVisual.FUNCTION
                ),
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
