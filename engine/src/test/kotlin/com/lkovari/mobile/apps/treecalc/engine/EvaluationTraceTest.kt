package com.lkovari.mobile.apps.treecalc.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class EvaluationTraceTest {
    @Test
    fun mixedPrecedencePushesThenCombinesRightFirst() {
        val tree = BinaryNode(
            OperatorKind.ADD,
            ValueNode(9.0, "9"),
            BinaryNode(
                OperatorKind.MUL,
                ValueNode(7.0, "7"),
                ValueNode(6.0, "6")
            )
        )
        val steps = tree.evaluationSteps(NumericBase.DECIMAL, AngleMode.DEGREES)
        assertEquals(5, steps.size)
        assertPush(steps[0], "9", listOf("9"))
        assertPush(steps[1], "7", listOf("9", "7"))
        assertPush(steps[2], "6", listOf("9", "7", "6"))
        assertBinary(
            steps[3],
            OperatorKind.MUL,
            left = "7",
            right = "6",
            result = "× = 42",
            stack = listOf("9", "× = 42")
        )
        assertBinary(
            steps[4],
            OperatorKind.ADD,
            left = "9",
            right = "× = 42",
            result = "+ = 51",
            stack = listOf("+ = 51")
        )
    }

    @Test
    fun unaryPopsOneOperand() {
        val tree = UnaryNode(OperatorKind.SQRT, ValueNode(9.0, "9"))
        val steps = tree.evaluationSteps(NumericBase.DECIMAL, AngleMode.DEGREES)
        assertEquals(2, steps.size)
        assertPush(steps[0], "9", listOf("9"))
        val action = steps[1].action
        if (action is StackAction.ApplyUnary) {
            assertEquals(OperatorKind.SQRT, action.operator)
            assertEquals("9", action.operand)
            assertEquals("√ = 3", action.result)
        } else {
            fail("expected unary")
        }
        assertEquals(listOf("√ = 3"), steps[1].stack)
    }

    @Test
    fun rightAssociativePowerKeepsTheBaseUnderTheInnerPower() {
        val tree = BinaryNode(
            OperatorKind.POW,
            ValueNode(2.0, "2"),
            BinaryNode(
                OperatorKind.POW,
                ValueNode(3.0, "3"),
                ValueNode(2.0, "2")
            )
        )
        val steps = tree.evaluationSteps(NumericBase.DECIMAL, AngleMode.DEGREES)
        assertEquals(5, steps.size)
        assertBinary(
            steps[3],
            OperatorKind.POW,
            left = "3",
            right = "2",
            result = "^ = 9",
            stack = listOf("2", "^ = 9")
        )
        assertBinary(
            steps[4],
            OperatorKind.POW,
            left = "2",
            right = "^ = 9",
            result = "^ = 512",
            stack = listOf("^ = 512")
        )
    }

    private fun assertPush(step: EvaluationStep, raw: String, stack: List<String>) {
        val action = step.action
        if (action is StackAction.PushValue) {
            assertEquals(raw, action.raw)
        } else {
            fail("expected push")
        }
        assertEquals(stack, step.stack)
    }

    private fun assertBinary(
        step: EvaluationStep,
        operator: OperatorKind,
        left: String,
        right: String,
        result: String,
        stack: List<String>
    ) {
        val action = step.action
        if (action is StackAction.ApplyBinary) {
            assertEquals(operator, action.operator)
            assertEquals(left, action.left)
            assertEquals(right, action.right)
            assertEquals(result, action.result)
        } else {
            fail("expected binary")
        }
        assertEquals(stack, step.stack)
        assertTrue(step.stack.isNotEmpty())
    }
}
