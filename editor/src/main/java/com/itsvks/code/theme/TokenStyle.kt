@file:Suppress("FunctionName")

package com.itsvks.code.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

fun TokenStyle(
    color: Color,
    fontWeight: FontWeight? = null,
    italic: Boolean = false
) = SpanStyle(
    color = color,
    fontWeight = fontWeight,
    fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal
)
