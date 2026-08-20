package com.lkovari.mobile.apps.treecalc.engine

object Operations {
    fun executeBinary(left: Double, right: Double, kind: OperatorKind): Double {
        return when (kind) {
            OperatorKind.ADD -> left + right
            OperatorKind.SUB -> left - right
            OperatorKind.MUL -> left * right
            OperatorKind.DIV -> {
                if (right == 0.0) {
                    throw CalculatorException(ErrorKind.DIVISION_BY_ZERO)
                }
                left / right
            }
            OperatorKind.MOD -> {
                if (right == 0.0) {
                    throw CalculatorException(ErrorKind.DIVISION_BY_ZERO)
                }
                left % right
            }
            OperatorKind.POW -> finite(Math.pow(left, right))
            OperatorKind.AND -> (toInt32(left) and toInt32(right)).toDouble()
            OperatorKind.OR -> (toInt32(left) or toInt32(right)).toDouble()
            OperatorKind.XOR -> (toInt32(left) xor toInt32(right)).toDouble()
            OperatorKind.LSH -> {
                val shift = toInt32(right)
                if (shift < 0 || shift > 31) {
                    throw CalculatorException(ErrorKind.DOMAIN)
                }
                (toInt32(left) shl shift).toDouble()
            }
            OperatorKind.NOT,
            OperatorKind.NEG,
            OperatorKind.SQRT,
            OperatorKind.REC,
            OperatorKind.SIN,
            OperatorKind.COS,
            OperatorKind.TAN,
            OperatorKind.LN,
            OperatorKind.LOG,
            OperatorKind.EXP,
            OperatorKind.SQ,
            OperatorKind.CUBE,
            OperatorKind.FACT -> throw CalculatorException(ErrorKind.MALFORMED_EXPRESSION)
        }
    }

    fun executeUnary(operand: Double, kind: OperatorKind): Double {
        return when (kind) {
            OperatorKind.NEG -> -operand
            OperatorKind.NOT -> toInt32(operand).inv().toDouble()
            OperatorKind.SQRT -> {
                if (operand < 0.0) {
                    throw CalculatorException(ErrorKind.DOMAIN)
                }
                Math.sqrt(operand)
            }
            OperatorKind.REC -> {
                if (operand == 0.0) {
                    throw CalculatorException(ErrorKind.DIVISION_BY_ZERO)
                }
                1.0 / operand
            }
            OperatorKind.SIN -> Math.sin(operand)
            OperatorKind.COS -> Math.cos(operand)
            OperatorKind.TAN -> finite(Math.tan(operand))
            OperatorKind.LN -> {
                if (operand <= 0.0) {
                    throw CalculatorException(ErrorKind.DOMAIN)
                }
                Math.log(operand)
            }
            OperatorKind.LOG -> {
                if (operand <= 0.0) {
                    throw CalculatorException(ErrorKind.DOMAIN)
                }
                Math.log10(operand)
            }
            OperatorKind.EXP -> finite(Math.exp(operand))
            OperatorKind.SQ -> operand * operand
            OperatorKind.CUBE -> operand * operand * operand
            OperatorKind.FACT -> factorial(operand)
            OperatorKind.ADD,
            OperatorKind.SUB,
            OperatorKind.MUL,
            OperatorKind.DIV,
            OperatorKind.MOD,
            OperatorKind.POW,
            OperatorKind.AND,
            OperatorKind.OR,
            OperatorKind.XOR,
            OperatorKind.LSH -> throw CalculatorException(ErrorKind.MALFORMED_EXPRESSION)
        }
    }

    private fun finite(value: Double): Double {
        if (value.isNaN() || value.isInfinite()) {
            throw CalculatorException(ErrorKind.DOMAIN)
        }
        return value
    }

    private fun toInt32(value: Double): Int {
        if (!NumberFormatter.isWhole(value)) {
            throw CalculatorException(ErrorKind.BITWISE_NON_INTEGER)
        }
        if (value > Int.MAX_VALUE.toDouble() || value < Int.MIN_VALUE.toDouble()) {
            throw CalculatorException(ErrorKind.DOMAIN)
        }
        return Math.rint(value).toInt()
    }

    private fun factorial(value: Double): Double {
        if (value < 0.0 || !NumberFormatter.isWhole(value)) {
            throw CalculatorException(ErrorKind.INVALID_FACTORIAL)
        }
        val n = Math.rint(value).toInt()
        if (n > 170) {
            throw CalculatorException(ErrorKind.DOMAIN)
        }
        var result = 1.0
        for (i in 2..n) {
            result *= i
        }
        return result
    }
}
