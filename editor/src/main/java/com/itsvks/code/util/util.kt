package com.itsvks.code.util

fun Char.isPrintable(): Boolean {
    val block = Character.UnicodeBlock.of(this)
    return !isISOControl() && block != null && block != Character.UnicodeBlock.SPECIALS
}
