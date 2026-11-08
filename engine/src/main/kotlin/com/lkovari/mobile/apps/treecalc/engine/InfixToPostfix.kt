package com.lkovari.mobile.apps.treecalc.engine

object InfixToPostfix {
    fun convert(infix: List<Token>): List<Token> {
        val output = ArrayDeque<Token>()
        val operators = ArrayDeque<Token>()
        for (token in infix) {
            when (token) {
                is Token.NumberLiteral -> output.addLast(token)
                is Token.OperatorToken -> {
                    while (operators.isNotEmpty()) {
                        val top = operators.last()
                        if (top is Token.OperatorToken && shouldPop(token.kind, top.kind)) {
                            output.addLast(operators.removeLast())
                        } else {
                            break
                        }
                    }
                    operators.addLast(token)
                }
                is Token.LeftParen -> operators.addLast(token)
                is Token.RightParen -> {
                    var foundLeft = false
                    while (operators.isNotEmpty()) {
                        val top = operators.removeLast()
                        if (top is Token.LeftParen) {
                            foundLeft = true
                            break
                        }
                        output.addLast(top)
                    }
                    if (!foundLeft) {
                        throw CalculatorException(
                            ErrorKind.UNBALANCED_PARENTHESES,
                            "Unbalanced parenthesis! Missing left parenthesis"
                        )
                    }
                }
            }
        }
        while (operators.isNotEmpty()) {
            val top = operators.removeLast()
            if (top is Token.LeftParen) {
                throw CalculatorException(
                    ErrorKind.UNBALANCED_PARENTHESES,
                    "Unbalanced parenthesis! Missing right parenthesis"
                )
            }
            output.addLast(top)
        }
        return output.toList()
    }

    fun sourceIndices(infix: List<Token>): List<Int> {
        val output = ArrayDeque<Int>()
        val operators = ArrayDeque<YardMark>()
        for ((index, token) in infix.withIndex()) {
            when (token) {
                is Token.NumberLiteral -> output.addLast(index)
                is Token.OperatorToken -> {
                    while (operators.isNotEmpty()) {
                        val top = operators.last()
                        if (top is YardMark.Operator) {
                            val stacked = infix[top.index]
                            if (stacked is Token.OperatorToken && shouldPop(token.kind, stacked.kind)) {
                                operators.removeLast()
                                output.addLast(top.index)
                            } else {
                                break
                            }
                        } else {
                            break
                        }
                    }
                    operators.addLast(YardMark.Operator(index))
                }
                is Token.LeftParen -> operators.addLast(YardMark.Paren)
                is Token.RightParen -> {
                    var foundLeft = false
                    while (operators.isNotEmpty()) {
                        val top = operators.removeLast()
                        if (top is YardMark.Paren) {
                            foundLeft = true
                            break
                        }
                        if (top is YardMark.Operator) {
                            output.addLast(top.index)
                        }
                    }
                    if (!foundLeft) {
                        return emptyList()
                    }
                }
            }
        }
        while (operators.isNotEmpty()) {
            val top = operators.removeLast()
            if (top is YardMark.Paren) {
                return emptyList()
            }
            if (top is YardMark.Operator) {
                output.addLast(top.index)
            }
        }
        return output.toList()
    }

    private fun shouldPop(incoming: OperatorKind, stackTop: OperatorKind): Boolean {
        return if (incoming.associativity == Associativity.RIGHT) {
            stackTop.precedence > incoming.precedence
        } else {
            stackTop.precedence >= incoming.precedence
        }
    }
}

private sealed class YardMark {
    data class Operator(val index: Int) : YardMark()
    data object Paren : YardMark()
}
