package com.lkovari.mobile.apps.treecalc.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test

class TreeNodeCirclePaletteTest {
    @Test
    fun lightRootFillIsPastelGreen() {
        assertPastelGreen(LightPalette.rootBadgeFill)
    }

    @Test
    fun lightOtherFillIsPastelBlue() {
        assertPastelBlue(LightPalette.operandFill)
        assertPastelBlue(LightPalette.badgeFill)
    }

    @Test
    fun lightBorderIsBlue() {
        assertBlue(LightPalette.operandRing)
    }

    @Test
    fun nightRootFillIsGreenAndDimmerThanLight() {
        assertGreen(DarkPalette.rootBadgeFill)
        assertTrue(luminance(DarkPalette.rootBadgeFill) < luminance(LightPalette.rootBadgeFill))
    }

    @Test
    fun nightOtherFillIsBlueAndDimmerThanLight() {
        assertBlue(DarkPalette.operandFill)
        assertBlue(DarkPalette.badgeFill)
        assertTrue(luminance(DarkPalette.operandFill) < luminance(LightPalette.operandFill))
    }

    @Test
    fun nightBorderIsBlueAndBrighterThanFill() {
        assertBlue(DarkPalette.operandRing)
        assertTrue(luminance(DarkPalette.operandRing) > luminance(DarkPalette.operandFill))
    }

    private fun assertPastelGreen(color: Color) {
        assertGreen(color)
        assertTrue(luminance(color) > 0.70f)
        assertTrue(color.green - color.red > 0.12f)
    }

    private fun assertPastelBlue(color: Color) {
        assertBlue(color)
        assertTrue(luminance(color) > 0.70f)
        assertTrue(color.blue - color.red > 0.15f)
    }

    private fun assertGreen(color: Color) {
        assertTrue(color.green > color.red)
        assertTrue(color.green > color.blue)
    }

    private fun assertBlue(color: Color) {
        assertTrue(color.blue > color.red)
        assertTrue(color.blue > color.green)
    }

    private fun luminance(color: Color): Float {
        return (color.red + color.green + color.blue) / 3f
    }
}
