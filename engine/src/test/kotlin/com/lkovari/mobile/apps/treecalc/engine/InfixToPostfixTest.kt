package com.lkovari.mobile.apps.treecalc.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class InfixToPostfixTest {
    @Test
    fun prefixMinusThenPowerIsUnaryNegate() {
        val postfix = InfixToPostfix.convert(
            listOf(
                Token.OperatorToken(OperatorKind.NEG),
                Token.NumberLiteral("5"),
                Token.OperatorToken(OperatorKind.POW),
                Token.NumberLiteral("2")
            )
        )
        assertEquals(listOf("5", "2", "^", "−"), postfix.map(::tokenText))
    }

    @Test
    fun postfixSineWrapsPrecedingDivision() {
        val postfix = InfixToPostfix.convert(
            listOf(
                Token.NumberLiteral("π"),
                Token.OperatorToken(OperatorKind.DIV),
                Token.NumberLiteral("2"),
                Token.OperatorToken(OperatorKind.SIN)
            )
        )
        assertEquals(listOf("π", "2", "÷", "sin"), postfix.map(::tokenText))
    }

    @Test
    fun multiplicationBindsTighterThanAddition() {
        val postfix = InfixToPostfix.convert(
            listOf(
                Token.NumberLiteral("9"),
                Token.OperatorToken(OperatorKind.ADD),
                Token.NumberLiteral("7"),
                Token.OperatorToken(OperatorKind.MUL),
                Token.NumberLiteral("6")
            )
        )
        assertEquals(
            listOf("9", "7", "6", "×", "+"),
            postfix.map(::tokenText)
        )
    }

    @Test
    fun powerIsRightAssociative() {
        val postfix = InfixToPostfix.convert(
            listOf(
                Token.NumberLiteral("2"),
                Token.OperatorToken(OperatorKind.POW),
                Token.NumberLiteral("3"),
                Token.OperatorToken(OperatorKind.POW),
                Token.NumberLiteral("2")
            )
        )
        assertEquals(
            listOf("2", "3", "2", "^", "^"),
            postfix.map(::tokenText)
        )
    }

    @Test
    fun parenthesesOverridePrecedence() {
        val postfix = InfixToPostfix.convert(
            listOf(
                Token.LeftParen,
                Token.NumberLiteral("1"),
                Token.OperatorToken(OperatorKind.ADD),
                Token.NumberLiteral("2"),
                Token.RightParen,
                Token.OperatorToken(OperatorKind.MUL),
                Token.NumberLiteral("3")
            )
        )
        assertEquals(
            listOf("1", "2", "+", "3", "×"),
            postfix.map(::tokenText)
        )
    }

    @Test
    fun unaryFollowsItsOperand() {
        val postfix = InfixToPostfix.convert(
            listOf(
                Token.NumberLiteral("9"),
                Token.OperatorToken(OperatorKind.SQRT)
            )
        )
        assertEquals(listOf("9", "√"), postfix.map(::tokenText))
    }

    @Test
    fun andBindsTighterThanOr() {
        val postfix = InfixToPostfix.convert(
            listOf(
                Token.NumberLiteral("1"),
                Token.OperatorToken(OperatorKind.OR),
                Token.NumberLiteral("2"),
                Token.OperatorToken(OperatorKind.AND),
                Token.NumberLiteral("3")
            )
        )
        assertEquals(
            listOf("1", "2", "3", "and", "or"),
            postfix.map(::tokenText)
        )
    }

    @Test
    fun emptyInfixYieldsEmptyPostfix() {
        assertEquals(emptyList<Token>(), InfixToPostfix.convert(emptyList()))
    }

    @Test
    fun missingLeftParenthesis() {
        assertCalculatorError(ErrorKind.UNBALANCED_PARENTHESES) {
            InfixToPostfix.convert(
                listOf(
                    Token.NumberLiteral("1"),
                    Token.RightParen
                )
            )
        }
    }

    @Test
    fun missingRightParenthesis() {
        assertCalculatorError(ErrorKind.UNBALANCED_PARENTHESES) {
            InfixToPostfix.convert(
                listOf(
                    Token.LeftParen,
                    Token.NumberLiteral("1")
                )
            )
        }
    }

    private fun tokenText(token: Token): String {
        return when (token) {
            is Token.NumberLiteral -> token.raw
            is Token.OperatorToken -> token.kind.displaySymbol
            is Token.LeftParen -> "("
            is Token.RightParen -> ")"
        }
    }
}
