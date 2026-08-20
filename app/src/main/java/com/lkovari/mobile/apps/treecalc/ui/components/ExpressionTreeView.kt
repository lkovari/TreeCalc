package com.lkovari.mobile.apps.treecalc.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkovari.mobile.apps.treecalc.R
import com.lkovari.mobile.apps.treecalc.engine.ExpressionNode
import com.lkovari.mobile.apps.treecalc.engine.NumericBase
import com.lkovari.mobile.apps.treecalc.engine.OperatorKind
import com.lkovari.mobile.apps.treecalc.ui.theme.LocalTreeCalcPalette

@Composable
fun ExpressionTreeView(
    node: ExpressionNode?,
    base: NumericBase,
    modifier: Modifier = Modifier
) {
    if (node == null) {
        Text(
            text = stringResource(R.string.tree_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(8.dp)
        )
    } else {
        TreeNodeRow(
            node = node,
            base = base,
            modifier = modifier,
            initiallyExpanded = true
        )
    }
}

@Composable
private fun TreeNodeRow(
    node: ExpressionNode,
    base: NumericBase,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    val hasChildren = node.childCount() > 0
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = hasChildren) { expanded = !expanded }
                .padding(vertical = 6.dp)
        ) {
            if (hasChildren) {
                Icon(
                    imageVector = if (expanded) {
                        Icons.Filled.KeyboardArrowDown
                    } else {
                        Icons.AutoMirrored.Filled.KeyboardArrowRight
                    },
                    contentDescription = if (expanded) {
                        stringResource(R.string.collapse_node)
                    } else {
                        stringResource(R.string.expand_node)
                    },
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(22.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            val kind = node.operatorKind()
            val palette = LocalTreeCalcPalette.current
            OperatorBadge(kind = kind)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = node.displayLabel(base),
                style = MaterialTheme.typography.bodyLarge,
                color = if (kind == null) {
                    palette.operandLabel
                } else {
                    MaterialTheme.colorScheme.onBackground
                }
            )
        }
        AnimatedVisibility(visible = expanded && hasChildren) {
            Column(modifier = Modifier.padding(start = 22.dp)) {
                val count = node.childCount()
                var index = 0
                while (index < count) {
                    val child = node.childAt(index)
                    if (child != null) {
                        TreeNodeRow(
                            node = child,
                            base = base,
                            initiallyExpanded = true
                        )
                    }
                    index += 1
                }
            }
        }
    }
}

@Composable
private fun OperatorBadge(kind: OperatorKind?) {
    val palette = LocalTreeCalcPalette.current
    if (kind == null) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(palette.operandFill)
                .border(2.5.dp, palette.operandRing, CircleShape)
        )
        return
    }
    val iconRes = operatorIconRes(kind)
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(palette.badgeFill),
        contentAlignment = Alignment.Center
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = kind.displaySymbol,
                tint = palette.badgeGlyph,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = kind.displaySymbol,
                style = MaterialTheme.typography.labelMedium,
                color = palette.badgeGlyph
            )
        }
    }
}

@DrawableRes
private fun operatorIconRes(kind: OperatorKind?): Int? {
    return when (kind) {
        OperatorKind.ADD -> R.drawable.ic_op_add
        OperatorKind.SUB, OperatorKind.NEG -> R.drawable.ic_op_sub
        OperatorKind.MUL -> R.drawable.ic_op_mul
        OperatorKind.DIV -> R.drawable.ic_op_div
        OperatorKind.POW -> R.drawable.ic_op_pow
        OperatorKind.SQRT -> R.drawable.ic_op_sqrt
        else -> null
    }
}
