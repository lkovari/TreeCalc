package com.lkovari.mobile.apps.treecalc.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

val PetalPaper = Color(0xFFF7F0F3)
val Porcelain = Color(0xFFFFFBFC)
val InkRose = Color(0xFF3A2A32)
val MutedBlush = Color(0xFF7A5A66)
val NumberSage = Color(0xFFC8DDD2)
val FunctionLilac = Color(0xFFD4D0EA)
val OperatorMist = Color(0xFFC5D6DA)
val LogicOrchid = Color(0xFFC4A8D0)
val EqualsBlush = Color(0xFFE8C0CC)
val ActionPeach = Color(0xFFEDD4B8)
val TitleMagenta = Color(0xFFC2187A)

val DuskPlum = Color(0xFF2A2430)
val DuskSurface = Color(0xFF3A3340)
val MoonCream = Color(0xFFF4ECEF)
val MutedMoon = Color(0xFFD4C4CC)
val NumberSageDark = Color(0xFF7A9A88)
val FunctionLilacDark = Color(0xFF9A94B8)
val OperatorMistDark = Color(0xFF7A9AA0)
val LogicOrchidDark = Color(0xFFA080B4)
val EqualsBlushDark = Color(0xFFC490A0)
val ActionPeachDark = Color(0xFFC4A078)

private val LightEdge = Color(0xFF5A3A44)
private val DarkEdge = Color(0xFFF6E8EE)

private fun Color.softEdge(towards: Color, amount: Float): Color = lerp(this, towards, amount)

data class TreeCalcPalette(
    val numberKey: Color,
    val functionKey: Color,
    val operatorKey: Color,
    val logicKey: Color,
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
    val logicKeyBorder: Color,
    val equalsKeyBorder: Color,
    val actionKeyBorder: Color,
    val disabledKeyBorder: Color,
    val chipIdle: Color,
    val chipIdleBorder: Color,
    val badgeFill: Color,
    val rootBadgeFill: Color,
    val badgeGlyph: Color,
    val operandFill: Color,
    val operandRing: Color,
    val operandLabel: Color,
    val disabledKey: Color,
    val disabledLabel: Color,
    val titleAccent: Color,
    val link: Color,
    val screenWashTop: Color,
    val screenWashBottom: Color,
    val splashTop: Color,
    val splashMid: Color,
    val splashBottom: Color,
    val splashOn: Color,
    val splashTagline: Color,
    val splashBadgeFill: Color,
    val splashStem: Color,
    val splashNodeRoot: Color,
    val splashNodeLeft: Color,
    val splashNodeRight: Color,
    val splashNodeOp: Color
)

val LightPalette = TreeCalcPalette(
    numberKey = NumberSage,
    functionKey = FunctionLilac,
    operatorKey = OperatorMist,
    logicKey = LogicOrchid,
    equalsKey = EqualsBlush,
    actionKey = ActionPeach,
    keyLabel = InkRose,
    operatorLabel = InkRose,
    equalsLabel = InkRose,
    displaySurface = Porcelain,
    displayBorder = Color(0xFFD8C4CC),
    numberKeyBorder = NumberSage.softEdge(LightEdge, 0.18f),
    functionKeyBorder = FunctionLilac.softEdge(LightEdge, 0.18f),
    operatorKeyBorder = OperatorMist.softEdge(LightEdge, 0.18f),
    logicKeyBorder = LogicOrchid.softEdge(LightEdge, 0.18f),
    equalsKeyBorder = EqualsBlush.softEdge(LightEdge, 0.20f),
    actionKeyBorder = ActionPeach.softEdge(LightEdge, 0.18f),
    disabledKeyBorder = Color(0xFFE4D8DC),
    chipIdle = Porcelain,
    chipIdleBorder = Color(0xFFD8C4CC),
    badgeFill = Color(0xFFA8D4F4),
    rootBadgeFill = Color(0xFFB6E8C4),
    badgeGlyph = InkRose,
    operandFill = Color(0xFFA8D4F4),
    operandRing = Color(0xFF2E8BC7),
    operandLabel = Color(0xFF6A7A9A),
    disabledKey = Color(0xFFEDE4E8),
    disabledLabel = Color(0xFFA898A0),
    titleAccent = TitleMagenta,
    link = Color(0xFF0563C1),
    screenWashTop = PetalPaper,
    screenWashBottom = Color(0xFFF3E6EC),
    splashTop = Color(0xFFF6DDE6),
    splashMid = Color(0xFFE8D6F0),
    splashBottom = Color(0xFFF4E8E0),
    splashOn = InkRose,
    splashTagline = Color(0xFF2E7FC4),
    splashBadgeFill = Color(0x66FFFFFF),
    splashStem = Color(0x88A85A78),
    splashNodeRoot = Color(0xFFD4B8D8),
    splashNodeLeft = Color(0xFFB8D4C8),
    splashNodeRight = Color(0xFFE8C0C8),
    splashNodeOp = Color(0xFFC0D4DC)
)

val DarkPalette = TreeCalcPalette(
    numberKey = NumberSageDark,
    functionKey = FunctionLilacDark,
    operatorKey = OperatorMistDark,
    logicKey = LogicOrchidDark,
    equalsKey = EqualsBlushDark,
    actionKey = ActionPeachDark,
    keyLabel = MoonCream,
    operatorLabel = MoonCream,
    equalsLabel = MoonCream,
    displaySurface = DuskSurface,
    displayBorder = Color(0xFF5A4E58),
    numberKeyBorder = NumberSageDark.softEdge(DarkEdge, 0.22f),
    functionKeyBorder = FunctionLilacDark.softEdge(DarkEdge, 0.22f),
    operatorKeyBorder = OperatorMistDark.softEdge(DarkEdge, 0.22f),
    logicKeyBorder = LogicOrchidDark.softEdge(DarkEdge, 0.22f),
    equalsKeyBorder = EqualsBlushDark.softEdge(DarkEdge, 0.22f),
    actionKeyBorder = ActionPeachDark.softEdge(DarkEdge, 0.22f),
    disabledKeyBorder = Color(0xFF4A424C),
    chipIdle = DuskSurface,
    chipIdleBorder = Color(0xFF5A4E58),
    badgeFill = Color(0xFF1E5A82),
    rootBadgeFill = Color(0xFF246B45),
    badgeGlyph = MoonCream,
    operandFill = Color(0xFF1E5A82),
    operandRing = Color(0xFF7ED0F5),
    operandLabel = Color(0xFFC0C8E0),
    disabledKey = Color(0xFF423848),
    disabledLabel = Color(0xFF8A7E86),
    titleAccent = TitleMagenta,
    link = Color(0xFF8AB4F8),
    screenWashTop = DuskPlum,
    screenWashBottom = Color(0xFF322830),
    splashTop = Color(0xFF3A2C38),
    splashMid = Color(0xFF322838),
    splashBottom = Color(0xFF2A2430),
    splashOn = MoonCream,
    splashTagline = Color(0xFF8EC8F0),
    splashBadgeFill = Color(0x33FFFFFF),
    splashStem = Color(0x88E0A0B4),
    splashNodeRoot = Color(0xFF9A7AA0),
    splashNodeLeft = Color(0xFF6E8F7C),
    splashNodeRight = Color(0xFFC490A0),
    splashNodeOp = Color(0xFF7A9AA0)
)
