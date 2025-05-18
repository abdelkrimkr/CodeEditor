package com.itsvks.code.theme

import androidx.compose.ui.graphics.Color
import com.itsvks.code.syntax.TokenType

object VsCodeDarkTheme : EditorTheme {
    override val backgroundColor = Color(0xFF1E1E1E)
    override val cursorColor = Color.White
    override val selectionColor = Color(0xFF264F78)
    override val defaultTextColor = Color.White
    override val gutterBgColor = Color(0xFF252526)
    override val gutterTextColor = Color(0xFF858585)

    override fun getColorForToken(type: TokenType) = when (type) {
        TokenType.KEYWORD -> Color(0xFF569CD6)
        TokenType.TYPE -> Color(0xFF4EC9B0)
        TokenType.STRING -> Color(0xFFCE9178)
        TokenType.COMMENT -> Color(0xFF6A9955)
        TokenType.NUMBER -> Color(0xFFB5CEA8)
        TokenType.OPERATOR -> Color(0xFFD4D4D4)
        TokenType.FUNCTION -> Color(0xFFDCDCAA)
        TokenType.PUNCTUATION -> Color(0xFFD4D4D4)
        TokenType.IDENTIFIER -> Color(0xFF9CDCFE)
        TokenType.ANNOTATION -> Color(0xFFD16969)
        TokenType.MACRO -> Color(0xFFB180D7)
        TokenType.ATTRIBUTE -> Color(0xFF569CD6)
        TokenType.CONST -> Color(0xFF4FC1FF)
        TokenType.STATIC -> Color(0xFF4FC1FF)
        TokenType.PLAIN -> defaultTextColor
    }
}
