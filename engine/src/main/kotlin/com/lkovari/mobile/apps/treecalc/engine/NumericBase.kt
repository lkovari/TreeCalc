package com.lkovari.mobile.apps.treecalc.engine

enum class NumericBase(val radix: Int) {
    BINARY(2),
    OCTAL(8),
    DECIMAL(10),
    HEXADECIMAL(16);

    val integerOnly: Boolean
        get() = this != DECIMAL

    fun allowsKey(key: CalculatorKey): Boolean {
        val digit = key.digitValue()
        if (digit != null) {
            return digit < radix
        }
        return when (key) {
            CalculatorKey.DOT,
            CalculatorKey.PI,
            CalculatorKey.SIN,
            CalculatorKey.COS,
            CalculatorKey.TAN,
            CalculatorKey.LN,
            CalculatorKey.LOG,
            CalculatorKey.EXP,
            CalculatorKey.FACT -> this == DECIMAL
            else -> true
        }
    }
}

fun CalculatorKey.digitValue(): Int? {
    return when (this) {
        CalculatorKey.DIGIT_0 -> 0
        CalculatorKey.DIGIT_1 -> 1
        CalculatorKey.DIGIT_2 -> 2
        CalculatorKey.DIGIT_3 -> 3
        CalculatorKey.DIGIT_4 -> 4
        CalculatorKey.DIGIT_5 -> 5
        CalculatorKey.DIGIT_6 -> 6
        CalculatorKey.DIGIT_7 -> 7
        CalculatorKey.DIGIT_8 -> 8
        CalculatorKey.DIGIT_9 -> 9
        CalculatorKey.DIGIT_A -> 10
        CalculatorKey.DIGIT_B -> 11
        CalculatorKey.DIGIT_C -> 12
        CalculatorKey.DIGIT_D -> 13
        CalculatorKey.DIGIT_E -> 14
        CalculatorKey.DIGIT_F -> 15
        else -> null
    }
}
