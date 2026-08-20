package com.lkovari.mobile.apps.treecalc.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

val SageMist = Color(0xFFF7F9F6)
val WarmPaper = Color(0xFFFFFFFF)
val NumberKeyLight = Color(0xFF46B06C)
val FunctionKeyLight = Color(0xFF4A9FD8)
val OperatorTeal = Color(0xFF2A8F9A)
val EqualsRose = Color(0xFFD0486C)
val InkGreen = Color(0xFF0B1F16)
val MutedSage = Color(0xFF2A5A48)

val NightForest = Color(0xFF3E4C49)
val NightSurface = Color(0xFF4A5C58)
val NumberKeyDark = Color(0xFF62C888)
val FunctionKeyDark = Color(0xFF9B8AE8)
val OperatorTealDark = Color(0xFF72D8D0)
val EqualsRoseDark = Color(0xFFF29BB3)
val MoonInk = Color(0xFFF6FAF7)
val MutedMoon = Color(0xFFD0DDD7)

val SplashTeal = Color(0xFF067A82)
val SplashForest = Color(0xFF0EA35C)
val SplashRose = Color(0xFFE02458)

private fun Color.deeperBorder(): Color = lerp(this, Color(0xFF000000), 0.38f)

private fun Color.lighterBorder(): Color = lerp(this, Color(0xFFFFFFFF), 0.46f)

data class TreeCalcPalette(
    val numberKey: Color,
    val functionKey: Color,
    val operatorKey: Color,
    val equalsKey: Color,
    val actionKey: Color,
    val keyLabel: Color,
    val operatorLabel: Color,
    val equalsLabel: Color,
    val displaySurface: Color,
    val displayBorder: Color,
    val numberKeyBorder: Color,
    val functionKeyBorder: Color,
    val operatorKeyBorder: Color,
    val equalsKeyBorder: Color,
    val actionKeyBorder: Color,
    val disabledKeyBorder: Color,
    val chipIdle: Color,
    val chipIdleBorder: Color,
    val badgeFill: Color,
    val badgeGlyph: Color,
    val operandFill: Color,
    val operandRing: Color,
    val operandLabel: Color,
    val disabledKey: Color,
    val disabledLabel: Color,
    val titleAccent: Color
)

val LightPalette = TreeCalcPalette(
    numberKey = NumberKeyLight,
    functionKey = FunctionKeyLight,
    operatorKey = OperatorTeal,
    equalsKey = EqualsRose,
    actionKey = Color(0xFFE0853A),
    keyLabel = Color(0xFFFFFFFF),
    operatorLabel = Color(0xFFFFFFFF),
    equalsLabel = Color(0xFFFFFFFF),
    displaySurface = Color(0xFFFFFFFF),
    displayBorder = OperatorTeal.deeperBorder(),
    numberKeyBorder = NumberKeyLight.deeperBorder(),
    functionKeyBorder = FunctionKeyLight.deeperBorder(),
    operatorKeyBorder = OperatorTeal.deeperBorder(),
    equalsKeyBorder = EqualsRose.deeperBorder(),
    actionKeyBorder = Color(0xFFE0853A).deeperBorder(),
    disabledKeyBorder = Color(0xFFB4BDB8).deeperBorder(),
    chipIdle = Color(0xFFFFFFFF),
    chipIdleBorder = Color(0xFF9AAEA6),
    badgeFill = OperatorTeal,
    badgeGlyph = Color(0xFFFFFFFF),
    operandFill = Color(0xFFFFFFFF),
    operandRing = Color(0xFF3D8FD4),
    operandLabel = Color(0xFF2E7FC4),
    disabledKey = Color(0xFFB4BDB8),
    disabledLabel = Color(0xFF5E6864),
    titleAccent = Color(0xFFC2187A)
)

val DarkPalette = TreeCalcPalette(
    numberKey = NumberKeyDark,
    functionKey = FunctionKeyDark,
    operatorKey = OperatorTealDark,
    equalsKey = EqualsRoseDark,
    actionKey = Color(0xFFE09248),
    keyLabel = Color(0xFF0E1C16),
    operatorLabel = Color(0xFF0E1C16),
    equalsLabel = Color(0xFF1A1014),
    displaySurface = Color(0xFF4A5C58),
    displayBorder = Color(0xFF4A5C58).lighterBorder(),
    numberKeyBorder = NumberKeyDark.lighterBorder(),
    functionKeyBorder = FunctionKeyDark.lighterBorder(),
    operatorKeyBorder = OperatorTealDark.lighterBorder(),
    equalsKeyBorder = EqualsRoseDark.lighterBorder(),
    actionKeyBorder = Color(0xFFE09248).lighterBorder(),
    disabledKeyBorder = Color(0xFF4A5552).lighterBorder(),
    chipIdle = NightSurface,
    chipIdleBorder = NightSurface.lighterBorder(),
    badgeFill = OperatorTealDark,
    badgeGlyph = Color(0xFF0E1C16),
    operandFill = Color(0xFFFFFFFF),
    operandRing = Color(0xFF8EC8F0),
    operandLabel = Color(0xFF8EC8F0),
    disabledKey = Color(0xFF4A5552),
    disabledLabel = Color(0xFFA8B2AE),
    titleAccent = Color(0xFFFF4FA3)
)
