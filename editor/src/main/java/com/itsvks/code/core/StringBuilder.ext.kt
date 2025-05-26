package com.itsvks.code.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

suspend fun File.toStringBuilder() = inputStream().toStringBuilder()

suspend fun InputStream.toStringBuilder() = withContext(Dispatchers.IO) {
    bufferedReader(Charsets.UTF_8).useLines {
        StringBuilder().apply { it.forEach(::appendLine) }
    }
}

fun String.toStringBuilder() = StringBuilder(this)

fun emptyStringBuilder() = StringBuilder()

val StringBuilder.lineCount: Int
    get() = count { it == '\n' } + 1

fun StringBuilder.getLineRange(lineIndex: Int): IntRange {
    if (lineIndex < 0) throw IndexOutOfBoundsException("Line index must be non-negative")

    var currentLine = 0
    var i = 0

    while (currentLine < lineIndex && i < length) {
        if (this[i] == '\r' && i + 1 < length && this[i + 1] == '\n') i++
        if (this[i] == '\n' || this[i] == '\r') currentLine++
        i++
    }

    if (currentLine != lineIndex) throw IndexOutOfBoundsException("Line index $lineIndex out of bounds")

    val start = i
    var end = i

    while (end < length && this[end] != '\n' && this[end] != '\r') end++

    if (end < length) {
        if (this[end] == '\r' && end + 1 < length && this[end + 1] == '\n') end += 2
        else end++
    }

    return start until end
}

fun StringBuilder.detectDefaultLineEnding(): String {
    for (i in indices) {
        if (this[i] == '\r') {
            return if (i + 1 < length && this[i + 1] == '\n') "\r\n" else "\r"
        }
        if (this[i] == '\n') return "\n"
    }
    return System.lineSeparator() // fallback
}

fun StringBuilder.getLine(lineIndex: Int): String = substring(getLineRange(lineIndex)).trimEnd('\n', '\r')

fun StringBuilder.delete(range: IntRange) = delete(range.first, range.last + 1)

fun StringBuilder.replace(range: IntRange, text: String) = replace(range.first, range.last + 1, text)

fun StringBuilder.removeLineAt(lineIndex: Int) = delete(getLineRange(lineIndex))

fun StringBuilder.updateLine(lineIndex: Int, text: String) = replace(getLineRange(lineIndex), text)

fun StringBuilder.insertLineAt(lineIndex: Int, text: String): StringBuilder {
    val insertPos = when {
        lineIndex < 0 -> throw IndexOutOfBoundsException("Line index must be non-negative")
        lineIndex == 0 -> 0 // insert at start
        lineIndex >= lineCount -> length // insert at end
        else -> getLineRange(lineIndex).first
    }

    val lineEnding = detectDefaultLineEnding()

    return insert(insertPos, text + lineEnding)
}
