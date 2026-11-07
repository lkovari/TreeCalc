package com.lkovari.mobile.apps.treecalc.engine

import java.math.BigInteger

sealed class DecimalKind {
    data class Terminating(val decimals: Int) : DecimalKind()
    data class Repeating(val preperiod: Int, val period: Int?) : DecimalKind()
}

object DecimalDisplay {
    private const val ALPHABET = "0123456789ABCDEF"
    const val MAX_FRACTION_DIGITS = 12
    const val MAX_INTEGER_DIGITS = 64

    fun classify(fraction: Fraction, radix: Int, maxPeriod: Int = 64): DecimalKind {
        val reduced = Fraction.reduced(fraction.num, fraction.den) ?: return DecimalKind.Repeating(0, null)
        val base = BigInteger.valueOf(radix.toLong())
        var q = reduced.den
        var pre = 0
        while (true) {
            val shared = q.gcd(base)
            if (shared <= BigInteger.ONE) {
                break
            }
            q = q.divide(shared)
            pre += 1
            if (pre > 128) {
                return DecimalKind.Repeating(pre, null)
            }
        }
        if (q == BigInteger.ONE) {
            return DecimalKind.Terminating(pre)
        }
        if (radix <= 1) {
            return DecimalKind.Repeating(pre, null)
        }
        var power = base.mod(q)
        if (power == BigInteger.ZERO) {
            return DecimalKind.Terminating(pre)
        }
        var length = 1
        while (power != BigInteger.ONE) {
            if (length >= maxPeriod) {
                return DecimalKind.Repeating(pre, null)
            }
            power = power.multiply(base).mod(q)
            length += 1
        }
        return DecimalKind.Repeating(pre, length)
    }

    fun format(fraction: Fraction, base: NumericBase, maxFractionDigits: Int = MAX_FRACTION_DIGITS): String? {
        val reduced = Fraction.reduced(fraction.num, fraction.den) ?: return null
        return when (val kind = classify(reduced, base.radix)) {
            is DecimalKind.Terminating -> digits(reduced, base.radix, kind.decimals, 0, false)
            is DecimalKind.Repeating -> {
                val period = kind.period ?: return null
                if (kind.preperiod + period > maxFractionDigits) {
                    return null
                }
                digits(reduced, base.radix, kind.preperiod, period, true)
            }
        }
    }

    fun literal(fraction: Fraction, base: NumericBase): String? {
        val reduced = Fraction.reduced(fraction.num, fraction.den) ?: return null
        if (reduced.den == BigInteger.ONE) {
            val negative = reduced.num.signum() < 0
            val digits = reduced.num.abs().toString(base.radix).uppercase()
            if (digits.length <= 4096) {
                return sign(negative) + digits
            }
        }
        return format(reduced, base)
    }

    fun present(value: Double, base: NumericBase, fraction: Fraction?): String {
        if (fraction != null) {
            val pretty = format(fraction, base)
            if (pretty != null) {
                return pretty
            }
        }
        return try {
            NumberFormatter.format(value, base)
        } catch (exception: CalculatorException) {
            "—"
        }
    }

    fun scientificInteger(magnitude: BigInteger, radix: Int, negative: Boolean): String {
        val digits = magnitude.abs().toString(radix).uppercase()
        if (digits.length <= 1) {
            return sign(negative) + digits
        }
        val body = digits.substring(1).trimEnd('0').take(11).trimEnd('0')
        val mantissa = if (body.isEmpty()) {
            digits.substring(0, 1)
        } else {
            digits.substring(0, 1) + "." + body
        }
        return sign(negative) + mantissa + "e" + (digits.length - 1)
    }

    private fun digits(
        fraction: Fraction,
        radix: Int,
        preperiod: Int,
        period: Int,
        repeating: Boolean
    ): String? {
        if (fraction.den <= BigInteger.ZERO || radix < 2 || radix > 16) {
            return null
        }
        val negative = fraction.num.signum() < 0
        val absNum = fraction.num.abs()
        val den = fraction.den
        val integer = absNum.divide(den)
        var remainder = absNum.remainder(den)
        val count = if (repeating) preperiod + period else preperiod
        val fractional = StringBuilder()
        val base = BigInteger.valueOf(radix.toLong())
        for (ignored in 0 until count) {
            val step = remainder.multiply(base)
            val digit = step.divide(den).toInt()
            if (digit < 0 || digit >= radix) {
                return null
            }
            fractional.append(ALPHABET[digit])
            remainder = step.remainder(den)
        }
        if (!repeating) {
            while (fractional.isNotEmpty() && fractional[fractional.length - 1] == '0') {
                fractional.deleteCharAt(fractional.length - 1)
            }
        }
        val whole = integer.toString(radix).uppercase()
        if (fractional.isEmpty()) {
            if (whole.length > MAX_INTEGER_DIGITS) {
                return scientificInteger(integer, radix, negative)
            }
            return sign(negative) + whole
        }
        if (repeating && period > 0) {
            val prefix = fractional.substring(0, preperiod.coerceAtMost(fractional.length))
            val cycle = fractional.substring(preperiod.coerceAtMost(fractional.length))
            return sign(negative) + whole + "." + prefix + "(" + cycle + ")"
        }
        return sign(negative) + whole + "." + fractional
    }

    private fun sign(negative: Boolean): String {
        return if (negative) "-" else ""
    }
}
