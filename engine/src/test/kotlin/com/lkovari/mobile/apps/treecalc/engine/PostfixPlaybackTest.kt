package com.lkovari.mobile.apps.treecalc.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PostfixPlaybackTest {
    @Test
    fun playTicksPauseAndResumes() {
        var playback = PostfixPlayback()
        playback = playback.toggle(count = 4, stepByStep = false)
        assertEquals(0, playback.index)
        assertTrue(playback.running)
        playback = playback.tick(4)
        assertEquals(1, playback.index)
        playback = playback.toggle(count = 4, stepByStep = false)
        assertEquals(1, playback.index)
        assertFalse(playback.running)
        playback = playback.toggle(count = 4, stepByStep = false)
        assertEquals(1, playback.index)
        assertTrue(playback.running)
        playback = playback.tick(4)
        assertEquals(2, playback.index)
    }

    @Test
    fun finishedWalkRestartsAndSelectionClears() {
        var playback = PostfixPlayback(index = 2, running = false)
        playback = playback.toggle(count = 3, stepByStep = false)
        assertEquals(0, playback.index)
        assertTrue(playback.running)
        playback = playback.select(1)
        assertEquals(1, playback.index)
        assertFalse(playback.running)
        playback = playback.select(1)
        assertNull(playback.index)
    }

    @Test
    fun emptyListDoesNothingAndStepByStepDoesNotRun() {
        val idle = PostfixPlayback().toggle(count = 0, stepByStep = false)
        assertNull(idle.index)
        assertFalse(idle.running)
        var stepped = PostfixPlayback()
        stepped = stepped.toggle(count = 3, stepByStep = true)
        assertEquals(0, stepped.index)
        assertFalse(stepped.running)
        stepped = stepped.toggle(count = 3, stepByStep = true)
        assertEquals(1, stepped.index)
        stepped = stepped.copy(index = 2)
        stepped = stepped.toggle(count = 3, stepByStep = true)
        assertEquals(2, stepped.index)
        assertFalse(stepped.running)
    }

    @Test
    fun stepAdvancesAndStaysOnTheLastToken() {
        var playback = PostfixPlayback()
        playback = playback.step(3)
        assertEquals(0, playback.index)
        assertFalse(playback.running)
        playback = playback.step(3)
        assertEquals(1, playback.index)
        playback = playback.copy(index = 2, running = true)
        playback = playback.step(3)
        assertEquals(2, playback.index)
        assertFalse(playback.running)
        assertEquals(2, playback.step(0).index)
    }

    @Test
    fun stopClearsTheStep() {
        val playback = PostfixPlayback(index = 2, running = true).stop()
        assertNull(playback.index)
        assertFalse(playback.running)
    }

    @Test
    fun lastTickStopsOnTheFinalToken() {
        var playback = PostfixPlayback(index = 1, running = true)
        playback = playback.tick(2)
        assertEquals(1, playback.index)
        assertFalse(playback.running)
    }
}
