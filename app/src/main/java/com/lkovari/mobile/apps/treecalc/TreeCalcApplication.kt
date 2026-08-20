package com.lkovari.mobile.apps.treecalc

import android.app.Application
import com.lkovari.mobile.apps.treecalc.settings.ThemePreferences

class TreeCalcApplication : Application() {
    lateinit var themePreferences: ThemePreferences
        private set

    override fun onCreate() {
        super.onCreate()
        themePreferences = ThemePreferences(this)
    }
}
