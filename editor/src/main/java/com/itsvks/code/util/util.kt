package com.itsvks.code.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

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

