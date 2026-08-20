package com.lkovari.mobile.apps.treecalc

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.lkovari.mobile.apps.treecalc.settings.ThemeMode
import com.lkovari.mobile.apps.treecalc.settings.ThemePreferences
import com.lkovari.mobile.apps.treecalc.settings.ThemeResolver
import com.lkovari.mobile.apps.treecalc.ui.screens.SplashScreen
import com.lkovari.mobile.apps.treecalc.ui.theme.TreeCalcTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.Intent

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        val preferences = themePreferences()
        setContent {
            val mode by preferences.themeMode.collectAsState(initial = ThemeMode.AUTO)
            TreeCalcTheme(darkTheme = ThemeResolver.isDark(mode)) {
                SplashScreen()
            }
        }
        lifecycleScope.launch {
            delay(3000)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }

    private fun themePreferences(): ThemePreferences {
        val application = applicationContext
        return if (application is TreeCalcApplication) {
            application.themePreferences
        } else {
            ThemePreferences(application)
        }
    }
}
