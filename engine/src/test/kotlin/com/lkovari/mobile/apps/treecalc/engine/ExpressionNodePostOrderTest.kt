package com.lkovari.mobile.apps.treecalc.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpressionNodePostOrderTest {
    @Test
    fun mixedPrecedenceMapsTokensToPaths() {
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
        val nodes = tree.postOrderNodes()
        assertEquals(5, nodes.size)
        assertEquals("9", (nodes[0] as ValueNode).raw)
        assertEquals("7", (nodes[1] as ValueNode).raw)
        assertEquals("6", (nodes[2] as ValueNode).raw)
        assertEquals(OperatorKind.MUL, nodes[3].operatorKind())
        assertEquals(OperatorKind.ADD, nodes[4].operatorKind())
        assertEquals("root/0", tree.pathForPostOrderIndex(0))
        assertEquals("root/1/0", tree.pathForPostOrderIndex(1))
        assertEquals("root/1/1", tree.pathForPostOrderIndex(2))
        assertEquals("root/1", tree.pathForPostOrderIndex(3))
        assertEquals("root", tree.pathForPostOrderIndex(4))
        assertNull(tree.pathForPostOrderIndex(5))
        assertNull(tree.pathForPostOrderIndex(-1))
    }

    @Test
    fun unarySqrtMapsOperandThenOperator() {
        val tree = ExpressionTreeBuilder.build(
            listOf(
                Token.NumberLiteral("9"),
                Token.OperatorToken(OperatorKind.SQRT)
            ),
            NumericBase.DECIMAL
        )
        val nodes = tree.postOrderNodes()
        assertEquals(2, nodes.size)
        assertEquals("9", (nodes[0] as ValueNode).raw)
        assertEquals(OperatorKind.SQRT, nodes[1].operatorKind())
        assertEquals("root/0", tree.pathForPostOrderIndex(0))
        assertEquals("root", tree.pathForPostOrderIndex(1))
    }

    @Test
    fun identicalValuesKeepDistinctPaths() {
        val tree = ExpressionTreeBuilder.build(
            InfixToPostfix.convert(
                listOf(
                    Token.NumberLiteral("5"),
                    Token.OperatorToken(OperatorKind.ADD),
                    Token.NumberLiteral("5")
                )
            ),
            NumericBase.DECIMAL
        )
        val nodes = tree.postOrderNodes()
        assertEquals(3, nodes.size)
        assertEquals("5", (nodes[0] as ValueNode).raw)
        assertEquals("5", (nodes[1] as ValueNode).raw)
        assertTrue(nodes[0] !== nodes[1])
        assertEquals("root/0", tree.pathForPostOrderIndex(0))
        assertEquals("root/1", tree.pathForPostOrderIndex(1))
        assertEquals("root", tree.pathForPostOrderIndex(2))
    }
}
