package com.lkovari.mobile.apps.treecalc.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class OperationsTest {
    @Test
    fun arithmeticBinaryOps() {
        assertEquals(9.0, Operations.executeBinary(4.0, 5.0, OperatorKind.ADD), 0.0)
        assertEquals(-1.0, Operations.executeBinary(4.0, 5.0, OperatorKind.SUB), 0.0)
        assertEquals(20.0, Operations.executeBinary(4.0, 5.0, OperatorKind.MUL), 0.0)
        assertEquals(2.0, Operations.executeBinary(10.0, 5.0, OperatorKind.DIV), 0.0)
        assertEquals(1.0, Operations.executeBinary(10.0, 3.0, OperatorKind.MOD), 0.0)
        assertEquals(8.0, Operations.executeBinary(2.0, 3.0, OperatorKind.POW), 0.0)
    }

    @Test
    fun bitwiseBinaryOps() {
        assertEquals(1.0, Operations.executeBinary(5.0, 3.0, OperatorKind.AND), 0.0)
        assertEquals(7.0, Operations.executeBinary(5.0, 3.0, OperatorKind.OR), 0.0)
        assertEquals(6.0, Operations.executeBinary(5.0, 3.0, OperatorKind.XOR), 0.0)
        assertEquals(12.0, Operations.executeBinary(3.0, 2.0, OperatorKind.LSH), 0.0)
    }

    @Test
    fun unaryOps() {
        assertEquals(-4.0, Operations.executeUnary(4.0, OperatorKind.NEG), 0.0)
        assertEquals(-1.0, Operations.executeUnary(0.0, OperatorKind.NOT), 0.0)
        assertEquals(-2.0, Operations.executeUnary(1.0, OperatorKind.NOT), 0.0)
        assertEquals(3.0, Operations.executeUnary(9.0, OperatorKind.SQRT), 0.0)
        assertEquals(0.25, Operations.executeUnary(4.0, OperatorKind.REC), 0.0)
        assertEquals(0.0, Operations.executeUnary(0.0, OperatorKind.SIN), 1e-12)
        assertEquals(1.0, Operations.executeUnary(0.0, OperatorKind.COS), 1e-12)
        assertEquals(0.0, Operations.executeUnary(0.0, OperatorKind.TAN), 1e-12)
        assertEquals(1.0, Operations.executeUnary(Math.E, OperatorKind.LN), 1e-12)
        assertEquals(2.0, Operations.executeUnary(100.0, OperatorKind.LOG), 1e-12)
        assertEquals(Math.E, Operations.executeUnary(1.0, OperatorKind.EXP), 1e-12)
        assertEquals(25.0, Operations.executeUnary(5.0, OperatorKind.SQ), 0.0)
        assertEquals(27.0, Operations.executeUnary(3.0, OperatorKind.CUBE), 0.0)
        assertEquals(1.0, Operations.executeUnary(0.0, OperatorKind.FACT), 0.0)
        assertEquals(1.0, Operations.executeUnary(1.0, OperatorKind.FACT), 0.0)
        assertEquals(120.0, Operations.executeUnary(5.0, OperatorKind.FACT), 0.0)
    }

    @Test
    fun divisionAndModuloByZero() {
        assertCalculatorError(ErrorKind.DIVISION_BY_ZERO) {
            Operations.executeBinary(8.0, 0.0, OperatorKind.DIV)
        }
        assertCalculatorError(ErrorKind.DIVISION_BY_ZERO) {
            Operations.executeBinary(8.0, 0.0, OperatorKind.MOD)
        }
        assertCalculatorError(ErrorKind.DIVISION_BY_ZERO) {
            Operations.executeUnary(0.0, OperatorKind.REC)
        }
    }

    @Test
    fun domainErrors() {
        assertCalculatorError(ErrorKind.DOMAIN) {
            Operations.executeUnary(-1.0, OperatorKind.SQRT)
        }
        assertCalculatorError(ErrorKind.DOMAIN) {
            Operations.executeUnary(0.0, OperatorKind.LN)
        }
        assertCalculatorError(ErrorKind.DOMAIN) {
            Operations.executeUnary(-2.0, OperatorKind.LN)
        }
        assertCalculatorError(ErrorKind.DOMAIN) {
            Operations.executeUnary(0.0, OperatorKind.LOG)
        }
        assertCalculatorError(ErrorKind.DOMAIN) {
            Operations.executeUnary(-2.0, OperatorKind.LOG)
        }
        assertCalculatorError(ErrorKind.DOMAIN) {
            Operations.executeBinary(-1.0, 0.5, OperatorKind.POW)
        }
        assertCalculatorError(ErrorKind.DOMAIN) {
            Operations.executeBinary(1.0, -1.0, OperatorKind.LSH)
        }
        assertCalculatorError(ErrorKind.DOMAIN) {
            Operations.executeBinary(1.0, 32.0, OperatorKind.LSH)
        }
        assertCalculatorError(ErrorKind.DOMAIN) {
            Operations.executeUnary(171.0, OperatorKind.FACT)
        }
        assertCalculatorError(ErrorKind.DOMAIN) {
            Operations.executeBinary(Int.MAX_VALUE.toDouble() + 1.0, 1.0, OperatorKind.AND)
        }
    }

    @Test
    fun bitwiseRequiresIntegers() {
        assertCalculatorError(ErrorKind.BITWISE_NON_INTEGER) {
            Operations.executeBinary(1.5, 1.0, OperatorKind.AND)
        }
        assertCalculatorError(ErrorKind.BITWISE_NON_INTEGER) {
            Operations.executeBinary(1.0, 2.5, OperatorKind.OR)
        }
        assertCalculatorError(ErrorKind.BITWISE_NON_INTEGER) {
            Operations.executeUnary(1.5, OperatorKind.NOT)
        }
    }

    @Test
    fun factorialRequiresNonNegativeWholeNumber() {
        assertCalculatorError(ErrorKind.INVALID_FACTORIAL) {
            Operations.executeUnary(-1.0, OperatorKind.FACT)
        }
        assertCalculatorError(ErrorKind.INVALID_FACTORIAL) {
            Operations.executeUnary(1.5, OperatorKind.FACT)
        }
    }

    @Test
    fun arityMismatchIsMalformed() {
        assertCalculatorError(ErrorKind.MALFORMED_EXPRESSION) {
            Operations.executeBinary(1.0, 2.0, OperatorKind.SQRT)
        }
        assertCalculatorError(ErrorKind.MALFORMED_EXPRESSION) {
            Operations.executeUnary(1.0, OperatorKind.ADD)
        }
    }
}
