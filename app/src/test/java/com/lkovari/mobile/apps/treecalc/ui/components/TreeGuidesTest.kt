package com.lkovari.mobile.apps.treecalc.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TreeGuidesTest {
    @Test
    fun rootHasNoSiblingRail() {
        val spec = treeGuideSpec(depth = 0, isLastSibling = true)
        assertTrue(spec.isRoot)
        assertEquals(TreeElbow.NONE, spec.elbow)
        assertFalse(spec.drawElbow)
        assertFalse(spec.verticalThrough)

        val stillRoot = treeGuideSpec(depth = 0, isLastSibling = false)
        assertTrue(stillRoot.isRoot)
        assertEquals(TreeElbow.NONE, stillRoot.elbow)
    }

    @Test
    fun lastSiblingDrawsLElbow() {
        val spec = treeGuideSpec(depth = 1, isLastSibling = true)
        assertFalse(spec.isRoot)
        assertTrue(spec.isLastSibling)
        assertEquals(TreeElbow.L, spec.elbow)
        assertTrue(spec.drawElbow)
        assertFalse(spec.verticalThrough)
        assertTrue(spec.verticalStopsAtCenter)
    }

    @Test
    fun nonLastSiblingDrawsTElbow() {
        val spec = treeGuideSpec(depth = 2, isLastSibling = false)
        assertFalse(spec.isRoot)
        assertFalse(spec.isLastSibling)
        assertEquals(TreeElbow.T, spec.elbow)
        assertTrue(spec.drawElbow)
        assertTrue(spec.verticalThrough)
        assertFalse(spec.verticalStopsAtCenter)
    }

    @Test
    fun rootChildrenDoNotInheritARail() {
        assertEquals(
            emptyList<Boolean>(),
            childAncestorContinues(
                depth = 0,
                ancestorContinues = emptyList(),
                isLastSibling = false
            )
        )
    }

    @Test
    fun nonLastParentAddsAContinuingRail() {
        assertEquals(
            listOf(true),
            childAncestorContinues(
                depth = 1,
                ancestorContinues = emptyList(),
                isLastSibling = false
            )
        )
    }

    @Test
    fun lastParentAddsASilentGutter() {
        assertEquals(
            listOf(true, false),
            childAncestorContinues(
                depth = 2,
                ancestorContinues = listOf(true),
                isLastSibling = true
            )
        )
    }
}
