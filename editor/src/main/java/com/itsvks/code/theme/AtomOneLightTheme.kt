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

    override fun getStyleForToken(type: TokenType) = when (type) {
        TokenType.PLAIN -> TokenStyle(defaultTextColor)
        TokenType.KEYWORD -> TokenStyle(Color(0xFFA626A4))
        TokenType.TYPE -> TokenStyle(Color(0xFF0184BC))
        TokenType.STRING -> TokenStyle(Color(0xFF50A14F))
        TokenType.COMMENT -> TokenStyle(Color(0xFFA0A1A7), italic = true)
        TokenType.NUMBER -> TokenStyle(Color(0xFF986801))
        TokenType.OPERATOR -> TokenStyle(Color(0xFF0184BC))
        TokenType.PUNCTUATION -> TokenStyle(Color(0xFF383A42))
        TokenType.FUNCTION -> TokenStyle(Color(0xFF4078F2))
        TokenType.IDENTIFIER -> TokenStyle(defaultTextColor)
        TokenType.ANNOTATION -> TokenStyle(Color(0xFFB33C3C))
        TokenType.MACRO -> TokenStyle(Color(0xFFD73A49))
        TokenType.ATTRIBUTE -> TokenStyle(Color(0xFF986801))
        TokenType.CONST -> TokenStyle(Color(0xFF005CC5))
        TokenType.STATIC -> TokenStyle(Color(0xFF032F62))
    }
}
