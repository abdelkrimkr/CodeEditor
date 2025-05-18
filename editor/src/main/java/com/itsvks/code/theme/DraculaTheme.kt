package com.itsvks.code.theme

import androidx.compose.ui.graphics.Color
import com.itsvks.code.syntax.TokenType

object DraculaTheme : EditorTheme {
    override val backgroundColor = Color(0xFF282A36)
    override val cursorColor = Color(0xFFFFF1F3)
    override val selectionColor = Color(0xFF44475A)
    override val defaultTextColor = Color(0xFFF8F8F2)
    override val gutterBgColor = Color(0xFF21222C)
    override val gutterTextColor = Color(0xFF6272A4)

    override fun getColorForToken(type: TokenType): Color = when (type) {
        TokenType.KEYWORD -> Color(0xFFFF79C6)
        TokenType.TYPE -> Color(0xFF8BE9FD)
        TokenType.STRING -> Color(0xFFF1FA8C)
        TokenType.COMMENT -> Color(0xFF6272A4)
        TokenType.NUMBER -> Color(0xFFFFB86C)
        TokenType.OPERATOR -> Color(0xFF50FA7B)
        TokenType.FUNCTION -> Color(0xFF50FA7B)
        TokenType.PUNCTUATION -> Color(0xFFF8F8F2)
        TokenType.IDENTIFIER -> Color(0xFFBD93F9)
        TokenType.ANNOTATION -> Color(0xFFFF79C6)
        TokenType.MACRO -> Color(0xFFFF5555)
        TokenType.ATTRIBUTE -> Color(0xFFFF79C6)
        TokenType.CONST -> Color(0xFFBD93F9)
        TokenType.STATIC -> Color(0xFFBD93F9)
        TokenType.PLAIN -> defaultTextColor
    }
}
