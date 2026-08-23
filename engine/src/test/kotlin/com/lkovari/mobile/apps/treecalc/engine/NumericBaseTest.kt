package com.lkovari.mobile.apps.treecalc.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NumericBaseTest {
    @Test
    fun radicesMatchTheNamedBases() {
        assertEquals(2, NumericBase.BINARY.radix)
        assertEquals(8, NumericBase.OCTAL.radix)
        assertEquals(10, NumericBase.DECIMAL.radix)
        assertEquals(16, NumericBase.HEXADECIMAL.radix)
    }

    @Test
    fun onlyDecimalAllowsFractionsAndScientificKeys() {
        assertFalse(NumericBase.DECIMAL.integerOnly)
        assertTrue(NumericBase.BINARY.integerOnly)
        assertTrue(NumericBase.OCTAL.integerOnly)
        assertTrue(NumericBase.HEXADECIMAL.integerOnly)

        val scientific = listOf(
            CalculatorKey.DOT,
            CalculatorKey.PI,
            CalculatorKey.SIN,
            CalculatorKey.COS,
            CalculatorKey.TAN,
            CalculatorKey.LN,
            CalculatorKey.LOG,
            CalculatorKey.EXP,
            CalculatorKey.FACT
        )
        for (key in scientific) {
            assertTrue(NumericBase.DECIMAL.allowsKey(key))
            assertFalse(NumericBase.BINARY.allowsKey(key))
            assertFalse(NumericBase.OCTAL.allowsKey(key))
            assertFalse(NumericBase.HEXADECIMAL.allowsKey(key))
        }
    }

    @Test
    fun digitAvailabilityFollowsRadix() {
        assertTrue(NumericBase.BINARY.allowsKey(CalculatorKey.DIGIT_0))
        assertTrue(NumericBase.BINARY.allowsKey(CalculatorKey.DIGIT_1))
        assertFalse(NumericBase.BINARY.allowsKey(CalculatorKey.DIGIT_2))

        assertTrue(NumericBase.OCTAL.allowsKey(CalculatorKey.DIGIT_7))
        assertFalse(NumericBase.OCTAL.allowsKey(CalculatorKey.DIGIT_8))

        assertTrue(NumericBase.DECIMAL.allowsKey(CalculatorKey.DIGIT_9))
        assertFalse(NumericBase.DECIMAL.allowsKey(CalculatorKey.DIGIT_A))

        assertTrue(NumericBase.HEXADECIMAL.allowsKey(CalculatorKey.DIGIT_F))
        assertTrue(NumericBase.HEXADECIMAL.allowsKey(CalculatorKey.DIGIT_A))
    }

    @Test
    fun operatorsStayEnabledInEveryBase() {
        val alwaysOn = listOf(
            CalculatorKey.ADD,
            CalculatorKey.SUB,
            CalculatorKey.MUL,
            CalculatorKey.DIV,
            CalculatorKey.AND,
            CalculatorKey.OR,
            CalculatorKey.XOR,
            CalculatorKey.NOT,
            CalculatorKey.LSH,
            CalculatorKey.EQUALS,
            CalculatorKey.CLEAR,
            CalculatorKey.BACKSPACE,
            CalculatorKey.MEMORY_ADD
        )
        for (base in NumericBase.entries) {
            for (key in alwaysOn) {
                assertTrue("$key should be enabled in $base", base.allowsKey(key))
            }
        }
    }

    @Test
    fun digitValueMapsHexLetters() {
        assertEquals(0, CalculatorKey.DIGIT_0.digitValue())
        assertEquals(9, CalculatorKey.DIGIT_9.digitValue())
        assertEquals(10, CalculatorKey.DIGIT_A.digitValue())
        assertEquals(15, CalculatorKey.DIGIT_F.digitValue())
        assertEquals(null, CalculatorKey.ADD.digitValue())
    }
}
