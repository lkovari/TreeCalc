package com.lkovari.mobile.apps.treecalc.engine

fun Token.expressionText(): String {
    return when (this) {
        is Token.NumberLiteral -> raw
        is Token.OperatorToken -> kind.displaySymbol
        is Token.LeftParen -> "("
        is Token.RightParen -> ")"
    }
}

fun expressionStepSpans(infix: List<Token>): List<IntRange> {
    if (infix.isEmpty()) {
        return emptyList()
    }
    val ranges = tokenRanges(infix)
    if (ranges.size != infix.size) {
        return emptyList()
    }
    val sources = InfixToPostfix.sourceIndices(infix)
    val stack = ArrayDeque<StepSpan>()
    val steps = mutableListOf<IntRange>()
    for (source in sources) {
        if (source < 0 || source >= infix.size) {
            return emptyList()
        }
        val token = infix[source]
        val tokenRange = ranges[source]
        val span = when (token) {
            is Token.NumberLiteral -> StepSpan(tokenRange.first, tokenRange.last)
            is Token.OperatorToken -> when (token.kind.arity) {
                OperatorArity.UNARY -> {
                    val operand = stack.removeLastOrNull() ?: return emptyList()
                    merged(tokenRange, operand)
                }
                OperatorArity.BINARY -> {
                    val right = stack.removeLastOrNull() ?: return emptyList()
                    val left = stack.removeLastOrNull() ?: return emptyList()
                    merged(tokenRange, left, right)
                }
            }
            is Token.LeftParen,
            is Token.RightParen -> return emptyList()
        }
        val enclosed = encloseParentheses(span, infix, ranges)
        stack.addLast(enclosed)
        steps.add(enclosed.start..enclosed.end)
    }
    if (stack.size != 1) {
        return emptyList()
    }
    return steps
}

private class StepSpan(val start: Int, val end: Int)

private fun tokenRanges(infix: List<Token>): List<IntRange> {
    val ranges = ArrayList<IntRange>(infix.size)
    var cursor = 0
    for ((index, token) in infix.withIndex()) {
        if (index > 0) {
            cursor += 1
        }
        val text = token.expressionText()
        if (text.isEmpty()) {
            return emptyList()
        }
        val start = cursor
        val end = cursor + text.length - 1
        ranges.add(start..end)
        cursor = end + 1
    }
    return ranges
}

private fun merged(token: IntRange, vararg nodes: StepSpan): StepSpan {
    var start = token.first
    var end = token.last
    for (node in nodes) {
        if (node.start < start) {
            start = node.start
        }
        if (node.end > end) {
            end = node.end
        }
    }
    return StepSpan(start, end)
}

private fun encloseParentheses(
    span: StepSpan,
    infix: List<Token>,
    ranges: List<IntRange>
): StepSpan {
    var start = span.start
    var end = span.end
    while (true) {
        var left = -1
        var right = -1
        for ((index, range) in ranges.withIndex()) {
            if (range.last < start) {
                left = index
            }
            if (right < 0 && range.first > end) {
                right = index
            }
        }
        if (left < 0 || right < 0) {
            break
        }
        val leftTouches = ranges[left].last + 2 == start
        val rightTouches = ranges[right].first == end + 2
        if (infix[left] is Token.LeftParen && infix[right] is Token.RightParen && leftTouches && rightTouches) {
            start = ranges[left].first
            end = ranges[right].last
        } else {
            break
        }
    }
    return StepSpan(start, end)
}
