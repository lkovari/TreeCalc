package com.lkovari.mobile.apps.treecalc.engine

private const val SAMPLE_EXPRESSION =
    "888/(6*1.25+3*(7-4*1.5+(9/3-1.2))*2)-11*(4.5+2^3)/(5-1.25)"

class CalculatorEngine {
    private var base: NumericBase = NumericBase.DECIMAL
    private val infix = mutableListOf<Token>()
    private val currentInput = StringBuilder()
    private var afterEquals = false
    private var lastResult: Double? = null
    private var lastTree: ExpressionNode? = null
    private var lastPostfix: List<Token> = emptyList()
    private var lastExpressionText: String = ""
    private var liveDisplay: String = "0"
    private var errorKind: ErrorKind? = null
    private var memory: Double = 0.0
    private var memorySet: Boolean = false

    fun snapshot(): EvaluationResult {
        val expression = if (afterEquals) {
            lastExpressionText
        } else {
            formatExpression(infix, currentInput.toString())
        }
        return EvaluationResult(
            display = liveDisplay,
            expression = expression,
            postfix = formatPostfix(lastPostfix),
            tree = lastTree,
            base = base,
            errorKind = errorKind,
            memorySet = memorySet,
            afterEquals = afterEquals
        )
    }

    fun setBase(newBase: NumericBase) {
        if (newBase == base) {
            return
        }
        base = newBase
        if (afterEquals) {
            val result = lastResult
            if (result != null) {
                liveDisplay = NumberFormatter.format(result, base)
            }
        }
        errorKind = null
    }

    fun isKeyEnabled(key: CalculatorKey): Boolean {
        return base.allowsKey(key)
    }

    fun press(key: CalculatorKey) {
        if (!isKeyEnabled(key) && key != CalculatorKey.CLEAR) {
            return
        }
        when (key) {
            CalculatorKey.CLEAR -> clear()
            CalculatorKey.EQUALS -> evaluate()
            CalculatorKey.BACKSPACE -> backspace()
            CalculatorKey.DOT -> enterDot()
            CalculatorKey.PI -> enterPi()
            CalculatorKey.NEGATE -> negate()
            CalculatorKey.LEFT_PAREN -> enterParen(true)
            CalculatorKey.RIGHT_PAREN -> enterParen(false)
            CalculatorKey.MEMORY_ADD -> memoryAdd()
            CalculatorKey.MEMORY_SUB -> memorySubtract()
            CalculatorKey.MEMORY_RECALL -> memoryRecall()
            CalculatorKey.MEMORY_CLEAR -> memoryClear()
            CalculatorKey.TEST -> loadSampleExpression()
            CalculatorKey.DIGIT_0,
            CalculatorKey.DIGIT_1,
            CalculatorKey.DIGIT_2,
            CalculatorKey.DIGIT_3,
            CalculatorKey.DIGIT_4,
            CalculatorKey.DIGIT_5,
            CalculatorKey.DIGIT_6,
            CalculatorKey.DIGIT_7,
            CalculatorKey.DIGIT_8,
            CalculatorKey.DIGIT_9,
            CalculatorKey.DIGIT_A,
            CalculatorKey.DIGIT_B,
            CalculatorKey.DIGIT_C,
            CalculatorKey.DIGIT_D,
            CalculatorKey.DIGIT_E,
            CalculatorKey.DIGIT_F -> enterDigit(digitChar(key))
            CalculatorKey.ADD,
            CalculatorKey.SUB,
            CalculatorKey.MUL,
            CalculatorKey.DIV,
            CalculatorKey.MOD,
            CalculatorKey.POW,
            CalculatorKey.AND,
            CalculatorKey.OR,
            CalculatorKey.XOR,
            CalculatorKey.LSH -> enterBinary(operatorKind(key))
            CalculatorKey.NOT,
            CalculatorKey.SQRT,
            CalculatorKey.REC,
            CalculatorKey.SIN,
            CalculatorKey.COS,
            CalculatorKey.TAN,
            CalculatorKey.LN,
            CalculatorKey.LOG,
            CalculatorKey.EXP,
            CalculatorKey.SQ,
            CalculatorKey.CUBE,
            CalculatorKey.FACT -> enterUnary(operatorKind(key))
        }
    }

    private fun loadSampleExpression() {
        clear()
        base = NumericBase.DECIMAL
        for (character in SAMPLE_EXPRESSION) {
            when (character) {
                '(' -> enterParen(true)
                ')' -> enterParen(false)
                '+' -> enterBinary(OperatorKind.ADD)
                '-' -> enterBinary(OperatorKind.SUB)
                '*' -> enterBinary(OperatorKind.MUL)
                '/' -> enterBinary(OperatorKind.DIV)
                '^' -> enterBinary(OperatorKind.POW)
                '.' -> enterDot()
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> enterDigit(character)
            }
        }
    }

    private fun clear() {
        infix.clear()
        currentInput.clear()
        afterEquals = false
        lastResult = null
        lastTree = null
        lastPostfix = emptyList()
        lastExpressionText = ""
        liveDisplay = NumberFormatter.format(0.0, base)
        errorKind = null
    }

