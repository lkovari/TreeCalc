package com.lkovari.mobile.apps.treecalc.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class ExpressionSpansTest {
    @Test
    fun multiplicationSpanSitsInsideTheSum() {
        val infix = listOf(
            Token.NumberLiteral("9"),
            Token.OperatorToken(OperatorKind.ADD),
            Token.NumberLiteral("7"),
            Token.OperatorToken(OperatorKind.MUL),
            Token.NumberLiteral("6")
        )
        assertEquals(
            listOf("9", "7", "6", "7 × 6", "9 + 7 × 6"),
            slices(infix)
        )
    }

    @Test
    fun parenthesesWrapTheGroupedStep() {
        val infix = listOf(
            Token.LeftParen,
            Token.NumberLiteral("1"),
            Token.OperatorToken(OperatorKind.ADD),
            Token.NumberLiteral("2"),
            Token.RightParen,
            Token.OperatorToken(OperatorKind.MUL),
            Token.NumberLiteral("3")
        )
        assertEquals(
            listOf("1", "2", "( 1 + 2 )", "3", "( 1 + 2 ) × 3"),
            slices(infix)
        )
    }

    @Test
    fun prefixMinusCoversThePowerItNegates() {
        val infix = listOf(
            Token.OperatorToken(OperatorKind.NEG),
            Token.NumberLiteral("5"),
            Token.OperatorToken(OperatorKind.POW),
            Token.NumberLiteral("2")
        )
        assertEquals(
            listOf("5", "2", "5 ^ 2", "− 5 ^ 2"),
            slices(infix)
        )
    }

    @Test
    fun postfixSineCoversTheDivision() {
        val infix = listOf(
            Token.NumberLiteral("π"),
            Token.OperatorToken(OperatorKind.DIV),
            Token.NumberLiteral("2"),
            Token.OperatorToken(OperatorKind.SIN)
        )
        assertEquals(
            listOf("π", "2", "π ÷ 2", "π ÷ 2 sin"),
            slices(infix)
        )
    }

    @Test
    fun unbalancedParenthesesProduceNoSpans() {
        val infix = listOf(
            Token.LeftParen,
            Token.NumberLiteral("1")
        )
        assertEquals(emptyList<String>(), slices(infix))
    }

    private fun slices(infix: List<Token>): List<String> {
        val text = infix.joinToString(" ") { token -> token.expressionText() }
        return expressionStepSpans(infix).map { range -> text.substring(range.first, range.last + 1) }
    }
}
