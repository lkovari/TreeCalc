package com.lkovari.mobile.apps.treecalc.viewmodel

import com.lkovari.mobile.apps.treecalc.engine.CalculatorKey
import com.lkovari.mobile.apps.treecalc.engine.ErrorKind
import com.lkovari.mobile.apps.treecalc.engine.NumericBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorViewModelTest {
    @Test
    fun startsAtZeroInDecimal() {
        val viewModel = CalculatorViewModel()
        val state = viewModel.state.value
        assertEquals("0", state.display)
        assertEquals(NumericBase.DECIMAL, state.base)
        assertFalse(state.afterEquals)
        assertFalse(state.memorySet)
        assertNull(state.errorKind)
        assertNull(state.tree)
        assertEquals("", state.expression)
        assertEquals("", state.postfix)
    }

    @Test
    fun pressEvaluatesAndPublishesTree() {
        val viewModel = CalculatorViewModel()
        viewModel.press(CalculatorKey.DIGIT_9)
        viewModel.press(CalculatorKey.ADD)
        viewModel.press(CalculatorKey.DIGIT_7)
        viewModel.press(CalculatorKey.MUL)
        viewModel.press(CalculatorKey.DIGIT_6)
        viewModel.press(CalculatorKey.EQUALS)
        val state = viewModel.state.value
        assertEquals("51", state.display)
        assertEquals("9, 7, 6, ×, +", state.postfix)
        assertNotNull(state.tree)
        assertTrue(state.afterEquals)
        assertNull(state.errorKind)
    }

    @Test
    fun setBaseReformatsTheLastResult() {
        val viewModel = CalculatorViewModel()
        viewModel.press(CalculatorKey.DIGIT_1)
        viewModel.press(CalculatorKey.DIGIT_0)
        viewModel.press(CalculatorKey.EQUALS)
        viewModel.setBase(NumericBase.HEXADECIMAL)
        assertEquals("A", viewModel.state.value.display)
        assertEquals(NumericBase.HEXADECIMAL, viewModel.state.value.base)
        viewModel.setBase(NumericBase.BINARY)
        assertEquals("1010", viewModel.state.value.display)
    }

    @Test
    fun errorsArePublishedToState() {
        val viewModel = CalculatorViewModel()
        viewModel.press(CalculatorKey.DIGIT_1)
        viewModel.press(CalculatorKey.DIV)
        viewModel.press(CalculatorKey.DIGIT_0)
        viewModel.press(CalculatorKey.EQUALS)
        assertEquals(ErrorKind.DIVISION_BY_ZERO, viewModel.state.value.errorKind)
        assertNull(viewModel.state.value.tree)
    }

    @Test
    fun memoryIndicatorFollowsMemoryKeys() {
        val viewModel = CalculatorViewModel()
        viewModel.press(CalculatorKey.DIGIT_4)
        viewModel.press(CalculatorKey.MEMORY_ADD)
        assertTrue(viewModel.state.value.memorySet)
        viewModel.press(CalculatorKey.MEMORY_CLEAR)
        assertFalse(viewModel.state.value.memorySet)
    }
}