    private fun evaluate() {
        flushInput()
        if (infix.isEmpty()) {
            val result = lastResult
            if (result != null) {
                afterEquals = true
                liveDisplay = NumberFormatter.format(result, base)
                errorKind = null
            } else {
                errorKind = ErrorKind.EMPTY_EXPRESSION
            }
            return
        }
        try {
            val postfix = InfixToPostfix.convert(infix)
            val tree = ExpressionTreeBuilder.build(postfix, base)
            val value = tree.evaluate()
            val display = NumberFormatter.format(value, base)
            lastResult = value
            lastTree = tree
            lastPostfix = postfix
            lastExpressionText = formatExpression(infix, "")
            afterEquals = true
            infix.clear()
            currentInput.clear()
            liveDisplay = display
            errorKind = null
        } catch (exception: CalculatorException) {
            lastTree = null
            lastPostfix = emptyList()
            errorKind = exception.kind
        } catch (exception: NumberFormatException) {
            lastTree = null
            lastPostfix = emptyList()
            errorKind = ErrorKind.MALFORMED_EXPRESSION
        }
    }

    private fun enterDigit(character: Char) {
        beginNewInputIfNeeded()
        if (!NumberFormatter.isValidDigit(character, base)) {
            errorKind = ErrorKind.INVALID_DIGIT
            return
        }
        val upper = character.uppercaseChar()
        val current = currentInput.toString()
        if (current == "0" && upper != '.') {
            currentInput.clear()
        } else if (current == "-0" && upper != '.') {
            currentInput.setLength(1)
        }
        currentInput.append(upper)
        liveDisplay = currentInput.toString()
        errorKind = null
    }

    private fun enterDot() {
        beginNewInputIfNeeded()
        val current = currentInput.toString()
        if (current.contains('.')) {
            return
        }
        if (current.isEmpty() || current == "-") {
            currentInput.append("0.")
        } else {
            currentInput.append('.')
        }
        liveDisplay = currentInput.toString()
        errorKind = null
    }

    private fun enterPi() {
        beginNewInputIfNeeded()
        flushInput()
        infix.add(Token.NumberLiteral(NumberFormatter.piSymbol()))
        liveDisplay = NumberFormatter.format(Math.PI, base)
        errorKind = null
    }

    private fun enterBinary(kind: OperatorKind) {
        if (afterEquals) {
            val result = lastResult
            infix.clear()
            currentInput.clear()
            afterEquals = false
            lastExpressionText = ""
            if (result != null) {
                infix.add(Token.NumberLiteral(NumberFormatter.format(result, base)))
            }
        }
        flushInput()
        if (infix.isEmpty()) {
            infix.add(Token.NumberLiteral(NumberFormatter.format(0.0, base)))
        }
        val last = infix.last()
        if (last is Token.OperatorToken && last.kind.arity == OperatorArity.BINARY) {
            infix.removeAt(infix.lastIndex)
        }
        infix.add(Token.OperatorToken(kind))
        errorKind = null
    }

    private fun enterUnary(kind: OperatorKind) {
        if (afterEquals) {
            val result = lastResult
            infix.clear()
            currentInput.clear()
            afterEquals = false
            lastExpressionText = ""
            if (result != null) {
                infix.add(Token.NumberLiteral(NumberFormatter.format(result, base)))
            }
        }
        flushInput()
        if (infix.isEmpty()) {
            errorKind = ErrorKind.MALFORMED_EXPRESSION
            return
        }
        infix.add(Token.OperatorToken(kind))
        errorKind = null
    }

    private fun enterParen(left: Boolean) {
        beginNewInputIfNeeded()
        flushInput()
        if (left) {
            infix.add(Token.LeftParen)
        } else {
            infix.add(Token.RightParen)
        }
        errorKind = null
    }

    private fun negate() {
        if (afterEquals) {
            val result = lastResult
            if (result != null) {
                lastResult = -result
                liveDisplay = NumberFormatter.format(-result, base)
                currentInput.clear()
                infix.clear()
            }
            return
        }
        if (currentInput.isEmpty()) {
            currentInput.append('-')
            liveDisplay = currentInput.toString()
            return
        }
        if (currentInput[0] == '-') {
            currentInput.deleteCharAt(0)
        } else {
            currentInput.insert(0, '-')
        }
        if (currentInput.isEmpty()) {
            liveDisplay = NumberFormatter.format(0.0, base)
        } else {
            liveDisplay = currentInput.toString()
        }
    }

    private fun backspace() {
        if (afterEquals) {
            return
        }
        if (currentInput.isNotEmpty()) {
            currentInput.deleteCharAt(currentInput.length - 1)
            liveDisplay = if (currentInput.isEmpty()) {
                NumberFormatter.format(0.0, base)
            } else {
                currentInput.toString()
            }
            return
        }
        if (infix.isEmpty()) {
            return
        }
        val removed = infix.removeAt(infix.lastIndex)
        if (removed is Token.NumberLiteral) {
            val raw = removed.raw
            if (raw.length > 1) {
                currentInput.append(raw.substring(0, raw.length - 1))
                liveDisplay = currentInput.toString()
            } else {
                liveDisplay = NumberFormatter.format(0.0, base)
            }
        }
    }

