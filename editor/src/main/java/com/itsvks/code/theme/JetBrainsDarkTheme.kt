package com.itsvks.code.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import com.itsvks.code.syntax.TokenType

object JetBrainsDarkTheme : EditorTheme {
    override val backgroundColor = Color(0xFF2B2B2B)
    override val cursorColor = Color(0xFFBBBBBB)
    override val selectionColor = Color(0xFF214283)
    override val defaultTextColor = Color(0xFFA9B7C6)
    override val gutterBgColor = Color(0xFF313335)
    override val activeLineColor = Color(0xFF323232)
    override val gutterTextColor = Color(0xFF606366)
    override val gutterBorderColor = Color(0xFF515658)
    override val invisibleCharColor = Color(0xFF4E5254)
    override val selectionHandleColor = Color(0xFF6897BB)
    override val scrollBarSelectedColor = Color(0x80CCCCCC)
    override val scrollBarColor = Color(0x802F2F2F)

    override fun getStyleForToken(type: TokenType)= when (type) {
        TokenType.PLAIN -> TokenStyle(defaultTextColor)
        TokenType.KEYWORD -> TokenStyle(Color(0xFFCC7832), italic = true)
        TokenType.TYPE -> TokenStyle(Color(0xFF287BDE))
        TokenType.STRING -> TokenStyle(Color(0xFF6A8759))
        TokenType.COMMENT -> TokenStyle(Color(0xFF808080), italic = true)
        TokenType.NUMBER -> TokenStyle(Color(0xFF6897BB))
        TokenType.OPERATOR -> TokenStyle(Color(0xFF89DDFF))
        TokenType.PUNCTUATION -> TokenStyle(Color(0xFFA9B7C6))
        TokenType.FUNCTION -> TokenStyle(Color(0xFFFFC66D))
        TokenType.IDENTIFIER -> TokenStyle(defaultTextColor)
        TokenType.ANNOTATION -> TokenStyle(Color(0xFFBBB529))
        TokenType.MACRO -> TokenStyle(Color(0xFF9876AA))
        TokenType.ATTRIBUTE -> TokenStyle(Color(0xFFDAA520))
        TokenType.CONST -> TokenStyle(Color(0xFF9876AA))
        TokenType.STATIC -> TokenStyle(Color(0xFF9876AA))
    }
}
