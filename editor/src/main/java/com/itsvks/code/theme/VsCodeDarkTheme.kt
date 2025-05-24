package com.itsvks.code.theme

import androidx.compose.ui.graphics.Color
import com.itsvks.code.syntax.TokenType

object VsCodeDarkTheme : EditorTheme {
    override val backgroundColor = Color(0xFF1E1E1E)
    override val cursorColor = Color(0xFFA7A7A7)
    override val selectionColor = Color(0x40ADD6FF)
    override val defaultTextColor = Color(0xFFD4D4D4)
    override val gutterBgColor = Color(0xFF252526)

    override val activeLineColor = Color(0xFF2A2D2E)
    override val gutterTextColor = Color(0xFF858585)
    override val gutterBorderColor = Color(0xFF3C3C3C)
    override val invisibleCharColor = Color(0xFF404040)
    override val selectionHandleColor = Color(0xFF569CD6)
    override val scrollBarSelectedColor = Color(0x80FFFFFF)
    override val scrollBarColor = Color(0x80808080)

    override fun getColorForToken(type: TokenType): Color = when (type) {
        TokenType.PLAIN -> defaultTextColor
        TokenType.KEYWORD -> Color(0xFF569CD6)
        TokenType.TYPE -> Color(0xFF4EC9B0)
        TokenType.STRING -> Color(0xFFD69D85)
        TokenType.COMMENT -> Color(0xFF6A9955)
        TokenType.NUMBER -> Color(0xFFB5CEA8)
        TokenType.OPERATOR -> Color(0xFFD4D4D4)
        TokenType.PUNCTUATION -> Color(0xFFD4D4D4)
        TokenType.FUNCTION -> Color(0xFFDCDCAA)
        TokenType.IDENTIFIER -> defaultTextColor
        TokenType.ANNOTATION -> Color(0xFFB5CEA8)
        TokenType.MACRO -> Color(0xFFFF5370)
        TokenType.ATTRIBUTE -> Color(0xFFD16969)
        TokenType.CONST -> Color(0xFF4FC1FF)
        TokenType.STATIC -> Color(0xFF9CDCFE)
    }
}
