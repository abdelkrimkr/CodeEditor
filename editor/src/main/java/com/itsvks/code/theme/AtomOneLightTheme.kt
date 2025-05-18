package com.itsvks.code.theme

import androidx.compose.ui.graphics.Color
import com.itsvks.code.syntax.TokenType

object AtomOneLightTheme : EditorTheme {
    override val backgroundColor = Color(0xFFFFFFFF)
    override val cursorColor = Color.Black
    override val selectionColor = Color(0xFFD6D6D6)
    override val defaultTextColor = Color(0xFF383A42)
    override val gutterBgColor = Color(0xFFF5F5F5)
    override val gutterTextColor = Color(0xFF999999)

    override fun getColorForToken(type: TokenType) = when (type) {
        TokenType.KEYWORD -> Color(0xFFa626a4)
        TokenType.TYPE -> Color(0xFF986801)
        TokenType.STRING -> Color(0xFF50A14F)
        TokenType.COMMENT -> Color(0xFFA0A1A7)
        TokenType.NUMBER -> Color(0xFF986801)
        TokenType.OPERATOR -> Color(0xFF383A42)
        TokenType.FUNCTION -> Color(0xFFE45649)
        TokenType.PUNCTUATION -> Color(0xFF383A42)
        TokenType.IDENTIFIER -> Color(0xFF4078F2)
        TokenType.ANNOTATION -> Color(0xFF0184BC)
        TokenType.MACRO -> Color(0xFF0184BC)
        TokenType.ATTRIBUTE -> Color(0xFF986801)
        TokenType.CONST -> Color(0xFFD75F00)
        TokenType.STATIC -> Color(0xFF4078F2)
        TokenType.PLAIN -> defaultTextColor
    }
}
