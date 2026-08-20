package com.lkovari.mobile.apps.treecalc.settings

enum class ThemeMode(val value: Int) {
    AUTO(0),
    LIGHT(1),
    DARK(2);

    companion object {
        fun fromInt(value: Int): ThemeMode {
            return entries.firstOrNull { mode -> mode.value == value } ?: AUTO
        }
    }
}
