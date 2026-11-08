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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lkovari.mobile.apps.treecalc.R
import android.provider.Settings
import com.lkovari.mobile.apps.treecalc.engine.EvaluationResult
import com.lkovari.mobile.apps.treecalc.engine.EvaluationStep
import com.lkovari.mobile.apps.treecalc.engine.PostfixPlayback
import com.lkovari.mobile.apps.treecalc.engine.StackAction
import com.lkovari.mobile.apps.treecalc.engine.evaluationSteps
import com.lkovari.mobile.apps.treecalc.ui.AdaptiveMetrics
import com.lkovari.mobile.apps.treecalc.ui.components.ExpressionTreeView
import com.lkovari.mobile.apps.treecalc.ui.components.GlossKey
import com.lkovari.mobile.apps.treecalc.ui.components.KeyVisual
import kotlinx.coroutines.delay
import com.lkovari.mobile.apps.treecalc.ui.rememberAdaptiveMetrics
import com.lkovari.mobile.apps.treecalc.ui.theme.LocalTreeCalcPalette
import com.lkovari.mobile.apps.treecalc.ui.theme.pastelScreenBrush

private const val PlaybackControlScale = 0.75f
private val PlaybackButtonHeight = 24.dp
private val PlaybackButtonWidth = 72.dp

