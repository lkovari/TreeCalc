package com.lkovari.mobile.apps.treecalc.engine

import java.math.BigInteger

private const val SAMPLE_EXPRESSION =
    "888/(6*1.25+3*(7-4*1.5+(9/3-1.2))*2)-11*(4.5/2^3)/(5-1.25)"

class CalculatorEngine {
    private var base: NumericBase = NumericBase.DECIMAL
    private val infix = mutableListOf<Token>()
    private val currentInput = StringBuilder()
    private var afterEquals = false
    private var lastResult: Double? = null
    private var lastFraction: Fraction? = null
    private var lastTree: ExpressionNode? = null
    private var lastPostfix: List<Token> = emptyList()
    private var savedInfix: List<Token> = emptyList()
    private var lastExpressionText: String = ""
    private var liveDisplay: String = "0"
    private var errorKind: ErrorKind? = null
    private var memory: Double = 0.0
    private var memoryFraction: Fraction? = null
    private var memorySet: Boolean = false
    private var angleMode: AngleMode = AngleMode.DEGREES

    fun snapshot(): EvaluationResult {
        val expression = if (afterEquals) {
            lastExpressionText
        } else {
            formatExpression(infix, currentInput.toString())
        }
        val postfixTokens = if (lastTree == null) {
            emptyList()
        } else {
            lastPostfix.map { token -> tokenText(token) }
        }
        val expressionSpans = if (afterEquals && lastTree != null) {
            val spans = expressionStepSpans(savedInfix)
            if (spans.size == postfixTokens.size) spans else emptyList()
        } else {
            emptyList()
        }
        return EvaluationResult(
            display = liveDisplay,
            expression = expression,
            postfix = formatPostfix(lastPostfix),
            postfixTokens = postfixTokens,
            expressionSpans = expressionSpans,
            tree = lastTree,
            base = base,
            errorKind = errorKind,
            memorySet = memorySet,
            afterEquals = afterEquals,
            angleMode = angleMode
        )
    }

