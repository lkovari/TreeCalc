package com.lkovari.mobile.apps.treecalc.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.fail

internal fun pressAll(engine: CalculatorEngine, expression: String) {
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
            'A', 'a' -> engine.press(CalculatorKey.DIGIT_A)
            'B', 'b' -> engine.press(CalculatorKey.DIGIT_B)
            'C', 'c' -> engine.press(CalculatorKey.DIGIT_C)
            'D', 'd' -> engine.press(CalculatorKey.DIGIT_D)
            'E', 'e' -> engine.press(CalculatorKey.DIGIT_E)
            'F', 'f' -> engine.press(CalculatorKey.DIGIT_F)
            '+' -> engine.press(CalculatorKey.ADD)
            '-' -> engine.press(CalculatorKey.SUB)
            '*' -> engine.press(CalculatorKey.MUL)
            '/' -> engine.press(CalculatorKey.DIV)
            '(' -> engine.press(CalculatorKey.LEFT_PAREN)
            ')' -> engine.press(CalculatorKey.RIGHT_PAREN)
            '.' -> engine.press(CalculatorKey.DOT)
            '^' -> engine.press(CalculatorKey.POW)
        }
    }
}

internal fun assertCalculatorError(kind: ErrorKind, block: () -> Unit) {
    try {
        block()
        fail("expected CalculatorException with $kind")
    } catch (exception: CalculatorException) {
        assertEquals(kind, exception.kind)
    }
}
