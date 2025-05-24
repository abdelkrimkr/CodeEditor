package com.itsvks.code.theme

import androidx.compose.ui.graphics.Color
import com.itsvks.code.syntax.TokenType

object DraculaTheme : EditorTheme {
    override val backgroundColor = Color(0xFF282A36)
    override val cursorColor = Color(0xFF50FA7B)
    override val selectionColor = Color(0x405D5F70)
    override val defaultTextColor = Color(0xFFF8F8F2)
    override val gutterBgColor = Color(0xFF21222C)

    override val activeLineColor = Color(0xFF44475A)
    override val gutterTextColor = Color(0xFF6272A4)
    override val gutterBorderColor = Color(0xFF3B3A32)
    override val invisibleCharColor = Color(0xFF4B5263)
    override val selectionHandleColor = Color(0xFF50FA7B)
    override val scrollBarSelectedColor = Color(0x80FFFFFF)
    override val scrollBarColor = Color(0x80444444)

    override fun getColorForToken(type: TokenType): Color = when (type) {
        TokenType.PLAIN -> defaultTextColor
        TokenType.KEYWORD -> Color(0xFFFF79C6)
        TokenType.TYPE -> Color(0xFF8BE9FD)
        TokenType.STRING -> Color(0xFFF1FA8C)
        TokenType.COMMENT -> Color(0xFF6272A4)
        TokenType.NUMBER -> Color(0xFFFFB86C)
        TokenType.OPERATOR -> Color(0xFFBD93F9)
        TokenType.PUNCTUATION -> Color(0xFFF8F8F2)
        TokenType.FUNCTION -> Color(0xFF50FA7B)
        TokenType.IDENTIFIER -> defaultTextColor
        TokenType.ANNOTATION -> Color(0xFFFFB86C)
        TokenType.MACRO -> Color(0xFFFF5555)
        TokenType.ATTRIBUTE -> Color(0xFF8BE9FD)
        TokenType.CONST -> Color(0xFFFF79C6)
        TokenType.STATIC -> Color(0xFFBD93F9)
    }
}