    private fun memoryAdd() {
        val value = currentNumericValue() ?: return
        memory += value
        memorySet = true
    }

    private fun memorySubtract() {
        val value = currentNumericValue() ?: return
        memory -= value
        memorySet = true
    }

    private fun memoryRecall() {
        beginNewInputIfNeeded()
        currentInput.clear()
        val formatted = NumberFormatter.format(memory, base)
        currentInput.append(formatted)
        liveDisplay = formatted
        errorKind = null
    }

    private fun memoryClear() {
        memory = 0.0
        memorySet = false
    }

    private fun beginNewInputIfNeeded() {
        if (afterEquals) {
            infix.clear()
            currentInput.clear()
            afterEquals = false
            lastExpressionText = ""
        }
    }

    private fun flushInput() {
        val text = currentInput.toString()
        if (text.isEmpty() || text == "-" || text == "." || text == "-.") {
            currentInput.clear()
            return
        }
        infix.add(Token.NumberLiteral(text))
        currentInput.clear()
    }

    private fun currentNumericValue(): Double? {
        val text = currentInput.toString()
        if (text.isNotEmpty() && text != "-" && text != "." && text != "-.") {
            return try {
                NumberFormatter.parse(text, base)
            } catch (exception: CalculatorException) {
                null
            } catch (exception: NumberFormatException) {
                null
            }
        }
        val result = lastResult
        if (result != null && (afterEquals || infix.isEmpty())) {
            return result
        }
        if (infix.isNotEmpty()) {
            val last = infix.last()
            if (last is Token.NumberLiteral) {
                return try {
                    NumberFormatter.parse(last.raw, base)
                } catch (exception: CalculatorException) {
                    null
                } catch (exception: NumberFormatException) {
                    null
                }
            }
        }
        return null
    }

    private fun digitChar(key: CalculatorKey): Char {
        return when (key) {
            CalculatorKey.DIGIT_0 -> '0'
            CalculatorKey.DIGIT_1 -> '1'
            CalculatorKey.DIGIT_2 -> '2'
            CalculatorKey.DIGIT_3 -> '3'
            CalculatorKey.DIGIT_4 -> '4'
            CalculatorKey.DIGIT_5 -> '5'
            CalculatorKey.DIGIT_6 -> '6'
            CalculatorKey.DIGIT_7 -> '7'
            CalculatorKey.DIGIT_8 -> '8'
            CalculatorKey.DIGIT_9 -> '9'
            CalculatorKey.DIGIT_A -> 'A'
            CalculatorKey.DIGIT_B -> 'B'
            CalculatorKey.DIGIT_C -> 'C'
            CalculatorKey.DIGIT_D -> 'D'
            CalculatorKey.DIGIT_E -> 'E'
            CalculatorKey.DIGIT_F -> 'F'
            else -> '0'
        }
    }

    private fun operatorKind(key: CalculatorKey): OperatorKind {
        return when (key) {
            CalculatorKey.ADD -> OperatorKind.ADD
            CalculatorKey.SUB -> OperatorKind.SUB
            CalculatorKey.MUL -> OperatorKind.MUL
            CalculatorKey.DIV -> OperatorKind.DIV
            CalculatorKey.MOD -> OperatorKind.MOD
            CalculatorKey.POW -> OperatorKind.POW
            CalculatorKey.AND -> OperatorKind.AND
            CalculatorKey.OR -> OperatorKind.OR
            CalculatorKey.XOR -> OperatorKind.XOR
            CalculatorKey.LSH -> OperatorKind.LSH
            CalculatorKey.NOT -> OperatorKind.NOT
            CalculatorKey.SQRT -> OperatorKind.SQRT
            CalculatorKey.REC -> OperatorKind.REC
            CalculatorKey.SIN -> OperatorKind.SIN
            CalculatorKey.COS -> OperatorKind.COS
            CalculatorKey.TAN -> OperatorKind.TAN
            CalculatorKey.LN -> OperatorKind.LN
            CalculatorKey.LOG -> OperatorKind.LOG
            CalculatorKey.EXP -> OperatorKind.EXP
            CalculatorKey.SQ -> OperatorKind.SQ
            CalculatorKey.CUBE -> OperatorKind.CUBE
            CalculatorKey.FACT -> OperatorKind.FACT
            else -> OperatorKind.ADD
        }
    }

    private fun formatExpression(tokens: List<Token>, current: String): String {
        val parts = mutableListOf<String>()
        for (token in tokens) {
            parts.add(tokenText(token))
        }
        if (current.isNotEmpty()) {
            parts.add(current)
        }
        return parts.joinToString(" ")
    }

    private fun formatPostfix(tokens: List<Token>): String {
        return tokens.joinToString(", ") { token -> tokenText(token) }
    }

    private fun tokenText(token: Token): String {
        return when (token) {
            is Token.NumberLiteral -> token.raw
            is Token.OperatorToken -> token.kind.displaySymbol
            is Token.LeftParen -> "("
            is Token.RightParen -> ")"
        }
    }
}
