package com.lkovari.mobile.apps.treecalc

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.lkovari.mobile.apps.treecalc.settings.ThemeMode
import com.lkovari.mobile.apps.treecalc.settings.ThemePreferences
import com.lkovari.mobile.apps.treecalc.settings.ThemeResolver
import com.lkovari.mobile.apps.treecalc.ui.TreeCalcApp
import com.lkovari.mobile.apps.treecalc.ui.theme.TreeCalcTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        val preferences = themePreferences()
        setContent {
            val mode by preferences.themeMode.collectAsState(initial = ThemeMode.AUTO)
            var resumeTick by remember { mutableIntStateOf(0) }
            val scope = rememberCoroutineScope()
            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                resumeTick += 1
            }
            val dark = remember(mode, resumeTick) { ThemeResolver.isDark(mode) }
            TreeCalcTheme(darkTheme = dark) {
                TreeCalcApp(
                    themeMode = mode,
                    onThemeMode = { next ->
                        scope.launch { preferences.setThemeMode(next) }
                    }
                )
            }
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
