package com.lkovari.mobile.apps.treecalc.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

private val Context.themeDataStore by preferencesDataStore(name = "treecalc_settings")

class ThemePreferences(private val context: Context) {
    private val themeModeKey = intPreferencesKey("theme_mode")

    val themeMode: Flow<ThemeMode> = context.themeDataStore.data.map { preferences ->
        ThemeMode.fromInt(preferences[themeModeKey] ?: ThemeMode.AUTO.value)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { preferences ->
            preferences[themeModeKey] = mode.value
        }
    }
}

object ThemeResolver {
    fun isDark(mode: ThemeMode, hourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): Boolean {
        return when (mode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.AUTO -> hourOfDay < 6 || hourOfDay >= 18
        }
    }
}
