package com.lkovari.mobile.apps.treecalc.engine

sealed class ExpressionNode {
    abstract fun evaluate(): Double
    abstract fun childCount(): Int
    abstract fun childAt(index: Int): ExpressionNode?
    abstract fun operatorKind(): OperatorKind?
    abstract fun displayLabel(base: NumericBase): String
}

data class ValueNode(
    val value: Double,
    val raw: String
) : ExpressionNode() {
    override fun evaluate(): Double {
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

    override fun displayLabel(base: NumericBase): String {
        return raw
    }
}

data class UnaryNode(
    val operator: OperatorKind,
    val operand: ExpressionNode
) : ExpressionNode() {
    override fun evaluate(): Double {
        return Operations.executeUnary(operand.evaluate(), operator)
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

    override fun displayLabel(base: NumericBase): String {
        val result = evaluate()
        return "${operator.treeLabel} = ${NumberFormatter.format(result, base)}"
    }
}

data class BinaryNode(
    val operator: OperatorKind,
    val left: ExpressionNode,
    val right: ExpressionNode
) : ExpressionNode() {
    override fun evaluate(): Double {
        return Operations.executeBinary(left.evaluate(), right.evaluate(), operator)
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

    override fun displayLabel(base: NumericBase): String {
        val result = evaluate()
        return "${operator.treeLabel} = ${NumberFormatter.format(result, base)}"
    }
}
