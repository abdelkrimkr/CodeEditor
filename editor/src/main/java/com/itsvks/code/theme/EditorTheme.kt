package com.itsvks.code.theme

import androidx.compose.ui.graphics.Color
import com.itsvks.code.syntax.TokenType

interface EditorTheme {
    val backgroundColor: Color
    val cursorColor: Color
    val selectionColor: Color
    val defaultTextColor: Color
    val gutterBgColor: Color
    val gutterTextColor: Color get() = defaultTextColor.copy(alpha = 0.6f)
    val invisibleCharColor: Color get() = Color(0xFFAAAAAA)

    fun getColorForToken(type: TokenType): Color
}
