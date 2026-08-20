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

    private fun shouldPop(incoming: OperatorKind, stackTop: OperatorKind): Boolean {
        return if (incoming.associativity == Associativity.RIGHT) {
            stackTop.precedence > incoming.precedence
        } else {
            stackTop.precedence >= incoming.precedence
        }
    }
}