@Composable
fun ExpressionTreeScreen(
    state: EvaluationResult,
    modifier: Modifier = Modifier,
    stepByStep: Boolean? = null
) {
    val palette = LocalTreeCalcPalette.current
    val metrics = rememberAdaptiveMetrics()
    val treeVerticalScroll = rememberScrollState()
    val treeHorizontalScroll = rememberScrollState()
    var playbackIndex by rememberSaveable(state.postfix) { mutableIntStateOf(-1) }
    var playbackRunning by rememberSaveable(state.postfix) { mutableStateOf(false) }
    var userStep by rememberSaveable { mutableStateOf(false) }
    val animationsDisabled = rememberAnimationsDisabled()
    val stepped = stepByStep ?: (userStep || animationsDisabled)
    val switchEnabled = stepByStep == null && !animationsDisabled
    val selectedStepIndex = playbackIndex.takeIf { index -> index >= 0 }
    val tokenCount = state.postfixTokens.size
    val steps = remember(state.tree, state.base, state.angleMode) {
        val tree = state.tree
        if (tree == null) {
            emptyList()
        } else {
            tree.evaluationSteps(state.base, state.angleMode)
        }
    }
    fun applyPlayback(next: PostfixPlayback) {
        playbackIndex = next.index ?: -1
        playbackRunning = next.running
    }
    LaunchedEffect(playbackRunning, playbackIndex, tokenCount, stepped) {
        if (!playbackRunning || stepped) {
            return@LaunchedEffect
        }
        delay(700)
        applyPlayback(PostfixPlayback(selectedStepIndex, true).tick(tokenCount))
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
    val onLastStep = stepped &&
        selectedStepIndex != null &&
        selectedStepIndex == tokenCount - 1
    val playbackEnabled = tokenCount > 0 && !onLastStep
    val playbackLabel = when {
        !stepped && playbackRunning -> stringResource(R.string.playback_pause)
        stepped && selectedStepIndex != null -> stringResource(R.string.playback_next)
        else -> stringResource(R.string.playback_play)
    }
    val showStop = stepped && selectedStepIndex != null
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
            val expression = state.expression
            if (expression.isNotEmpty()) {
                val highlight = selectedStepIndex?.let { index ->
                    state.expressionSpans.getOrNull(index)
                }
                val highlightedText = highlight?.let { range ->
                    if (range.first >= 0 && range.last < expression.length && range.first <= range.last) {
                        expression.substring(range.first, range.last + 1)
                    } else {
                        null
                    }
                }
                val highlightDescription = highlightedText?.let { slice ->
                    stringResource(R.string.cd_expression_highlight, slice)
                }
                val styledExpression = buildAnnotatedString {
                    if (highlight == null || highlightedText == null) {
                        append(expression)
                    } else {
                        append(expression.substring(0, highlight.first))
                        withStyle(
                            SpanStyle(
                                background = palette.badgeFill,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        ) {
                            append(highlightedText)
                        }
                        if (highlight.last + 1 < expression.length) {
                            append(expression.substring(highlight.last + 1))
                        }
                    }
                }
                Text(
                    text = styledExpression,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tree.expression")
                        .semantics {
                            if (highlightDescription != null) {
                                contentDescription = highlightDescription
                            }
                        },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    softWrap = true
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
            Text(
                text = stringResource(R.string.postfix_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    CompositionLocalProvider(
                        LocalMinimumInteractiveComponentSize provides Dp.Unspecified
                    ) {
                        Switch(
                            checked = stepped,
                            onCheckedChange = { checked ->
                                userStep = checked
                                if (checked) {
                                    applyPlayback(PostfixPlayback(selectedStepIndex, false))
                                }
                            },
                            enabled = switchEnabled,
                            modifier = Modifier
                                .scaledTo(PlaybackControlScale)
                                .testTag("tree.step")
                        )
                    }
                    Text(
                        text = stringResource(R.string.step_by_step),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (showStop) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        GlossKey(
                            label = stringResource(R.string.playback_stop),
                            visual = KeyVisual.ACTION,
                            enabled = true,
                            selected = false,
                            onClick = {
                                applyPlayback(
                                    PostfixPlayback(selectedStepIndex, playbackRunning).stop()
                                )
                            },
                            modifier = Modifier
                                .height(PlaybackButtonHeight)
                                .width(PlaybackButtonWidth),
                            fontSize = 11.sp,
                            testTag = "tree.playback.stop"
                        )
                    }
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    GlossKey(
                        label = playbackLabel,
                        visual = KeyVisual.ACTION,
                        enabled = playbackEnabled,
                        selected = false,
                        onClick = {
                            val playback = PostfixPlayback(selectedStepIndex, playbackRunning)
                            val next = if (stepped) {
                                playback.step(tokenCount)
                            } else {
                                playback.toggle(tokenCount, false)
                            }
                            applyPlayback(next)
                        },
                        modifier = Modifier
                            .height(PlaybackButtonHeight)
                            .width(PlaybackButtonWidth),
                        fontSize = 11.sp,
                        testTag = "tree.playback"
                    )
                }
            }
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
                                applyPlayback(PostfixPlayback(selectedStepIndex, playbackRunning).select(index))
                            },
                            metrics = metrics
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.trace_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 108.dp),
                shape = RoundedCornerShape(10.dp),
                color = palette.displaySurface,
                shadowElevation = metrics.keyElevation,
                tonalElevation = 0.dp
            ) {
                if (steps.isEmpty()) {
                    Text(
                        text = "—",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                } else {
                    OperationTrace(
                        steps = steps,
                        selectedIndex = selectedStepIndex,
                        onSelect = { index ->
                            applyPlayback(
                                PostfixPlayback(selectedStepIndex, playbackRunning).select(index)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.tree_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
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
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun OperationTrace(
    steps: List<EvaluationStep>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        steps.forEachIndexed { index, step ->
            val sentence = step.action.sentence()
            OperationRow(
                sentence = sentence,
                selected = index == selectedIndex,
                onClick = { onSelect(index) }
            )
        }
    }
}

@Composable
private fun OperationRow(
    sentence: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val palette = LocalTreeCalcPalette.current
    val description = stringResource(R.string.cd_trace_step, sentence)
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    LaunchedEffect(selected) {
        if (selected) {
            withFrameNanos { }
            bringIntoViewRequester.bringIntoView()
        }
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .clickable { onClick() }
            .semantics {
                contentDescription = description
                this.selected = selected
            },
        shape = RoundedCornerShape(8.dp),
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
        Text(
            text = sentence,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun StackAction.sentence(): String {
    return when (this) {
        is StackAction.PushValue -> stringResource(R.string.trace_push, raw)
        is StackAction.ApplyUnary -> stringResource(
            R.string.trace_unary,
            wrappedOperand(operand),
            operator.displaySymbol,
            result
        )
        is StackAction.ApplyBinary -> stringResource(
            R.string.trace_binary,
            wrappedOperand(left),
            operator.displaySymbol,
            wrappedOperand(right),
            result
        )
    }
}

private fun Modifier.scaledTo(scale: Float): Modifier {
    return layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val width = (placeable.width * scale).roundToInt()
        val height = (placeable.height * scale).roundToInt()
        layout(width, height) {
            placeable.placeWithLayer(0, 0) {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0f)
            }
        }
    }
}

private fun wrappedOperand(label: String): String {
    if (label.any { character -> character == ' ' || character == '=' }) {
        return "($label)"
    }
    return label
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

@Composable
private fun rememberAnimationsDisabled(): Boolean {
    val context = LocalContext.current
    return remember {
        val scale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        )
        scale == 0f
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
