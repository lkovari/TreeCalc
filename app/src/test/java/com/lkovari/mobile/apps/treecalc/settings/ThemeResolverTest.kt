package com.lkovari.mobile.apps.treecalc.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {
    @Test
    fun fromIntMapsKnownValues() {
        assertEquals(ThemeMode.AUTO, ThemeMode.fromInt(0))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromInt(1))
        assertEquals(ThemeMode.DARK, ThemeMode.fromInt(2))
    }

    @Test
    fun fromIntFallsBackToAuto() {
        assertEquals(ThemeMode.AUTO, ThemeMode.fromInt(-1))
        assertEquals(ThemeMode.AUTO, ThemeMode.fromInt(99))
    }
}

class ThemeResolverTest {
    @Test
    fun lightIsNeverDark() {
        assertFalse(ThemeResolver.isDark(ThemeMode.LIGHT, 0))
        assertFalse(ThemeResolver.isDark(ThemeMode.LIGHT, 12))
        assertFalse(ThemeResolver.isDark(ThemeMode.LIGHT, 23))
    }

    @Test
    fun darkIsAlwaysDark() {
        assertTrue(ThemeResolver.isDark(ThemeMode.DARK, 0))
        assertTrue(ThemeResolver.isDark(ThemeMode.DARK, 12))
        assertTrue(ThemeResolver.isDark(ThemeMode.DARK, 18))
    }

    @Test
    fun autoFollowsDaylightHours() {
        assertTrue(ThemeResolver.isDark(ThemeMode.AUTO, 0))
        assertTrue(ThemeResolver.isDark(ThemeMode.AUTO, 5))
        assertFalse(ThemeResolver.isDark(ThemeMode.AUTO, 6))
        assertFalse(ThemeResolver.isDark(ThemeMode.AUTO, 12))
        assertFalse(ThemeResolver.isDark(ThemeMode.AUTO, 17))
        assertTrue(ThemeResolver.isDark(ThemeMode.AUTO, 18))
        assertTrue(ThemeResolver.isDark(ThemeMode.AUTO, 23))
    }
}
