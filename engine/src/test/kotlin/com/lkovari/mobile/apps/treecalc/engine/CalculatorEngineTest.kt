package com.lkovari.mobile.apps.treecalc.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEngineTest {
    @Test
    fun mixedPrecedenceMatchesDesktopExample() {
        val engine = CalculatorEngine()
        pressAll(engine, "9+7*6")
        engine.press(CalculatorKey.EQUALS)
        val result = engine.snapshot()
        assertEquals("51", result.display)
        assertEquals("9, 7, 6, ×, +", result.postfix)
        assertNotNull(result.tree)
        assertTrue(result.tree is BinaryNode)
    }

    @Test
    fun parenthesizedDivision() {
        val engine = CalculatorEngine()
        pressAll(engine, "(9+7)/(5-3)")
        engine.press(CalculatorKey.EQUALS)
        assertEquals("8", engine.snapshot().display)
    }

    @Test
    fun chainedAdditionAndMultiplication() {
        val engine = CalculatorEngine()
        pressAll(engine, "1+2*3+4+5*6")
        engine.press(CalculatorKey.EQUALS)
        assertEquals("41", engine.snapshot().display)
    }

    @Test
    fun powerIsRightAssociative() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.DIGIT_2)
        engine.press(CalculatorKey.POW)
        engine.press(CalculatorKey.DIGIT_3)
        engine.press(CalculatorKey.POW)
        engine.press(CalculatorKey.DIGIT_2)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("512", engine.snapshot().display)
    }

    @Test
    fun hexAddition() {
        val engine = CalculatorEngine()
        engine.setBase(NumericBase.HEXADECIMAL)
        engine.press(CalculatorKey.DIGIT_A)
        engine.press(CalculatorKey.ADD)
        engine.press(CalculatorKey.DIGIT_5)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("F", engine.snapshot().display)
    }

    @Test
    fun binaryAddition() {
        val engine = CalculatorEngine()
        engine.setBase(NumericBase.BINARY)
        pressAll(engine, "1010")
        engine.press(CalculatorKey.ADD)
        engine.press(CalculatorKey.DIGIT_1)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("1011", engine.snapshot().display)
    }

    @Test
    fun octalAddition() {
        val engine = CalculatorEngine()
        engine.setBase(NumericBase.OCTAL)
        pressAll(engine, "7+1")
        engine.press(CalculatorKey.EQUALS)
        assertEquals("10", engine.snapshot().display)
    }

    @Test
    fun xorIsEvaluated() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.DIGIT_5)
        engine.press(CalculatorKey.XOR)
        engine.press(CalculatorKey.DIGIT_3)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("6", engine.snapshot().display)
    }

    @Test
    fun unaryMinusOnInput() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.DIGIT_5)
        engine.press(CalculatorKey.NEGATE)
        engine.press(CalculatorKey.ADD)
        engine.press(CalculatorKey.DIGIT_3)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("-2", engine.snapshot().display)
    }

    @Test
    fun secondEqualsKeepsResult() {
        val engine = CalculatorEngine()
        pressAll(engine, "1+2")
        engine.press(CalculatorKey.EQUALS)
        engine.press(CalculatorKey.EQUALS)
        val result = engine.snapshot()
        assertEquals("3", result.display)
        assertNull(result.errorKind)
    }

    @Test
    fun operatorAfterEqualsContinuesFromResult() {
        val engine = CalculatorEngine()
        pressAll(engine, "1+2")
        engine.press(CalculatorKey.EQUALS)
        engine.press(CalculatorKey.ADD)
        engine.press(CalculatorKey.DIGIT_4)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("7", engine.snapshot().display)
    }

    @Test
    fun digitAfterEqualsStartsNewExpression() {
        val engine = CalculatorEngine()
        pressAll(engine, "1+2")
        engine.press(CalculatorKey.EQUALS)
        engine.press(CalculatorKey.DIGIT_8)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("8", engine.snapshot().display)
    }

    @Test
    fun divisionByZeroIsAnError() {
        val engine = CalculatorEngine()
        pressAll(engine, "1/0")
        engine.press(CalculatorKey.EQUALS)
        assertEquals(ErrorKind.DIVISION_BY_ZERO, engine.snapshot().errorKind)
    }

    @Test
    fun unbalancedParenthesesAreAnError() {
        val engine = CalculatorEngine()
        pressAll(engine, "(1+2")
        engine.press(CalculatorKey.EQUALS)
        assertEquals(ErrorKind.UNBALANCED_PARENTHESES, engine.snapshot().errorKind)
    }

    @Test
    fun extraClosingParenthesisIsAnError() {
        val engine = CalculatorEngine()
        pressAll(engine, "1+2)")
        engine.press(CalculatorKey.EQUALS)
        assertEquals(ErrorKind.UNBALANCED_PARENTHESES, engine.snapshot().errorKind)
    }

    @Test
    fun missingLeftParenthesisThrowsFromConverter() {
        val infix = listOf(
            Token.NumberLiteral("1"),
            Token.OperatorToken(OperatorKind.ADD),
            Token.NumberLiteral("2"),
            Token.RightParen
        )
        try {
            InfixToPostfix.convert(infix)
            throw AssertionError("expected CalculatorException")
        } catch (exception: CalculatorException) {
            assertEquals(ErrorKind.UNBALANCED_PARENTHESES, exception.kind)
        }
    }

    @Test
    fun missingRightParenthesisThrowsFromConverter() {
        val infix = listOf(
            Token.LeftParen,
            Token.NumberLiteral("1"),
            Token.OperatorToken(OperatorKind.ADD),
            Token.NumberLiteral("2")
        )
        try {
            InfixToPostfix.convert(infix)
            throw AssertionError("expected CalculatorException")
        } catch (exception: CalculatorException) {
            assertEquals(ErrorKind.UNBALANCED_PARENTHESES, exception.kind)
        }
    }

    @Test
    fun failedEqualsClearsPreviousTree() {
        val engine = CalculatorEngine()
        pressAll(engine, "1+2")
        engine.press(CalculatorKey.EQUALS)
        assertNotNull(engine.snapshot().tree)
        pressAll(engine, "(3+4")
        engine.press(CalculatorKey.EQUALS)
        val result = engine.snapshot()
        assertEquals(ErrorKind.UNBALANCED_PARENTHESES, result.errorKind)
        assertNull(result.tree)
        assertEquals("", result.postfix)
    }

    @Test
    fun sqrtDoesNotTruncate() {
        val engine = CalculatorEngine()
        pressAll(engine, "2.25")
        engine.press(CalculatorKey.SQRT)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("1.5", engine.snapshot().display)
    }

    @Test
    fun reciprocalAndSquare() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.DIGIT_4)
        engine.press(CalculatorKey.REC)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("0.25", engine.snapshot().display)
        engine.press(CalculatorKey.SQ)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("0.0625", engine.snapshot().display)
    }

    @Test
    fun memoryRoundTrip() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.DIGIT_8)
        engine.press(CalculatorKey.MEMORY_ADD)
        engine.press(CalculatorKey.CLEAR)
        engine.press(CalculatorKey.MEMORY_RECALL)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("8", engine.snapshot().display)
        engine.press(CalculatorKey.MEMORY_CLEAR)
        engine.press(CalculatorKey.CLEAR)
        engine.press(CalculatorKey.MEMORY_RECALL)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("0", engine.snapshot().display)
    }

    @Test
    fun leftShift() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.DIGIT_3)
        engine.press(CalculatorKey.LSH)
        engine.press(CalculatorKey.DIGIT_2)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("12", engine.snapshot().display)
    }

    @Test
    fun hexDigitsDisabledInDecimal() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.DIGIT_A)
        assertEquals("0", engine.snapshot().display)
    }

    @Test
    fun binaryEnablesOnlyZeroAndOne() {
        val engine = CalculatorEngine()
        engine.setBase(NumericBase.BINARY)
        assertTrue(engine.isKeyEnabled(CalculatorKey.DIGIT_0))
        assertTrue(engine.isKeyEnabled(CalculatorKey.DIGIT_1))
        assertTrue(!engine.isKeyEnabled(CalculatorKey.DIGIT_2))
        assertTrue(!engine.isKeyEnabled(CalculatorKey.DIGIT_7))
        assertTrue(!engine.isKeyEnabled(CalculatorKey.DIGIT_8))
        assertTrue(!engine.isKeyEnabled(CalculatorKey.DIGIT_A))
        engine.press(CalculatorKey.DIGIT_7)
        assertEquals("0", engine.snapshot().display)
    }

    @Test
    fun octalEnablesZeroThroughSeven() {
        val engine = CalculatorEngine()
        engine.setBase(NumericBase.OCTAL)
        assertTrue(engine.isKeyEnabled(CalculatorKey.DIGIT_0))
        assertTrue(engine.isKeyEnabled(CalculatorKey.DIGIT_7))
        assertTrue(!engine.isKeyEnabled(CalculatorKey.DIGIT_8))
        assertTrue(!engine.isKeyEnabled(CalculatorKey.DIGIT_9))
        assertTrue(!engine.isKeyEnabled(CalculatorKey.DIGIT_A))
        engine.press(CalculatorKey.DIGIT_8)
        assertEquals("0", engine.snapshot().display)
    }

    @Test
    fun hexadecimalEnablesLetters() {
        val engine = CalculatorEngine()
        engine.setBase(NumericBase.HEXADECIMAL)
        assertTrue(engine.isKeyEnabled(CalculatorKey.DIGIT_0))
        assertTrue(engine.isKeyEnabled(CalculatorKey.DIGIT_9))
        assertTrue(engine.isKeyEnabled(CalculatorKey.DIGIT_A))
        assertTrue(engine.isKeyEnabled(CalculatorKey.DIGIT_F))
        engine.press(CalculatorKey.DIGIT_A)
        assertEquals("A", engine.snapshot().display)
    }

    @Test
    fun testButtonLoadsSampleExpressionWithoutEvaluating() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.TEST)
        val result = engine.snapshot()
        assertEquals(NumericBase.DECIMAL, result.base)
        assertTrue(result.expression.contains("888"))
        assertTrue(result.expression.contains("("))
        assertTrue(result.expression.contains("^"))
        assertNull(result.errorKind)
        assertTrue(!result.afterEquals)
        assertNull(result.tree)
    }

    @Test
    fun switchingBaseAfterTestKeepsTypedDecimalExpression() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.TEST)
        val before = engine.snapshot().expression
        engine.setBase(NumericBase.HEXADECIMAL)
        val after = engine.snapshot()
        assertEquals(NumericBase.HEXADECIMAL, after.base)
        assertEquals(before, after.expression)
        assertTrue(after.expression.contains("1.25"))
        assertTrue(after.expression.contains("4.5"))
        assertTrue(!after.afterEquals)
    }

    @Test
    fun testButtonExpressionEvaluatesAndBuildsTree() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.TEST)
        engine.press(CalculatorKey.EQUALS)
        val result = engine.snapshot()
        assertNull(result.errorKind)
        assertNotNull(result.tree)
        assertTrue(result.postfix.isNotEmpty())
        assertTrue(result.postfix.contains("÷"))
        assertTrue(result.afterEquals)
    }

    @Test
    fun desktopNestedDivisionSample() {
        val engine = CalculatorEngine()
        pressAll(engine, "777/(5*1.33+2*(5-3*1.3)*3)")
        engine.press(CalculatorKey.EQUALS)
        val result = engine.snapshot()
        assertNull(result.errorKind)
        assertEquals(58.64150943396227, result.display.toDouble(), 1e-9)
        assertEquals("777, 5, 1.33, ×, 2, 5, 3, 1.3, ×, -, ×, 3, ×, +, ÷", result.postfix)
        assertNotNull(result.tree)
    }

    @Test
    fun deeplyNestedMixedOperations() {
        val engine = CalculatorEngine()
        pressAll(engine, "((3+4)*(8-2)+9)/(5+1)")
        engine.press(CalculatorKey.EQUALS)
        val result = engine.snapshot()
        assertNull(result.errorKind)
        assertEquals("8.5", result.display)
        assertNotNull(result.tree)
    }

    @Test
    fun powerInsideParenthesesWithDivision() {
        val engine = CalculatorEngine()
        pressAll(engine, "(2^3+8)/4")
        engine.press(CalculatorKey.EQUALS)
        assertNull(engine.snapshot().errorKind)
        assertEquals("4", engine.snapshot().display)
    }

    @Test
    fun squareThenReciprocal() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.DIGIT_5)
        engine.press(CalculatorKey.SQ)
        engine.press(CalculatorKey.REC)
        engine.press(CalculatorKey.EQUALS)
        assertNull(engine.snapshot().errorKind)
        assertEquals("0.04", engine.snapshot().display)
    }

    @Test
    fun factorialOfFive() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.DIGIT_5)
        engine.press(CalculatorKey.FACT)
        engine.press(CalculatorKey.EQUALS)
        assertNull(engine.snapshot().errorKind)
        assertEquals("120", engine.snapshot().display)
    }

    @Test
    fun moduloAfterNestedSubtract() {
        val engine = CalculatorEngine()
        pressAll(engine, "(20-3)")
        engine.press(CalculatorKey.MOD)
        pressAll(engine, "5")
        engine.press(CalculatorKey.EQUALS)
        assertNull(engine.snapshot().errorKind)
        assertEquals("2", engine.snapshot().display)
    }

    @Test
    fun hexBitwiseAndOr() {
        val engine = CalculatorEngine()
        engine.setBase(NumericBase.HEXADECIMAL)
        engine.press(CalculatorKey.DIGIT_F)
        engine.press(CalculatorKey.AND)
        engine.press(CalculatorKey.DIGIT_A)
        engine.press(CalculatorKey.OR)
        engine.press(CalculatorKey.DIGIT_1)
        engine.press(CalculatorKey.EQUALS)
        assertNull(engine.snapshot().errorKind)
        assertEquals("B", engine.snapshot().display)
    }

    @Test
    fun expThenLnRoundTrip() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.DIGIT_2)
        engine.press(CalculatorKey.EXP)
        engine.press(CalculatorKey.LN)
        engine.press(CalculatorKey.EQUALS)
        val value = engine.snapshot().display.toDouble()
        assertNull(engine.snapshot().errorKind)
        assertEquals(2.0, value, 1e-9)
    }

    @Test
    fun cubeThenSqrtOfNine() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.DIGIT_3)
        engine.press(CalculatorKey.CUBE)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("27", engine.snapshot().display)
        engine.press(CalculatorKey.DIGIT_9)
        engine.press(CalculatorKey.SQRT)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("3", engine.snapshot().display)
    }

    @Test
    fun sampleTestExpressionHasFiniteResultAndTree() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.TEST)
        engine.press(CalculatorKey.EQUALS)
        val result = engine.snapshot()
        assertNull(result.errorKind)
        assertNotNull(result.tree)
        val value = result.display.toDouble()
        assertTrue(value.isFinite())
        assertEquals(34.89320987654321, value, 1e-9)
    }

    private fun pressAll(engine: CalculatorEngine, expression: String) {
        for (character in expression) {
            when (character) {
                '0' -> engine.press(CalculatorKey.DIGIT_0)
                '1' -> engine.press(CalculatorKey.DIGIT_1)
                '2' -> engine.press(CalculatorKey.DIGIT_2)
                '3' -> engine.press(CalculatorKey.DIGIT_3)
                '4' -> engine.press(CalculatorKey.DIGIT_4)
                '5' -> engine.press(CalculatorKey.DIGIT_5)
                '6' -> engine.press(CalculatorKey.DIGIT_6)
                '7' -> engine.press(CalculatorKey.DIGIT_7)
                '8' -> engine.press(CalculatorKey.DIGIT_8)
                '9' -> engine.press(CalculatorKey.DIGIT_9)
                '+' -> engine.press(CalculatorKey.ADD)
                '-' -> engine.press(CalculatorKey.SUB)
                '*' -> engine.press(CalculatorKey.MUL)
                '/' -> engine.press(CalculatorKey.DIV)
                '(' -> engine.press(CalculatorKey.LEFT_PAREN)
                ')' -> engine.press(CalculatorKey.RIGHT_PAREN)
                '.' -> engine.press(CalculatorKey.DOT)
                '^' -> engine.press(CalculatorKey.POW)
                else -> {
                }
            }
        }
    }
}
