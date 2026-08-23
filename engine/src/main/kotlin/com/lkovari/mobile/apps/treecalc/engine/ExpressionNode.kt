package com.lkovari.mobile.apps.treecalc.engine

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
        val result = evaluate(angleMode)
        return "${operator.treeLabel} = ${NumberFormatter.format(result, base)}"
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
        val result = evaluate(angleMode)
        return "${operator.treeLabel} = ${NumberFormatter.format(result, base)}"
    }
}
