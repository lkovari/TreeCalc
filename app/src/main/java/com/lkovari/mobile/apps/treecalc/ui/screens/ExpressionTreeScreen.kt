package com.lkovari.mobile.apps.treecalc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lkovari.mobile.apps.treecalc.R
import com.lkovari.mobile.apps.treecalc.engine.EvaluationResult
import com.lkovari.mobile.apps.treecalc.ui.components.ExpressionTreeView
import com.lkovari.mobile.apps.treecalc.ui.rememberAdaptiveMetrics
import com.lkovari.mobile.apps.treecalc.ui.theme.LocalTreeCalcPalette

@Composable
fun ExpressionTreeScreen(
    state: EvaluationResult,
    modifier: Modifier = Modifier
) {
    val palette = LocalTreeCalcPalette.current
    val metrics = rememberAdaptiveMetrics()
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = metrics.contentMaxWidth)
                .fillMaxSize()
                .padding(horizontal = metrics.screenPadding, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.postfix_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (state.postfix.isEmpty()) {
                    "—"
                } else {
                    state.postfix
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.displaySurface)
                    .padding(metrics.displayPadding)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.tree_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.displaySurface)
                    .padding(12.dp)
            ) {
                ExpressionTreeView(node = state.tree, base = state.base)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
