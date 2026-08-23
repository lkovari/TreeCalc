package com.lkovari.mobile.apps.treecalc.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEngineBehaviorTest {
    @Test
    fun emptyEqualsIsAnError() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.EQUALS)
        assertEquals(ErrorKind.EMPTY_EXPRESSION, engine.snapshot().errorKind)
        assertEquals("0", engine.snapshot().display)
    }

    @Test
    fun clearResetsExpressionButKeepsMemory() {
        val engine = CalculatorEngine()
        pressAll(engine, "8")
        engine.press(CalculatorKey.MEMORY_ADD)
        pressAll(engine, "12")
        engine.press(CalculatorKey.CLEAR)
        val cleared = engine.snapshot()
        assertEquals("0", cleared.display)
        assertEquals("", cleared.expression)
        assertNull(cleared.errorKind)
        assertNull(cleared.tree)
        assertTrue(cleared.memorySet)
        engine.press(CalculatorKey.MEMORY_RECALL)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("8", engine.snapshot().display)
    }

    @Test
    fun backspaceEditsCurrentInput() {
        val engine = CalculatorEngine()
        pressAll(engine, "123")
        engine.press(CalculatorKey.BACKSPACE)
        assertEquals("12", engine.snapshot().display)
        engine.press(CalculatorKey.BACKSPACE)
        engine.press(CalculatorKey.BACKSPACE)
        assertEquals("0", engine.snapshot().display)
    }

    @Test
    fun backspaceAfterEqualsDoesNothing() {
        val engine = CalculatorEngine()
        pressAll(engine, "1+2")
        engine.press(CalculatorKey.EQUALS)
        engine.press(CalculatorKey.BACKSPACE)
        val result = engine.snapshot()
        assertEquals("3", result.display)
        assertTrue(result.afterEquals)
    }

    @Test
    fun secondDotIsIgnored() {
        val engine = CalculatorEngine()
        pressAll(engine, "1.2.5")
        engine.press(CalculatorKey.EQUALS)
        assertEquals("1.25", engine.snapshot().display)
    }

    @Test
    fun leadingDotStartsFractionalNumber() {
        val engine = CalculatorEngine()
        pressAll(engine, ".5")
        engine.press(CalculatorKey.EQUALS)
        assertEquals("0.5", engine.snapshot().display)
    }

    @Test
    fun leadingOperatorUsesZero() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.ADD)
        engine.press(CalculatorKey.DIGIT_5)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("5", engine.snapshot().display)
    }

    @Test
    fun replacingTheLastOperator() {
        val engine = CalculatorEngine()
        pressAll(engine, "8+")
        engine.press(CalculatorKey.MUL)
        pressAll(engine, "2")
        engine.press(CalculatorKey.EQUALS)
        assertEquals("16", engine.snapshot().display)
    }

    @Test
    fun piEvaluatesToPi() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.PI)
        engine.press(CalculatorKey.EQUALS)
        assertEquals(Math.PI, engine.snapshot().display.toDouble(), 1e-9)
        assertTrue(engine.snapshot().expression.contains("π"))
    }

    @Test
    fun negateTogglesCurrentInput() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.DIGIT_7)
        engine.press(CalculatorKey.NEGATE)
        assertEquals("-7", engine.snapshot().display)
        engine.press(CalculatorKey.NEGATE)
        assertEquals("7", engine.snapshot().display)
    }

    @Test
    fun negateAfterEqualsFlipsTheResult() {
        val engine = CalculatorEngine()
        pressAll(engine, "1+2")
        engine.press(CalculatorKey.EQUALS)
        engine.press(CalculatorKey.NEGATE)
        assertEquals("-3", engine.snapshot().display)
        engine.press(CalculatorKey.ADD)
        engine.press(CalculatorKey.DIGIT_1)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("-2", engine.snapshot().display)
    }

    @Test
    fun unaryAfterEqualsContinuesFromResult() {
        val engine = CalculatorEngine()
        pressAll(engine, "3+6")
        engine.press(CalculatorKey.EQUALS)
        engine.press(CalculatorKey.SQRT)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("3", engine.snapshot().display)
    }

    @Test
    fun unaryOnEmptyInputIsMalformed() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.SQRT)
        assertEquals(ErrorKind.MALFORMED_EXPRESSION, engine.snapshot().errorKind)
    }

    @Test
    fun memorySubtractAndRecall() {
        val engine = CalculatorEngine()
        pressAll(engine, "10")
        engine.press(CalculatorKey.MEMORY_ADD)
        engine.press(CalculatorKey.CLEAR)
        pressAll(engine, "3")
        engine.press(CalculatorKey.MEMORY_SUB)
        engine.press(CalculatorKey.CLEAR)
        engine.press(CalculatorKey.MEMORY_RECALL)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("7", engine.snapshot().display)
        assertTrue(engine.snapshot().memorySet)
    }

    @Test
    fun memoryAddUsesLastResult() {
        val engine = CalculatorEngine()
        pressAll(engine, "5+5")
        engine.press(CalculatorKey.EQUALS)
        engine.press(CalculatorKey.MEMORY_ADD)
        engine.press(CalculatorKey.CLEAR)
        engine.press(CalculatorKey.MEMORY_RECALL)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("10", engine.snapshot().display)
    }

    @Test
    fun switchingBaseAfterEqualsReformatsTheResult() {
        val engine = CalculatorEngine()
        pressAll(engine, "10")
        engine.press(CalculatorKey.EQUALS)
        engine.setBase(NumericBase.HEXADECIMAL)
        val hex = engine.snapshot()
        assertEquals(NumericBase.HEXADECIMAL, hex.base)
        assertEquals("A", hex.display)
        engine.setBase(NumericBase.BINARY)
        assertEquals("1010", engine.snapshot().display)
        engine.setBase(NumericBase.OCTAL)
        assertEquals("12", engine.snapshot().display)
    }

    @Test
    fun scientificKeysAreIgnoredInHex() {
        val engine = CalculatorEngine()
        engine.setBase(NumericBase.HEXADECIMAL)
        engine.press(CalculatorKey.DIGIT_A)
        engine.press(CalculatorKey.SIN)
        engine.press(CalculatorKey.DOT)
        engine.press(CalculatorKey.PI)
        engine.press(CalculatorKey.FACT)
        assertEquals("A", engine.snapshot().display)
        assertEquals(NumericBase.HEXADECIMAL, engine.snapshot().base)
    }

    @Test
    fun clearStillWorksWhenKeyWouldOtherwiseBeDisabled() {
        val engine = CalculatorEngine()
        engine.setBase(NumericBase.BINARY)
        pressAll(engine, "1")
        engine.press(CalculatorKey.CLEAR)
        assertEquals("0", engine.snapshot().display)
    }

    @Test
    fun trigonometricAndLogFunctions() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.DIGIT_0)
        engine.press(CalculatorKey.SIN)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("0", engine.snapshot().display)

        engine.press(CalculatorKey.DIGIT_0)
        engine.press(CalculatorKey.COS)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("1", engine.snapshot().display)

        pressAll(engine, "100")
        engine.press(CalculatorKey.LOG)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("2", engine.snapshot().display)
    }

    @Test
    fun bitwiseNot() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.DIGIT_0)
        engine.press(CalculatorKey.NOT)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("-1", engine.snapshot().display)
    }

    @Test
    fun andOrPrecedence() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.DIGIT_1)
        engine.press(CalculatorKey.OR)
        engine.press(CalculatorKey.DIGIT_2)
        engine.press(CalculatorKey.AND)
        engine.press(CalculatorKey.DIGIT_3)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("3", engine.snapshot().display)
        assertEquals("1, 2, 3, and, or", engine.snapshot().postfix)
    }

    @Test
    fun domainErrorsSurfaceThroughTheEngine() {
        val sqrt = CalculatorEngine()
        pressAll(sqrt, "1")
        sqrt.press(CalculatorKey.NEGATE)
        sqrt.press(CalculatorKey.SQRT)
        sqrt.press(CalculatorKey.EQUALS)
        assertEquals(ErrorKind.DOMAIN, sqrt.snapshot().errorKind)

        val ln = CalculatorEngine()
        ln.press(CalculatorKey.DIGIT_0)
        ln.press(CalculatorKey.LN)
        ln.press(CalculatorKey.EQUALS)
        assertEquals(ErrorKind.DOMAIN, ln.snapshot().errorKind)
    }

    @Test
    fun invalidFactorialSurfacesThroughTheEngine() {
        val engine = CalculatorEngine()
        pressAll(engine, "1.5")
        engine.press(CalculatorKey.FACT)
        engine.press(CalculatorKey.EQUALS)
        assertEquals(ErrorKind.INVALID_FACTORIAL, engine.snapshot().errorKind)
    }

    @Test
    fun bitwiseNonIntegerSurfacesThroughTheEngine() {
        val engine = CalculatorEngine()
        pressAll(engine, "1.5")
        engine.press(CalculatorKey.AND)
        engine.press(CalculatorKey.DIGIT_1)
        engine.press(CalculatorKey.EQUALS)
        assertEquals(ErrorKind.BITWISE_NON_INTEGER, engine.snapshot().errorKind)
    }

    @Test
    fun moduloByZeroIsDivisionByZero() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.DIGIT_8)
        engine.press(CalculatorKey.MOD)
        engine.press(CalculatorKey.DIGIT_0)
        engine.press(CalculatorKey.EQUALS)
        assertEquals(ErrorKind.DIVISION_BY_ZERO, engine.snapshot().errorKind)
    }

    @Test
    fun reciprocalOfZeroIsDivisionByZero() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.DIGIT_0)
        engine.press(CalculatorKey.REC)
        engine.press(CalculatorKey.EQUALS)
        assertEquals(ErrorKind.DIVISION_BY_ZERO, engine.snapshot().errorKind)
    }

    @Test
    fun hexLettersEvaluate() {
        val engine = CalculatorEngine()
        engine.setBase(NumericBase.HEXADECIMAL)
        pressAll(engine, "B+1")
        engine.press(CalculatorKey.EQUALS)
        assertEquals("C", engine.snapshot().display)
        engine.press(CalculatorKey.CLEAR)
        pressAll(engine, "F+1")
        engine.press(CalculatorKey.EQUALS)
        assertEquals("10", engine.snapshot().display)
    }

    @Test
    fun liveExpressionTracksTypedTokens() {
        val engine = CalculatorEngine()
        pressAll(engine, "9+7*6")
        val live = engine.snapshot()
        assertEquals("9 + 7 × 6", live.expression)
        assertFalse(live.afterEquals)
        assertNull(live.tree)
        engine.press(CalculatorKey.EQUALS)
        val done = engine.snapshot()
        assertEquals("9 + 7 × 6", done.expression)
        assertTrue(done.afterEquals)
        assertNotNull(done.tree)
    }

    @Test
    fun clearAfterErrorRestoresACleanState() {
        val engine = CalculatorEngine()
        pressAll(engine, "1/0")
        engine.press(CalculatorKey.EQUALS)
        assertEquals(ErrorKind.DIVISION_BY_ZERO, engine.snapshot().errorKind)
        engine.press(CalculatorKey.CLEAR)
        val cleared = engine.snapshot()
        assertNull(cleared.errorKind)
        assertEquals("0", cleared.display)
        assertEquals("", cleared.expression)
        assertFalse(cleared.afterEquals)
    }
}
