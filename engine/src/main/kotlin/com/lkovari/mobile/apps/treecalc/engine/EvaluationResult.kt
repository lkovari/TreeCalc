package com.lkovari.mobile.apps.treecalc.engine

data class EvaluationResult(
    val display: String,
    val expression: String,
    val postfix: String,
    val tree: ExpressionNode?,
    val base: NumericBase,
    val errorKind: ErrorKind?,
    val memorySet: Boolean,
    val afterEquals: Boolean
)
