package com.lkovari.mobile.apps.treecalc.engine

import java.math.BigInteger

data class Fraction(val num: BigInteger, val den: BigInteger) {
    fun negated(): Fraction? {
        return reduced(num.negate(), den)
    }

    fun added(other: Fraction): Fraction? {
        return reduced(
            num.multiply(other.den).add(other.num.multiply(den)),
            den.multiply(other.den)
        )
    }

    fun subtracted(other: Fraction): Fraction? {
        val flipped = other.negated() ?: return null
        return added(flipped)
    }

    fun multiplied(other: Fraction): Fraction? {
        return reduced(num.multiply(other.num), den.multiply(other.den))
    }

    fun divided(other: Fraction): Fraction? {
        if (other.num == BigInteger.ZERO) {
            return null
        }
        return reduced(num.multiply(other.den), den.multiply(other.num))
    }

    fun raisedTo(exponent: Int): Fraction? {
        if (exponent == 0) {
            return reduced(BigInteger.ONE, BigInteger.ONE)
        }
        if (exponent < 0) {
            if (num == BigInteger.ZERO) {
                return null
            }
            val positive = raisedTo(-exponent) ?: return null
            return reduced(positive.den, positive.num)
        }
        if (exponent > MAX_EXPONENT) {
            return null
        }
        var resultNum = BigInteger.ONE
        var resultDen = BigInteger.ONE
        var baseNum = num
        var baseDen = den
        var remaining = exponent
        while (remaining > 0) {
            if (remaining and 1 == 1) {
                resultNum = resultNum.multiply(baseNum)
                resultDen = resultDen.multiply(baseDen)
                if (resultNum.bitLength() > MAX_BITS || resultDen.bitLength() > MAX_BITS) {
                    return null
                }
            }
            remaining = remaining shr 1
            if (remaining > 0) {
                baseNum = baseNum.multiply(baseNum)
                baseDen = baseDen.multiply(baseDen)
                if (baseNum.bitLength() > MAX_BITS || baseDen.bitLength() > MAX_BITS) {
                    return null
                }
            }
        }
        return reduced(resultNum, resultDen)
    }

    fun toDouble(): Double {
        return num.toDouble() / den.toDouble()
    }

    companion object {
        const val MAX_EXPONENT = 256
        const val MAX_BITS = 4096

        fun reduced(num: BigInteger, den: BigInteger): Fraction? {
            if (den == BigInteger.ZERO) {
                return null
            }
            var nextNum = num
            var nextDen = den
            if (nextDen.signum() < 0) {
                nextNum = nextNum.negate()
                nextDen = nextDen.negate()
            }
            val divisor = nextNum.gcd(nextDen)
            if (divisor == BigInteger.ZERO) {
                return null
            }
            return Fraction(nextNum.divide(divisor), nextDen.divide(divisor))
        }

        fun fromLiteral(raw: String, radix: Int): Fraction? {
            if (radix < 2 || radix > 16) {
                return null
            }
            val text = raw.trim()
            if (text == NumberFormatter.piSymbol() || text.equals("pi", ignoreCase = true)) {
                return null
            }
            if (text.isEmpty() || text == "-" || text == "." || text == "-.") {
                return null
            }
            if (radix == 10 && text.contains('e', ignoreCase = true)) {
                return null
            }
            val negative = text.startsWith("-")
            val body = if (negative) text.substring(1) else text
            if (body.isEmpty() || body.contains("-")) {
                return null
            }
            val open = body.indexOf('(')
            val close = body.lastIndexOf(')')
            var main = body
            var period = ""
            if (open >= 0 || close >= 0) {
                if (open < 0 || close != body.length - 1 || open >= close) {
                    return null
                }
                if (body.indexOf('(', open + 1) >= 0) {
                    return null
                }
                period = body.substring(open + 1, close)
                main = body.substring(0, open)
                if (period.isEmpty() || !main.contains('.')) {
                    return null
                }
            }
            val parts = main.split('.', limit = 3)
            if (parts.size > 2) {
                return null
            }
            val whole = parts[0]
            val frac = if (parts.size == 2) parts[1] else ""
            val wholeN = parseDigits(whole, radix) ?: return null
            val fracN = parseDigits(frac, radix) ?: return null
            val periodN = parseDigits(period, radix) ?: return null
            val scale = BigInteger.valueOf(radix.toLong()).pow(frac.length)
            var value = reduced(wholeN, BigInteger.ONE)?.added(reduced(fracN, scale) ?: return null) ?: return null
            if (period.isNotEmpty()) {
                val cycle = BigInteger.valueOf(radix.toLong()).pow(period.length)
                if (cycle <= BigInteger.ONE) {
                    return null
                }
                val repeatPart = reduced(periodN, cycle.subtract(BigInteger.ONE))
                    ?.divided(reduced(scale, BigInteger.ONE) ?: return null)
                    ?: return null
                value = value.added(repeatPart) ?: return null
            }
            if (whole.isEmpty() && frac.isEmpty() && period.isEmpty()) {
                return null
            }
            return if (negative) value.negated() else value
        }

        private fun parseDigits(text: String, radix: Int): BigInteger? {
            if (text.isEmpty()) {
                return BigInteger.ZERO
            }
            var value = BigInteger.ZERO
            val base = BigInteger.valueOf(radix.toLong())
            for (character in text) {
                val digit = digitValue(character) ?: return null
                if (digit >= radix) {
                    return null
                }
                value = value.multiply(base).add(BigInteger.valueOf(digit.toLong()))
            }
            return value
        }

        private fun digitValue(character: Char): Int? {
            return when (character) {
                in '0'..'9' -> character - '0'
                in 'A'..'F' -> character - 'A' + 10
                in 'a'..'f' -> character - 'a' + 10
                else -> null
            }
        }
    }
}
