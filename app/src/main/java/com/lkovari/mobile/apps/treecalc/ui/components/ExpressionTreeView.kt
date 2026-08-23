package com.lkovari.mobile.apps.treecalc.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkovari.mobile.apps.treecalc.R
import com.lkovari.mobile.apps.treecalc.engine.AngleMode
import com.lkovari.mobile.apps.treecalc.engine.ExpressionNode
import com.lkovari.mobile.apps.treecalc.engine.NumericBase
import com.lkovari.mobile.apps.treecalc.engine.OperatorKind
import com.lkovari.mobile.apps.treecalc.ui.theme.LocalTreeCalcPalette

private val GuideGutter = 18.dp
private val ExpandBoxSize = 16.dp

@Composable
fun ExpressionTreeView(
    node: ExpressionNode?,
    base: NumericBase,
    angleMode: AngleMode = AngleMode.DEGREES,
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
        key(node) {
            TreeNodeRow(
                node = node,
                base = base,
                angleMode = angleMode,
                path = "root",
                depth = 0,
                isLastSibling = true,
                ancestorContinues = emptyList(),
                modifier = modifier
            )
        }
    }
}

@Composable
private fun TreeNodeRow(
    node: ExpressionNode,
    base: NumericBase,
    angleMode: AngleMode,
    path: String,
    depth: Int,
    isLastSibling: Boolean,
    ancestorContinues: List<Boolean>,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable(path) { mutableStateOf(true) }
    val hasChildren = node.childCount() > 0
    val spec = treeGuideSpec(depth = depth, isLastSibling = isLastSibling)
    val palette = LocalTreeCalcPalette.current
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .clickable(enabled = hasChildren) { expanded = !expanded }
                .drawBehind {
                    drawTreeGuides(
                        spec = spec,
                        ancestorContinues = ancestorContinues,
                        color = palette.displayBorder
                    )
                }
                .padding(vertical = 4.dp)
        ) {
            val gutterCount = ancestorContinues.size + if (spec.drawElbow) 1 else 0
            if (gutterCount > 0) {
                Spacer(
                    modifier = Modifier
                        .width(GuideGutter * gutterCount)
                        .fillMaxHeight()
                )
            }
            if (hasChildren) {
                Box(
                    modifier = Modifier
                        .size(ExpandBoxSize)
                        .border(
                            width = 1.dp,
                            color = palette.displayBorder,
                            shape = RoundedCornerShape(2.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (expanded) {
                            Icons.Filled.Remove
                        } else {
                            Icons.Filled.Add
                        },
                        contentDescription = if (expanded) {
                            stringResource(R.string.collapse_node)
                        } else {
                            stringResource(R.string.expand_node)
                        },
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(12.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(ExpandBoxSize))
            }
            Spacer(modifier = Modifier.width(8.dp))
            val kind = node.operatorKind()
            OperatorBadge(kind = kind, isRoot = spec.isRoot)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = node.displayLabel(base, angleMode),
                style = MaterialTheme.typography.bodyLarge,
                color = if (kind == null) {
                    palette.operandLabel
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
                maxLines = 1,
                softWrap = false
            )
        }
        AnimatedVisibility(visible = expanded && hasChildren) {
            Column {
                val count = node.childCount()
                val childContinues = childAncestorContinues(
                    depth = depth,
                    ancestorContinues = ancestorContinues,
                    isLastSibling = isLastSibling
                )
                var index = 0
                while (index < count) {
                    val child = node.childAt(index)
                    if (child != null) {
                        val childPath = "$path/$index"
                        key(childPath) {
                            TreeNodeRow(
                                node = child,
                                base = base,
                                angleMode = angleMode,
                                path = childPath,
                                depth = depth + 1,
                                isLastSibling = index == count - 1,
                                ancestorContinues = childContinues
                            )
                        }
                    }
                    index += 1
                }
            }
        }
    }
}

private fun DrawScope.drawTreeGuides(
    spec: TreeGuideSpec,
    ancestorContinues: List<Boolean>,
    color: Color
) {
    val gutter = GuideGutter.toPx()
    val strokeWidth = 1.5.dp.toPx()
    val dash = PathEffect.dashPathEffect(
        floatArrayOf(4.dp.toPx(), 3.dp.toPx()),
        0f
    )
    val midY = size.height / 2f
    var index = 0
    while (index < ancestorContinues.size) {
        if (ancestorContinues[index]) {
            val x = gutter * index + gutter / 2f
            drawLine(
                color = color,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
                pathEffect = dash
            )
        }
        index += 1
    }
    if (spec.drawElbow) {
        val x = gutter * ancestorContinues.size + gutter / 2f
        val verticalEndY = if (spec.verticalThrough) {
            size.height
        } else {
            midY
        }
        drawLine(
            color = color,
            start = Offset(x, 0f),
            end = Offset(x, verticalEndY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
            pathEffect = dash
        )
        drawLine(
            color = color,
            start = Offset(x, midY),
            end = Offset(x + gutter / 2f, midY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
            pathEffect = dash
        )
    }
}

@Composable
private fun OperatorBadge(kind: OperatorKind?, isRoot: Boolean) {
    val palette = LocalTreeCalcPalette.current
    val fill = if (isRoot) {
        palette.rootBadgeFill
    } else {
        palette.badgeFill
    }
    val iconRes = operatorIconRes(kind)
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(fill, CircleShape)
            .border(2.5.dp, palette.operandRing, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (kind == null) {
            return@Box
        }
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
