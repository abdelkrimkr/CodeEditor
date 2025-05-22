package com.itsvks.code.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

fun Char.isPrintable(): Boolean {
    val block = Character.UnicodeBlock.of(this)
    return !isISOControl() && block != null && block != Character.UnicodeBlock.SPECIALS
}

@Composable
fun rememberImeHeight(): State<Dp> {
    val ime = WindowInsets.ime
    val density = LocalDensity.current

    return rememberUpdatedState(
        with(density) { ime.getBottom(density).toDp() }
    )
}

@Composable
fun rememberCharWidth(char: Char, fontSize: TextUnit, fontFamily: FontFamily): State<Int> {
    val textMeasurer = rememberTextMeasurer()
    val layoutResult = textMeasurer.measure(
        text = char.toString(),
        style = TextStyle(
            fontSize = fontSize,
            fontFamily = fontFamily
        )
    )
    return rememberUpdatedState(layoutResult.size.width)
}
