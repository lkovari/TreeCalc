package com.lkovari.mobile.apps.treecalc.engine

object ExpressionTreeBuilder {
    fun build(postfix: List<Token>, base: NumericBase): ExpressionNode {
        val stack = ArrayDeque<ExpressionNode>()
        for (token in postfix) {
            when (token) {
                is Token.NumberLiteral -> {
                    val value = NumberFormatter.parse(token.raw, base)
                    stack.addLast(ValueNode(value, token.raw))
                }
                is Token.OperatorToken -> {
                    when (token.kind.arity) {
                        OperatorArity.UNARY -> {
                            if (stack.isEmpty()) {
                                throw CalculatorException(ErrorKind.MALFORMED_EXPRESSION)
                            }
                            val operand = stack.removeLast()
                            stack.addLast(UnaryNode(token.kind, operand))
                        }
                        OperatorArity.BINARY -> {
                            if (stack.size < 2) {
                                throw CalculatorException(ErrorKind.MALFORMED_EXPRESSION)
                            }
                            val right = stack.removeLast()
                            val left = stack.removeLast()
                            stack.addLast(BinaryNode(token.kind, left, right))
                        }
                    }
                }
                is Token.LeftParen,
                is Token.RightParen -> throw CalculatorException(ErrorKind.MALFORMED_EXPRESSION)
            }
        }
        if (stack.size != 1) {
            throw CalculatorException(ErrorKind.MALFORMED_EXPRESSION)
        }
        return stack.removeLast()
    }
}
