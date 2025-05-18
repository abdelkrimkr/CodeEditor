package com.itsvks.code.theme

import androidx.compose.ui.graphics.Color
import com.itsvks.code.syntax.TokenType

object JetBrainsDarkTheme : EditorTheme {
    override val backgroundColor = Color(0xFF2B2B2B)
    override val cursorColor = Color.White
    override val selectionColor = Color(0xFF214283)
    override val defaultTextColor = Color(0xFFA9B7C6)
    override val gutterBgColor = Color(0xFF313335)
    override val gutterTextColor = Color(0xFF606366)

    override fun getColorForToken(type: TokenType) = when (type) {
        TokenType.KEYWORD -> Color(0xFFCC7832)
        TokenType.TYPE -> Color(0xFF287BDE)
        TokenType.STRING -> Color(0xFF6A8759)
        TokenType.COMMENT -> Color(0xFF808080)
        TokenType.NUMBER -> Color(0xFF6897BB)
        TokenType.OPERATOR -> Color(0xFFA9B7C6)
        TokenType.FUNCTION -> Color(0xFFFFC66D)
        TokenType.PUNCTUATION -> Color(0xFFA9B7C6)
        TokenType.IDENTIFIER -> Color(0xFFA9B7C6)
        TokenType.ANNOTATION -> Color(0xFFBBB529)
        TokenType.MACRO -> Color(0xFF9876AA)
        TokenType.ATTRIBUTE -> Color(0xFF287BDE)
        TokenType.CONST -> Color(0xFF9876AA)
        TokenType.STATIC -> Color(0xFF9876AA)
        TokenType.PLAIN -> defaultTextColor
    }
}
