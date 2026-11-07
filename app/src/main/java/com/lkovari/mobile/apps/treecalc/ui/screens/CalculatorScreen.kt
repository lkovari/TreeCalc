package com.lkovari.mobile.apps.treecalc.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lkovari.mobile.apps.treecalc.R
import com.lkovari.mobile.apps.treecalc.engine.AngleMode
import com.lkovari.mobile.apps.treecalc.engine.CalculatorKey
import com.lkovari.mobile.apps.treecalc.engine.ErrorKind
import com.lkovari.mobile.apps.treecalc.engine.EvaluationResult
import com.lkovari.mobile.apps.treecalc.engine.NumericBase
import com.lkovari.mobile.apps.treecalc.ui.components.CalculatorKeypad
import com.lkovari.mobile.apps.treecalc.ui.rememberAdaptiveMetrics
import com.lkovari.mobile.apps.treecalc.ui.theme.LocalTreeCalcPalette
import com.lkovari.mobile.apps.treecalc.ui.theme.pastelScreenBrush

@Composable
fun CalculatorScreen(
    state: EvaluationResult,
    onKey: (CalculatorKey) -> Unit,
    onBase: (NumericBase) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalTreeCalcPalette.current
    val metrics = rememberAdaptiveMetrics()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(pastelScreenBrush(palette)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = metrics.contentMaxWidth)
                .fillMaxHeight()
                .padding(horizontal = metrics.screenPadding, vertical = 4.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(metrics.displayCorner),
                color = palette.displaySurface,
                border = BorderStroke(1.dp, palette.displayBorder),
                shadowElevation = metrics.keyElevation,
                tonalElevation = 0.dp
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = metrics.displayPadding, vertical = if (metrics.compact) 8.dp else 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = baseDisplayLabel(state.base),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = angleDisplayLabel(state.angleMode),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (state.memorySet) {
                        Text(
                            text = stringResource(R.string.memory_indicator),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = state.display,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = resultTextSize(state.display.length, metrics.resultSize)
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("calc-result")
                )
                Spacer(modifier = Modifier.height(4.dp))
                ExpressionLine(text = state.expression.ifEmpty { " " })
                val errorText = errorMessage(state.errorKind)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = errorText ?: " ",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (errorText == null) Color.Transparent else MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calc-error")
                )
            }
            }
            Spacer(modifier = Modifier.height(if (metrics.compact) 6.dp else 8.dp))
            CalculatorKeypad(
                onKey = onKey,
                onBase = onBase,
                base = state.base,
                angleMode = state.angleMode,
                metrics = metrics,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun ExpressionLine(text: String) {
    val scroll = rememberScrollState()
    LaunchedEffect(text, scroll.maxValue) {
        scroll.scrollTo(scroll.maxValue)
    }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .testTag("calc-expression")
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier
                .horizontalScroll(scroll)
                .widthIn(min = maxWidth)
        )
    }
}

private fun resultTextSize(length: Int, base: TextUnit): TextUnit {
    val scale = when {
        length <= 9 -> 1f
        length <= 12 -> 0.75f
        else -> 0.56f
    }
    return (base.value * scale).sp
}

@Composable
private fun baseDisplayLabel(base: NumericBase): String {
    return when (base) {
        NumericBase.BINARY -> stringResource(R.string.base_display_binary)
        NumericBase.OCTAL -> stringResource(R.string.base_display_octal)
        NumericBase.DECIMAL -> stringResource(R.string.base_display_decimal)
        NumericBase.HEXADECIMAL -> stringResource(R.string.base_display_hexadecimal)
    }
}

@Composable
private fun angleDisplayLabel(mode: AngleMode): String {
    return when (mode) {
        AngleMode.DEGREES -> stringResource(R.string.angle_degrees)
        AngleMode.RADIANS -> stringResource(R.string.angle_radians)
    }
}

@Composable
private fun errorMessage(kind: ErrorKind?): String? {
    return when (kind) {
        ErrorKind.EMPTY_EXPRESSION -> stringResource(R.string.error_empty)
        ErrorKind.UNBALANCED_PARENTHESES -> stringResource(R.string.error_parentheses)
        ErrorKind.DIVISION_BY_ZERO -> stringResource(R.string.error_division)
        ErrorKind.DOMAIN -> stringResource(R.string.error_domain)
        ErrorKind.BITWISE_NON_INTEGER -> stringResource(R.string.error_bitwise)
        ErrorKind.INVALID_DIGIT -> stringResource(R.string.error_digit)
        ErrorKind.MALFORMED_EXPRESSION -> stringResource(R.string.error_malformed)
        ErrorKind.INVALID_FACTORIAL -> stringResource(R.string.error_factorial)
        ErrorKind.UNDEFINED -> stringResource(R.string.error_undefined)
        null -> null
    }
}
