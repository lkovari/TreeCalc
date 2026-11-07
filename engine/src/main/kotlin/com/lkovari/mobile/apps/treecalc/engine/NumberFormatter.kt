package com.lkovari.mobile.apps.treecalc.engine

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import kotlin.math.abs

object NumberFormatter {
    private const val PI_SYMBOL = "π"
    private const val SCIENTIFIC_UPPER = 1e15
    private const val SCIENTIFIC_LOWER = 1e-9

    fun parse(raw: String, base: NumericBase): Double {
        val trimmed = raw.trim()
        if (trimmed == PI_SYMBOL || trimmed.equals("PI", ignoreCase = true)) {
            return Math.PI
        }
        if (trimmed.isEmpty() || trimmed == "-" || trimmed == "." || trimmed == "-.") {
            throw CalculatorException(ErrorKind.MALFORMED_EXPRESSION)
        }
        val fraction = Fraction.fromLiteral(trimmed, base.radix)
        if (fraction != null) {
            return fraction.toDouble()
        }
        if (base == NumericBase.DECIMAL && trimmed.contains('e', ignoreCase = true)) {
            return try {
                trimmed.toDouble()
            } catch (exception: NumberFormatException) {
                throw CalculatorException(ErrorKind.MALFORMED_EXPRESSION)
            }
        }
        val approximate = approximateLiteral(trimmed, base.radix)
        if (approximate != null && approximate.isFinite()) {
            return approximate
        }
        throw CalculatorException(ErrorKind.MALFORMED_EXPRESSION)
    }

    fun format(value: Double, base: NumericBase): String {
        if (value.isNaN() || value.isInfinite()) {
            throw CalculatorException(ErrorKind.DOMAIN)
        }
        val magnitude = abs(value)
        if (base == NumericBase.DECIMAL) {
            if (magnitude >= SCIENTIFIC_UPPER || (magnitude > 0.0 && magnitude < SCIENTIFIC_LOWER)) {
                return scientific(value)
            }
            if (isWhole(value) && magnitude <= Long.MAX_VALUE.toDouble()) {
                return Math.rint(value).toLong().toString()
            }
            var text = BigDecimal.valueOf(value)
                .setScale(12, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString()
            if (text == "-0") {
                text = "0"
            }
            return text
        }
        if (magnitude >= SCIENTIFIC_UPPER || (magnitude > 0.0 && magnitude < SCIENTIFIC_LOWER)) {
            return scientific(value)
        }
        if (isWhole(value) && magnitude <= Long.MAX_VALUE.toDouble()) {
            return Math.rint(value).toLong().toString(base.radix).uppercase()
        }
        return approximate(value, base.radix)
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

    private fun scientific(value: Double): String {
        val text = String.format(Locale.US, "%.11e", value).lowercase(Locale.US)
        val parts = text.split('e')
        if (parts.size != 2) {
            return text
        }
        val exponent = parts[1].toIntOrNull() ?: return text
        var mantissa = parts[0]
        if (mantissa.contains('.')) {
            mantissa = mantissa.trimEnd('0').trimEnd('.')
        }
        return mantissa + "e" + exponent
    }

    private fun approximateLiteral(text: String, radix: Int): Double? {
        val negative = text.startsWith("-")
        val body = if (negative) text.substring(1) else text
        if (body.isEmpty()) {
            return null
        }
        val parts = body.split('.', limit = 3)
        if (parts.size > 2) {
            return null
        }
        val scale = radix.toDouble()
        var value = 0.0
        for (character in parts[0]) {
            val digit = hexDigit(character) ?: return null
            if (digit >= radix) {
                return null
            }
            value = value * scale + digit
        }
        if (parts.size == 2) {
            var place = 1.0
            for (character in parts[1]) {
                val digit = hexDigit(character) ?: return null
                if (digit >= radix) {
                    return null
                }
                place /= scale
                value += digit * place
            }
        }
        return if (negative) -value else value
    }

    private fun approximate(value: Double, radix: Int): String {
        val negative = value < 0.0
        val magnitude = abs(value)
        val whole = magnitude.toLong()
        var remainder = magnitude - whole.toDouble()
        val fractional = StringBuilder()
        for (ignored in 0 until 12) {
            remainder *= radix.toDouble()
            var digit = remainder.toInt()
            var next = remainder - digit.toDouble()
            if (next > 1.0 - 1e-9) {
                digit += 1
                next = 0.0
            }
            if (digit >= radix) {
                digit = radix - 1
                next = 0.0
            }
            if (digit < 0) {
                digit = 0
            }
            fractional.append("0123456789ABCDEF"[digit])
            remainder = next
            if (remainder < 1e-10) {
                break
            }
        }
        while (fractional.isNotEmpty() && fractional[fractional.length - 1] == '0') {
            fractional.deleteCharAt(fractional.length - 1)
        }
        val sign = if (negative) "-" else ""
        val head = whole.toString(radix).uppercase()
        if (fractional.isEmpty()) {
            return sign + head
        }
        return sign + head + "." + fractional
    }

    private fun hexDigit(character: Char): Int? {
        return when (character) {
            in '0'..'9' -> character - '0'
            in 'A'..'F' -> character - 'A' + 10
            in 'a'..'f' -> character - 'a' + 10
            else -> null
        }
    }
}
