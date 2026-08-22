package com.lkovari.mobile.apps.treecalc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lkovari.mobile.apps.treecalc.R
import com.lkovari.mobile.apps.treecalc.settings.ThemeMode
import com.lkovari.mobile.apps.treecalc.ui.screens.AboutScreen
import com.lkovari.mobile.apps.treecalc.ui.screens.CalculatorScreen
import com.lkovari.mobile.apps.treecalc.ui.screens.ExpressionTreeScreen
import com.lkovari.mobile.apps.treecalc.ui.screens.HelpScreen
import com.lkovari.mobile.apps.treecalc.ui.theme.LocalTreeCalcPalette
import com.lkovari.mobile.apps.treecalc.ui.theme.pastelScreenBrush
import com.lkovari.mobile.apps.treecalc.viewmodel.CalculatorViewModel

private enum class OverlayScreen {
    NONE,
    ABOUT,
    HELP
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeCalcApp(
    themeMode: ThemeMode,
    onThemeMode: (ThemeMode) -> Unit,
    calculatorViewModel: CalculatorViewModel = viewModel()
) {
    val state by calculatorViewModel.state.collectAsStateWithLifecycle()
    val palette = LocalTreeCalcPalette.current
    val metrics = rememberAdaptiveMetrics()
    var overlay by remember { mutableStateOf(OverlayScreen.NONE) }
    var menuOpen by remember { mutableStateOf(false) }
    var themeMenuOpen by remember { mutableStateOf(false) }

    when (overlay) {
        OverlayScreen.ABOUT -> AboutScreen(onBack = { overlay = OverlayScreen.NONE })
        OverlayScreen.HELP -> HelpScreen(onBack = { overlay = OverlayScreen.NONE })
        OverlayScreen.NONE -> {
            val startPage = (Int.MAX_VALUE / 2).let { middle -> middle - (middle % 2) }
            val pagerState = rememberPagerState(initialPage = startPage, pageCount = { Int.MAX_VALUE })
            val onCalculator = pagerState.currentPage % 2 == 0
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(
                                    if (onCalculator) {
                                        R.string.screen_calculator
                                    } else {
                                        R.string.screen_tree
                                    }
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = palette.titleAccent,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = metrics.titleSize,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        },
                        actions = {
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = stringResource(R.string.cd_menu)
                                )
                            }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.menu_about)) },
                                    onClick = {
                                        menuOpen = false
                                        overlay = OverlayScreen.ABOUT
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.menu_help)) },
                                    onClick = {
                                        menuOpen = false
                                        overlay = OverlayScreen.HELP
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.menu_theme)) },
                                    onClick = {
                                        menuOpen = false
                                        themeMenuOpen = true
                                    }
                                )
                            }
                            DropdownMenu(expanded = themeMenuOpen, onDismissRequest = { themeMenuOpen = false }) {
                                ThemeMenuItem(stringResource(R.string.theme_auto), themeMode == ThemeMode.AUTO) {
                                    onThemeMode(ThemeMode.AUTO)
                                    themeMenuOpen = false
                                }
                                ThemeMenuItem(stringResource(R.string.theme_light), themeMode == ThemeMode.LIGHT) {
                                    onThemeMode(ThemeMode.LIGHT)
                                    themeMenuOpen = false
                                }
                                ThemeMenuItem(stringResource(R.string.theme_dark), themeMode == ThemeMode.DARK) {
                                    onThemeMode(ThemeMode.DARK)
                                    themeMenuOpen = false
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = palette.screenWashTop,
                            titleContentColor = palette.titleAccent,
                            actionIconContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                },
                containerColor = palette.screenWashTop
            ) { inner ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(inner)
                        .background(pastelScreenBrush(palette))
                ) {
                    PageIndicator(selectedIndex = if (onCalculator) 0 else 1)
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        beyondViewportPageCount = 1
                    ) { page ->
                        if (page % 2 == 0) {
                            CalculatorScreen(
                                state = state,
                                onKey = { key -> calculatorViewModel.press(key) },
                                onBase = { base -> calculatorViewModel.setBase(base) }
                            )
                        } else {
                            ExpressionTreeScreen(state = state)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PageIndicator(selectedIndex: Int) {
    val palette = LocalTreeCalcPalette.current
    val calculatorDesc = stringResource(R.string.cd_page_calculator)
    val treeDesc = stringResource(R.string.cd_page_tree)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp)
            .semantics {
                contentDescription = if (selectedIndex == 0) {
                    calculatorDesc
                } else {
                    treeDesc
                }
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PageDot(selected = selectedIndex == 0, fill = palette.titleAccent, idle = palette.displayBorder)
        Box(modifier = Modifier.size(8.dp))
        PageDot(selected = selectedIndex == 1, fill = palette.titleAccent, idle = palette.displayBorder)
    }
}

@Composable
private fun PageDot(selected: Boolean, fill: Color, idle: Color) {
    Box(
        modifier = Modifier
            .width(if (selected) 18.dp else 6.dp)
            .height(6.dp)
            .clip(CircleShape)
            .background(if (selected) fill else idle)
    )
}

@Composable
private fun ThemeMenuItem(label: String, selected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label) },
        onClick = onClick,
        trailingIcon = {
            Checkbox(checked = selected, onCheckedChange = null)
        }
    )
}
