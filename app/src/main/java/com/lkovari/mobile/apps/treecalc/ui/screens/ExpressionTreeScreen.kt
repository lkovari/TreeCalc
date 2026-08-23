package com.lkovari.mobile.apps.treecalc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkovari.mobile.apps.treecalc.R
import com.lkovari.mobile.apps.treecalc.engine.EvaluationResult
import com.lkovari.mobile.apps.treecalc.ui.components.ExpressionTreeView
import com.lkovari.mobile.apps.treecalc.ui.rememberAdaptiveMetrics
import com.lkovari.mobile.apps.treecalc.ui.theme.LocalTreeCalcPalette
import com.lkovari.mobile.apps.treecalc.ui.theme.pastelScreenBrush

@Composable
fun ExpressionTreeScreen(
    state: EvaluationResult,
    modifier: Modifier = Modifier
) {
    val palette = LocalTreeCalcPalette.current
    val metrics = rememberAdaptiveMetrics()
    val postfixScroll = rememberScrollState()
    val treeVerticalScroll = rememberScrollState()
    val treeHorizontalScroll = rememberScrollState()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(pastelScreenBrush(palette)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = metrics.contentMaxWidth)
                .fillMaxSize()
                .padding(horizontal = metrics.screenPadding, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.postfix_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(metrics.displayCorner),
                color = palette.displaySurface,
                shadowElevation = metrics.keyElevation,
                tonalElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(metrics.displayPadding)
                ) {
                    Text(
                        text = if (state.postfix.isEmpty()) {
                            "—"
                        } else {
                            state.postfix
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.horizontalScroll(postfixScroll)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.tree_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(metrics.displayCorner),
                color = palette.displaySurface,
                shadowElevation = metrics.keyElevation,
                tonalElevation = 0.dp
            ) {
                if (state.tree == null) {
                    ExpressionTreeView(node = null, base = state.base, angleMode = state.angleMode)
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        key(state.postfix) {
                            ExpressionTreeView(
                                node = state.tree,
                                base = state.base,
                                angleMode = state.angleMode,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .horizontalScroll(treeHorizontalScroll)
                                    .verticalScroll(treeVerticalScroll)
                                    .wrapContentWidth(
                                        align = Alignment.Start,
                                        unbounded = true
                                    )
                                    .wrapContentHeight(
                                        align = Alignment.Top,
                                        unbounded = true
                                    )
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
