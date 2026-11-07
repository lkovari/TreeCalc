package com.lkovari.mobile.apps.treecalc.ui.screens

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.unit.sp
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lkovari.mobile.apps.treecalc.BuildConfig
import com.lkovari.mobile.apps.treecalc.engine.BinaryNode
import com.lkovari.mobile.apps.treecalc.engine.ErrorKind
import com.lkovari.mobile.apps.treecalc.engine.EvaluationResult
import com.lkovari.mobile.apps.treecalc.engine.NumericBase
import com.lkovari.mobile.apps.treecalc.engine.OperatorKind
import com.lkovari.mobile.apps.treecalc.engine.ValueNode
import com.lkovari.mobile.apps.treecalc.ui.FittingTitle
import com.lkovari.mobile.apps.treecalc.ui.theme.TitleMagenta
import com.lkovari.mobile.apps.treecalc.ui.theme.TreeCalcTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalculatorScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsResultExpressionAndBase() {
        composeRule.setContent {
            TreeCalcTheme(darkTheme = false) {
                CalculatorScreen(
                    state = EvaluationResult(
                        display = "51",
                        expression = "9 + 7 × 6",
                        postfix = "9, 7, 6, ×, +",
                        tree = null,
                        base = NumericBase.DECIMAL,
                        errorKind = null,
                        memorySet = false,
                        afterEquals = true
                    ),
                    onKey = {},
                    onBase = {}
                )
            }
        }
        composeRule.onNodeWithText("51").assertIsDisplayed()
        composeRule.onNodeWithText("9 + 7 × 6").assertIsDisplayed()
        composeRule.onNodeWithText("Dec").assertIsDisplayed()
    }

    @Test
    fun showsMemoryIndicatorAndDivisionError() {
        composeRule.setContent {
            TreeCalcTheme(darkTheme = false) {
                CalculatorScreen(
                    state = EvaluationResult(
                        display = "1",
                        expression = "1 ÷ 0",
                        postfix = "",
                        tree = null,
                        base = NumericBase.HEXADECIMAL,
                        errorKind = ErrorKind.DIVISION_BY_ZERO,
                        memorySet = true,
                        afterEquals = false
                    ),
                    onKey = {},
                    onBase = {}
                )
            }
        }
        composeRule.onNodeWithText("M").assertIsDisplayed()
        composeRule.onNodeWithText("Hex").assertIsDisplayed()
        composeRule.onNodeWithText("Division by zero.").assertIsDisplayed()
    }

    @Test
    fun longExpressionScrollsWithoutShiftingKeyColumns() {
        val expression = "888 / ( 6 × 1.25 + 3 × ( 7 − 4 × 1.5 + ( 9 ÷ 3 − 1.2 ) ) × 2 )"
        composeRule.setContent {
            TreeCalcTheme(darkTheme = false) {
                Box(Modifier.size(width = 360.dp, height = 640.dp)) {
                    CalculatorScreen(
                        state = EvaluationResult(
                            display = "0",
                            expression = expression,
                            postfix = "",
                            tree = null,
                            base = NumericBase.DECIMAL,
                            errorKind = null,
                            memorySet = false,
                            afterEquals = false
                        ),
                        onKey = {},
                        onBase = {}
                    )
                }
            }
        }
        composeRule.onNodeWithText(expression).assertIsDisplayed()
        val functionLeft = composeRule.onNodeWithTag("calc-key-PI").getUnclippedBoundsInRoot().left
        val numberLeft = composeRule.onNodeWithTag("calc-key-DIGIT_F").getUnclippedBoundsInRoot().left
        assertEquals(functionLeft, numberLeft)
    }
}

