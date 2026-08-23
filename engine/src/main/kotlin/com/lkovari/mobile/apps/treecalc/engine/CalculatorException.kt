package com.lkovari.mobile.apps.treecalc.engine

enum class ErrorKind {
    EMPTY_EXPRESSION,
    UNBALANCED_PARENTHESES,
    DIVISION_BY_ZERO,
    UNDEFINED,
    DOMAIN,
    BITWISE_NON_INTEGER,
    INVALID_DIGIT,
    MALFORMED_EXPRESSION,
    INVALID_FACTORIAL
}

class CalculatorException(val kind: ErrorKind, detail: String = kind.name) : Exception(detail)
