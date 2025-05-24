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

    override fun getStyleForToken(type: TokenType) = when (type) {
        TokenType.PLAIN -> TokenStyle(defaultTextColor)
        TokenType.KEYWORD -> TokenStyle(Color(0xFF569CD6))
        TokenType.TYPE -> TokenStyle(Color(0xFF4EC9B0))
        TokenType.STRING -> TokenStyle(Color(0xFFD69D85))
        TokenType.COMMENT -> TokenStyle(Color(0xFF6A9955), italic = true)
        TokenType.NUMBER -> TokenStyle(Color(0xFFB5CEA8))
        TokenType.OPERATOR -> TokenStyle(Color(0xFFD4D4D4))
        TokenType.PUNCTUATION -> TokenStyle(Color(0xFFD4D4D4))
        TokenType.FUNCTION -> TokenStyle(Color(0xFFDCDCAA))
        TokenType.IDENTIFIER -> TokenStyle(defaultTextColor)
        TokenType.ANNOTATION -> TokenStyle(Color(0xFFB5CEA8))
        TokenType.MACRO -> TokenStyle(Color(0xFFFF5370))
        TokenType.ATTRIBUTE -> TokenStyle(Color(0xFFD16969))
        TokenType.CONST -> TokenStyle(Color(0xFF4FC1FF))
        TokenType.STATIC -> TokenStyle(Color(0xFF9CDCFE))
    }
}
