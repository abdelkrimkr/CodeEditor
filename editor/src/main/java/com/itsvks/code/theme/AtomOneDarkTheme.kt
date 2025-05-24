package com.itsvks.code.theme

import androidx.compose.ui.graphics.Color
import com.itsvks.code.syntax.TokenType

object AtomOneDarkTheme : EditorTheme {
    override val backgroundColor = Color(0xFF282C34)
    override val cursorColor = Color(0xFFDCDCAA)
    override val selectionColor = Color(0x403D4973)
    override val defaultTextColor = Color(0xFFDCDCDC)
    override val gutterBgColor = Color(0xFF21252B)

    override val activeLineColor = Color(0xFF2C313A)
    override val gutterTextColor = Color(0xFF9DA5B4)
    override val gutterBorderColor = Color(0xFF3B4048)
    override val invisibleCharColor = Color(0xFF3B4048)
    override val selectionHandleColor = Color(0xFFDCDCAA)
    override val scrollBarSelectedColor = Color(0x80FFFFFF)
    override val scrollBarColor = Color(0x80D3D3D3)

    override fun getStyleForToken(type: TokenType) = when (type) {
        TokenType.PLAIN -> TokenStyle(defaultTextColor)
        TokenType.KEYWORD -> TokenStyle(Color(0xFFC678DD))
        TokenType.TYPE -> TokenStyle(Color(0xFF61AFEF))
        TokenType.STRING -> TokenStyle(Color(0xFF98C379))
        TokenType.COMMENT -> TokenStyle(Color(0xFF5C6370), italic = true)
        TokenType.NUMBER -> TokenStyle(Color(0xFFD19A66))
        TokenType.OPERATOR -> TokenStyle(Color(0xFF56B6C2))
        TokenType.PUNCTUATION -> TokenStyle(Color(0xFFD4D4D4))
        TokenType.FUNCTION -> TokenStyle(Color(0xFF61AFEF))
        TokenType.IDENTIFIER -> TokenStyle(defaultTextColor)
        TokenType.ANNOTATION -> TokenStyle(Color(0xFF56B6C2))
        TokenType.MACRO -> TokenStyle(Color(0xFFE06C75))
        TokenType.ATTRIBUTE -> TokenStyle(Color(0xFF56B6C2))
        TokenType.CONST -> TokenStyle(Color(0xFFE5C07B))
        TokenType.STATIC -> TokenStyle(Color(0xFFE06C75))
    }
}