    fun setBase(newBase: NumericBase) {
        if (newBase == base) {
            return
        }
        val previous = base
        base = newBase
        infix.replaceAll { token -> convertToken(token, previous, newBase) }
        savedInfix = savedInfix.map { token -> convertToken(token, previous, newBase) }
        if (afterEquals) {
            lastExpressionText = formatExpression(savedInfix, "")
        }
        lastPostfix = lastPostfix.map { token -> convertToken(token, previous, newBase) }
        if (lastTree != null && lastPostfix.isNotEmpty()) {
            try {
                lastTree = ExpressionTreeBuilder.build(lastPostfix, base)
            } catch (exception: CalculatorException) {
                lastTree = null
            }
        }
        if (currentInput.isNotEmpty()) {
            currentInput.replace(0, currentInput.length, convertLiteral(currentInput.toString(), previous, newBase))
            liveDisplay = currentInput.toString()
        } else if (afterEquals) {
            val result = lastResult
            if (result != null) {
                liveDisplay = displayText(result, lastFraction)
            }
        } else {
            liveDisplay = convertLiteral(liveDisplay, previous, newBase)
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
            CalculatorKey.ANGLE_MODE -> toggleAngleMode()
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
        lastFraction = null
        lastTree = null
        lastPostfix = emptyList()
        savedInfix = emptyList()
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
                liveDisplay = displayText(result, lastFraction)
                errorKind = null
            } else {
                errorKind = ErrorKind.EMPTY_EXPRESSION
            }
            return
        }
        try {
            val postfix = InfixToPostfix.convert(infix)
            val tree = ExpressionTreeBuilder.build(postfix, base)
            val fraction = tree.exactFraction(base)
            val evaluated = evaluatedValue(tree, fraction)
            lastResult = evaluated.value
            lastFraction = fraction
            lastTree = tree
            lastPostfix = postfix
            savedInfix = infix.toList()
            lastExpressionText = formatExpression(infix, "")
            afterEquals = true
            infix.clear()
            currentInput.clear()
            liveDisplay = evaluated.fallback ?: displayText(evaluated.value, fraction)
            errorKind = null
        } catch (exception: CalculatorException) {
            lastTree = null
            lastPostfix = emptyList()
            lastFraction = null
            errorKind = exception.kind
        } catch (exception: NumberFormatException) {
            lastTree = null
            lastPostfix = emptyList()
            lastFraction = null
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
            currentInput.append('.')
            liveDisplay = currentInput.toString()
            errorKind = ErrorKind.MALFORMED_EXPRESSION
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
                addNumberToken(tokenTextForValue(result, lastFraction))
            }
        }
        flushInput()
        if (kind == OperatorKind.SUB && isPrefixUnaryPosition()) {
            infix.add(Token.OperatorToken(OperatorKind.NEG))
            errorKind = null
            return
        }
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
                addNumberToken(tokenTextForValue(result, lastFraction))
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
                val fraction = lastFraction?.negated()
                val negated = -result
                lastFraction = fraction
                lastResult = negated
                savedInfix = listOf(Token.OperatorToken(OperatorKind.NEG), Token.LeftParen) +
                    savedInfix +
                    listOf(Token.RightParen)
                lastExpressionText = formatExpression(savedInfix, "")
                rebuildTree(savedInfix)
                liveDisplay = displayText(negated, fraction)
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
        adjustMemory(subtract = false)
    }

    private fun memorySubtract() {
        adjustMemory(subtract = true)
    }

    private fun adjustMemory(subtract: Boolean) {
        val value = currentNumericValue() ?: return
        val incoming = currentFraction()
        val stored = if (memorySet) memoryFraction else Fraction.reduced(BigInteger.ZERO, BigInteger.ONE)
        if (incoming != null && stored != null) {
            val next = if (subtract) stored.subtracted(incoming) else stored.added(incoming)
            if (next != null) {
                memoryFraction = next
                memory = next.toDouble()
                memorySet = true
                return
            }
        }
        memory = if (subtract) memory - value else memory + value
        memoryFraction = null
        memorySet = true
    }

    private fun memoryRecall() {
        beginNewInputIfNeeded()
        currentInput.clear()
        val fraction = memoryFraction
        val formatted = if (fraction != null) {
            DecimalDisplay.literal(fraction, base) ?: displayText(memory, fraction)
        } else {
            NumberFormatter.format(memory, base)
        }
        currentInput.append(formatted)
        liveDisplay = formatted
        errorKind = null
    }

    private fun memoryClear() {
        memory = 0.0
        memoryFraction = null
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
        if (text.isEmpty() || text == "." || text == "-.") {
            currentInput.clear()
            return
        }
        addNumberToken(text)
        currentInput.clear()
    }

    private fun addNumberToken(raw: String) {
        if (raw == "-") {
            infix.add(Token.OperatorToken(OperatorKind.NEG))
            return
        }
        if (raw.startsWith("-") && raw != NumberFormatter.piSymbol()) {
            infix.add(Token.OperatorToken(OperatorKind.NEG))
            infix.add(Token.NumberLiteral(raw.substring(1)))
            return
        }
        infix.add(Token.NumberLiteral(raw))
    }

    private fun isPrefixUnaryPosition(): Boolean {
        if (infix.isEmpty()) {
            return true
        }
        val last = infix.last()
        return last is Token.LeftParen ||
            (last is Token.OperatorToken && last.kind.arity == OperatorArity.BINARY) ||
            (last is Token.OperatorToken && last.kind == OperatorKind.NEG)
    }

    private fun toggleAngleMode() {
        angleMode = if (angleMode == AngleMode.DEGREES) {
            AngleMode.RADIANS
        } else {
            AngleMode.DEGREES
        }
        errorKind = null
    }

    private fun currentFraction(): Fraction? {
        val text = currentInput.toString()
        if (text.isNotEmpty() && text != "-" && text != "." && text != "-.") {
            return Fraction.fromLiteral(text, base.radix)
        }
        if (lastFraction != null && (afterEquals || infix.isEmpty())) {
            return lastFraction
        }
        if (infix.isNotEmpty()) {
            val last = infix.last()
            if (last is Token.NumberLiteral) {
                return Fraction.fromLiteral(last.raw, base.radix)
            }
        }
        return null
    }

    private fun displayText(value: Double, fraction: Fraction?): String {
        return DecimalDisplay.present(value, base, fraction)
    }

    private fun tokenTextForValue(value: Double, fraction: Fraction?): String {
        if (fraction != null) {
            val literal = DecimalDisplay.literal(fraction, base)
            if (literal != null) {
                return literal
            }
        }
        return displayText(value, fraction)
    }

    private data class Evaluated(val value: Double, val fallback: String?)

    private fun evaluatedValue(tree: ExpressionNode, fraction: Fraction?): Evaluated {
        return try {
            val value = tree.evaluate(angleMode)
            if (value.isNaN()) {
                throw CalculatorException(ErrorKind.DOMAIN)
            }
            if (value.isInfinite()) {
                if (fraction != null) {
                    Evaluated(fraction.toDouble(), null)
                } else {
                    val fallback = tree.approximateIntegerPower(base)
                        ?: throw CalculatorException(ErrorKind.DOMAIN)
                    Evaluated(Double.POSITIVE_INFINITY, fallback)
                }
            } else {
                Evaluated(value, null)
            }
        } catch (exception: CalculatorException) {
            if (exception.kind == ErrorKind.DOMAIN && fraction != null) {
                Evaluated(fraction.toDouble(), null)
            } else if (exception.kind == ErrorKind.DOMAIN) {
                val fallback = tree.approximateIntegerPower(base) ?: throw exception
                Evaluated(Double.POSITIVE_INFINITY, fallback)
            } else {
                throw exception
            }
        }
    }

    private fun rebuildTree(tokens: List<Token>) {
        try {
            val postfix = InfixToPostfix.convert(tokens)
            lastPostfix = postfix
            lastTree = ExpressionTreeBuilder.build(postfix, base)
        } catch (exception: CalculatorException) {
            lastTree = null
            lastPostfix = emptyList()
        }
    }

    private fun convertToken(token: Token, from: NumericBase, to: NumericBase): Token {
        if (token is Token.NumberLiteral) {
            return Token.NumberLiteral(convertLiteral(token.raw, from, to))
        }
        return token
    }

    private fun convertLiteral(raw: String, from: NumericBase, to: NumericBase): String {
        val trimmed = raw.trim()
        if (trimmed == NumberFormatter.piSymbol() || trimmed.equals("pi", ignoreCase = true)) {
            return NumberFormatter.piSymbol()
        }
        val trailingDot = trimmed.endsWith('.') && !trimmed.contains('(')
        val body = if (trailingDot) trimmed.dropLast(1) else trimmed
        if (body.isEmpty() || body == "-") {
            return if (trailingDot) body.ifEmpty { "0" } + "." else trimmed
        }
        val fraction = Fraction.fromLiteral(body, from.radix)
        val formatted = if (fraction != null) {
            DecimalDisplay.literal(fraction, to)
                ?: DecimalDisplay.format(fraction, to)
                ?: NumberFormatter.format(fraction.toDouble(), to)
        } else {
            NumberFormatter.format(NumberFormatter.parse(trimmed, from), to)
        }
        return if (trailingDot && !formatted.contains('.')) {
            formatted + "."
        } else {
            formatted
        }
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
