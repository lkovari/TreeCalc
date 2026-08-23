package com.lkovari.mobile.apps.treecalc.engine

import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.roundToLong

object NumberFormatter {
    private const val PI_SYMBOL = "π"

    fun parse(raw: String, base: NumericBase): Double {
        val trimmed = raw.trim()
        if (trimmed == PI_SYMBOL || trimmed.equals("PI", ignoreCase = true)) {
            return Math.PI
        }
        if (trimmed.isEmpty() || trimmed == "-" || trimmed == "." || trimmed == "-.") {
            throw CalculatorException(ErrorKind.MALFORMED_EXPRESSION)
        }
        return try {
            if (base == NumericBase.DECIMAL) {
                trimmed.toDouble()
            } else {
                trimmed.toLong(base.radix).toDouble()
            }
        } catch (exception: NumberFormatException) {
            throw CalculatorException(ErrorKind.MALFORMED_EXPRESSION)
        }
    }

    fun format(value: Double, base: NumericBase): String {
        if (value.isNaN() || value.isInfinite()) {
            throw CalculatorException(ErrorKind.DOMAIN)
        }
        if (base == NumericBase.DECIMAL) {
            if (isWhole(value) && abs(value) <= Long.MAX_VALUE.toDouble()) {
                return Math.rint(value).toLong().toString()
            }
            var text = value.toBigDecimal()
                .setScale(12, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString()
            if (text == "-0") {
                text = "0"
            }
            return text
        }
        val asLong = if (isWhole(value)) {
            value.toLong()
        } else {
            value.roundToLong()
        }
        return asLong.toString(base.radix).uppercase()
    }

    fun isValidDigit(character: Char, base: NumericBase): Boolean {
        val upper = character.uppercaseChar()
        return when (base) {
            NumericBase.BINARY -> upper in '0'..'1'
            NumericBase.OCTAL -> upper in '0'..'7'
            NumericBase.DECIMAL -> upper in '0'..'9'
            NumericBase.HEXADECIMAL -> upper in '0'..'9' || upper in 'A'..'F'
        }
    }

    fun isWhole(value: Double): Boolean {
        return abs(value - Math.rint(value)) < 1e-9
    }

    fun piSymbol(): String {
        return PI_SYMBOL
    }
}
