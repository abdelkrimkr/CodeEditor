package com.itsvks.code.util

fun Char.isPrintable(): Boolean {
    val block = Character.UnicodeBlock.of(this)
    return !Character.isISOControl(this) && block != null && block != Character.UnicodeBlock.SPECIALS
}
