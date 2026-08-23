package com.lkovari.mobile.apps.treecalc.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lkovari.mobile.apps.treecalc.engine.BinaryNode
import com.lkovari.mobile.apps.treecalc.engine.ErrorKind
import com.lkovari.mobile.apps.treecalc.engine.EvaluationResult
import com.lkovari.mobile.apps.treecalc.engine.NumericBase
import com.lkovari.mobile.apps.treecalc.engine.OperatorKind
import com.lkovari.mobile.apps.treecalc.engine.ValueNode
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
    }

    @Test
    fun evaluatedTreeShowsPostfixAndLabels() {
        val tree = BinaryNode(
            OperatorKind.ADD,
            ValueNode(9.0, "9"),
            BinaryNode(
                OperatorKind.MUL,
                ValueNode(7.0, "7"),
                ValueNode(6.0, "6")
            )
        )
        composeRule.setContent {
            TreeCalcTheme(darkTheme = false) {
                ExpressionTreeScreen(
                    state = EvaluationResult(
                        display = "51",
                        expression = "9 + 7 × 6",
                        postfix = "9, 7, 6, ×, +",
                        tree = tree,
                        base = NumericBase.DECIMAL,
                        errorKind = null,
                        memorySet = false,
                        afterEquals = true
                    )
                )
            }
        }
        composeRule.onNodeWithText("9, 7, 6, ×, +").assertIsDisplayed()
        composeRule.onNodeWithText("+ = 51").assertIsDisplayed()
        composeRule.onNodeWithText("× = 42").assertIsDisplayed()
        composeRule.onNodeWithText("9").assertIsDisplayed()
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
        composeRule.onNodeWithText("laszlo.kovary@gmail.com").performScrollTo().assertIsDisplayed()
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
        composeRule.onNodeWithText("Equals and the tree").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("TreeCalc v1.0.1").performScrollTo().assertIsDisplayed()
    }
}