@RunWith(AndroidJUnit4::class)
class ExpressionTreeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyTreeShowsPlaceholder() {
        composeRule.setContent {
            TreeCalcTheme(darkTheme = false) {
                ExpressionTreeScreen(
                    state = EvaluationResult(
                        display = "0",
                        expression = "",
                        postfix = "",
                        tree = null,
                        base = NumericBase.DECIMAL,
                        errorKind = null,
                        memorySet = false,
                        afterEquals = false
                    )
                )
            }
        }
        composeRule.onNodeWithText("—").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Evaluate an expression with = to see postfix notation and the expression tree."
        ).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Postfix step: 9").assertDoesNotExist()
    }

    @Test
    fun evaluatedTreeShowsPostfixAndLabels() {
        composeRule.setContent {
            TreeCalcTheme(darkTheme = false) {
                ExpressionTreeScreen(state = sampleEvaluatedState())
            }
        }
        composeRule.onNodeWithContentDescription("Postfix step: 9").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Postfix step: 7").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Postfix step: 6").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Postfix step: ×").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Postfix step: +").assertIsDisplayed()
        composeRule.onNodeWithText("+ = 51").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("× = 42").performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithText("9").assertCountEquals(2)
    }

    @Test
    fun collapsingRootHidesChildrenThenExpandRestoresThem() {
        composeRule.setContent {
            TreeCalcTheme(darkTheme = false) {
                ExpressionTreeScreen(state = sampleEvaluatedState())
            }
        }
        composeRule.onAllNodesWithText("9").assertCountEquals(2)
        composeRule.onAllNodesWithContentDescription("Collapse")[0].performClick()
        composeRule.onAllNodesWithText("9").assertCountEquals(1)
        composeRule.onNodeWithText("+ = 51").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Expand").performClick()
        composeRule.onAllNodesWithText("9").assertCountEquals(2)
    }

    @Test
    fun tappingPostfixTokenHighlightsMatchingTreeNode() {
        composeRule.setContent {
            TreeCalcTheme(darkTheme = false) {
                ExpressionTreeScreen(state = sampleEvaluatedState())
            }
        }
        composeRule.onNodeWithContentDescription("Postfix step: ×").performClick()
        composeRule.onNodeWithText("× = 42").assertIsSelected()
        composeRule.onNodeWithText("+ = 51").assertIsNotSelected()
        composeRule.onNodeWithContentDescription("Postfix step: ×").assertIsSelected()
        composeRule.onNodeWithContentDescription("Postfix step: ×").performClick()
        composeRule.onNodeWithText("× = 42").assertIsNotSelected()
    }

    @Test
    fun playSelectsTheFirstTokenAndPauseHoldsIt() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            TreeCalcTheme(darkTheme = false) {
                ExpressionTreeScreen(state = sampleEvaluatedState(), stepByStep = false)
            }
        }
        composeRule.onNodeWithText("Play").assertIsDisplayed()
        composeRule.onNodeWithText("Play").performClick()
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.onNodeWithContentDescription("Postfix step: 9").assertIsSelected()
        composeRule.onNodeWithText("Pause").performClick()
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.onNodeWithContentDescription("Postfix step: 9").assertIsSelected()
        composeRule.onNodeWithContentDescription("Postfix step: 7").assertIsNotSelected()
    }

    @Test
    fun playIsDisabledWithoutTokens() {
        composeRule.setContent {
            TreeCalcTheme(darkTheme = false) {
                ExpressionTreeScreen(
                    state = EvaluationResult(
                        display = "0",
                        expression = "",
                        postfix = "",
                        tree = null,
                        base = NumericBase.DECIMAL,
                        errorKind = null,
                        memorySet = false,
                        afterEquals = false
                    )
                )
            }
        }
        composeRule.onNodeWithTag("tree.playback").assertIsNotEnabled()
    }

    private fun sampleEvaluatedState(): EvaluationResult {
        val tree = BinaryNode(
            OperatorKind.ADD,
            ValueNode(9.0, "9"),
            BinaryNode(
                OperatorKind.MUL,
                ValueNode(7.0, "7"),
                ValueNode(6.0, "6")
            )
        )
        return EvaluationResult(
            display = "51",
            expression = "9 + 7 × 6",
            postfix = "9, 7, 6, ×, +",
            postfixTokens = listOf("9", "7", "6", "×", "+"),
            tree = tree,
            base = NumericBase.DECIMAL,
            errorKind = null,
            memorySet = false,
            afterEquals = true
        )
    }
}

@RunWith(AndroidJUnit4::class)
class AboutAndHelpScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun aboutScreenShowsIdentity() {
        composeRule.setContent {
            TreeCalcTheme(darkTheme = false) {
                AboutScreen(onBack = {})
            }
        }
        composeRule.onNodeWithText("About TreeCalc").assertIsDisplayed()
        composeRule.onNodeWithText("Privacy policy").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Original desktop repository").performScrollTo().performClick()
        composeRule.onNodeWithText("https://github.com/lkovari/Calculator").assertIsDisplayed()
        composeRule.onNodeWithText("Privacy policy").performClick()
        composeRule.onNodeWithText(
            "https://lkovari.github.io/KLHome/assets/bigfiles/treecalc-privacy-policy.html"
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Support").performScrollTo().performClick()
        composeRule.onAllNodesWithText("laszlo.kovary@gmail.com").assertCountEquals(1)
        composeRule.onNodeWithText("laszlo.kovary@gmail.com").assertIsDisplayed()
        composeRule.onNodeWithText("Copyright").performScrollTo().performClick()
        composeRule.onNodeWithText("László Kővári © 2005 – 2026").assertIsDisplayed()
    }

    @Test
    fun helpScreenShowsUsageSections() {
        composeRule.setContent {
            TreeCalcTheme(darkTheme = false) {
                HelpScreen(onBack = {})
            }
        }
        composeRule.onNodeWithText("Help").assertIsDisplayed()
        composeRule.onNodeWithText("Switching screens").assertIsDisplayed()
        composeRule.onNodeWithText("Numerical systems").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Converting numerical systems").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Equals and the tree").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Repeating decimals").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Postfix playback").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("TreeCalc v${BuildConfig.VERSION_NAME}").performScrollTo().assertIsDisplayed()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RunWith(AndroidJUnit4::class)
class ScreenTitleTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun expressionTreeCalculatorFitsThePhoneAppBar() {
        var englishOverflow = true
        var hungarianOverflow = true
        composeRule.setContent {
            TreeCalcTheme(darkTheme = false) {
                Column(Modifier.size(width = 360.dp, height = 200.dp)) {
                    TopAppBar(
                        title = {
                            FittingTitle(
                                text = "Expression Tree Calculator",
                                color = TitleMagenta,
                                maxSize = 20.sp,
                                onOverflow = { englishOverflow = it }
                            )
                        },
                        actions = {
                            IconButton(onClick = {}) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                            }
                        }
                    )
                    TopAppBar(
                        title = {
                            FittingTitle(
                                text = "Kifejezésfa-számológép",
                                color = TitleMagenta,
                                maxSize = 20.sp,
                                onOverflow = { hungarianOverflow = it }
                            )
                        },
                        actions = {
                            IconButton(onClick = {}) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                            }
                        }
                    )
                }
            }
        }
        composeRule.waitForIdle()
        assertFalse(englishOverflow)
        assertFalse(hungarianOverflow)
        composeRule.onNodeWithText("Expression Tree Calculator").assertIsDisplayed()
        composeRule.onNodeWithText("Kifejezésfa-számológép").assertIsDisplayed()
    }
}
