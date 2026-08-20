package com.lkovari.mobile.apps.treecalc.engine

sealed class Token {
    data class NumberLiteral(val raw: String) : Token()
    data class OperatorToken(val kind: OperatorKind) : Token()
    data object LeftParen : Token()
    data object RightParen : Token()
}
