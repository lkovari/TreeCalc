package com.lkovari.mobile.apps.treecalc.engine

sealed class StackAction {
    data class PushValue(val raw: String) : StackAction()
    data class ApplyUnary(
        val operator: OperatorKind,
        val operand: String,
        val result: String
    ) : StackAction()
    data class ApplyBinary(
        val operator: OperatorKind,
        val left: String,
        val right: String,
        val result: String
    ) : StackAction()
}

data class EvaluationStep(
    val action: StackAction,
    val stack: List<String>
)

fun ExpressionNode.evaluationSteps(base: NumericBase, angleMode: AngleMode): List<EvaluationStep> {
    val stack = ArrayDeque<ExpressionNode>()
    val steps = mutableListOf<EvaluationStep>()
    for (node in postOrderNodes()) {
        val action = when (node) {
            is ValueNode -> {
                stack.addLast(node)
                StackAction.PushValue(node.raw)
            }
            is UnaryNode -> {
                val operand = node.operand.displayLabel(base, angleMode)
                stack.removeLast()
                stack.addLast(node)
                StackAction.ApplyUnary(
                    operator = node.operator,
                    operand = operand,
                    result = node.displayLabel(base, angleMode)
                )
            }
            is BinaryNode -> {
                val left = node.left.displayLabel(base, angleMode)
                val right = node.right.displayLabel(base, angleMode)
                stack.removeLast()
                stack.removeLast()
                stack.addLast(node)
                StackAction.ApplyBinary(
                    operator = node.operator,
                    left = left,
                    right = right,
                    result = node.displayLabel(base, angleMode)
                )
            }
        }
        steps.add(
            EvaluationStep(
                action = action,
                stack = stack.map { item -> item.displayLabel(base, angleMode) }
            )
        )
    }
    return steps
}
