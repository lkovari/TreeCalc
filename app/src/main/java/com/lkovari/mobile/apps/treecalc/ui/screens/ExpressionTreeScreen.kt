package com.lkovari.mobile.apps.treecalc.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lkovari.mobile.apps.treecalc.R
import com.lkovari.mobile.apps.treecalc.engine.EvaluationResult
import com.lkovari.mobile.apps.treecalc.ui.AdaptiveMetrics
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
    val treeVerticalScroll = rememberScrollState()
    val treeHorizontalScroll = rememberScrollState()
    var selectedStepIndex by rememberSaveable(state.postfix) {
        mutableStateOf<Int?>(null)
    }
    val highlightedPath = remember(state.tree, selectedStepIndex) {
        val tree = state.tree
        val index = selectedStepIndex
        if (tree == null || index == null) {
            null
        } else {
            tree.pathForPostOrderIndex(index)
        }
    }
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
                    val tokens = state.postfixTokens
                    if (state.tree == null || tokens.isEmpty()) {
                        Text(
                            text = "—",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    } else {
                        PostfixTokenRow(
                            tokens = tokens,
                            selectedIndex = selectedStepIndex,
                            onSelect = { index ->
                                selectedStepIndex = if (selectedStepIndex == index) {
                                    null
                                } else {
                                    index
                                }
                            },
                            metrics = metrics
                        )
                    }
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
                                highlightedPath = highlightedPath,
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

@Composable
private fun PostfixTokenRow(
    tokens: List<String>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
    metrics: AdaptiveMetrics
) {
    val palette = LocalTreeCalcPalette.current
    val listState = rememberLazyListState()
    LaunchedEffect(selectedIndex) {
        val index = selectedIndex
        if (index != null) {
            listState.animateScrollToItemIfNotVisible(index)
        }
    }
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val maxChipWidth = maxWidth * 0.6f
            LazyRow(
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                itemsIndexed(tokens) { index, token ->
                    val selected = index == selectedIndex
                    val description = stringResource(R.string.cd_postfix_step, token)
                    Surface(
                        modifier = Modifier
                            .height(metrics.chipHeight)
                            .widthIn(max = maxChipWidth)
                            .clickable { onSelect(index) }
                            .semantics {
                                contentDescription = description
                                this.selected = selected
                            },
                        shape = RoundedCornerShape(percent = 50),
                        color = if (selected) {
                            palette.badgeFill
                        } else {
                            palette.chipIdle
                        },
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (selected) {
                                palette.operandRing
                            } else {
                                palette.chipIdleBorder
                            }
                        ),
                        shadowElevation = 0.dp,
                        tonalElevation = 0.dp
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = token,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = metrics.chipLabelSize
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

private suspend fun LazyListState.animateScrollToItemIfNotVisible(index: Int) {
    val visibleItem = layoutInfo.visibleItemsInfo.firstOrNull { item ->
        item.index == index
    }
    if (visibleItem == null) {
        animateScrollToItem(index)
        return
    }
    val viewportStart = layoutInfo.viewportStartOffset
    val viewportEnd = layoutInfo.viewportEndOffset
    val fullyVisible = visibleItem.offset >= viewportStart &&
        visibleItem.offset + visibleItem.size <= viewportEnd
    if (!fullyVisible) {
        animateScrollToItem(index)
    }
}
