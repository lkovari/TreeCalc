package com.lkovari.mobile.apps.treecalc.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculatorEngineMathFixesTest {
    @Test
    fun minusAfterTimesIsUnary() {
        val engine = CalculatorEngine()
        pressAll(engine, "5*-2")
        engine.press(CalculatorKey.EQUALS)
        assertNull(engine.snapshot().errorKind)
        assertEquals("-10", engine.snapshot().display)
    }

    @Test
    fun minusAfterPlusTimesIsUnary() {
        val engine = CalculatorEngine()
        pressAll(engine, "5+3*-2")
        engine.press(CalculatorKey.EQUALS)
        assertNull(engine.snapshot().errorKind)
        assertEquals("-1", engine.snapshot().display)
    }

    @Test
    fun negateBeforeParenthesesAppliesToTheGroup() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.NEGATE)
        pressAll(engine, "(5+3)*2")
        engine.press(CalculatorKey.EQUALS)
        assertNull(engine.snapshot().errorKind)
        assertEquals("-16", engine.snapshot().display)
    }

    @Test
    fun unaryMinusBindsLooserThanPower() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.DIGIT_5)
        engine.press(CalculatorKey.NEGATE)
        engine.press(CalculatorKey.POW)
        engine.press(CalculatorKey.DIGIT_2)
        engine.press(CalculatorKey.EQUALS)
        assertNull(engine.snapshot().errorKind)
        assertEquals("-25", engine.snapshot().display)
    }

    @Test
    fun parenthesizedNegativeNumberSquaredIsPositive() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.LEFT_PAREN)
        engine.press(CalculatorKey.DIGIT_5)
        engine.press(CalculatorKey.NEGATE)
        engine.press(CalculatorKey.RIGHT_PAREN)
        engine.press(CalculatorKey.POW)
        engine.press(CalculatorKey.DIGIT_2)
        engine.press(CalculatorKey.EQUALS)
        assertNull(engine.snapshot().errorKind)
        assertEquals("25", engine.snapshot().display)
    }

    @Test
    fun leadingMinusThenPowerIsNegative() {
        val engine = CalculatorEngine()
        pressAll(engine, "-2^2")
        engine.press(CalculatorKey.EQUALS)
        assertNull(engine.snapshot().errorKind)
        assertEquals("-4", engine.snapshot().display)
    }

    @Test
    fun sinOfNinetyDegreesIsOne() {
        val engine = CalculatorEngine()
        pressAll(engine, "90")
        engine.press(CalculatorKey.SIN)
        engine.press(CalculatorKey.EQUALS)
        assertNull(engine.snapshot().errorKind)
        assertEquals("1", engine.snapshot().display)
        assertEquals(AngleMode.DEGREES, engine.snapshot().angleMode)
    }

    @Test
    fun sinOfPiOverTwoInRadiansIsOne() {
        val engine = CalculatorEngine()
        engine.press(CalculatorKey.ANGLE_MODE)
        assertEquals(AngleMode.RADIANS, engine.snapshot().angleMode)
        engine.press(CalculatorKey.PI)
        engine.press(CalculatorKey.DIV)
        engine.press(CalculatorKey.DIGIT_2)
        engine.press(CalculatorKey.SIN)
        engine.press(CalculatorKey.EQUALS)
        assertNull(engine.snapshot().errorKind)
        assertEquals(1.0, engine.snapshot().display.toDouble(), 1e-9)
    }

    @Test
    fun pointOnePlusPointTwoFormatsAsPointThree() {
        val engine = CalculatorEngine()
        pressAll(engine, "0.1+0.2")
        engine.press(CalculatorKey.EQUALS)
        assertEquals("0.3", engine.snapshot().display)
    }

    @Test
    fun extraDecimalPointIsAnError() {
        val engine = CalculatorEngine()
        pressAll(engine, "5.5.5")
        engine.press(CalculatorKey.EQUALS)
        assertEquals(ErrorKind.MALFORMED_EXPRESSION, engine.snapshot().errorKind)
    }

    @Test
    fun zeroDividedByZeroIsUndefined() {
        val engine = CalculatorEngine()
        pressAll(engine, "0/0")
        engine.press(CalculatorKey.EQUALS)
        assertEquals(ErrorKind.UNDEFINED, engine.snapshot().errorKind)
    }

    @Test
    fun nonzeroDividedByZeroStaysDivisionByZero() {
        val engine = CalculatorEngine()
        pressAll(engine, "5/0")
        engine.press(CalculatorKey.EQUALS)
        assertEquals(ErrorKind.DIVISION_BY_ZERO, engine.snapshot().errorKind)
    }
}
