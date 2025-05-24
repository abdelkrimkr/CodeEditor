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

    override fun getColorForToken(type: TokenType): Color = when (type) {
        TokenType.PLAIN -> defaultTextColor
        TokenType.KEYWORD -> Color(0xFFC678DD)
        TokenType.TYPE -> Color(0xFF61AFEF)
        TokenType.STRING -> Color(0xFF98C379)
        TokenType.COMMENT -> Color(0xFF5C6370)
        TokenType.NUMBER -> Color(0xFFD19A66)
        TokenType.OPERATOR -> Color(0xFF56B6C2)
        TokenType.PUNCTUATION -> Color(0xFFD4D4D4)
        TokenType.FUNCTION -> Color(0xFF61AFEF)
        TokenType.IDENTIFIER -> defaultTextColor
        TokenType.ANNOTATION -> Color(0xFF56B6C2)
        TokenType.MACRO -> Color(0xFFE06C75)
        TokenType.ATTRIBUTE -> Color(0xFF56B6C2)
        TokenType.CONST -> Color(0xFFE5C07B)
        TokenType.STATIC -> Color(0xFFE06C75)
    }
}
