package com.lkovari.mobile.apps.treecalc.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NumberFormatterTest {
    @Test
    fun parseDecimalAndPi() {
        assertEquals(12.5, NumberFormatter.parse("12.5", NumericBase.DECIMAL), 0.0)
        assertEquals(Math.PI, NumberFormatter.parse("π", NumericBase.DECIMAL), 0.0)
        assertEquals(Math.PI, NumberFormatter.parse("PI", NumericBase.DECIMAL), 0.0)
        assertEquals(Math.PI, NumberFormatter.parse("pi", NumericBase.DECIMAL), 0.0)
        assertEquals(-3.0, NumberFormatter.parse("-3", NumericBase.DECIMAL), 0.0)
    }

    @Test
    fun parseIntegerBases() {
        assertEquals(10.0, NumberFormatter.parse("1010", NumericBase.BINARY), 0.0)
        assertEquals(8.0, NumberFormatter.parse("10", NumericBase.OCTAL), 0.0)
        assertEquals(255.0, NumberFormatter.parse("FF", NumericBase.HEXADECIMAL), 0.0)
        assertEquals(10.0, NumberFormatter.parse("A", NumericBase.HEXADECIMAL), 0.0)
    }

    @Test
    fun parseRejectsEmptyAndGarbage() {
        assertCalculatorError(ErrorKind.MALFORMED_EXPRESSION) {
            NumberFormatter.parse("", NumericBase.DECIMAL)
        }
        assertCalculatorError(ErrorKind.MALFORMED_EXPRESSION) {
            NumberFormatter.parse("-", NumericBase.DECIMAL)
        }
        assertCalculatorError(ErrorKind.MALFORMED_EXPRESSION) {
            NumberFormatter.parse(".", NumericBase.DECIMAL)
        }
        assertCalculatorError(ErrorKind.MALFORMED_EXPRESSION) {
            NumberFormatter.parse("xyz", NumericBase.DECIMAL)
        }
        assertCalculatorError(ErrorKind.MALFORMED_EXPRESSION) {
            NumberFormatter.parse("2", NumericBase.BINARY)
        }
    }

    @Test
    fun formatHidesBinaryFloatingPointNoise() {
        assertEquals("0.3", NumberFormatter.format(0.1 + 0.2, NumericBase.DECIMAL))
        assertEquals("0.3", NumberFormatter.format(0.1 + 0.1 + 0.1, NumericBase.DECIMAL))
    }

    @Test
    fun formatDecimalStripsTrailingZeros() {
        assertEquals("12", NumberFormatter.format(12.0, NumericBase.DECIMAL))
        assertEquals("1.5", NumberFormatter.format(1.5, NumericBase.DECIMAL))
        assertEquals("0", NumberFormatter.format(-0.0, NumericBase.DECIMAL))
        assertEquals("-3", NumberFormatter.format(-3.0, NumericBase.DECIMAL))
    }

    @Test
    fun formatIntegerBases() {
        assertEquals("1010", NumberFormatter.format(10.0, NumericBase.BINARY))
        assertEquals("10", NumberFormatter.format(8.0, NumericBase.OCTAL))
        assertEquals("F", NumberFormatter.format(15.0, NumericBase.HEXADECIMAL))
        assertEquals("A", NumberFormatter.format(10.0, NumericBase.HEXADECIMAL))
        assertEquals("2", NumberFormatter.format(1.5, NumericBase.HEXADECIMAL))
    }

    @Test
    fun formatRejectsNonFinite() {
        assertCalculatorError(ErrorKind.DOMAIN) {
            NumberFormatter.format(Double.NaN, NumericBase.DECIMAL)
        }
        assertCalculatorError(ErrorKind.DOMAIN) {
            NumberFormatter.format(Double.POSITIVE_INFINITY, NumericBase.DECIMAL)
        }
    }

    @Test
    fun validDigitsFollowTheBase() {
        assertTrue(NumberFormatter.isValidDigit('0', NumericBase.BINARY))
        assertTrue(NumberFormatter.isValidDigit('1', NumericBase.BINARY))
        assertFalse(NumberFormatter.isValidDigit('2', NumericBase.BINARY))
        assertTrue(NumberFormatter.isValidDigit('7', NumericBase.OCTAL))
        assertFalse(NumberFormatter.isValidDigit('8', NumericBase.OCTAL))
        assertTrue(NumberFormatter.isValidDigit('9', NumericBase.DECIMAL))
        assertFalse(NumberFormatter.isValidDigit('A', NumericBase.DECIMAL))
        assertTrue(NumberFormatter.isValidDigit('a', NumericBase.HEXADECIMAL))
        assertTrue(NumberFormatter.isValidDigit('F', NumericBase.HEXADECIMAL))
        assertFalse(NumberFormatter.isValidDigit('G', NumericBase.HEXADECIMAL))
    }

    @Test
    fun wholeNumberTolerance() {
        assertTrue(NumberFormatter.isWhole(4.0))
        assertTrue(NumberFormatter.isWhole(4.0000000001))
        assertFalse(NumberFormatter.isWhole(4.5))
    }

    @Test
    fun piSymbolIsConstant() {
        assertEquals("π", NumberFormatter.piSymbol())
    }
}
