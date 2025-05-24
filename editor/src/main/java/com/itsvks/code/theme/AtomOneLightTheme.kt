package com.itsvks.code.theme

import androidx.compose.ui.graphics.Color
import com.itsvks.code.syntax.TokenType

object AtomOneLightTheme : EditorTheme {
    override val backgroundColor = Color(0xFFFFFFFF)
    override val cursorColor = Color(0xFF000000)
    override val selectionColor = Color(0x403D4973)
    override val defaultTextColor = Color(0xFF383A42)
    override val gutterBgColor = Color(0xFFF0F0F0)

    override val activeLineColor = Color(0xFFEAEAEA)
    override val gutterTextColor = Color(0xFF999999)
    override val gutterBorderColor = Color(0xFFD0D0D0)
    override val invisibleCharColor = Color(0xFFD0D0D0)
    override val selectionHandleColor = Color(0xFF000000)
    override val scrollBarSelectedColor = Color(0x80000000)
    override val scrollBarColor = Color(0x80808080)

    override fun getColorForToken(type: TokenType): Color = when (type) {
        TokenType.PLAIN -> defaultTextColor
        TokenType.KEYWORD -> Color(0xFFA626A4)
        TokenType.TYPE -> Color(0xFF0184BC)
        TokenType.STRING -> Color(0xFF50A14F)
        TokenType.COMMENT -> Color(0xFFA0A1A7)
        TokenType.NUMBER -> Color(0xFF986801)
        TokenType.OPERATOR -> Color(0xFF0184BC)
        TokenType.PUNCTUATION -> Color(0xFF383A42)
        TokenType.FUNCTION -> Color(0xFF4078F2)
        TokenType.IDENTIFIER -> defaultTextColor
        TokenType.ANNOTATION -> Color(0xFFB33C3C)
        TokenType.MACRO -> Color(0xFFD73A49)
        TokenType.ATTRIBUTE -> Color(0xFF986801)
        TokenType.CONST -> Color(0xFF005CC5)
        TokenType.STATIC -> Color(0xFF032F62)
    }
}
