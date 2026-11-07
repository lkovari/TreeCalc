package com.lkovari.mobile.apps.treecalc.engine

import java.math.BigInteger

sealed class ExpressionNode {
    abstract fun evaluate(angleMode: AngleMode): Double
    fun evaluate(): Double {
        return evaluate(AngleMode.DEGREES)
    }
    abstract fun childCount(): Int
    abstract fun childAt(index: Int): ExpressionNode?
    abstract fun operatorKind(): OperatorKind?
    abstract fun displayLabel(base: NumericBase, angleMode: AngleMode): String
    fun displayLabel(base: NumericBase): String {
        return displayLabel(base, AngleMode.DEGREES)
    }

    fun postOrderNodes(): List<ExpressionNode> {
        val nodes = mutableListOf<ExpressionNode>()
        collectPostOrder(nodes)
        return nodes
    }

    fun approximateIntegerPower(base: NumericBase): String? {
        if (this !is BinaryNode || operator != OperatorKind.POW) {
            return null
        }
        val leftValue = left.exactFraction(base) ?: return null
        val rightValue = right.exactFraction(base) ?: return null
        if (leftValue.den != BigInteger.ONE || rightValue.den != BigInteger.ONE) {
            return null
        }
        if (rightValue.num.bitLength() > 20) {
            return null
        }
        val exponent = rightValue.num.toInt()
        if (exponent < 0 || exponent > 8192) {
            return null
        }
        val magnitude = leftValue.num.abs().pow(exponent)
        val negative = leftValue.num.signum() < 0 && exponent % 2 == 1
        return DecimalDisplay.scientificInteger(magnitude, base.radix, negative)
    }

    fun exactFraction(base: NumericBase): Fraction? {
        return when (this) {
            is ValueNode -> Fraction.fromLiteral(raw, base.radix)
            is UnaryNode -> {
                val value = operand.exactFraction(base) ?: return null
                when (operator) {
                    OperatorKind.NEG -> value.negated()
                    OperatorKind.REC -> Fraction.reduced(BigInteger.ONE, BigInteger.ONE)?.divided(value)
                    OperatorKind.SQ -> value.raisedTo(2)
                    OperatorKind.CUBE -> value.raisedTo(3)
                    else -> null
                }
            }
            is BinaryNode -> {
                val leftValue = left.exactFraction(base) ?: return null
                val rightValue = right.exactFraction(base) ?: return null
                when (operator) {
                    OperatorKind.ADD -> leftValue.added(rightValue)
                    OperatorKind.SUB -> leftValue.subtracted(rightValue)
                    OperatorKind.MUL -> leftValue.multiplied(rightValue)
                    OperatorKind.DIV -> leftValue.divided(rightValue)
                    OperatorKind.POW -> {
                        if (rightValue.den != BigInteger.ONE) {
                            return null
                        }
                        if (rightValue.num.bitLength() > 31) {
                            return null
                        }
                        val exponent = rightValue.num.toInt()
                        if (exponent < -Fraction.MAX_EXPONENT || exponent > Fraction.MAX_EXPONENT) {
                            return null
                        }
                        leftValue.raisedTo(exponent)
                    }
                    else -> null
                }
            }
        }
    }

    fun pathForPostOrderIndex(index: Int): String? {
        val paths = mutableListOf<String>()
        collectPostOrderPaths("root", paths)
        if (index < 0 || index >= paths.size) {
            return null
        }
        return paths[index]
    }

    private fun collectPostOrder(out: MutableList<ExpressionNode>) {
        var i = 0
        while (i < childCount()) {
            val child = childAt(i)
            if (child != null) {
                child.collectPostOrder(out)
            }
            i += 1
        }
        out.add(this)
    }

    private fun collectPostOrderPaths(path: String, out: MutableList<String>) {
        var i = 0
        while (i < childCount()) {
            val child = childAt(i)
            if (child != null) {
                child.collectPostOrderPaths("$path/$i", out)
            }
            i += 1
        }
        out.add(path)
    }
}

data class ValueNode(
    val value: Double,
    val raw: String
) : ExpressionNode() {
    override fun evaluate(angleMode: AngleMode): Double {
        return value
    }

    override fun childCount(): Int {
        return 0
    }

    override fun childAt(index: Int): ExpressionNode? {
        return null
    }

    override fun operatorKind(): OperatorKind? {
        return null
    }

    override fun displayLabel(base: NumericBase, angleMode: AngleMode): String {
        return raw
    }
}

data class UnaryNode(
    val operator: OperatorKind,
    val operand: ExpressionNode
) : ExpressionNode() {
    override fun evaluate(angleMode: AngleMode): Double {
        return Operations.executeUnary(operand.evaluate(angleMode), operator, angleMode)
    }

    override fun childCount(): Int {
        return 1
    }

    override fun childAt(index: Int): ExpressionNode? {
        return if (index == 0) {
            operand
        } else {
            null
        }
    }

    override fun operatorKind(): OperatorKind {
        return operator
    }

    override fun displayLabel(base: NumericBase, angleMode: AngleMode): String {
        return "${operator.treeLabel} = ${presented(base, angleMode)}"
    }
}

data class BinaryNode(
    val operator: OperatorKind,
    val left: ExpressionNode,
    val right: ExpressionNode
) : ExpressionNode() {
    override fun evaluate(angleMode: AngleMode): Double {
        return Operations.executeBinary(left.evaluate(angleMode), right.evaluate(angleMode), operator)
    }

    override fun childCount(): Int {
        return 2
    }

    override fun childAt(index: Int): ExpressionNode? {
        return when (index) {
            0 -> left
            1 -> right
            else -> null
        }
    }

    override fun operatorKind(): OperatorKind {
        return operator
    }

    override fun displayLabel(base: NumericBase, angleMode: AngleMode): String {
        return "${operator.treeLabel} = ${presented(base, angleMode)}"
    }
}

private fun ExpressionNode.presented(base: NumericBase, angleMode: AngleMode): String {
    val fraction = exactFraction(base)
    val value = try {
        evaluate(angleMode)
    } catch (exception: CalculatorException) {
        if (fraction != null) {
            fraction.toDouble()
        } else {
            return approximateIntegerPower(base) ?: throw exception
        }
    }
    if (value.isInfinite() || value.isNaN()) {
        if (fraction != null) {
            return DecimalDisplay.present(fraction.toDouble(), base, fraction)
        }
        return approximateIntegerPower(base) ?: "—"
    }
    return DecimalDisplay.present(value, base, fraction)
}
