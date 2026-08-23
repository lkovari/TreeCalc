package com.lkovari.mobile.apps.treecalc.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpressionTreeBuilderTest {
    @Test
    fun mixedPrecedenceBuildsMulUnderAdd() {
        val tree = ExpressionTreeBuilder.build(
            InfixToPostfix.convert(
                listOf(
                    Token.NumberLiteral("9"),
                    Token.OperatorToken(OperatorKind.ADD),
                    Token.NumberLiteral("7"),
                    Token.OperatorToken(OperatorKind.MUL),
                    Token.NumberLiteral("6")
                )
            ),
            NumericBase.DECIMAL
        )
        assertTrue(tree is BinaryNode)
        val add = tree as BinaryNode
        assertEquals(OperatorKind.ADD, add.operator)
        assertEquals(9.0, (add.left as ValueNode).value, 0.0)
        val mul = add.right as BinaryNode
        assertEquals(OperatorKind.MUL, mul.operator)
        assertEquals(7.0, (mul.left as ValueNode).value, 0.0)
        assertEquals(6.0, (mul.right as ValueNode).value, 0.0)
        assertEquals(51.0, tree.evaluate(), 0.0)
        assertEquals("+ = 51", tree.displayLabel(NumericBase.DECIMAL))
        assertEquals(2, tree.childCount())
        assertEquals(add.left, tree.childAt(0))
        assertEquals(add.right, tree.childAt(1))
        assertNull(tree.childAt(2))
    }

    @Test
    fun powerTreeIsRightAssociative() {
        val tree = ExpressionTreeBuilder.build(
            InfixToPostfix.convert(
                listOf(
                    Token.NumberLiteral("2"),
                    Token.OperatorToken(OperatorKind.POW),
                    Token.NumberLiteral("3"),
                    Token.OperatorToken(OperatorKind.POW),
                    Token.NumberLiteral("2")
                )
            ),
            NumericBase.DECIMAL
        )
        val root = tree as BinaryNode
        assertEquals(OperatorKind.POW, root.operator)
        assertEquals(2.0, (root.left as ValueNode).value, 0.0)
        val inner = root.right as BinaryNode
        assertEquals(OperatorKind.POW, inner.operator)
        assertEquals(3.0, (inner.left as ValueNode).value, 0.0)
        assertEquals(2.0, (inner.right as ValueNode).value, 0.0)
        assertEquals(512.0, tree.evaluate(), 0.0)
    }

    @Test
    fun unaryNodeWrapsOperand() {
        val tree = ExpressionTreeBuilder.build(
            listOf(
                Token.NumberLiteral("9"),
                Token.OperatorToken(OperatorKind.SQRT)
            ),
            NumericBase.DECIMAL
        )
        val sqrt = tree as UnaryNode
        assertEquals(OperatorKind.SQRT, sqrt.operator)
        assertEquals(9.0, (sqrt.operand as ValueNode).value, 0.0)
        assertEquals(3.0, tree.evaluate(), 0.0)
        assertEquals("√ = 3", tree.displayLabel(NumericBase.DECIMAL))
        assertEquals(1, tree.childCount())
        assertEquals(sqrt.operand, tree.childAt(0))
        assertNull(tree.childAt(1))
    }

    @Test
    fun valueNodeHasNoChildren() {
        val node = ValueNode(8.0, "8")
        assertEquals(8.0, node.evaluate(), 0.0)
        assertEquals(0, node.childCount())
        assertNull(node.childAt(0))
        assertNull(node.operatorKind())
        assertEquals("8", node.displayLabel(NumericBase.DECIMAL))
    }

    @Test
    fun hexLiteralIsParsedInHexBase() {
        val tree = ExpressionTreeBuilder.build(
            listOf(Token.NumberLiteral("A")),
            NumericBase.HEXADECIMAL
        )
        assertEquals(10.0, tree.evaluate(), 0.0)
        assertEquals("A", tree.displayLabel(NumericBase.HEXADECIMAL))
    }

    @Test
    fun emptyPostfixIsMalformed() {
        assertCalculatorError(ErrorKind.MALFORMED_EXPRESSION) {
            ExpressionTreeBuilder.build(emptyList(), NumericBase.DECIMAL)
        }
    }

    @Test
    fun leftoverValuesAreMalformed() {
        assertCalculatorError(ErrorKind.MALFORMED_EXPRESSION) {
            ExpressionTreeBuilder.build(
                listOf(Token.NumberLiteral("1"), Token.NumberLiteral("2")),
                NumericBase.DECIMAL
            )
        }
    }

    @Test
    fun unaryWithoutOperandIsMalformed() {
        assertCalculatorError(ErrorKind.MALFORMED_EXPRESSION) {
            ExpressionTreeBuilder.build(
                listOf(Token.OperatorToken(OperatorKind.SQRT)),
                NumericBase.DECIMAL
            )
        }
    }

    @Test
    fun binaryWithoutTwoOperandsIsMalformed() {
        assertCalculatorError(ErrorKind.MALFORMED_EXPRESSION) {
            ExpressionTreeBuilder.build(
                listOf(
                    Token.NumberLiteral("1"),
                    Token.OperatorToken(OperatorKind.ADD)
                ),
                NumericBase.DECIMAL
            )
        }
    }

    @Test
    fun leftoverParenthesesAreMalformed() {
        assertCalculatorError(ErrorKind.MALFORMED_EXPRESSION) {
            ExpressionTreeBuilder.build(
                listOf(Token.LeftParen, Token.NumberLiteral("1")),
                NumericBase.DECIMAL
            )
        }
    }
}
