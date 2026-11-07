package com.lkovari.mobile.apps.treecalc.engine

import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExactFractionTest {
    @Test
    fun repeatingAndTerminatingDisplays() {
        assertEquals("0.(3)", resultOf("1/3"))
        assertEquals("0.1(6)", resultOf("1/6"))
        assertEquals("0.(142857)", resultOf("1/7"))
        assertEquals("0.08(3)", resultOf("1/12"))
        assertEquals("0.125", resultOf("1/8"))
        assertEquals("-0.(3)", resultOf("0-1/3"))
        assertEquals("1.(3)", resultOf("1/3+1"))
        assertEquals("0.(09)", resultOf("1/11"))
        assertEquals("0.3", resultOf("0.1+0.2"))
    }

    @Test
    fun longPeriodFallsBackWithoutParentheses() {
        val engine = evaluate("1/17")
        assertNull(engine.snapshot().errorKind)
        assertFalse(engine.snapshot().display.contains("("))
        assertEquals(NumberFormatter.format(1.0 / 17.0, NumericBase.DECIMAL), engine.snapshot().display)
    }

    @Test
    fun signedPowersAndSquareRootStayHonest() {
        assertEquals("-4", resultOf("0-2^2"))
        assertEquals("25", resultOf("(0-5)^2"))
        assertEquals("3", resultOf("9r"))
        val noisy = resultOf("2r+2r")
        assertFalse(noisy.contains("("))
        assertFalse(noisy.contains("0.(0)"))
    }

    @Test
    fun baseSwitchRewritesEntryResultAndExpression() {
        val ten = CalculatorEngine()
        pressAll(ten, "10")
        ten.press(CalculatorKey.EQUALS)
        ten.setBase(NumericBase.HEXADECIMAL)
        assertEquals("A", ten.snapshot().display)
        ten.setBase(NumericBase.BINARY)
        assertEquals("1010", ten.snapshot().display)
        ten.setBase(NumericBase.DECIMAL)
        assertEquals("10", ten.snapshot().display)

        val half = evaluate("0.5")
        half.setBase(NumericBase.HEXADECIMAL)
        assertEquals("0.8", half.snapshot().display)
        half.setBase(NumericBase.BINARY)
        assertEquals("0.1", half.snapshot().display)
        half.setBase(NumericBase.OCTAL)
        assertEquals("0.4", half.snapshot().display)

        val third = evaluate("1/3")
        third.setBase(NumericBase.BINARY)
        assertEquals("0.(01)", third.snapshot().display)
        third.setBase(NumericBase.OCTAL)
        assertEquals("0.(25)", third.snapshot().display)
        third.setBase(NumericBase.HEXADECIMAL)
        assertEquals("0.(5)", third.snapshot().display)

        val tenth = evaluate("1/10")
        tenth.setBase(NumericBase.HEXADECIMAL)
        assertEquals("0.1(9)", tenth.snapshot().display)
    }

    @Test
    fun midEntryExpressionAndTrailingDotAndPi() {
        val engine = CalculatorEngine()
        pressAll(engine, "10+10")
        engine.setBase(NumericBase.HEXADECIMAL)
        assertEquals("A + A", engine.snapshot().expression)
        assertEquals("A", engine.snapshot().display)
        engine.press(CalculatorKey.EQUALS)
        assertEquals("14", engine.snapshot().display)

        val dotted = CalculatorEngine()
        pressAll(dotted, "12.")
        dotted.setBase(NumericBase.HEXADECIMAL)
        assertEquals("C.", dotted.snapshot().display)

        val pi = CalculatorEngine()
        pi.press(CalculatorKey.PI)
        pi.setBase(NumericBase.HEXADECIMAL)
        assertEquals("π", pi.snapshot().expression.trim())
        assertNull(pi.snapshot().errorKind)
    }

    @Test
    fun disabledDigitIsSilentAndDivisionErrorsStay() {
        val engine = CalculatorEngine()
        engine.setBase(NumericBase.BINARY)
        engine.press(CalculatorKey.DIGIT_2)
        assertEquals("0", engine.snapshot().display)
        assertNull(engine.snapshot().errorKind)

        val undefined = CalculatorEngine()
        pressAll(undefined, "0/0")
        undefined.press(CalculatorKey.EQUALS)
        assertEquals(ErrorKind.UNDEFINED, undefined.snapshot().errorKind)

        val division = CalculatorEngine()
        pressAll(division, "5/0")
        division.press(CalculatorKey.EQUALS)
        assertEquals(ErrorKind.DIVISION_BY_ZERO, division.snapshot().errorKind)

        val empty = CalculatorEngine()
        empty.press(CalculatorKey.EQUALS)
        assertEquals(ErrorKind.EMPTY_EXPRESSION, empty.snapshot().errorKind)
    }

    @Test
    fun powerBoundaryStaysExactThenBecomesApproximate() {
        val exact = evaluate("2^256")
        assertNull(exact.snapshot().errorKind)
        assertEquals(
            DecimalDisplay.scientificInteger(BigInteger.ONE.shiftLeft(256), 10, false),
            exact.snapshot().display
        )
        exact.press(CalculatorKey.ADD)
        exact.press(CalculatorKey.DIGIT_0)
        exact.press(CalculatorKey.EQUALS)
        assertNull(exact.snapshot().errorKind)
        assertEquals(
            DecimalDisplay.scientificInteger(BigInteger.ONE.shiftLeft(256), 10, false),
            exact.snapshot().display
        )

        val beyond = evaluate("2^257")
        assertNull(beyond.snapshot().errorKind)
        assertEquals(NumberFormatter.format(Math.pow(2.0, 257.0), NumericBase.DECIMAL), beyond.snapshot().display)
        assertFalse(beyond.snapshot().display.contains("("))
    }

    @Test
    fun memoryKeepsAnExactThird() {
        val engine = evaluate("1/3")
        engine.press(CalculatorKey.MEMORY_ADD)
        engine.press(CalculatorKey.CLEAR)
        engine.press(CalculatorKey.MEMORY_RECALL)
        engine.press(CalculatorKey.SUB)
        pressAll(engine, "1/6")
        engine.press(CalculatorKey.EQUALS)
        assertEquals("0.1(6)", engine.snapshot().display)
        engine.setBase(NumericBase.HEXADECIMAL)
        assertEquals("0.2(A)", engine.snapshot().display)
    }

    @Test
    fun hexAndBinaryWholeResults() {
        val hex = CalculatorEngine()
        hex.setBase(NumericBase.HEXADECIMAL)
        pressAll(hex, "FF+1")
        hex.press(CalculatorKey.EQUALS)
        assertEquals("100", hex.snapshot().display)

        val binary = CalculatorEngine()
        binary.setBase(NumericBase.BINARY)
        pressAll(binary, "11111111")
        binary.press(CalculatorKey.EQUALS)
        binary.setBase(NumericBase.DECIMAL)
        assertEquals("255", binary.snapshot().display)

        val negated = evaluate("1/3")
        negated.press(CalculatorKey.NEGATE)
        assertEquals("-0.(3)", negated.snapshot().display)
        negated.setBase(NumericBase.HEXADECIMAL)
        assertEquals("-0.(5)", negated.snapshot().display)
    }

    @Test
    fun parenthesizedLiteralRoundTrips() {
        val fraction = Fraction.fromLiteral("0.(3)", 10)
        assertEquals("0.(3)", fraction?.let { DecimalDisplay.format(it, NumericBase.DECIMAL) })
        val mixed = Fraction.fromLiteral("0.1(6)", 10)
        assertEquals("0.1(6)", mixed?.let { DecimalDisplay.format(it, NumericBase.DECIMAL) })
        val oneAndAHalf = Fraction.fromLiteral("1.5", 10)
        assertEquals("1.8", oneAndAHalf?.let { DecimalDisplay.format(it, NumericBase.HEXADECIMAL) })
    }

    private fun resultOf(expression: String): String {
        return evaluate(expression).snapshot().display
    }

    private fun evaluate(expression: String): CalculatorEngine {
        val engine = CalculatorEngine()
        for (character in expression) {
            when (character) {
                'r' -> engine.press(CalculatorKey.SQRT)
                else -> pressAll(engine, character.toString())
            }
        }
        engine.press(CalculatorKey.EQUALS)
        return engine
    }
}
